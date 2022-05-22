package com.tum.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class Browser extends AppCompatActivity {
//This is the default tum browser
    ViewDialog dialog = new ViewDialog();
    private MaterialProgressBar progress;
    private RelativeLayout noConnection;

    private MaterialToolbar toolbar;

    private boolean isPanelShowing(){
        return noConnection.getVisibility() == View.VISIBLE;
    }

    private void showNoConnection(){
        if (!isPanelShowing()){
            Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);
            noConnection.startAnimation(bottomUp);
            noConnection.setVisibility(View.VISIBLE);
        }
    }

    private void hideNoConnection(){
        if (isPanelShowing()){
            Animation upBottom = AnimationUtils.loadAnimation(this, R.anim.bottom_down);
            noConnection.startAnimation(upBottom);
            noConnection.setVisibility(View.GONE);
        }
    }

    private boolean checkConnection(){
        if (!AndroidUtils.isOnline(Browser.this)) {
            showNoConnection();
            return false;
        }
        else {
            hideNoConnection();
            return true;
        }
    }

    WebView webView;
    String title;

    private String getUserName() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Browser.this);
        title = sharedPreferences.getString("title",null);

        if (title.equals("Elearning")) return AndroidUtils.ElearningPassword(sharedPreferences.getString("regNo",""));
        else if (title.equals("Eregister")) return sharedPreferences.getString("regNo","").toUpperCase(Locale.ROOT);
        else return null;
    }
    private String getPassword() {

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Browser.this);
        title = sharedPreferences.getString("title",null);

        if (title.equals("Elearning")) return sharedPreferences.getString("ElearningPassword", "");
        else if (title.equals("Eregister")) return sharedPreferences.getString("EregisterPassword","");
        else return null;
    }

    private boolean doubleBackToExitPressedOnce = false;
    private static final String TAG = MainActivity.class.getSimpleName();
    private String mCM;
    private ValueCallback<Uri> mUM;
    private ValueCallback<Uri[]> mUMA;
    private final static int FCR=1;

    //select whether you want to upload multiple files (set 'true' for yes)
    private final boolean multiple_files = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        Uri[] results = null;
        //checking if response is positive
        if(resultCode== Activity.RESULT_OK){
            if(requestCode == FCR){
                if(null == mUMA){
                    return;
                }
                if(intent == null || intent.getData() == null){
                    if(mCM != null){
                        results = new Uri[]{Uri.parse(mCM)};
                    }
                }else{
                    String dataString = intent.getDataString();
                    if(dataString != null){
                        results = new Uri[]{Uri.parse(dataString)};
                    } else {
                        if(multiple_files) {
                            if (intent.getClipData() != null) {
                                final int numSelectedFiles = intent.getClipData().getItemCount();
                                results = new Uri[numSelectedFiles];
                                for (int i = 0; i < numSelectedFiles; i++) {
                                    results[i] = intent.getClipData().getItemAt(i).getUri();
                                }
                            }
                        }
                    }
                }
            }
        }
        mUMA.onReceiveValue(results);
        mUMA = null;
    }

    @Override
    protected void onStart(){
        super.onStart();
        WebAction();
    }

    @SuppressLint({"SetJavaScriptEnabled", "WrongViewCast"})

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_browser);

        toolbar = findViewById(R.id.toolbar);

        noConnection = (RelativeLayout) findViewById(R.id.hiddenLayout);
        progress = (MaterialProgressBar) findViewById(R.id.progressLayout);

        if (!checkConnection()) new Handler().postDelayed(this::showNoConnection,0);

        findViewById(R.id.retryButton).setOnClickListener(view -> {
            if (checkConnection()) {
                hideNoConnection();
                WebAction();
            }
            else {
                hideNoConnection();
                new Handler().postDelayed(this::showNoConnection, 0);
            }
        });
        findViewById(R.id.cancelButton).setOnClickListener(view -> finish());
        findViewById(R.id.back_button).setOnClickListener(view -> finish());

        if(Build.VERSION.SDK_INT >=23 && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(Browser.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
        }

        ActionBar actionBar = getSupportActionBar();
        if(actionBar !=null)
        {
            actionBar.hide();
        }

        webView = findViewById(R.id.webView);


        webView.setWebViewClient(new Callback());
        webView.setWebChromeClient(new WebChromeClient() {

            @SuppressLint("ObsoleteSdkInt")
            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                if (multiple_files && Build.VERSION.SDK_INT >= 18) {
                    i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FCR);
            }

            //handling input[type="file"] requests for android API 21+
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

                //checking for storage permission to write images for upload
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Browser.this, perms, FCR);

                    //checking for WRITE_EXTERNAL_STORAGE permission
                } else if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Browser.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, FCR);

                    //checking for CAMERA permissions
                } else if (ContextCompat.checkSelfPermission(Browser.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Browser.this, new String[]{Manifest.permission.CAMERA}, FCR);
                }
                if (mUMA != null) {
                    mUMA.onReceiveValue(null);
                }
                mUMA = filePathCallback;
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(Browser.this.getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCM);
                    } catch (IOException ex) {
                        Log.e(TAG, "Image file creation failed", ex);
                    }
                    if (photoFile != null) {
                        mCM = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }
                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("*/*");
                if (multiple_files) {
                    contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }
                Intent[] intentArray;
                if (takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "File Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(chooserIntent, FCR);
                return true;
            }
        });

        // webViewFunction(link);
    }

    public class Callback extends WebViewClient{
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
            Toast.makeText(getApplicationContext(), "Failed loading!", Toast.LENGTH_SHORT).show();
        }
    }

    //creating new image file here
    private File createImageFile() throws IOException{
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_"+timeStamp+"_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName,".jpg",storageDir);
    }
    //back/down key handling
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event){
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            switch(keyCode){
                case KeyEvent.KEYCODE_BACK:
                    if(webView.canGoBack()){
                        webView.goBack();
                    }else{
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    public class browser extends WebViewClient {


    }

    private static class GoogleClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }
    }
    @SuppressLint("SetJavaScriptEnabled")
    public void WebAction(String URL) {
        dialog.showProgress(Browser.this,"opening file",true);

        webView = findViewById(R.id.webView);

        webView.setWebViewClient(new WebViewClient() {
            private int running =0;
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                running ++;
                webView.loadUrl(url);
                return true;
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                dialog.hideProgress();
                showNoConnection();
            }
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                running = Math.max(running,1);
                dialog.showProgress(Browser.this,"opening file",true);
            }

            public void onPageFinished(WebView view, String url) {
                if (--running == 0) {
                    AndroidUtils.toastMessage(Browser.this, "Loading finished.");
                    dialog.hideProgress();
                } else WebAction(URL);
                dialog.hideProgress();
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
        webView.loadUrl("https://docs.google.com/gview?embedded=true&url="+url);
        webView.clearCache(true);
        webView.clearHistory();

        webView.setOnKeyListener((view, i, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) onStart();
            return false;
        });
        dialog.hideProgress();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void WebAction(){
        WebSettings webSettings = webView.getSettings();
        assert webView != null;
        webView.getSettings().setAllowFileAccess(true);
        webSettings.setMixedContentMode(0);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        String username = getUserName();
        String password = getPassword();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Browser.this);
        String title = sharedPreferences.getString("title",null);
        toolbar.setTitle(title);


        webView.setWebViewClient(new WebViewClient(){


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (title.equals("Elearning")){
                    new Handler().postDelayed(()->{
                        view.loadUrl("javascript: {" +
                                "document.getElementById('username').value = '" + username + "';" +
                                "document.getElementById('password').value = '" + password + "';" +
                                "document.getElementById('loginbtn').click();" +
                                "};");
                    },1000);
                }

                else if (title.equals("Eregister")){
                    new Handler().postDelayed(()->{
                        view.loadUrl("javascript: {" +
                                "document.getElementById('gwt-uid-26').value = '" + username + "';" +
                                "document.getElementById('gwt-uid-27').value = '" + password + "';" +
                                "document.getElementsByClassName('btn O56MLC-b-a waves-effect waves-light btn-large')[0].click();" +
                                "};");
                    },3000);
                }

                progress.setVisibility(View.GONE);
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (!AndroidUtils.isOnline(getApplicationContext())) showNoConnection();
            }

            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error){
                final AlertDialog.Builder builder = new AlertDialog.Builder(Browser.this);
                builder.setMessage(title);
                builder.setTitle(R.string.app_name);
                builder.setPositiveButton("Proceed", (dialogInterface, i) -> { handler.proceed();dialogInterface.dismiss();});
                builder.setNegativeButton("Cancel", (dialogInterface, i) -> { dialogInterface.dismiss();handler.cancel();finish();});
                final AlertDialog dialog = builder.create();
                dialog.show();
            }

        });
        webView.setWebChromeClient(new GoogleClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().getDefaultFontSize();
        webView.loadUrl(sharedPreferences.getString("link","https://www.tum.ac.ke/"));
        webView.clearCache(true);
        webView.clearHistory();
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        CookieManager.getInstance().setAcceptCookie(true);

        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(Browser.this);
            final CharSequence[] items = {"Download","View","Cancel"};
            builder.setTitle(URLUtil.guessFileName(url, contentDisposition,mimetype)).setItems(items, (dialogInterface, i) -> {
                switch (i){
                    case 0: {
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        request.setMimeType(mimetype);
                        String cookies = CookieManager.getInstance().getCookie(url);
                        request.addRequestHeader("cookie", cookies);
                        request.addRequestHeader("User-Agent", userAgent);
                        request.setDescription("Downloading file...");
                        request.setTitle(URLUtil.guessFileName(url, contentDisposition,mimetype));
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalFilesDir(Browser.this,
                                Environment.DIRECTORY_DOWNLOADS, ".pdf");

                        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        dm.enqueue(request);

                        Toast.makeText(getApplicationContext(), "Downloading File",
                                Toast.LENGTH_LONG).show();
                    }
                    break;
                    case 1: WebAction(url); break;
                    default: dialogInterface.dismiss();
                }
            }).show();
        });
    }
}