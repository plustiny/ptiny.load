package ptiny.load;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.concurrent.atomic.AtomicBoolean;

//AndroidManifest.xml に登録するのを忘れない:
public class MyApp extends Application {
    public static MyApp instance;
    private final AtomicBoolean crashing = new AtomicBoolean(false);
    private final Thread.UncaughtExceptionHandler origHandler = Thread.getDefaultUncaughtExceptionHandler();
    
    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(this::uncaughtException);
        instance = this;
        L.init(this);
        L.d("\n\ninit");
        Pref.init(this);
        Pref pref = Pref.instance;
        L.d("pref inited");
        int level = pref.I(R.string.pref_debuglevel, 1);
        pref.put(R.string.pref_debuglevel, level);
        L.debuglevel = level;
        L.d("debuglevel",level);
        int system = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        int theme = pref.I(R.string.pref_theme, system);
        L.d("theme", theme, system);
        AppCompatDelegate.setDefaultNightMode(theme);
    }

    public static Context getAppContext() {
        return instance.getApplicationContext();
    }

    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            if (crashing.getAndSet(true)) return;
            L.e("ERROR: UncatughtException!", ex);
            L.e("stack: ", L.simpleStack());
        } catch (Throwable ignored) {
            // NOP
        } finally {
            if (origHandler != null) {
                origHandler.uncaughtException(thread, ex);
            } else {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(2);
            }
        }
    }
}
