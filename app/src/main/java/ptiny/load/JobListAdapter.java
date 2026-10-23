package ptiny.load;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Iterator;

public final class JobListAdapter extends BaseAdapter {
    private static boolean isEmpty(String s){
        if( s == null)  return true;
        if( s.isEmpty())    return true;
        return false;
    }
    
    private final MainActivity main;
    private final ArrayList<Job> filtered;    
    private final LayoutInflater inflater;
    
    JobListAdapter(MainActivity main, ArrayList<Job> jobs) {
        this.main = main;
        this.inflater = LayoutInflater.from(main);
        this.filtered = jobs;
    }

    @Override
    public int getCount() {
        L.d("getCount", filtered.size());
        return filtered.size();
    }

    @Override
    public Object getItem(int position) {
        L.d("getItem", position);
        return filtered.get(position);
    }

    @Override
    public long getItemId(int position) {
        L.d("getItemId", position);
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        L.d("getView", position, filtered.size());
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item, parent, false);
        }
        TextView textTitle = convertView.findViewById(R.id.textTitle);
        TextView textBody = convertView.findViewById(R.id.textBody);
        TextView textError = convertView.findViewById(R.id.textError);

        L.d("filtered.get", position);
        Job job = filtered.get(position);
        String title = String.format("[%s] %s %s",
                main.jobStatusFrom(job.status), job.name, job.post); 
        textTitle.setText(title);
        if(isEmpty(job.body)){
            textBody.setVisibility(View.GONE);
        }else{
            textBody.setVisibility(View.VISIBLE);
            textBody.setText(job.body);
        }
        if(isEmpty(job.error)){
            textError.setVisibility(View.GONE);
        }else{
            textError.setText(job.error);
            textError.setVisibility(View.VISIBLE);
        }
        L.d("job", job);

        return convertView;
    }
}

