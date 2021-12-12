package com.tum.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Memos#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Memos extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Memos() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Memos.
     */
    // TODO: Rename and change types and number of parameters
    public static Memos newInstance(String param1, String param2) {
        Memos fragment = new Memos();
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
        return inflater.inflate(R.layout.fragment_memos, container, false);
    }
    private ViewDialog dialog = new ViewDialog();

    public void onViewCreated(View view,Bundle savedInstanceState){
        String userGroupName,group;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        userGroupName = sharedPreferences.getString("userGroupName",null).replace(".","");
        group = sharedPreferences.getString("group",null);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Data").child("Courses").child(group).child(group + "_Groups").child(userGroupName).child("Memos");

        showNotes();

        view.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            final EditText Memo = view.findViewById(R.id.memo);
            @Override
            public void onClick(View view) {
                if (!AndroidUtils.nonEmpty(Memo) ) AndroidUtils.toastMessage(requireContext(),"Please write a memo.");
                else if (!AndroidUtils.isInternetAvailable(requireActivity())) dialog.showDialog(requireActivity());
                else if (!AndroidUtils.isOnline(requireContext())) toast("Check your connection.");
                else {dialog.showProgress(requireActivity(),"Uploading memo ...",false);

                    Date date = new Date();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);

                    String memo = Memo.getText().toString();
                    String Date = AndroidUtils.getFormattedDate(calendar.getTime());

                    DatabaseReference newDataRefer = databaseReference.push();
                    newDataRefer.child("Memo").setValue(memo);
                    newDataRefer.child("Date").setValue(Date);

                    Memo.setText("");
                    toast("Memo has been sent.");

                    dialog.hideProgress();
                }

            }
        });
    }


    @Override
    public void onStart(){
        super.onStart();

    }

    private void showNotes() {
        SwipeRefreshLayout swipeRefreshLayout = requireView().findViewById(R.id.swipe);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setOnRefreshListener(this::showNotes);

        String userGroupName,group;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        userGroupName = sharedPreferences.getString("userGroupName",null).replace(".","");
        group = sharedPreferences.getString("group",null);

        Query query = FirebaseDatabase.getInstance().getReference().child("Data").child("Courses").child(group).child(group + "_Groups").child(userGroupName).child("Memos");
        query.keepSynced(true);

        RecyclerView notesList = requireView().findViewById(R.id.memos_view);

        notesList.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseRecyclerOptions<NotesAdapter> options = new FirebaseRecyclerOptions.Builder<NotesAdapter>().setQuery(query, NotesAdapter.class).build();
        FirebaseRecyclerAdapter<NotesAdapter, Notes.NotesHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<NotesAdapter, Notes.NotesHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull Notes.NotesHolder holder, int position, @NonNull NotesAdapter model) {
                holder.setMemo(model.getMemo());
                holder.setDate(model.getDate());
            }

            @NonNull
            @Override
            public Notes.NotesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.memo_layout,parent,false);

                return new Notes.NotesHolder(view);
            }
        };
        firebaseRecyclerAdapter.startListening();
        notesList.setAdapter(firebaseRecyclerAdapter);
    }

    private String userId(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        return user.getUid();
    }

    private void toast(String message){
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }
}