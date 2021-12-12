package com.tum.app;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link upload_doc#newInstance} factory method to
 * create an instance of this fragment.
 */
public class upload_doc extends Fragment {
    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 41;
    private final int PICK_FILE_REQUEST = 1;

    Uri pdfUri = null;
    final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("pdf_files");
    final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Data").child("Downloads");

    ViewDialog viewDialog = new ViewDialog();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public upload_doc() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment upload_doc.
     */
    // TODO: Rename and change types and number of parameters
    public static upload_doc newInstance(String param1, String param2) {
        upload_doc fragment = new upload_doc();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_upload_doc, container, false);
    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState){
        view.findViewById(R.id.chooseFile).setOnClickListener(view1 -> {
            if (checkPermissionForReadExternalStorage()) choosePdf();

            else {
                try {
                    requestPermissionForReadExternalStorage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        view.findViewById(R.id.upload_button).setOnClickListener(new View.OnClickListener() {
            final EditText editText = view.findViewById(R.id.textDocName);
            @Override
            public void onClick(View view) {
                Date date = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                String Date = AndroidUtils.getFormattedDate(calendar.getTime());
                String PdfTitle = editText.getText().toString();

                if (!AndroidUtils.nonEmpty(editText)) AndroidUtils.toastMessage(getActivity(),"Please input the document title.");
                else if (pdfUri == null) AndroidUtils.toastMessage(getActivity(),"Please choose a pdf file to upload");
                else if (!AndroidUtils.isInternetAvailable(requireActivity())) showNetError();
                else {
                    viewDialog.showProgress(getActivity(),"uploading document",false);

                    StorageReference filepath = storageReference.child(pdfUri.getLastPathSegment());

                    filepath.putFile(pdfUri).addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());

                        Uri downloadUrl = uriTask.getResult();

                        DatabaseReference newDataRefer = databaseReference.push();

                        newDataRefer.child("PdfTitle").setValue(PdfTitle);
                        newDataRefer.child("Date").setValue(Date);
                        newDataRefer.child("PdfURI").setValue(downloadUrl.toString());

                        NavHostFragment.findNavController(upload_doc.this).navigate(R.id.uploadToHome);
                    });
                }

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {

            pdfUri = data.getData();

            Context context = getContext();
            assert context != null;

            EditText editText = requireView().findViewById(R.id.textDocName);
            editText.setText(AndroidUtils.getNameFromURI(pdfUri,context));
        }
    }
    
    public boolean checkPermissionForReadExternalStorage(){
        Context context = getContext();
        assert context != null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            int result = context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public void requestPermissionForReadExternalStorage() throws Exception{
        try {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_STORAGE_PERMISSION_REQUEST_CODE);
        } catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (requestCode == READ_STORAGE_PERMISSION_REQUEST_CODE){
            Context context = getContext();
            assert context != null;

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) choosePdf();
            else AndroidUtils.toastMessage(context,"Please give permission to read external storage");
        }
    }

    private void choosePdf(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    private void showNetError() {
        Context context = getContext();
        assert context != null;

        if (!AndroidUtils.isInternetAvailable(context)) viewDialog.showDialog(getActivity());
        else viewDialog.showNoConnection(getActivity());
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
    }
    @Override
    public void onStop(){
        super.onStop();
        viewDialog.hideProgress();
    }
}