package ptiny.load;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.json.JSONException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ConnectException; 
import java.net.SocketTimeoutException;
import java.util.ArrayList;

abstract class HttpTask implements Runnable {
    protected String toStr(Object... objs){
        ArrayList<String> strs = new ArrayList<>();
        for(Object o : objs){
            if( o != null){
                strs.add(o.toString());
            }
        }
        return String.join(" ",strs);
    }

    protected void handleException(Exception e){
        if (e instanceof MalformedURLException) {
            L.e("URL", e);
            new ToastTask(R.string.err_url).show();
        } else if (e instanceof ConnectException) {
            L.e("Conn", e);
            new ToastTask(R.string.err_conn).show();
        } else if (e instanceof NullPointerException) {
            L.e("Null", e);
            new ToastTask(R.string.err_load).show();
        } else if (e instanceof JSONException) {
            L.e("Json", e);
            new ToastTask(R.string.err_json).show();
        } else if (e instanceof SocketTimeoutException){
            L.e("Timeout", e);
            new ToastTask(R.string.err_timeout).show();
        } else if (e instanceof IOException){
            L.e("IO",e);
            new ToastTask(R.string.err_io).show();
        }else {
            L.e("Exception", e);
            new ToastTask(R.string.err_load).show();
        }
    }
};

