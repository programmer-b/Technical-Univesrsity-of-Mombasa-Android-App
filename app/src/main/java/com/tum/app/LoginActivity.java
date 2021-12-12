package com.tum.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class LoginActivity extends AppCompatActivity {
    private final ViewDialog dialog = new ViewDialog();
    final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Students");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {

            final EditText Email = findViewById(R.id.loginEmail);
            final EditText Password = findViewById(R.id.loginPassword);
            @Override
            public void onClick(View view) {
                if (!AndroidUtils.isInternetAvailable(LoginActivity.this)) dialog.showDialog(LoginActivity.this);
                else if (!AndroidUtils.isOnline(LoginActivity.this)) toast("No connection available");
                else if (!AndroidUtils.nonEmpty(Email)) AndroidUtils.toastMessage(LoginActivity.this,"Email cannot be empty.");
                else if (!AndroidUtils.nonEmpty(Password)) AndroidUtils.toastMessage(LoginActivity.this,"Password cannot be empty.");
                else if (!AndroidUtils.validateEmail(Email)) AndroidUtils.toastMessage(LoginActivity.this,"Your email is invalid !");
                else if (!AndroidUtils.matchMinLength(Password,6)) AndroidUtils.toastMessage
                        (LoginActivity.this, "Your password must be more than 6 characters.");
                else {
                    findViewById(R.id.progressLayout).setVisibility(View.VISIBLE);
                    String email = Email.getText().toString();
                    String password = Password.getText().toString();

                    Email.setEnabled(false);
                    Password.setEnabled(false);
                    findViewById(R.id.loginButton).setEnabled(false);

                    final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("email",email);
                    editor.putString("password",password);
                    editor.putBoolean("canNotify",true);
                    editor.putBoolean("login",true);
                    editor.apply();

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            databaseReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.hasChild(userId())) {
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putBoolean("setup",true);
                                        editor.putBoolean("portalConfig",true);
                                        editor.apply();
                                        goHome();
                                    }
                                    else openSetup();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                        else if (!task.isSuccessful()){
                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) openSetup();
                                else {
                                    AndroidUtils.toastMessage(LoginActivity.this, "You entered a wrong email or password");
                                    findViewById(R.id.progressLayout).setVisibility(View.GONE);
                                    Email.setEnabled(true);
                                    Password.setEnabled(true);
                                    findViewById(R.id.loginButton).setEnabled(true);
                                }
                            });
                        }
                        else {
                            AndroidUtils.toastMessage(LoginActivity.this, "Something went wrong");
                            findViewById(R.id.progressLayout).setVisibility(View.GONE);
                            Email.setEnabled(true);
                            Password.setEnabled(true);
                            findViewById(R.id.loginButton).setEnabled(true);
                        }
                    });
                }
            }
        });
    }

    private void toast(String message){
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
    }

    private void goHome() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private String userId(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        return user.getUid();
    }

    private void openSetup(){

        new Handler().postDelayed(()-> {
            startActivity(new Intent(LoginActivity.this,SetupActivity.class));
            finish();
        },4000);
    }
}