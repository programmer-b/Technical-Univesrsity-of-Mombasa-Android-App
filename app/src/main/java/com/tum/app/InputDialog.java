package com.tum.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

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



public class InputDialog {

    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    String userId;
    {
        assert user != null;
        userId = user.getUid();
    }

    final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Students").child(userId);

    @SuppressLint("SetTextI18n")
    public void showEregister(Activity activity, String regNo){
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.input_dialog);


        EditText userName = dialog.findViewById(R.id.textUserName);
        EditText password = dialog.findViewById(R.id.textPassword);
        Button button = dialog.findViewById(R.id.buttonOk);

        dialog.show();

        TextView textView = dialog.findViewById(R.id.inputTitle);
        textView.setText("Confirm Eregister credentials");

        userName.setText(regNo.toUpperCase(Locale.ROOT));
        password.setText(regNo.toUpperCase(Locale.ROOT));

        button.setOnClickListener(view -> {
            String EPassword = password.getText().toString();
            editor.putString("EregisterPassword",EPassword);
            editor.apply();

            dialog.dismiss();

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Map<String,Object> postValues = new HashMap<String,Object>();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) postValues.put(snapshot.getKey(),snapshot.getValue());

                    postValues.put("EregisterPassword",EPassword);
                    databaseReference.updateChildren(postValues);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
    }

    @SuppressLint("SetTextI18n")
    public void showElearning(Activity activity, String regNo){
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.input_dialog);

        dialog.show();

        EditText userName = dialog.findViewById(R.id.textUserName);
        EditText password = dialog.findViewById(R.id.textPassword);
        Button button = dialog.findViewById(R.id.buttonOk);

        TextView textView = dialog.findViewById(R.id.inputTitle);
        textView.setText("Confirm Elearning credentials");

        userName.setText(AndroidUtils.ElearningPassword(regNo));
        password.setText(AndroidUtils.ElearningPassword(regNo));

        button.setOnClickListener(view1 -> {
            String ePassword = password.getText().toString();

            editor.putString("ElearningPassword",ePassword);
            editor.apply();

            dialog.dismiss();

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                    Map<String,Object> postValues1 = new HashMap<String,Object>();

                    for (DataSnapshot snapshot1 : dataSnapshot1.getChildren()) postValues1.put(snapshot1.getKey(),snapshot1.getValue());

                    postValues1.put("ElearningPassword",ePassword);
                    databaseReference.updateChildren(postValues1);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
    }
}
