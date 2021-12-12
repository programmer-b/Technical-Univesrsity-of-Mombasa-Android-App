package com.tum.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class Splash extends AppCompatActivity {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        boolean login,setup,portalConfig;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        login = sharedPreferences.getBoolean("login",false);
        setup = sharedPreferences.getBoolean("setup",false);
        portalConfig = sharedPreferences.getBoolean("portalConfig",false);

        if (user == null) startActivity(new Intent(Splash.this,LoginActivity.class));
        else if (!portalConfig){
            if (!setup){
                if (!login) startActivity(new Intent(Splash.this,LoginActivity.class));
                else startActivity(new Intent(Splash.this,SetupActivity.class));
            }
            else startActivity(new Intent(Splash.this,SetupActivity.class));
        }
        else startActivity(new Intent(Splash.this, MainActivity.class));
        finish();
    }
}