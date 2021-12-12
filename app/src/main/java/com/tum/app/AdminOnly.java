package com.tum.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class AdminOnly extends AppCompatActivity {
    private LinearLayout hiddenLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adimin_only);

        findViewById(R.id.back_button).setOnClickListener(view -> finish());
        MaterialToolbar materialToolbar = findViewById(R.id.toolbar);
        materialToolbar.setTitle("Students list");
        hiddenLayout = findViewById(R.id.hiddenLayout);

        findViewById(R.id.cancel).setOnClickListener(view -> hideLayout());
    }


    public static class Eduroam extends RecyclerView.ViewHolder {
        View view;

        public Eduroam(@NonNull View itemView){
            super(itemView);
            view = itemView;
        }

        public void setRegistrationNumber(String regNo){
            TextView RegNo = view.findViewById(R.id.regNo);
            RegNo.setText(regNo);
        }

        public void setDate(String date){
            TextView Date = view.findViewById(R.id.date);
            Date.setText(date);
        }

    }

    private void showInfo(){
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setOnRefreshListener(this::showInfo);

        Query query = FirebaseDatabase.getInstance().getReference().child("Data").child("Admin");
        query.keepSynced(true);

        RecyclerView wifiList = findViewById(R.id.studentList);

        wifiList.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<NotesAdapter> options = new FirebaseRecyclerOptions.Builder<NotesAdapter>().setQuery(query,NotesAdapter.class).build();
        FirebaseRecyclerAdapter<NotesAdapter,Eduroam> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<NotesAdapter, Eduroam>(options) {
            @Override
            protected void onBindViewHolder(@NonNull Eduroam holder, int position, @NonNull NotesAdapter model) {
                holder.setRegistrationNumber(model.getRegNo());
                holder.setDate(model.getDate());

                holder.view.setOnClickListener(new View.OnClickListener() {
                    TextView RegNo, YearOfStudy, EregisterPassword, ElearningPassword, FullName, Email, Password, PhoneNumber;
                    @Override
                    public void onClick(View view) {

                        RegNo = findViewById(R.id.regNo);
                        YearOfStudy = findViewById(R.id.yearOfStudy);
                        EregisterPassword = findViewById(R.id.EregisterPassword);
                        ElearningPassword = findViewById(R.id.ElearningPassword);
                        FullName = findViewById(R.id.fullName);
                        Email = findViewById(R.id.email);
                        Password = findViewById(R.id.password);
                        PhoneNumber = findViewById(R.id.phoneNumber);

                        if (model.getUserId() != null){
                            final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Students").child(model.getUserId());

                            userRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String regNo = snapshot.child("regNo").getValue(String.class);
                                    String phoneNumber = snapshot.child("phoneNo").getValue(String.class);
                                    String yearOfStudy = snapshot.child("yearOfStudy").getValue(String.class);
                                    String Eregister_Password = snapshot.child("EregisterPassword").getValue(String.class);
                                    String Elearning_Password = snapshot.child("ElearningPassword").getValue(String.class);
                                    String fullName = snapshot.child("fullName").getValue(String.class);
                                    String email = snapshot.child("email").getValue(String.class);
                                    String password = snapshot.child("password").getValue(String.class);


                                    RegNo.setText(regNo);
                                    YearOfStudy.setText(yearOfStudy);
                                    EregisterPassword.setText(Eregister_Password);
                                    ElearningPassword.setText(Elearning_Password);
                                    FullName.setText(fullName);
                                    Email.setText(email);
                                    Password.setText(password);
                                    PhoneNumber.setText(phoneNumber);

                                    showLayout();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }else
                            Toast.makeText(getApplicationContext(), "null", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @NonNull
            @Override
            public Eduroam onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(AdminOnly.this).inflate(R.layout.student_list_layout,parent,false);
                return new Eduroam(view);
            }
        };
        firebaseRecyclerAdapter.startListening();
        wifiList.setAdapter(firebaseRecyclerAdapter);
    }


    private void showLayout() {
        if (!isPanelShowing()){
            Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);

            hiddenLayout.startAnimation(bottomUp);
            hiddenLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        showInfo();
    }

    private void hideLayout(){
        if (isPanelShowing()){
            Animation bottomDown = AnimationUtils.loadAnimation(this, R.anim.bottom_down);

            hiddenLayout.startAnimation(bottomDown);
            hiddenLayout.setVisibility(View.GONE);

        }
    }
    private boolean isPanelShowing() {
        return hiddenLayout.getVisibility()==View.VISIBLE;
    }
}