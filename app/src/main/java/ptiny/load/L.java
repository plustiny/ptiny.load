package ptiny.load;

import android.util.Log;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.lang.ref.WeakReference;

public final class L {
    public static int debuglevel = 1;
    private static WeakReference<Context> appCtx;
    private static File path;
    private static String packageName;

    private L() {}

    public static void init(Application app) {
        appCtx = new WeakReference(app.getApplicationContext());
        String file = "log" + System.currentTimeMillis() + ".txt";
        path = new File(app.getExternalCacheDir(), file);
        packageName = app.getApplicationContext().getPackageName();
    }

    // Convenience debug methods
    public static void d(String msg, @Nullable Object... objs) {
        String objStr = objectsToString(objs);
        Log.d(msg, objStr);
        log("DEBUG", msg, objStr);
    }

   public static void d(String msg, @Nullable Object obj) {
        String objStr = objectsToString(obj);
        Log.d(msg, objStr);
        log("DEBUG", msg, objStr);
    }

    public static void d(@Nullable Object obj) {
        String objStr = objectsToString(obj);
        Log.d("", objStr);
        log("DEBUG", "", objStr);
    }

    // Convenience error method
    public static void e(String msg, @Nullable Object... objs) {
        String objStr = objectsToString(objs);
        Log.e(msg, objStr);
        log("ERROR", msg, objStr);
    }
    
    public static void e(String msg, @Nullable Object obj) {
        String objStr = objectsToString(obj);
        Log.e(msg, objStr);
        log("ERROR", msg, objStr);
    }

    public static void e(@Nullable Object obj) {
        String objStr = objectsToString(obj);
        Log.e("", objStr);
        log("ERROR", "", objStr);
    }

    public static void log(String tag, String msg, String objStr) {
        try{
            if(debuglevel < 1)  return;
            StackTraceElement caller = getCaller();
            String className = callerClassSimpleName(caller);
            int line = caller.getLineNumber();

            // Optional toast on main thread if context is available
            if (1 < debuglevel && appCtx.get() != null) {
                final String m = msg +":"+objStr;
                toast(m);
            }

            //String stack = simpleStack();
            String s = String.format("%s: (%s:%d) %s %s%n", tag, className, line, msg, objStr);

            if (path != null) {
                try (FileOutputStream fos = new FileOutputStream(path, true)) {
                    fos.write(s.getBytes(StandardCharsets.UTF_8));
                }
            }
        }catch (Exception e) {
            toast(e); 
        }
    }

        // Helpers
    private static StackTraceElement getCaller() {
        try{
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        for(int i = 2; i < st.length; i++){
            String className = callerClassSimpleName(st[i]);
            if(className != null && !className.equals("L")){
                return st[i];
            }
        }
        }catch(Exception ignore){
        }
        return new StackTraceElement("Unknown", "unknownMethod", null, -1);
    }

    private static String objectsToString(Object... objs){
        try{
            String objStr = "";
            for(Object o : objs){
                String cls = "null";
                String val = "null";
                if(o != null){
                    cls = o.getClass().getSimpleName();
                    val = o.toString();
                }
                objStr = String.format("%s  %s:%s", objStr, cls, val);
            }
            return objStr;
        }catch(Exception e){
            return e.toString();
        }
    }

    private static String callerClassSimpleName(StackTraceElement ste) {
        String fq = ste.getClassName();
        int idx = fq.lastIndexOf('.');
        return idx >= 0 ? fq.substring(idx + 1) : fq;
    }

    public static String simpleStack(){
        String stack = "";
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        for(int i = 0; i < st.length; i++){
            String c = callerClassSimpleName(st[i]);
            int l = st[i].getLineNumber();
            stack = String.format("%s(%s:%d)", stack, c, l);
        }
        return stack + "\n";
    }

    private static void toast(Object obj){
        final String mes = obj == null? "null": obj.toString();
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(appCtx.get(), mes, Toast.LENGTH_LONG).show()
                );
    }
}
