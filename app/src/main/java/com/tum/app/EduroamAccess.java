package com.tum.app;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


public class EduroamAccess extends AppCompatActivity {
    private final ViewDialog dialog = new ViewDialog();
    private LinearLayout hiddenLayout;
    private FloatingActionButton fab;

    final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Data").child("Eduroam");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eduroam_access);
        hiddenLayout = findViewById(R.id.hiddenLayout);

        findViewById(R.id.cancel).setOnClickListener(view -> hideLayout());
        fab = findViewById(R.id.fabWifi);
        fab.setOnClickListener(view -> showLayout());

        findViewById(R.id.back_button).setOnClickListener(view -> finish());
        findViewById(R.id.upload_button).setOnClickListener(new View.OnClickListener() {

            final EditText Network = findViewById(R.id.network);
            final EditText Password = findViewById(R.id.password);
            final EditText Location = findViewById(R.id.location);
            @Override
            public void onClick(View view) {
                if (!AndroidUtils.nonEmpty(Network) && !AndroidUtils.nonEmpty(Password) && !AndroidUtils.nonEmpty(Location)) toast("Please fill in all the fields");
                else {
                    dialog.showProgress(EduroamAccess.this,"uploading ...",true);
                    String network = Network.getText().toString();
                    String password = Password.getText().toString();
                    String location = Location.getText().toString();

                    DatabaseReference newDataRefer = databaseReference.push();
                    newDataRefer.child("Network").setValue(network);
                    newDataRefer.child("Password").setValue(password);
                    newDataRefer.child("Location").setValue(location);

                    Network.setText(null);
                    Password.setText(null);
                    Location.setText(null);

                    hideLayout();
                }
            }
        });

    }


    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    public static class Eduroam extends RecyclerView.ViewHolder {
        View view;

        public Eduroam(@NonNull View itemView){
            super(itemView);
            view = itemView;
        }

        public void setNetwork(String Network){
            TextView network = view.findViewById(R.id.network);
            network.setText(Network);
        }

        public void setPassword(String Password){
            TextView password = view.findViewById(R.id.password);
            password.setText(Password);
        }

        public void setLocation(String Location){
            TextView location = view.findViewById(R.id.location);
            location.setText(Location);
        }
    }

    private void showNetwork(){
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setOnRefreshListener(this::showNetwork);

        Query query = FirebaseDatabase.getInstance().getReference().child("Data").child("Eduroam");
        query.keepSynced(true);

        RecyclerView wifiList = findViewById(R.id.wifi_list);

        wifiList.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions<NotesAdapter> options = new FirebaseRecyclerOptions.Builder<NotesAdapter>().setQuery(query, NotesAdapter.class).build();
        FirebaseRecyclerAdapter<NotesAdapter,Eduroam> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<NotesAdapter, Eduroam>(options) {
            @Override
            protected void onBindViewHolder(@NonNull Eduroam holder, int position, @NonNull NotesAdapter model) {
                 holder.setNetwork(model.getNetwork());
                 holder.setPassword(model.getPassword());
                 holder.setLocation(model.getLocation());
            }

            @NonNull
            @Override
            public Eduroam onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(EduroamAccess.this).inflate(R.layout.eduroam_layout,parent,false);
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

            fab.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        showNetwork();
    }

    private void hideLayout(){
        if (isPanelShowing()){
            Animation bottomDown = AnimationUtils.loadAnimation(this, R.anim.bottom_down);

            hiddenLayout.startAnimation(bottomDown);
            hiddenLayout.setVisibility(View.GONE);

            new Handler().postDelayed(()-> fab.setVisibility(View.VISIBLE),600);
            dialog.hideProgress();
        }
    }
    private boolean isPanelShowing() {
        return hiddenLayout.getVisibility()==View.VISIBLE;
    }
}