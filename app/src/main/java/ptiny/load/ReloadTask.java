package ptiny.load;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.concurrent.atomic.AtomicBoolean;

import ptiny.load.HttpConnection.Response;

class ReloadTask extends HttpTask {
    final AtomicBoolean running = new AtomicBoolean(false);
    private final MainActivity main;

    ReloadTask(MainActivity activity){
        this.main = activity;
    }

    @Override
    public void run() {
        L.d("ReloadTask", running.get());
        Pref pref = main.pref;
        HttpConnection http = main.http;
        if(running.getAndSet(true))	return;
        try{
            main.refreshTask.show();
            if(!pref.contains(R.string.pref_metadata)){
                L.d("query metadata");
                Response res = http.queryMetadata();
                String msg = String.format("Metadata > %s", res.getStatus());
                new InfoTask(main, msg).show();
                
                JSONObject metadata = new JSONObject(res.body);
                L.d("meta", metadata);
                pref.put(R.string.pref_metadata, metadata);
                main.setMetadata(metadata);
            }
            L.d("query select");
            Response res = http.querySelect(-1, -1);
            String msg = String.format("Select > %s", res.getStatus());
            new InfoTask(main, msg).show();

            String json = res.body;
            L.d("json", json);
            int total = main.joblist.update(json);
            main.filtered.setAll(main.joblist);
            int size = main.filtered.applyFilter(main.filter);

            main.notifyTask.post();
            if(total == 0){
                new InfoTask(main, R.string.msg_no_jobs).show();
            }else if(size == 0){
                new InfoTask(main, R.string.msg_not_found).show();
            }
        }catch(Exception e){
            L.e(e);
            handleException(e);
        }finally{
            main.refreshTask.dismiss();
            running.getAndSet(false);
        }
    }
}
