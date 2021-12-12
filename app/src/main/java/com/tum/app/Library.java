package com.tum.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;


public class Library extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        findViewById(R.id.back_button).setOnClickListener(view -> onBackPressed());
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        findViewById(R.id.exam_bank_layout).setOnClickListener(view -> {
            Intent intent = new Intent(this,Browser.class);
            editor.putString("title","Exam bank");
            editor.putString("link","https://erepository.tum.ac.ke/");
            editor.apply();
            startActivity(intent);
        });

        findViewById(R.id.library_catalogue_layout).setOnClickListener(view -> {
            Intent intent = new Intent(this,Browser.class);
            editor.putString("title","Library catalogue");
            editor.putString("link","https://opac.tum.ac.ke/");
            editor.apply();
            startActivity(intent);
        });

        findViewById(R.id.book_space_layout).setOnClickListener(view -> {
            Intent intent = new Intent(this,Browser.class);
            editor.putString("title","Library seat reservation");
            editor.putString("link","https://docs.google.com/forms/d/e/1FAIpQLSe9mj_cTlOweNMvtuhlrubyYpJrO653-V6Au7q4o1OcIa1kQg/viewform");
            editor.apply();
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed(){
        finish();
    }
}