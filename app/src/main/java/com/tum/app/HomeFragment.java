package com.tum.app;

import static android.widget.Toast.makeText;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    ViewDialog dialog = new ViewDialog();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private static final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    public static final String userId;

    static {
        assert user != null;
        userId = user.getUid();
    }

    private static final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Students").child(userId);

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private final ViewDialog viewDialog = new ViewDialog();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // TODO: Rename and change types of parameters
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    @Override
    public  void onViewCreated(View view, Bundle savedInstanceState){

        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);

        if (hours<12) time(view,"Morning,");
        else if (hours<=15) time(view,"Afternoon,");
        else if (hours<=20) time(view,"Evening,");
        else time(view,"Night,");

        RelativeLayout trendingLayout = view.findViewById(R.id.trendingLayout);

        int []backgroundArray = {R.drawable.trending_bg_one, R.drawable.trending_bg_two, R.drawable.trending_bg_four, R.drawable.trending_bg_five, R.drawable.trending_bg_three};
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            int i = 0;
            @Override
            public void run() {
                trendingLayout.setBackgroundResource(backgroundArray[i]);
                i++;
                if (i>backgroundArray.length-1) i=0;
                handler.postDelayed(this,7050);
            }
        };
        handler.postDelayed(runnable, 5000);

        userRef.keepSynced(true);

        Context context = getContext();
        assert context != null;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String regNo = snapshot.child("regNo").getValue(String.class);
                String yearOfStudy = snapshot.child("yearOfStudy").getValue(String.class);
                String EregisterPassword = snapshot.child("EregisterPassword").getValue(String.class);
                String ElearningPassword = snapshot.child("ElearningPassword").getValue(String.class);
                String profileImage = snapshot.child("profileImage").getValue(String.class);
                String fullName = snapshot.child("fullName").getValue(String.class);

                TextView textView = (TextView) view.findViewById(R.id.textUserName);
                TextView textView1 = (TextView) view.findViewById(R.id.userGroup);

                CircleImageView circleImageView = (CircleImageView) view.findViewById(R.id.setup_profile_image);

                textView.setText(AndroidUtils.firstName(fullName));
                textView1.setText(AndroidUtils.userGroup(regNo));

                Picasso.get().load(profileImage).placeholder(R.drawable.default_profile_image).into(circleImageView);

                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString("EregisterPassword", EregisterPassword);
                editor.putString("ElearningPassword", ElearningPassword);
                editor.putString("regNo", regNo);
                editor.putString("userGroup", AndroidUtils.userGroup(regNo).replace("\n","  "));

                int end = regNo.indexOf("/");
                String group = null;
                if (end != 1) group = regNo.substring(0,end);

                editor.putString("group",group);
                editor.putString("userGroupName", group+yearOfStudy);

                editor.apply();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        view.findViewById(R.id.imageLogout).setOnClickListener(view1 -> dialog.Logout(requireActivity(),requireContext()));

        view.findViewById(R.id.search).setOnClickListener(view1 -> {
            view.findViewById(R.id.progressLayout).setVisibility(View.VISIBLE);
            view.findViewById(R.id.searchEdit).setEnabled(false);

            new Handler().postDelayed(()->{
                view.findViewById(R.id.progressLayout).setVisibility(View.GONE);
                toast("No match found !");
                view.findViewById(R.id.searchEdit).setEnabled(true);
            }, 2700);
        });

        SharedPreferences.Editor editor = sharedPreferences.edit();
        view.findViewById(R.id.layout_Eregister).setOnClickListener(view1 -> {
            if (AndroidUtils.isOnline(context)) {
                Intent intent = new Intent(requireContext(),Browser.class);
                editor.putString("title","Eregister");
                editor.putString("link","https://eregistrar.tum.ac.ke/Campuscura/?TenantID=tum#login;TenantID=tum;Apply=false");
                editor.apply();
                requireActivity().overridePendingTransition(0,0);
                startActivity(intent);
            }
            else showNetError();
        });

        view.findViewById(R.id.layout_Elearning).setOnClickListener(view1 -> {
            if (AndroidUtils.isOnline(context)) {
                Intent intent = new Intent(requireContext(),Browser.class);
                editor.putString("title","Elearning");
                editor.putString("link","https://elearning.tum.ac.ke/login/index.php");
                editor.apply();
                requireActivity().overridePendingTransition(0,0);
                startActivity(intent);
            }
            else showNetError();
        });

        view.findViewById(R.id.layout_News).setOnClickListener(view1 -> startActivity(new Intent(context,News.class)));
        view.findViewById(R.id.layout_Downloads).setOnClickListener(view1 -> startActivity(new Intent(context,Downloads.class)));
        view.findViewById(R.id.layout_Library).setOnClickListener(view1 -> startActivity(new Intent(context,Library.class)));
        view.findViewById(R.id.layout_Class).setOnClickListener(view1 -> startActivity(new Intent(context,UserGroup.class)));
        view.findViewById(R.id.layout_Eduroam_access).setOnClickListener(view1 -> startActivity(new Intent(context,EduroamAccess.class)));
        view.findViewById(R.id.layout_Map).setOnClickListener(view1 -> startActivity(new Intent(context,Maps.class)));
    }

    private void showNetError() {
        Context context = getContext();
        assert context != null;

        if (!AndroidUtils.isInternetAvailable(context)) viewDialog.showDialog(getActivity());
        else toast("Please connect to the internet !");
    }
    private void toast(String message){
        Toast toast = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }

    private void time(View view,String Time){
        TextView textView = view.findViewById(R.id.textTime);
        textView.setText(Time);
    }
}