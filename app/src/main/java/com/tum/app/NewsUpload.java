package com.tum.app;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewsUpload#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewsUpload extends Fragment {
    ViewDialog viewDialog = new ViewDialog();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public NewsUpload() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NewsUpload.
     */
    // TODO: Rename and change types and number of parameters
    public static NewsUpload newInstance(String param1, String param2) {
        NewsUpload fragment = new NewsUpload();
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
        return inflater.inflate(R.layout.fragment_news_upload, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setOnKeyListener((view1, i, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) NavHostFragment.findNavController(NewsUpload.this)
                    .navigate(R.id.uploadToHome);
            return false;
        });

        view.findViewById(R.id.back_button).setOnClickListener(view1 -> NavHostFragment.findNavController(NewsUpload.this)
                .navigate(R.id.uploadToHome));

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Data").child("News");

        view.findViewById(R.id.upload_button).setOnClickListener(new View.OnClickListener() {
            final EditText News = view.findViewById(R.id.textNews);
            final EditText Title = view.findViewById(R.id.title);

            @Override
            public void onClick(View view) {
                Date date = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                String Date = AndroidUtils.getFormattedDate(calendar.getTime());
                String news = News.getText().toString();
                String title = Title.getText().toString();


                Context context = getContext();
                assert context != null;

                if (!AndroidUtils.nonEmpty(News)) AndroidUtils.toastMessage(context,"Please write news to submit");
                else if (!AndroidUtils.isInternetConnected()) showNetError();
                else {
                    viewDialog.showProgress(requireActivity(),"uploading news",false);
                    ConstraintLayout constraintLayout = requireView().findViewById(R.id.progressLayout);
                    constraintLayout.setVisibility(View.VISIBLE);

                    DatabaseReference newsRefer = databaseReference.push();
                    newsRefer.child("Date").setValue(Date);
                    newsRefer.child("News").setValue(news);
                    newsRefer.child("Title").setValue(title);

                    NavHostFragment.findNavController(NewsUpload.this).navigate(R.id.uploadToHome);
                }
            }
        });
    }

    private void showNetError() {
        Context context = getContext();
        assert context != null;

        if (!AndroidUtils.isInternetAvailable(context)) viewDialog.showDialog(getActivity());
        else viewDialog.showNoConnection(getActivity());
    }
    @Override
    public void onStop(){
        super.onStop();
        viewDialog.hideProgress();
    }
}