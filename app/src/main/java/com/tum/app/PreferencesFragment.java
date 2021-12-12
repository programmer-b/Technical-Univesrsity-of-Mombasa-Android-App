package com.tum.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PreferencesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreferencesFragment extends Fragment {
    ViewDialog dialog = new ViewDialog();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PreferencesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment preferencesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PreferencesFragment newInstance(String param1, String param2) {
        PreferencesFragment fragment = new PreferencesFragment();
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
        return inflater.inflate(R.layout.fragment_preferences, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState){
        SwitchMaterial notification = (SwitchMaterial) view.findViewById(R.id.notification);
        SwitchMaterial sync = (SwitchMaterial) view.findViewById(R.id.sync);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        boolean canNotify = sharedPreferences.getBoolean("canNotify",false);
        notification.setChecked(canNotify);
        boolean Sync = sharedPreferences.getBoolean("sync",false);
        sync.setChecked(Sync);

        notification.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            editor.putBoolean("canNotify", isChecked);
            editor.apply();
        });

        sync.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            editor.putBoolean("sync", isChecked);
            editor.apply();
        });

        view.findViewById(R.id.passwords).setOnClickListener(view1 -> startActivity(new Intent(requireContext(),Preferences_Activities.class)));
        view.findViewById(R.id.tum).setOnClickListener(view1 -> {
            if (AndroidUtils.isInternetAvailable(requireContext())) {
                Intent intent = new Intent(requireContext(),Browser.class);
                editor.putString("title","Home");
                editor.putString("link","https://www.tum.ac.ke/");
                editor.apply();
                startActivity(intent);
            } else dialog.showDialog(requireActivity());
        });
        view.findViewById(R.id.about).setOnClickListener(view1 -> dialog.showAbout(requireActivity()));
        view.findViewById(R.id.share).setOnClickListener(view1 -> {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT,"https://www.tum.ac.ke/");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT,"TECHNICAL UNIVERSITY OF MOMBASA.");
            startActivity(Intent.createChooser(sharingIntent,"Share via"));
        });
        view.findViewById(R.id.privacy).setOnClickListener(view1 -> dialog.showPrivacy(requireActivity()));

        view.findViewById(R.id.admin).setOnClickListener(view1 -> startActivity(new Intent(requireContext(),AdminOnly.class)));

    }

    public void onStart(){
        super.onStart();
        if (!Objects.equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail(), "dancan219w@gmail.com")) requireView().findViewById(R.id.admin).setVisibility(View.GONE);
    }
}