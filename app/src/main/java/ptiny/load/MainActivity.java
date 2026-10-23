package ptiny.load;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo; 
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ColorDrawable;
import android.content.ClipboardManager;
import android.content.ClipData;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Comparator;
import java.util.Collections;

public final class MainActivity extends AppCompatActivity {
	
	protected Pref pref = Pref.instance;
	protected JobList joblist = new JobList();
	protected JobList filtered = new JobList();
	protected int filter = -1; 
	protected HttpConnection http;
	protected RefreshTask refreshTask;
	protected NotifyTask notifyTask;
	protected ReloadTask reloadTask;
	private ScheduledFuture<?> reloadFuture;
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private int interval;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		L.d("onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setOnClickListener((view) -> {
			view.setBackgroundColor(Color.GRAY);
			view.setAlpha(0.6f);
			view.postDelayed(() -> {
				view.setBackgroundColor(Color.TRANSPARENT);
				view.setAlpha(1.0f);
			}, 100);
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
		});

		final ListView listView = findViewById(R.id.listview);
		final JobListAdapter adapter = new JobListAdapter(this, filtered);
		listView.setAdapter(adapter);
		registerForContextMenu(listView);
		listView.setOnItemClickListener((parent, view, position, id) -> {
			Job job = filtered.get(position);
			showInfoDialog(job);
		});
		
		final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				executor.execute(reloadTask);
			}
		});

		refreshTask= new RefreshTask(swipeRefreshLayout); 
		notifyTask = new NotifyTask(adapter);
		reloadTask = new ReloadTask(this);

		loadSettings();
		handleSharedIntent(getIntent());
	}

	@Override
	protected void onRestart(){
		L.d("onRestart");
		super.onRestart();
		loadSettings();
	}

	@Override
	protected void onResume(){
		L.d("onResume");
		super.onResume();
		if(http == null){
			new InfoTask(this, R.string.err_settings_required).show();
			return;
		}
		if( 0 < interval){
			reloadFuture = executor.scheduleAtFixedRate( reloadTask, 0, interval, TimeUnit.SECONDS);
		}else{
			executor.execute(reloadTask);
		}
	}

	@Override
	protected void onPause() {
		L.d("onPause");
		super.onPause();
		if (reloadFuture != null) {
			reloadFuture.cancel(true);
			reloadFuture = null;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		L.d("onNewIntent");
		super.onNewIntent(intent);
		setIntent(intent);
		handleSharedIntent(intent);
	}

	private void handleSharedIntent(Intent intent){
		L.d("handleSharedIntent");
		if (http == null){
			L.d("http is null");
			return;
		}
		L.d("intent", intent);
		if (intent == null)	return;
		if ( ! Intent.ACTION_SEND.equals(intent.getAction()))	return;
		if ( ! intent.getType().startsWith("text"))	return;
		String subj = intent.getStringExtra(Intent.EXTRA_SUBJECT);
		String text = intent.getStringExtra(Intent.EXTRA_TEXT);
		L.d("subj, text", subj, text);
		if ( text != null && text.length() > 0 ){
			executor.execute(new PostTask(this, text));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_reload) {
			L.d("menu > reload");
			executor.execute(reloadTask);
			return true;
		} else if (id == R.id.action_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		} else if (id == R.id.action_filter){
			View anchor = findViewById(R.id.action_filter);
			showFilterPopupMenu(anchor);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showFilterPopupMenu(final View anchor){
		if(!pref.contains(R.string.pref_metadata)){
			new ToastTask(R.string.err_no_metadata).show();
			return;
		}

		PopupMenu popup = new PopupMenu(this, anchor);
		Menu menu = popup.getMenu();

		int idx = 0;
		Set<String> valset = pref.SS("js_vals");
		List<String> list = new ArrayList<>(valset);
		Collections.sort(list, (a, b) -> Integer.compare(Integer.parseInt(a), Integer.parseInt(b)));
		L.d("list: ", list);
		for(String val : list){
			int code = Integer.parseInt(val);
			String text = jobStatusFrom(code);
			menu.add(idx++, code, Menu.NONE, text);
		}

		popup.setOnMenuItemClickListener(item -> {
			int oldvalue = filter;
			filter = item.getItemId();
			if(this.filter != oldvalue){
				filtered.setAll(joblist);
				filtered.applyFilter(filter);
				notifyTask.post();
			}
			int all = jobStatusFrom("all");
			if(filter == all){
				L.d("all",filter);
				anchor.setBackgroundColor(Color.TRANSPARENT);
				anchor.setAlpha(1.0f);
			}else{
				anchor.setBackgroundColor(Color.GRAY);
				anchor.setAlpha(0.6f);
			}
			return true;
		});
		popup.show();
	}


	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		L.d("onCreateContextMenu");
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.menu_main_context, menu);
		L.d("Inflated");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		L.d("onContextItemSelected");
		AdapterView.AdapterContextMenuInfo info =
			(AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		int position = info.position;
		Job job = filtered.get(position);

		int id = item.getItemId();
		L.d("id", id);
		if(id == R.id.cmenu_restart){
			executor.execute(new RestartTask(this, job.id, -1));
			return true;
		}else if(id == R.id.cmenu_cancel){
			executor.execute(new CancelTask(this, job.id, -1));
			return true;
		}else if(id == R.id.cmenu_delete){
			executor.execute(new DeleteTask(this, job.id, -1));
			return true;
		}else if(id == R.id.cmenu_delete_finished){
			int finished = jobStatusFrom("finished");
			if(finished < 0) return false;
			executor.execute(new DeleteTask(this, null, finished));
			return true;
		}else if(id == R.id.cmenu_delete_terminated){
			int term = jobStatusFrom("terminated");
			if(term < 0 ) return false;
			executor.execute(new DeleteTask(this, null, term));
			return true;
		}else if(id == R.id.cmenu_delete_all){
			executor.execute(new DeleteTask(this, null, filter));
			return true;
		}else{
			return super.onContextItemSelected(item);
		}
	}

	private void showInfoDialog(Job job){
		String out = "";
		String err = "";
		if(!job.body.isEmpty())
			out = String.format("\n%s\n",job.body);
		if(!job.error.isEmpty())
			err = String.format("\n%s\n",job.error);
		String mes = String.format(
				"[id] %s\n" + "[%s] %s %s\n" + "[exit code] %d\n" + "%s" + "%s",
				job.id, jobStatusFrom(job.status), job.name, job.post, job.ec, out, err);
		TextView textView = new TextView(this);
		textView.setText(mes);
		textView.setTextIsSelectable(true);
		textView.setPadding(50, 40, 50, 40);
		textView.setTextSize(16);
		textView.setLineSpacing(0f, 1.2f);
		textView.setOnClickListener(v -> {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("", mes);
			clipboard.setPrimaryClip(clip);
			new ToastTask(R.string.msg_copied).show();
		});

		new AlertDialog.Builder(this)
			.setTitle(pref.s(R.string.cmenu_info))
			.setIcon(android.R.drawable.ic_dialog_info)
			.setView(textView)
			.setPositiveButton("OK", null)
			.show();
	}

	protected void setMetadata(JSONObject metadata){
		JSONObject obj = metadata.optJSONObject("job_status");
		Iterator<String> keys = obj.keys();
		Set<String> keyset = new HashSet();
		Set<String> valset = new HashSet(); 
		while (keys.hasNext()) {
			String key = keys.next();
			String value = obj.optString(key);
			pref.put("js_" + key, value);
			pref.put("js_" + value, key);
			keyset.add(key);
			valset.add(value);
		}
		pref.put("js_keys", keyset);// processing, unprocessing,...
		pref.put("js_vals", valset);// "1","2","4",...
	}

	public String jobStatusFrom(int i){
		return pref.S("js_" + i);
	}
	
	public int jobStatusFrom(String s){
		return pref.I("js_" + s, -2);
	}

	private void loadSettings(){
		L.d("loadSettings");
		if(pref.contains(R.string.pref_metadata)){
			JSONObject metadata = pref.J(R.string.pref_metadata);
			setMetadata(metadata);
		}
		String scheme = pref.S(R.string.pref_scheme);
		String addr = pref.S(R.string.pref_address);
		String port = pref.S(R.string.pref_port); 
		String user = pref.S(R.string.pref_user);
		String pswd = pref.S(R.string.pref_pswd);
		if(scheme == null || addr == null || port == null || user == null || pswd == null){
			http = null;
		}else{
			String baseurl = String.format("%s://%s:%s/",scheme,addr,port);
			http = new HttpConnection(baseurl, user, pswd);
			L.d("baseurl:", baseurl);
		}
		interval = pref.I(R.string.pref_refresh, 0);
	}
}

