package ptiny.load;

import androidx.annotation.StringRes;
import com.google.android.material.snackbar.Snackbar;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import android.view.View;
import android.view.View.OnClickListener;

class ToastTask implements Runnable {
    private String mes;

    ToastTask(String mes){
        String app_name = Pref.instance.s(R.string.app_name);
        this.mes = String.format("[%s] %s",app_name, mes);
    }
    ToastTask(@StringRes int resid){
        this(Pref.instance.s(resid));
    }

    public void show(){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(this);
    }

    @Override
    public void run() {
        Toast.makeText(MyApp.instance, mes, Toast.LENGTH_LONG).show();
    }
};

