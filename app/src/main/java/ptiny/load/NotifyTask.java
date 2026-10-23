package ptiny.load;

import android.widget.BaseAdapter;
import android.os.Handler;
import android.os.Looper;

class NotifyTask implements Runnable {
    private BaseAdapter adapter;

    NotifyTask(BaseAdapter adapter){
        this.adapter = adapter;
    }

    public void post(){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(this);
    }

    @Override
    public void run() {
        adapter.notifyDataSetChanged();
    }
};

