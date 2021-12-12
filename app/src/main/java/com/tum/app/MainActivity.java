package com.tum.app;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(navigate);
        openFragment(HomeFragment.newInstance("",""));
    }

    public void openFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment).addToBackStack(null).commit();
    }

    @SuppressLint("NonConstantResourceId")
    BottomNavigationView.OnNavigationItemSelectedListener navigate = item -> {
        switch (item.getItemId()){
            case R.id.menuHome:
                openFragment(HomeFragment.newInstance("",""));
                return true;
            case R.id.menuProfile:
                openFragment(ProfileFragment.newInstance("",""));
                return true;
            case R.id.menuPreferences:
                openFragment(PreferencesFragment.newInstance("",""));
                return true;
        }
        return false;
    };


    private static long backPressedTime;
    @Override
    public void onBackPressed(){
        long PERIOD = 2900;
        if (backPressedTime + PERIOD > System.currentTimeMillis()) finish();
        else AndroidUtils.toastMessage(this,"press once again to exit");
        backPressedTime = System.currentTimeMillis();
    }

}