package ptiny.load;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Pattern;

public class JobList extends ArrayList<Job> {
    private static final Pattern TRIM_R_HEAD = Pattern.compile("^(\\s*[\\n\\r]+)+");
    private static final Pattern TRIM_R_TAIL = Pattern.compile("([\\n\\r]+\\s*)+$");

    private static String optStr(JSONObject obj, String key){
        if(obj.isNull(key))    return "";
        String s = obj.optString(key);
        s = TRIM_R_HEAD.matcher(s).replaceFirst("");
        s = TRIM_R_TAIL.matcher(s).replaceFirst("");
        return s;
    }

    public int update(String json) throws NullPointerException, JSONException {
        if(json == null){
            throw new NullPointerException();
        }
        JSONArray array = new JSONArray(json);
        L.d("json ary size", array.length());
        this.clear();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            L.d("json obj", obj);
            String id     = obj.optString("id","...");
            String script = optStr(obj, "scriptname");
            String post		= optStr(obj, "post");
            String out		= optStr(obj, "out");
            String err    = optStr(obj, "err");
            String status	= obj.optString("status","0");
            String ec     = obj.optString("ec","0");
            Job job = new Job();
            job.id      = id;
            job.ec      = Integer.parseInt(ec);
            job.status  = Integer.parseInt(status);
            job.name    = script;
            job.post    = post;
            job.body    = out;
            job.error   = err;
            L.d("job:", job);
            this.add(job);
        }
        L.d("joblist size",this.size());
        return this.size();
    }

    public int setAll(ArrayList<Job> newval){
        this.clear();
        this.addAll(newval);
        return this.size();
    }

    public int applyFilter(int filter){
        for (int i = size() - 1; i >= 0; i--) {
            Job job = get(i);
            if((job.status & filter) == 0)
                remove(i);
        }
        return this.size();
    }
}
