package ptiny.load;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class AboutActivity extends AppCompatActivity {
    private WebView webview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        webview = findViewById(R.id.webview);
        webview.setWebViewClient(new WebViewClient());

        WebSettings ws = webview.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);

        String lang = Locale.getDefault().getLanguage();
        L.d("lang",lang);
        try{
            String filename = "html/index." + lang + ".html";
            L.d("filename", filename);
            getAssets().open(filename).close();
            webview.loadUrl("file:///android_asset/" + filename);
        }catch(IOException e){
            webview.loadUrl("file:///android_asset/html/index.html");
        }

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (webview.canGoBack()) {
                            webview.goBack();
                        } else {
                            setEnabled(false);
                            getOnBackPressedDispatcher().onBackPressed();
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (webview != null) {
            webview.loadUrl("about:blank");
            webview.stopLoading();
            webview.setWebViewClient(null);
            webview.destroy();
            webview = null;
        }
        super.onDestroy();
    }
}
