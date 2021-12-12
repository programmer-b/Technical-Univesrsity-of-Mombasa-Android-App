package com.tum.app;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Notes#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Notes extends Fragment {

    public static boolean isTopicEmpty = false;

    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 41;
    private final int PICK_FILE_REQUEST = 1;

    Uri pdfUri = null;

    final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("pdf_files");

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Notes() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Notes.
     */
    // TODO: Rename and change types and number of parameters
    public static Notes newInstance(String param1, String param2) {
        Notes fragment = new Notes();
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
        return inflater.inflate(R.layout.fragment_notes, container, false);
    }

    private View hiddenLayout;
    private ViewDialog dialog = new ViewDialog();
    
    public void onViewCreated(View view,Bundle savedInstanceState){

        String userGroupName,group;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        userGroupName = sharedPreferences.getString("userGroupName",null).replace(".","");
        group = sharedPreferences.getString("group",null);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Data").child("Courses").child(group).child(group + "_Groups").child(userGroupName).child("Notes");

        view.findViewById(R.id.fabNotes).setOnClickListener(view1 -> slideUpDown());
        view.findViewById(R.id.cancel).setOnClickListener(view1 -> hideLayout());
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
            final EditText Unit = view.findViewById(R.id.unit);
            final EditText Topic = view.findViewById(R.id.topic);
            @Override
            public void onClick(View view) {
                if (!AndroidUtils.nonEmpty(Topic) || !AndroidUtils.nonEmpty(Unit)) AndroidUtils.toastMessage(requireContext(),"Please fill all the fields");
                else if (!AndroidUtils.isInternetAvailable(requireActivity())) dialog.showDialog(requireActivity());
                else if (!AndroidUtils.isOnline(requireContext())) toast("Check your connection.");
                else {dialog.showProgress(requireActivity(),"uploading",false);

                    Date date = new Date();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);

                    String Date = AndroidUtils.getFormattedDate(calendar.getTime());
                    String Unit_e = Unit.getText().toString();
                    String Topic_e = Topic.getText().toString();

                    if (!Topic_e.equals("")) Notes.isTopicEmpty = true;

                    StorageReference filepath = storageReference.child(pdfUri.getLastPathSegment());

                    filepath.putFile(pdfUri).addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());

                        Uri downloadUrl = uriTask.getResult();

                        DatabaseReference newDataRefer = databaseReference.push();

                        newDataRefer.child("Unit").setValue(Unit_e);
                        newDataRefer.child("Topic").setValue(Topic_e);
                        newDataRefer.child("Date").setValue(Date);
                        newDataRefer.child("PdfURI").setValue(downloadUrl.toString());

                        Unit.setText("");
                        Topic.setText("");
                        pdfUri = null;
                        hideLayout();
                    });
                }
            }
        });
    }

    public void slideUpDown(){
        hiddenLayout = requireView().findViewById(R.id.hiddenLayout);
        if(!isPanelShowing()){
            Animation bottomUp = AnimationUtils.loadAnimation(requireContext(), R.anim.bottom_up);

            hiddenLayout.startAnimation(bottomUp);
            hiddenLayout.setVisibility(View.VISIBLE);
            requireView().findViewById(R.id.fabNotes).setVisibility(View.GONE);
        }
    }

    public void hideLayout(){
        hiddenLayout = requireView().findViewById(R.id.hiddenLayout);
        Animation bottomDown = AnimationUtils.loadAnimation(requireContext(), R.anim.bottom_down);

        hiddenLayout.startAnimation(bottomDown);
        hiddenLayout.setVisibility(View.GONE);

        new Handler().postDelayed(()-> requireView().findViewById(R.id.fabNotes).setVisibility(View.VISIBLE),600);
        dialog.hideProgress();
    }

    public static class NotesHolder extends RecyclerView.ViewHolder {
        View mView;

        public NotesHolder(@NonNull View itemView){
            super(itemView);
            mView = itemView;
        }
        public void setUnit(String Unit){
            TextView newsTitle = mView.findViewById(R.id.Unit);
            newsTitle.setText(Unit);
        }

        public void setTopic(String Topic){
            TextView newsTitle = mView.findViewById(R.id.Topic);
            newsTitle.setText(Topic);
        }

        public void setDate(String Date){
            TextView newsDate = mView.findViewById(R.id.date);
            newsDate.setText(Date);
        }

        public void setMemo(String Memo){
            TextView newsDate = mView.findViewById(R.id.memo);
            newsDate.setText(Memo);
        }

    }

    @Override
    public void onStart(){
        super.onStart();
        showNotes();
    }

    private void showNotes() {
        SwipeRefreshLayout swipeRefreshLayout = requireView().findViewById(R.id.swipe);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setOnRefreshListener(this::showNotes);

        String userGroupName,group;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        userGroupName = sharedPreferences.getString("userGroupName",null).replace(".","");
        group = sharedPreferences.getString("group",null);

        Query query = FirebaseDatabase.getInstance().getReference().child("Data").child("Courses").child(group).child(group + "_Groups").child(userGroupName).child("Notes");
        query.keepSynced(true);

        RecyclerView notesList = requireView().findViewById(R.id.notes_view);

        notesList.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseRecyclerOptions<NotesAdapter> options = new FirebaseRecyclerOptions.Builder<NotesAdapter>().setQuery(query, NotesAdapter.class).build();
        FirebaseRecyclerAdapter<NotesAdapter, NotesHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<NotesAdapter, NotesHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull NotesHolder holder, int position, @NonNull NotesAdapter model) {
                holder.setUnit(model.getUnit());
                holder.setTopic(model.getTopic());
                holder.setDate(model.getDate());
                holder.mView.setOnClickListener(view -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("URL", model.getPdfURI());
                    editor.putString("master","notes");
                    editor.apply();

                    startActivity(new Intent(requireContext(), PDF.class));
                });
            }

            @NonNull
            @Override
            public NotesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_layout,parent,false);

                return new NotesHolder(view);
            }
        };
        firebaseRecyclerAdapter.startListening();
        notesList.setAdapter(firebaseRecyclerAdapter);
    }

    private boolean isPanelShowing(){
        return hiddenLayout.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {

            pdfUri = data.getData();

            Context context = getContext();
            assert context != null;

            EditText editText = requireView().findViewById(R.id.topic);
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
    private void toast(String message){
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }
}