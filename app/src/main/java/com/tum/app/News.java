package com.tum.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


public class News extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
    }

    @Override
    public void onBackPressed(){
        finish();
    }
}