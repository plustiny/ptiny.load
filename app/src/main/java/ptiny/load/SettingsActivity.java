package ptiny.load;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatDelegate;

import android.widget.ArrayAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.content.Intent;
import android.widget.TextView;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {
    Pref pref = Pref.instance;
    RadioGroup radioScheme;
    EditText address;
    EditText port;
    EditText user;
    EditText pswd;
    RadioGroup radioTheme;
    EditText refresh;
    EditText timeout;
    Spinner debuglevel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        TextView about = findViewById(R.id.settings_about);
        about.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        radioScheme = findViewById(R.id.radio_scheme);
        String scheme = pref.S(R.string.pref_scheme);
        L.d("settings > scheme", scheme);
        if(scheme == null || scheme.startsWith("https")){
            L.d("https");
            radioScheme.check(R.id.radio_https);
        }else{
            L.d("http");
            radioScheme.check(R.id.radio_http);
        }
        address = findViewById(R.id.edit_address);
        address.setText(pref.S(R.string.pref_address,""));
        port    = findViewById(R.id.edit_port);
        port.setText(pref.S(R.string.pref_port,""));
        user    = findViewById(R.id.edit_user);
        user.setText(pref.S(R.string.pref_user,""));
        pswd    = findViewById(R.id.edit_pswd);
        pswd.setText(pref.S(R.string.pref_pswd,""));
        radioTheme  = findViewById(R.id.radio_theme);
        String system = findViewById(R.id.radio_system).getTag().toString();
        String theme = pref.S(R.string.pref_theme, system);
        for(int i : new int[]{R.id.radio_light, R.id.radio_dark, R.id.radio_system}){
            RadioButton rb = findViewById(i);
            if(rb.getTag().toString().equals(theme)){
                radioTheme.check(i);
            }
        }
        refresh = findViewById(R.id.edit_refresh);
        refresh.setText(pref.S(R.string.pref_refresh,"0"));
        timeout = findViewById(R.id.edit_timeout);
        timeout.setText(pref.S(R.string.pref_timeout,"20"));
        
        debuglevel = findViewById(R.id.spinner_debuglevel);
        ArrayList<DebugLevel> levels = DebugLevel.debugLevels(); 
        ArrayAdapter<DebugLevel> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                levels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        debuglevel.setAdapter(adapter);
        for(int i = 0; i < levels.size(); i++){
            if(levels.get(i).level == pref.I(R.string.pref_debuglevel, 0)){
                debuglevel.setSelection(i);
            }
        }
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_commit) {
            commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void commit(){
        try{
            String scheme = "https";
            int checked = radioScheme.getCheckedRadioButtonId();
            if(checked == R.id.radio_http){
                scheme = "http";
            }
            pref.put(R.string.pref_scheme,  scheme);
            pref.put(R.string.pref_address, address.getText().toString());
            pref.put(R.string.pref_port,    port.getText().toString());
            pref.put(R.string.pref_user,    user.getText().toString());
            pref.put(R.string.pref_pswd,    pswd.getText().toString());
            checked = radioTheme.getCheckedRadioButtonId();
            RadioButton rb = findViewById(checked);
            pref.put(R.string.pref_theme, rb.getTag().toString());
            int system = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            int theme = pref.I(R.string.pref_theme, system);
            AppCompatDelegate.setDefaultNightMode(theme);
            pref.put(R.string.pref_refresh, refresh.getText().toString());
            pref.put(R.string.pref_timeout, timeout.getText().toString());

            DebugLevel level = (DebugLevel)debuglevel.getSelectedItem();
            pref.put(R.string.pref_debuglevel, level.level);
            pref.commit();
            new ToastTask(R.string.msg_settings_saved).show();
            finish();
        }catch(Exception e){
            new ToastTask(R.string.err_settings).show();
        }
    }
}

