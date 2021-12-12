package com.tum.app;

import static android.content.ContentValues.TAG;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class Preferences_Activities extends AppCompatActivity {
    final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Students").child(userId());
    private EditText EregisterPassword;
    private EditText ElearningPassword;

    public static boolean hasSetText = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences_activities);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Passwords");

        findViewById(R.id.back_button).setOnClickListener(view -> finish());

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        EditText eregisterUsername = findViewById(R.id.EregisterUsername);
        EregisterPassword = findViewById(R.id.EregisterPassword);
        EditText elearningUsername = findViewById(R.id.ElearningUsername);
        ElearningPassword = findViewById(R.id.ElearningPassword);

        String regNo = sharedPreferences.getString("regNo","");


        eregisterUsername.setText(regNo.toUpperCase(Locale.ROOT));
        elearningUsername.setText(AndroidUtils.ElearningPassword(regNo));


        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String eregisterPassword = snapshot.child("EregisterPassword").getValue(String.class);
                String elearningPassword = snapshot.child("ElearningPassword").getValue(String.class);

                if (!hasSetText){
                    ElearningPassword.setText(elearningPassword);
                    EregisterPassword.setText(eregisterPassword);

                    hasSetText = true;
                }

                findViewById(R.id.buttonOk).setOnClickListener(new View.OnClickListener() {
                    String eregister_password;
                    String elearning_password;
                    ConstraintLayout constraintLayout;

                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(View view) {

                       if (!AndroidUtils.isOnline(getApplicationContext()))
                           Toast.makeText(getApplicationContext(), "You don't have an internet connection.", Toast.LENGTH_SHORT).show();
                       else {
                           constraintLayout = findViewById(R.id.progressLayout);
                           constraintLayout.setVisibility(View.VISIBLE);

                           eregister_password = getString(EregisterPassword);
                           elearning_password = getString(ElearningPassword);

                           userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                               @Override
                               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                   Map<String,Object> postValues = new HashMap<String,Object>();

                                   for (DataSnapshot snapshot : dataSnapshot.getChildren()) postValues.put(snapshot.getKey(),snapshot.getValue());

                                   postValues.put("EregisterPassword",eregister_password);
                                   postValues.put("ElearningPassword",elearning_password);
                                   userRef.updateChildren(postValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                           if (task.isSuccessful()){
                                               ElearningPassword.setText(elearning_password);
                                               EregisterPassword.setText(eregister_password);
                                           }
                                       }
                                   });
                               }

                               @Override
                               public void onCancelled(@NonNull DatabaseError error) {

                               }
                           });

                           SharedPreferences.Editor editor = sharedPreferences.edit();
                           editor.putString("EregisterPassword",eregister_password);
                           editor.putString("ElearningPassword",elearning_password);
                           editor.apply();

                           new Handler().postDelayed(()->{
                               constraintLayout.setVisibility(View.GONE);
                               Toast.makeText(getApplicationContext(), "Portal updated successfully.", Toast.LENGTH_LONG).show();
                               onStart();
                           },4000);
                       }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: ");
            }
        });

    }
    
    private String getString(EditText editText){
        return editText.getText().toString().trim();
    }
    private static String userId(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        return user.getUid();
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onStop(){
        super.onStop();
        hasSetText = false;
    }
}