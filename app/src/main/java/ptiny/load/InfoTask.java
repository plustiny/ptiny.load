package ptiny.load;

import androidx.annotation.StringRes;
import com.google.android.material.snackbar.Snackbar;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import android.view.View;
import android.view.View.OnClickListener;

class InfoTask implements Runnable {
    private View root;
    private String mes;

    InfoTask(Activity activity, String mes){
        this.root = activity.findViewById(android.R.id.content);
        String app_name = Pref.instance.s(R.string.app_name);
        this.mes = String.format("[%s] %s",app_name, mes);
    }
    InfoTask(Activity activity, @StringRes int resid){
        this(activity, Pref.instance.s(resid));
    }

    public void show(){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(this);
    }

    @Override
    public void run() {
        final Snackbar snackbar = Snackbar.make(root, mes, 5*Snackbar.LENGTH_LONG);
        snackbar.setAction(android.R.string.ok, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }
};

