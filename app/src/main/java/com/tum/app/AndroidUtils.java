package com.tum.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class AndroidUtils extends Application{

    private static final boolean YES = true;
    private static final boolean NO = false;
    private static final String CHANNEL_NAME = "FCM";
    private static final String CHANNEL_DESC = "Firebase Cloud Messaging";

    private static final String EMAIL_PATTERN_1 = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private static final String EMAIL_PATTERN_2 = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+\\.+[a-z]+";

    public static AndroidUtils androidUtils;

    public static String encodeImage(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);

        byte[] b = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(b,Base64.DEFAULT);
    }

    @SuppressLint("Recycle")
    public static String getNameFromURI(Uri uri,Context context){
        Cursor c = context.getContentResolver().query(uri,null,null,null,null);
        int index_ed_name = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        c.moveToFirst();
        String fileName = c.getString(index_ed_name);
        c.close();
        return fileName;
    }

    public static String userGroup(String regNo){
        int end = regNo.indexOf("/");
        String group = null;
        if (end != 1) group = regNo.substring(0,end);
        String year = "";

        year = regNo.substring(regNo.length()-4);

        String s;
        s = group + "\n" + year;
        return s;
    }

    public static String userGroupName(String regNo,String yearOfStudy){
        int end = regNo.indexOf("/");
        String group = null;
        if (end != 1) group = regNo.substring(0,end);
        String year = "";

        year = regNo.substring(regNo.length()-4);

        String s;
        s = group + year + yearOfStudy;
        return s;
    }
    @SuppressLint("SimpleDateFormat")
    public static String getFormattedDate(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int day = cal.get(Calendar.DATE);

        if (!((day>10) && (day<19))) switch (day%10){
            case 1:
                return new SimpleDateFormat("d 'st' 'of' MMMM yyyy").format(date);
            case 2:
                return new SimpleDateFormat("d 'nd' 'of' MMMM yyyy").format(date);
            case 3:
                return new SimpleDateFormat("d 'rd' 'of' MMMM yyyy").format(date);
            default:
                return new SimpleDateFormat("d 'th' 'of' MMMM yyyy").format(date);
        }
        return new SimpleDateFormat("d 'th' 'of' MMMM yyyy").format(date);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void notify(Context context, String title, String message){


    }

    public static  boolean isInternetConnected(){
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor()==0);
        } catch (Exception e){
            return false;
        }
    }

    public static boolean isOnline(Context context){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            URL url = new URL("https://www.google.com/");
            HttpURLConnection httpURLConnection = null;
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(2000);
            httpURLConnection.connect();

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) return YES;
            else return NO;
        } catch (IOException e) {
            e.printStackTrace();
            return NO;
        }
    }

    public static void toastMessage(Context context,String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }


    public static String ElearningPassword(String registration){
        String pass = registration.replace("/","");
        return pass.toLowerCase(Locale.ROOT);
    }

    public static boolean nonEmpty(EditText editText) {
        if (editText != null && !(TextUtils.isEmpty(editText.getText().toString().trim()))) {
            return YES;
        } else {
            Log.d("SERI_PAR->Error", "edit text object is null");
            return NO;
        }
    }

    public static void  hideActionBar(AppBarLayout newsAppbar, MaterialToolbar newsToolbar) {
        newsAppbar.setVisibility(View.VISIBLE);
        newsAppbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> newsAppbar.animate()
                .translationY(-newsToolbar.getHeight())
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start(),3000);
    }

    public static String firstName(String fullName){
        String arr[]  =  fullName.split(" ",2);
        String firstWord = arr[0];

        String s = firstWord.toLowerCase(Locale.ROOT);
        String s1 = s.substring(0,1);
        String s2 = s.substring(1,s.length());
        s1=s1.toUpperCase(Locale.ROOT);
        return s1+s2;
    }

    public static int wordCount(EditText editText){
        String string = editText.getText().toString();

        String[] words = string.split("\\s+");
        return words.length;
    }
   public static int validateSlashCount(String reg_no){
       return reg_no.length() - reg_no.replace("/","").length();
   }
   public static boolean validateYear(String reg_no){
        String year = reg_no.substring(reg_no.length()-4);
        if (isNumeric(year)){
            int foo = Integer.parseInt(year);
            if (foo>2009 && foo<2025) {
                return YES;
            }
            else return NO;
        }
        else{
            return NO;
        }
   }
   public static int validateLength(String reg_no){
        return reg_no.length();
   }

   public static boolean validateRegistrationNumber(String reg_no){
        if (validateSlashCount(reg_no)==2 && validateYear(reg_no) && validateLength(reg_no)>=10 ) return YES;
        else return NO;
   }

   public static boolean isNumeric(String year){
        try {
            Double.parseDouble(year);
            return true;
        }catch (NumberFormatException e){
            return false;
        }
   }

   public static boolean nonEmpty(Context context, EditText editText, String msg) {
        if (nonEmpty(editText)) {
            return YES;
        } else {
            showToast(context, msg);
            return NO;
        }
    }

    public static boolean validateEmail(EditText editText) {
        if (nonEmpty(editText)) {
            String emailAsString = removeBlankSpace(editText.getText().toString());
            return emailAsString.matches(EMAIL_PATTERN_1)
                    || emailAsString.matches(EMAIL_PATTERN_2);

        } else {
            Log.d("SERI_PAR->Error", "edit text object is null");
            return NO;
        }
    }

    public static boolean validateEmail(Context context, EditText editText, String msg) {
        if (validateEmail(editText)) {
            return YES;
        } else {
            showToast(context, msg);
            return NO;
        }
    }

    public static boolean matchMinLength(EditText editText, int length) {
        if (nonEmpty(editText)) {
            String content = removeBlankSpace(editText.getText().toString());
            return content.length() >= length ? YES : NO;
        } else {
            Log.e("SERI_PAR->Error", "edit text object is null");
            return NO;
        }
    }

    public static boolean matchMinLength(Context context, EditText editText, int length, String msg) {
        if (matchMinLength(editText, length)) {
            return YES;
        } else {
            showToast(context, msg);
            return NO;
        }
    }

    public static boolean noSpecialCharacters(EditText editText) {
        if (nonEmpty(editText)) {
            String content = removeBlankSpace(editText.getText().toString());
            return content.matches("[a-zA-Z0-9.? ]*");
        } else {
            Log.e("SERI_PAR->Error", "edit text object is null");
            return NO;
        }
    }

    public static boolean noSpecialCharacters(Context context, EditText editText, String msg) {
        if (noSpecialCharacters(editText)) {
            return YES;
        } else {
            showToast(context, msg);
            return NO;
        }
    }

    public static boolean matchLength(EditText editText, int length) {
        if (nonEmpty(editText)) {
            String content = removeBlankSpace(editText.getText().toString());
            return content.length() == length;
        } else {
            Log.d("SERI_PAR->Error", "edit text object is null");
            return NO;
        }
    }

    public static boolean matchLength(Context context, EditText editText, int length, String msg) {
        if (matchLength(editText, length)) {
            return YES;
        } else {
            showToast(context, msg);
            return NO;
        }
    }

    public static boolean mobileNumberValidation(EditText editText) {
        if (nonEmpty(editText)) {
            String mobileNumber = removeBlankSpace(editText.getText().toString().trim());
            return Patterns.PHONE.matcher(mobileNumber).matches();
        } else {
            Log.d("SERI_PAR->Error", "edit text object is null");
            return NO;
        }
    }

    public static boolean mobileNumberValidation(Context context, EditText editText, String msg) {
        if (mobileNumberValidation(editText)) {
            return YES;
        } else {
            showToast(context, msg);
            return NO;
        }
    }

    public static boolean mobileNumberValidation(EditText editText, Pattern pattern) {
        if (nonEmpty(editText)) {
            String mobileNumber = removeBlankSpace(editText.getText().toString());
            return pattern.matcher(mobileNumber).matches();
        } else {
            return NO;
        }
    }

    public static boolean mobileNumberValidation(Context context, EditText editText, Pattern pattern, String msg) {
        if (mobileNumberValidation(editText, pattern)) {
            return YES;
        } else {
            showToast(context, msg);
            return NO;
        }
    }

    public static boolean mobileNumberValidation(EditText editText, int minLength, int maxLength) {
        if (minLength > 0 && maxLength > 0 && nonEmpty(editText)) {
            String mobileNumber = removeBlankSpace(editText.getText().toString().trim());
            return mobileNumber.length() >= minLength && mobileNumber.length() <= maxLength;
        } else {
            return NO;
        }
    }

    public static boolean mobileNumberValidation(Context context, EditText editText, int minLength, int maxLength, String msg) {
        if (mobileNumberValidation(editText, minLength, maxLength)) {
            return YES;
        } else {
            showToast(context, msg);
            return NO;
        }
    }

    public static boolean matchText(EditText baseEditText, EditText... editTexts) {
        if (nonEmpty(baseEditText)) {
            String matchString = baseEditText.getText().toString();
            for (EditText editText : editTexts) {
                if (editText == null || !(matchString.equals(editText.getText().toString()))) {
                    return NO;
                }
            }
        } else {
            return NO;
        }
        return YES;
    }

    public static boolean matchText(Context context, String msg, EditText baseEditText, EditText... editTexts) {
        if (matchText(baseEditText, editTexts)) {
            return YES;
        } else {
            showToast(context, msg);
            return NO;
        }
    }

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null;
    }

    public static boolean isInternetAvailable(Context context, String msg) {
        if (isInternetAvailable(context)) {
            return YES;
        } else {
            showToast(context, msg);
            return NO;
        }
    }

    public static boolean checkAllEditTexts(EditText... editTexts) {
        for (EditText edit : editTexts) {
            if (edit == null || !(edit.getText().toString().trim().length() > 0)) {
                return NO;
            }
        }
        return YES;
    }

    public static boolean checkAllEditTexts(Context context, String msg, EditText... editTexts) {
        if (checkAllEditTexts(editTexts)) {
            return YES;
        } else {
            showToast(context, msg);
            return NO;
        }
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static String removeBlankSpace(String value) {
        value = value.replace(" ", "");
        return value;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        androidUtils = this;
    }

}
