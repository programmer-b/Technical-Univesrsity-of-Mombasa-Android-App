package com.tum.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewsView#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewsView extends Fragment {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Data").child("News");
    private SwipeRefreshLayout swipeRefreshLayout;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public NewsView() {
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
    public static NewsView newInstance(String param1, String param2) {
        NewsView fragment = new NewsView();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseReference.keepSynced(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

        }
    }

    private void showNews() {
        swipeRefreshLayout = requireView().findViewById(R.id.swipe);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setOnRefreshListener(this::showNews);

        Query query = FirebaseDatabase.getInstance().getReference().child("Data").child("News");
        query.keepSynced(true);

        RecyclerView newsList = requireView().findViewById(R.id.news_view);

        newsList.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseRecyclerOptions<NewsAdapter> options = new FirebaseRecyclerOptions.Builder<NewsAdapter>().setQuery(query,NewsAdapter.class).build();
        FirebaseRecyclerAdapter<NewsAdapter,NewsHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<NewsAdapter, NewsHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull NewsHolder holder, int position, @NonNull NewsAdapter model) {
                holder.setNews(model.getNews());
                holder.setDate(model.getDate());
                holder.setTitle(model.getTitle());
            }

            @NonNull
            @Override
            public NewsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_layout,parent,false);

                return new NewsHolder(view);
            }
        };
        firebaseRecyclerAdapter.startListening();
        newsList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news_view, container, false);

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.postNews).setOnClickListener(view12 -> NavHostFragment.findNavController(NewsView.this)
                .navigate(R.id.homeToUpload));
        view.findViewById(R.id.back_button).setOnClickListener(view1 -> requireActivity().finish());

    }

    public static class NewsHolder extends RecyclerView.ViewHolder {
        View mView;

        public NewsHolder(@NonNull View itemView){
            super(itemView);
            mView = itemView;
        }
        public void setNews(String News){
            TextView newsTitle = mView.findViewById(R.id.news);
            newsTitle.setText(News);
        }
        public void setDate(String Date){
            TextView newsDate = mView.findViewById(R.id.date);
            newsDate.setText(Date);
        }
        public void setTitle(String Title){
            TextView newsDate = mView.findViewById(R.id.title);
            newsDate.setText(Title);
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        showNews();
        if (!Objects.equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail(), "dancan219w@gmail.com")) requireView().findViewById(R.id.postNews).setVisibility(View.GONE);

    }
}