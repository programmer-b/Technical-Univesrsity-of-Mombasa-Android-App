package com.tum.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewsView#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DownloadsList extends Fragment {
    ViewDialog dialog = new ViewDialog();


    private SwipeRefreshLayout swipeRefreshLayout;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DownloadsList() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NewsView.
     */
    // TODO: Rename and change types and number of parameters
    public static DownloadsList newInstance(String param1, String param2) {
        DownloadsList fragment = new DownloadsList();
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

    private void showDownloads() {
        swipeRefreshLayout = requireView().findViewById(R.id.swipe);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setOnRefreshListener(this::showDownloads);

        Query query = FirebaseDatabase.getInstance().getReference().child("Data").child("Downloads");
        query.keepSynced(true);

        RecyclerView downloadsList = requireView().findViewById(R.id.downloads_list);

        downloadsList.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseRecyclerOptions<DownloadsAdapter> options = new FirebaseRecyclerOptions.Builder<DownloadsAdapter>().setQuery(query,DownloadsAdapter.class).build();
        FirebaseRecyclerAdapter<DownloadsAdapter,DownloadsHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<DownloadsAdapter, DownloadsHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull DownloadsHolder holder, int position, @NonNull DownloadsAdapter model) {
                holder.setPdfTitle(model.getPdfTitle());
                holder.setDate(model.getDate());

                Context context = getContext();
                assert context != null;


                holder.mView.setOnClickListener(new View.OnClickListener() {
                    final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

                    @Override
                    public void onClick(View view) {

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("URL", model.getPdfURI());
                        editor.putString("master","downloads");
                        editor.apply();

                        NavHostFragment.findNavController(DownloadsList.this).navigate(R.id.homeToContent);
                    }
                });
            }

            @NonNull
            @Override
            public DownloadsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_layout,parent,false);

                return new DownloadsHolder(view);
            }
        };
        firebaseRecyclerAdapter.startListening();
        downloadsList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_downloads_list, container, false);

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.uploadPdf).setOnClickListener(view12 -> NavHostFragment.findNavController(DownloadsList.this)
                .navigate(R.id.homeToUpload));
        view.findViewById(R.id.back_button).setOnClickListener(view1 -> requireActivity().finish());

    }

    public static class DownloadsHolder extends RecyclerView.ViewHolder {
        View mView;

        public DownloadsHolder (@NonNull View itemView){
            super(itemView);
            mView = itemView;
        }
        public void setPdfTitle(String Downloads){
            TextView pdfTitle = mView.findViewById(R.id.title);
            mView.findViewById(R.id.news).setVisibility(View.GONE);
            pdfTitle.setText(Downloads);
        }
        public void setDate(String Date){
            TextView newsDate = mView.findViewById(R.id.date);
            newsDate.setText(Date);
        }

    }

    @Override
    public void onStart(){
        super.onStart();
        showDownloads();
        if (!Objects.equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail(), "dancan219w@gmail.com")) requireView().findViewById(R.id.uploadPdf).setVisibility(View.GONE);

    }

}