package com.tum.app;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.wang.avi.AVLoadingIndicatorView;

import java.util.HashMap;
import java.util.Map;

public class ViewDialog {
    public void showDialog(Activity activity){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.no_internet_dialog);

        TextView neutralText = (TextView) dialog.findViewById(R.id.cancelButton);
        TextView positiveTExt = (TextView) dialog.findViewById(R.id.turnOnButton);

        neutralText.setOnClickListener(view -> dialog.dismiss());
        positiveTExt.setOnClickListener(view -> activity.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS)));

        dialog.show();
        new Handler().postDelayed(dialog::dismiss,7000);
    }

    public void showComingSoon(Activity activity){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.coming_soon);

        TextView neutralText = (TextView) dialog.findViewById(R.id.cancelButton);

        neutralText.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
        new Handler().postDelayed(dialog::dismiss,7000);
    }

    public void showNoConnection(Activity activity){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.no_connection);
        dialog.show();
        new Handler().postDelayed(dialog::dismiss,3000);
    }
    @SuppressLint("SetTextI18n")
    public void showDownload(Activity activity, boolean finished, String fileName){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.download_layout);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);

        dialog.show();

        TextView textView = dialog.findViewById(R.id.fileName);
        ImageView imageView = dialog.findViewById(R.id.statusIcon);
        if (!finished) {
            imageView.setImageResource(R.drawable.down_arrow);

            textView.setText("downloading" + fileName);
            new Handler().postDelayed(()->{
                textView.setText("Finished");
                new Handler().postDelayed(dialog::dismiss,2500);
            },2500);
        }
        else {
            imageView.setImageResource(R.drawable.finished);

            textView.setText("Finished");
            new Handler().postDelayed(dialog::dismiss,2500);
        }
    }


    Dialog dialog;

    public void showProgress(Activity activity,String message,boolean cancelable){
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(cancelable);
        dialog.setContentView(R.layout.progress_dialog);

        TextView textView = dialog.findViewById(R.id.message);
        textView.setText(message);
        dialog.show();
    }

    public void showProgress(Activity activity,String message,String indicatorName){
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.progress_dialog);

        AVLoadingIndicatorView av = dialog.findViewById(R.id.av);
        av.setIndicator(indicatorName);

        TextView textView = dialog.findViewById(R.id.message);
        textView.setText(message);
        dialog.show();
    }

    public void hideProgress(){
        if (dialog != null){
            dialog.dismiss();
            dialog = null;
        }
    }

    public void Logout(Activity activity, Context context){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.logout);

        TextView neutralText = (TextView) dialog.findViewById(R.id.cancel);
        TextView positiveText = (TextView) dialog.findViewById(R.id.ok);

        neutralText.setOnClickListener(view -> dialog.dismiss());
        positiveText.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            activity.startActivity(new Intent(context,LoginActivity.class));
            activity.finish();
        });

        dialog.show();
    }

    public void updateProfile(Context  context,Activity activity,String dialogTitle,String group,String valueName){
        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference().child("Data").child("Courses").child(group).child(group + "_Info");

        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.edit_profile);

        dialog.findViewById(R.id.profileImage).setVisibility(View.GONE);
        EditText valueEdit = dialog.findViewById(R.id.valueEdit);
        valueEdit.setHint(dialogTitle);

        TextView title = dialog.findViewById(R.id.title);
        title.setText(dialogTitle);

        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String valueOld = dataSnapshot.child(valueName).getValue(String.class);
                valueEdit.setText(valueOld);

                dialog.findViewById(R.id.cancel).setOnClickListener(view -> dialog.dismiss());
                dialog.findViewById(R.id.buttonOk).setOnClickListener(new View.OnClickListener() {
                    AVLoadingIndicatorView av;
                    @Override
                    public void onClick(View view) {
                        av = dialog.findViewById(R.id.av);
                        av.setVisibility(View.VISIBLE);
                        String valueNew = valueEdit.getText().toString();

                        ProfileFragment profileFragment = new ProfileFragment();

                        switch (valueName){
                            case "faculty": ProfileFragment.faculty = valueNew; break;
                            case "dean": ProfileFragment.dean = valueNew; break;
                            case "headOfDepartment": ProfileFragment.headOfDepartment = valueNew; break;
                            case "department":ProfileFragment.department = valueNew; break;
                            default: break;
                        }

                        Map<String,Object> postValues = new HashMap<String,Object>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) postValues.put(snapshot.getKey(),snapshot.getValue());
                        postValues.put(valueName,valueNew);
                        groupRef.updateChildren(postValues);

                        new Handler().postDelayed(()->{
                            av.setVisibility(View.GONE);
                            dialog.dismiss();
                            context.startActivity(new Intent(context, MainActivity.class));
                        },3000);

                        Log.d(TAG, "Restart your application now to apply your updates.");
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: Database error.");
            }
        });
        dialog.show();
    }

    public void updateProfile(Activity activity,Context context){
        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Students").child(userId());

        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.edit_profile);

        String Title = "Year of study";

        dialog.findViewById(R.id.profileImage).setVisibility(View.GONE);
        EditText valueEdit = dialog.findViewById(R.id.valueEdit);
        valueEdit.setHint(Title);

        TextView title = dialog.findViewById(R.id.title);
        title.setText(Title);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String valueOld = dataSnapshot.child("yearOfStudy").getValue(String.class);
                valueEdit.setText(valueOld);

                dialog.findViewById(R.id.cancel).setOnClickListener(view -> dialog.dismiss());
                dialog.findViewById(R.id.buttonOk).setOnClickListener(new View.OnClickListener() {
                    AVLoadingIndicatorView av;
                    @Override
                    public void onClick(View view) {
                        av = dialog.findViewById(R.id.av);
                        av.setVisibility(View.VISIBLE);
                        String valueNew = valueEdit.getText().toString();

                        ProfileFragment profileFragment = new ProfileFragment();

                        Map<String,Object> postValues = new HashMap<String,Object>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) postValues.put(snapshot.getKey(),snapshot.getValue());
                        postValues.put("yearOfStudy",valueNew);
                        userRef.updateChildren(postValues);
                        new Handler().postDelayed(()->{
                            av.setVisibility(View.GONE);
                            dialog.dismiss();
                            context.startActivity(new Intent(context, MainActivity.class));
                        },3000);
                        Log.d(TAG, "Restart your application now to apply your updates.");
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: Database error");
            }
        });
        dialog.show();
    }


    private static String userId(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        return user.getUid();
    }

    public void portalConfig(Activity activity,Context context,String title, String regNo){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.portal_config);

        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Students").child(userId());
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        TextView Title = dialog.findViewById(R.id.title);
        Title.setText(title);
        EditText Username = dialog.findViewById(R.id.username);
        EditText Password = dialog.findViewById(R.id.password);

        if (title.equals("Eregister")){
            Username.setText(regNo);
            Password.setText(regNo);
        }
        else {
            Username.setText(AndroidUtils.ElearningPassword(regNo));
            Password.setText(AndroidUtils.ElearningPassword(regNo));
        }

        dialog.findViewById(R.id.buttonCancel).setOnClickListener(view -> dialog.dismiss());
        dialog.findViewById(R.id.buttonNext).setOnClickListener(view -> {
            dialog.findViewById(R.id.progressLayout).setVisibility(View.VISIBLE);
            String password = Password.getText().toString();

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Map<String, Object> postValues = new HashMap<String, Object>();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                        postValues.put(snapshot.getKey(), snapshot.getValue());

                    if (title.equals("Eregister")){
                        postValues.put("EregisterPassword", password);
                        postValues.put("EregisterConfirmed","true");

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("EregisterPassword",password);
                        editor.putBoolean("EregisterConfirmed",true);
                        Intent intent = new Intent(context,Browser.class);
                        editor.putString("title","Eregister");
                        editor.putString("link","https://eregistrar.tum.ac.ke/Campuscura/?TenantID=tum#login;TenantID=tum;Apply=false");
                        editor.apply();
                        context.startActivity(intent);
                    }
                    else {
                        postValues.put("ElearningPassword", password);
                        postValues.put("ElearningConfirmed","true");

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("ElearningPassword",password);
                        editor.putBoolean("ElearningConfirmed",true);
                        Intent intent = new Intent(context,Browser.class);
                        editor.putString("title","Elearning");
                        editor.putString("link","https://elearning.tum.ac.ke/login/index.php");
                        editor.apply();
                        context.startActivity(intent);
                    }

                    new Handler().postDelayed(dialog::dismiss,4000);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
        dialog.show();
    }

    public void showPrivacy(Activity activity){
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.privacy_policy);
        dialog.findViewById(R.id.buttonOk).setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    public void showAbout(Activity activity){
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.about_page);
        dialog.findViewById(R.id.buttonOk).setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }
}
