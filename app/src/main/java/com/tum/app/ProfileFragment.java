package com.tum.app;

import static android.content.ContentValues.TAG;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    private static final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Students").child(userId());
    ViewDialog dialog = new ViewDialog();

    public static String faculty;
    public static String yearOfStudy;
    public static String dean;
    public static String department;
    public static String headOfDepartment;
    public static String group;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        view.findViewById(R.id.Faculty).setOnClickListener(view1 -> dialog.updateProfile(requireContext(),requireActivity(), "Faculty/School.",ProfileFragment.group,"faculty"));
        view.findViewById(R.id.year).setOnClickListener(view1 -> dialog.updateProfile(requireActivity(),requireContext()));
        view.findViewById(R.id.Dean).setOnClickListener(view1 -> dialog.updateProfile(requireContext(),requireActivity(), "Dean of school.",ProfileFragment.group,"dean"));
        view.findViewById(R.id.Department).setOnClickListener(view1 -> dialog.updateProfile(requireContext(),requireActivity(), "Department.",ProfileFragment.group,"department"));
        view.findViewById(R.id.Head).setOnClickListener(view1 -> dialog.updateProfile(requireContext(),requireActivity(), "Head of department.",ProfileFragment.group,"headOfDepartment"));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        ProfileFragment.group = sharedPreferences.getString("group", null);

        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference().child("Data").child("Courses").child(group).child(group + "_Info");

        FirebaseAuth auth = FirebaseAuth.getInstance();

        TextView fullName = view.findViewById(R.id.textFullName);
        TextView registrationNumber = view.findViewById(R.id.regNo);
        TextView phoneNumber = view.findViewById(R.id.phoneNumber);
        CircleImageView profileImage = view.findViewById(R.id.profileImage);

        TextView email = view.findViewById(R.id.email);

        TextView yearOfStudy = view.findViewById(R.id.yearOfStudy);
        TextView userGroup = view.findViewById(R.id.userGroup);
        TextView faculty = view.findViewById(R.id.faculty);
        TextView department = view.findViewById(R.id.department);
        TextView dean = view.findViewById(R.id.dean);
        TextView headOfDepartment = view.findViewById(R.id.headOfDepartment);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String regNo = snapshot.child("regNo").getValue(String.class);
                assert regNo != null;
                ProfileFragment.yearOfStudy = snapshot.child("yearOfStudy").getValue(String.class);

                fullName.setText(snapshot.child("fullName").getValue(String.class));
                registrationNumber.setText(regNo);
                userGroup.setText(AndroidUtils.userGroup(regNo).replace("\n","  "));

                phoneNumber.setText(snapshot.child("phoneNo").getValue(String.class));
                Picasso.get().load(snapshot.child("profileImage").getValue(String.class)).placeholder(R.drawable.default_profile_image).into(profileImage);
                yearOfStudy.setText(ProfileFragment.yearOfStudy);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "Database error");
            }
        });

        userRef.keepSynced(true);
        email.setText(Objects.requireNonNull(auth.getCurrentUser()).getEmail());

        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ProfileFragment.faculty = snapshot.child("faculty").getValue(String.class);
                ProfileFragment.dean = snapshot.child("dean").getValue(String.class);
                ProfileFragment.department = snapshot.child("department").getValue(String.class);
                ProfileFragment.headOfDepartment = snapshot.child("headOfDepartment").getValue(String.class);

                faculty.setText(ProfileFragment.faculty);
                dean.setText(ProfileFragment.dean);
                department.setText(ProfileFragment.department);
                headOfDepartment.setText(ProfileFragment.headOfDepartment);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "Database error");
            }
        });

        groupRef.keepSynced(true);

    }
    private static String userId(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        return user.getUid();
    }

    @Override
    public void onStart(){
        super.onStart();
    }
}