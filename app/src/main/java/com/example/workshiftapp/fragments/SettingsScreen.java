package com.example.workshiftapp.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.workshiftapp.R;
import com.example.workshiftapp.activities.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsScreen extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private MainActivity mainActivity;
    GoogleSignInClient googleSignInClient;
    GoogleAccountCredential googleAccountCredential;


    public SettingsScreen() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsScreen.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsScreen newInstance(String param1, String param2) {
        SettingsScreen fragment = new SettingsScreen();
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
        mainActivity= (MainActivity) getActivity();

        View view = inflater.inflate(R.layout.fragment_settings_screen, container, false);
        Button googleSignOutButton = view.findViewById(R.id.logOutBtn);
        if (mainActivity != null)
        {
            googleAccountCredential = mainActivity.getGoogleAccountCredential();
            googleSignInClient = mainActivity.getGetGoogleSignInClient();
        }


        // Handle Google sign-out
        googleSignOutButton.setOnClickListener(v -> {
            if (googleAccountCredential == null)
            {
                Navigation.findNavController(view).navigate(R.id.action_generalAppScreen_to_loginScreen);
                mainActivity.setEmailUser(null);
                mainActivity.setUserPhoto(null);
                mainActivity.setFullName(null);
                mainActivity.setGoogleAccountCredential(null);
                mainActivity.setGoogleSignInClient(null);
                Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();
            }
            else
            {
                googleSignInClient.signOut().addOnSuccessListener(unused -> {
                    mAuth.signOut();
                    googleAccountCredential = null;
                    mainActivity.setEmailUser(null);
                    mainActivity.setUserPhoto(null);
                    mainActivity.setFullName(null);
                    mainActivity.setGoogleAccountCredential(null);
                    mainActivity.setGoogleSignInClient(null);

                    Toast.makeText(requireContext(), "Signed out from Google account", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).navigate(R.id.action_generalAppScreen_to_loginScreen);

                });
            }

        });
        return view;
    }
}