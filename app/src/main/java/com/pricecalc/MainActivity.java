package com.pricecalc;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {
    WebView w;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        w = new WebView(this);
        setContentView(w);
        WebSettings s = w.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        w.setWebViewClient(new WebViewClient());
        w.addJavascriptInterface(new Bridge(), "Android");
        w.loadUrl("file:///android_asset/index.html");
    }

    // Native HTTP GET (no CORS limits). Result is sent back to window[cb](text, error).
    class Bridge {
        @JavascriptInterface
        public void get(final String u, final String cb) {
            new Thread(new Runnable() {
                public void run() {
                    String t = "", e = "";
                    try {
                        if (!u.startsWith("http://") && !u.startsWith("https://")) throw new Exception("bad url");
                        HttpURLConnection c = (HttpURLConnection) new URL(u).openConnection();
                        c.setConnectTimeout(10000);
                        c.setReadTimeout(10000);
                        BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream(), "UTF-8"));
                        StringBuilder sb = new StringBuilder();
                        String l;
                        while ((l = r.readLine()) != null) sb.append(l);
                        r.close();
                        t = sb.toString();
                    } catch (Exception x) {
                        e = String.valueOf(x);
                    }
                    final String js = "window['" + cb + "'](" + JSONObject.quote(t) + "," + JSONObject.quote(e) + ")";
                    runOnUiThread(new Runnable() {
                        public void run() { w.evaluateJavascript(js, null); }
                    });
                }
            }).start();
        }
    }

    @Override
    public void onBackPressed() {
        if (w.canGoBack()) w.goBack(); else super.onBackPressed();
    }
}
