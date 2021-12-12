package com.tum.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tum.app.databinding.ActivityMapBinding;
import com.tum.app.databinding.ActivityMapsBinding;

public class Maps extends FragmentActivity implements OnMapReadyCallback {
    ViewDialog dialog = new ViewDialog();

    private WebView webView;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;


    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onPause(){
        super.onPause();
    }
    @Override
    protected void onStart(){super.onStart();}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            toast("success");
        } else {
            toast("Please enable permissions.");
        }
    }

    public static boolean checkPermission(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else return false;
    }

    private void requestPermission() {
        try {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.tum.app.databinding.ActivityMapBinding binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        findViewById(R.id.search).setOnClickListener(view1 -> {
            findViewById(R.id.progressLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.searchEdit).setEnabled(false);

            new Handler().postDelayed(()->{
                findViewById(R.id.progressLayout).setVisibility(View.GONE);
                toast("No match found !");
                findViewById(R.id.searchEdit).setEnabled(true);
            }, 2700);
        });

        webView = findViewById(R.id.direction);
        webView.setVisibility(View.GONE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        findViewById(R.id.back_button).setOnClickListener(view -> {
            if (isDirection()) reverseToMap();
            else finish();
        });
    }

    protected LocationManager locationManager;

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {


        if (checkPermission(this)) googleMap.setMyLocationEnabled(true);

        googleMap.getUiSettings().setMapToolbarEnabled(true);

        // Add a marker in TUM and move the camera
        LatLng tum = new LatLng(-4.0377, 39.6691);
        googleMap.addMarker(new MarkerOptions().position(tum).title("Marker in Technical university of Mombasa"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(tum));
        float zoomLevel = 16.0f;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tum, zoomLevel));

        CircleOptions circleOptions = new CircleOptions();

        circleOptions.center(tum)
                .radius(200)
                .strokeColor(R.color.green)
                .fillColor(0x30ff0000)
                .strokeWidth(2);
        googleMap.addCircle(circleOptions);


        findViewById(R.id.location).setOnClickListener(view -> {
            if (!AndroidUtils.isOnline(getApplicationContext())) dialog.showNoConnection(this);
            else {      if (checkPermission(this)){
                try {
                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                    if (isGPSEnabled && isNetworkEnabled){
                        showProgress();
                        new Handler().postDelayed(()->{
                            reverseToDirection();
                            WebAction();
                        },4000);
                    }
                    else {
                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                        alertDialog.setTitle("GPS settings.");
                        alertDialog.setMessage("GPS is turned off, Wanna enable?");
                        alertDialog.setPositiveButton("Ok", (dialogInterface, i) -> {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                            finish();
                        });
                        alertDialog.setNegativeButton("Cancel", (dialogInterface, i) -> {
                            dialogInterface.cancel();
                        });
                        alertDialog.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else try {
                    requestPermission();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
    private void toast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    public void WebAction() {
        webView.setWebViewClient(new WebViewClient() {
            private int running =0;
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                running ++;
                webView.loadUrl(url);
                return true;
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                toast("Error occurred.");
                hideProgress();
                reverseToMap();

            }

            public void onPageStarted(WebView view, String url, Bitmap favicon){
                running = Math.max(running,1);
            }

            public void onPageFinished(WebView view, String url) {
                if (--running == 0) {
                    toast("Just few more seconds.");
                }
                new Handler().postDelayed(()->hideProgress(),4000);
            }

        });

        if (checkPermission(this)){
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        String lat = String.valueOf(location.getLatitude());
                        String lon = String.valueOf(location.getLongitude());

                        String url1 = "https://maps.google.com/maps?saddr=";
                        String url2 = lat +","+ lon + "&daddr=-4.0377,39.6691";
                        webView.getSettings().setSupportZoom(true);
                        webView.getSettings().setJavaScriptEnabled(true);
                        webView.getSettings().setDomStorageEnabled(true);
                        webView.getSettings().setBuiltInZoomControls(true);
                        webView.loadUrl(url1 + url2);
                    });
        }
    }

    private void reverseToDirection(){
        webView.setVisibility(View.VISIBLE);
        findViewById(R.id.map).setVisibility(View.GONE);
    }

    private void reverseToMap(){
        webView.setVisibility(View.GONE);
        findViewById(R.id.map).setVisibility(View.VISIBLE);
    }
    private boolean isDirection(){
        return webView.getVisibility() == View.VISIBLE;
    }
    private void showProgress(){
        findViewById(R.id.av).setVisibility(View.VISIBLE);
    }
    private void hideProgress(){
        findViewById(R.id.av).setVisibility(View.GONE);
    }
}