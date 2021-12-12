package com.tum.app;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;

import java.net.URLEncoder;


public class PDF extends AppCompatActivity {

    ViewDialog dialog = new ViewDialog();

    WebView webView;
    private AppBarLayout appbar;

    private MaterialToolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);

        findViewById(R.id.back_button).setOnClickListener(view12 -> finish());
    }

    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    public void WebAction(String URL) {

        appbar = findViewById(R.id.appbar);
        toolbar = findViewById(R.id.toolbar);

        webView = (WebView) findViewById(R.id.webView);

        webView.setWebViewClient(new WebViewClient() {
            private int running =0;
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                running ++;
                webView.loadUrl(url);
                return true;
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

                webView.loadUrl("file:///android_assets/error.html");
                dialog.hideProgress();

            }
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                running = Math.max(running,1);

            }

            public void onPageFinished(WebView view, String url) {
                if (--running == 0) {
                    AndroidUtils.toastMessage(PDF.this, "Loading finished.");
                    dialog.hideProgress();
                } else {

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(PDF.this);
                    String URL = sharedPreferences.getString("URL","");

                    WebAction(URL);
                }
            }

        });

        String url="";
        try {
            url = URLEncoder.encode(URL,"UTF-8");
        } catch (Exception e) { }
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.loadUrl("https://docs.google.com/gview?embedded=true&url="+url);
        webView.clearCache(true);
        webView.clearHistory();
    }

    @Override
    public void onStart(){
        super.onStart();

        dialog.showProgress(this,"opening file",true);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(PDF.this);
        AndroidUtils.toastMessage(PDF.this,sharedPreferences.getString("URL",""));
        String url = sharedPreferences.getString("URL","");

        WebAction(url);
    }

    @Override
    public void onStop(){
        super.onStop();
        dialog.hideProgress();

        webView = (WebView) findViewById(R.id.webView);
        webView.destroy();
    }
}