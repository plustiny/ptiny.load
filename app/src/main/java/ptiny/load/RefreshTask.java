package ptiny.load;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.os.Handler;
import android.os.Looper;

class RefreshTask implements Runnable {
    private SwipeRefreshLayout refresh;
    private boolean refreshing = false;

    RefreshTask(SwipeRefreshLayout refresh){
        this.refresh = refresh;
    }
    
    private RefreshTask set(boolean b){
        this.refreshing = b;
        return this;
    }
    
    private void post(){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(this);
    }
    
    public void show(){
        set(true).post();
    }

    public void dismiss(){
        set(false).post();
    }

    @Override
    public void run() {
        refresh.setRefreshing(this.refreshing);
    }
};

