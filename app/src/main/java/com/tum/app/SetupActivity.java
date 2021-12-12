package com.tum.app;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
    public static boolean isProfilePicAvailable = false;
    public static boolean isPhotoPicked = false;
    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 41;
    private final int PICK_IMAGE_REQUEST = 1;
    CircleImageView circleImageView;

    final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Students").child(userId());
    final DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference().child("Data").child("Admin");


    final ViewDialog dialog = new ViewDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
        String password = sharedPreferences.getString("password", "");


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("fullName"))
                {
                    if (snapshot.hasChild("regNo")){
                        if (snapshot.hasChild("phoneNo")){
                            openPortalConfig();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        circleImageView = findViewById(R.id.setup_profile_image);

        circleImageView.setOnClickListener(view1 -> {
            if (checkPermissionForReadExternalStorage()) choosePhoto();
            else {
                try {
                    requestPermissionForReadExternalStorage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.save_account_button).setOnClickListener(new View.OnClickListener() {
            final EditText FullName = findViewById(R.id.fullName);
            final EditText RegNo = findViewById(R.id.registrationNumber);
            final EditText PhoneNo = findViewById(R.id.phoneNumber);

            @Override
            public void onClick(View view) {
                if (!AndroidUtils.isInternetAvailable(getApplicationContext())) dialog.showDialog(SetupActivity.this);
                else if (!AndroidUtils.isOnline(getApplicationContext())) AndroidUtils.toastMessage(getApplicationContext(), "No connection available.");
                else if (!AndroidUtils.nonEmpty(FullName) || !AndroidUtils.nonEmpty(RegNo) || !AndroidUtils.nonEmpty(PhoneNo))
                    AndroidUtils.toastMessage(getApplicationContext(), "Please fill all the fields.");
                else if (AndroidUtils.wordCount(FullName)<2) AndroidUtils.toastMessage(getApplicationContext(),"Please enter name in full.");
                else if (!AndroidUtils.validateRegistrationNumber(RegNo.getText().toString())) AndroidUtils.toastMessage(getApplicationContext(),"Incorrect registration number.");
                else if (!AndroidUtils.mobileNumberValidation(PhoneNo)) AndroidUtils.toastMessage(getApplicationContext(),"Invalid phone number.");
                else {
                    findViewById(R.id.progressLayout).setVisibility(View.VISIBLE);
                    String fullName = FullName.getText().toString();
                    String regNo = RegNo.getText().toString();
                    String phoneNo = PhoneNo.getText().toString();

                    findViewById(R.id.mainLayout).setEnabled(false);
                    FullName.setEnabled(false);
                    RegNo.setEnabled(false);
                    PhoneNo.setEnabled(false);
                    findViewById(R.id.save_account_button).setEnabled(false);


                    final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("fullName",fullName);
                    editor.putString("regNo",regNo);
                    editor.putString("phoneNo",phoneNo);
                    editor.putBoolean("setup",true);

                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Map<String,Object> postValues = new HashMap<String,Object>();

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) postValues.put(snapshot.getKey(),snapshot.getValue());

                            postValues.put("fullName",fullName);
                            postValues.put("regNo", regNo);
                            postValues.put("phoneNo",phoneNo);
                            postValues.put("email",email);
                            postValues.put("password",password);
                            postValues.put("EregisterPassword",regNo.toUpperCase(Locale.ROOT));
                            postValues.put("ElearningPassword",AndroidUtils.ElearningPassword(regNo));

                            databaseReference.updateChildren(postValues);

                            editor.putBoolean("isAccountSetup",true);
                            editor.putBoolean("canNotify",true);
                            editor.putBoolean("portalConfig",true);
                            editor.apply();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    Date date = new Date();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);

                    String Date = AndroidUtils.getFormattedDate(calendar.getTime());

                    DatabaseReference adminRefer = adminRef.push();

                    adminRefer.child("UserId").setValue(userId());
                    adminRefer.child("regNo").setValue(regNo);
                    adminRefer.child("Date").setValue(Date);

                }

            }
        });
    }

    private void goHome() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void choosePhoto() {
        SetupActivity.isPhotoPicked = true;

        Intent intent = new Intent();
        intent.setType("image/*")
                .setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "TUM profile picture"), PICK_IMAGE_REQUEST);
    }

    public boolean checkPermissionForReadExternalStorage() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = getApplicationContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public void requestPermissionForReadExternalStorage() throws Exception {
        try {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_STORAGE_PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_STORAGE_PERMISSION_REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                choosePhoto();
            else
                AndroidUtils.toastMessage(getApplicationContext(), "Please give permission to read external storage");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null){
            final Uri imageUri = data.getData();
            Picasso.get().load(imageUri).placeholder(R.drawable.add_user).into(circleImageView);

            final InputStream imageStream;
            try {
                imageStream = this.getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                String encodedImage = AndroidUtils.encodeImage(selectedImage);

                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("profileImage",encodedImage);
                editor.apply();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("profileImages");

            StorageReference FilePath = imageRef.child(userId()+".jpg");
            FilePath.putFile(imageUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    Task<Uri> result = Objects.requireNonNull(Objects.requireNonNull(task.getResult().getMetadata()).getReference()).getDownloadUrl();
                    result.addOnSuccessListener(uri -> {
                        final String downloadUri = uri.toString();
                        databaseReference.child("profileImage").setValue(downloadUri).addOnSuccessListener(unused -> {
                            SetupActivity.isProfilePicAvailable = true;
                        });
                    });
                }
            });
        }
    }

    private void openPortalConfig() {
        new Handler().postDelayed(this::goHome,6000);
    }

    private String userId(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        return user.getUid();
    }
}