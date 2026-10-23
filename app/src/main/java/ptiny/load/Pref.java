package ptiny.load;

import android.app.Application;
import android.content.Context;

import android.content.SharedPreferences;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.IntegerRes;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.Set;
import java.util.Map;
import java.util.Collections;
import java.lang.ref.WeakReference;

public final class Pref {
    private final static String Config = "config";
    public static Pref instance; 
    private WeakReference<Context> context;

    private Pref(){}

    public static void init(Application app){
        instance = new Pref(); 
        instance.context = new WeakReference(app.getApplicationContext());
    }

    private Context c(){
        Context c = context.get();
        assert c != null : "Pref not initialized. Call Pref.init(MyApp) first.";
        return c;
    }

    private SharedPreferences getPrefs() {
        return c().getSharedPreferences(Config, Context.MODE_PRIVATE);
    }

    public boolean commit() {
        return getPrefs().edit().commit();
    }

    public String s(@StringRes int id){
        return c().getResources().getString(id);
    }

    public String s(@StringRes int id, Object... objs){
        return c().getResources().getString(id, objs);
    }

    public int i(@IntegerRes int id){
        return c().getResources().getInteger(id);
    }

    public boolean contains(@StringRes int keyId){
        String key = s(keyId);
        return getPrefs().contains(key);
    }

    public String S(String key){
        String ret = getPrefs().getString(key, null);
        return ret;
    }

    public String S(@StringRes int keyId){
        return S(keyId, null);
    }

    public String S(@StringRes int keyId, String defaultValue){
        L.d("pref#S", keyId, defaultValue);
        String key = s(keyId);
        String ret = getPrefs().getString(key, defaultValue);
        return ret;
    }

    public int I(String key, int defaultValue){
        String def = String.valueOf(defaultValue);
        String val = getPrefs().getString(key, def);
        int ret = Integer.parseInt(val);
        return ret;
    }

    public int I(@StringRes int keyId, int defaultValue){
        L.d("pref#I", keyId, defaultValue);
        String key = s(keyId);
        String def = String.valueOf(defaultValue);
        String val = getPrefs().getString(key, def);
        int ret = Integer.parseInt(val);
        return ret;
    }

    public boolean B(@StringRes int keyId, boolean defaultValue){
        L.d("pref#B", keyId, defaultValue);
        String key = s(keyId);
        String def = Boolean.toString(defaultValue);
        String val = getPrefs().getString(key, def);
        return Boolean.parseBoolean(val);
    }

    public JSONObject J(@StringRes int keyId){
        L.d("pref#J", keyId);
        try{
            String key = s(keyId);
            String str = getPrefs().getString(key, null);
            return new JSONObject(str);
        }catch(Exception e){
            return null;
        }
    }

    public Set<String> SS(String key){
        return getPrefs().getStringSet(key, Collections.emptySet());
    }

    public void put(String key, Set<String> val){
        getPrefs().edit().putStringSet(key, val).apply();
    }
    
    public void put(String key, String val){
        getPrefs().edit().putString(key, val).apply();
    }
    
    public void put(@StringRes int keyId, @Nullable Object value){
        L.d("pref#put", keyId, value);
        String key = s(keyId);
        String val = value == null? null: value.toString();
        getPrefs().edit().putString(key, val).apply();
    }

    public void put(@StringRes int keyId, String value){
        L.d("pref#put", keyId, "###");
        String key = s(keyId);
        getPrefs().edit().putString(key, value).apply();
    }

    public void put(@StringRes int keyId, int value){
        L.d("pref#put", keyId, value);
        String key = s(keyId);
        String val = String.valueOf(value);
        getPrefs().edit().putString(key, val).apply();
    }

    public void put(@StringRes int keyId, boolean value){
        L.d("pref#put", keyId, value);
        String key = s(keyId);
        String val = String.valueOf(value);
        getPrefs().edit().putString(key, val).apply();
    }
 
    public void put(@StringRes int keyId, JSONObject value){
        L.d("pref#put", keyId, value);
        String key = s(keyId);
        String str = value.toString();
        getPrefs().edit().putString(key, str).apply();
    }
}
