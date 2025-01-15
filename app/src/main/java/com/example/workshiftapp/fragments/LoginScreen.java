package com.example.workshiftapp.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.workshiftapp.R;
import com.example.workshiftapp.activities.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginScreen extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LoginScreen() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment loginScreen.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginScreen newInstance(String param1, String param2) {
        LoginScreen fragment = new LoginScreen();
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
        View view = inflater.inflate(R.layout.fragment_login_screen, container, false);

        Button button_Login = view.findViewById(R.id.cirLoginButton);
        Button button_Register_Page = view.findViewById(R.id.logRegisterBtn);
        com.google.android.gms.common.SignInButton googleSignInButton = view.findViewById(R.id.signIn); // Added this line


        button_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText logemailEditText = view.findViewById(R.id.login_TextEmail);
                EditText logpassEditText = view.findViewById(R.id.login_TextPassword);
                if (!logemailEditText.getText().toString().isEmpty() && !logpassEditText.getText().toString().isEmpty()) {

                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.login(v);

                } else {
                    // Show a message if the email field is empty
                    Toast.makeText(requireContext(), "Please enter your email and password", Toast.LENGTH_SHORT).show();
                }
            }
        });


        button_Register_Page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_loginScreen_to_registrationScreen);

            }
        });

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Access MainActivity and call startGoogleSignIn
                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity != null) {
                    mainActivity.startGoogleSignIn();  // Call the Google Sign-In method
                } else {
                    Toast.makeText(requireContext(), "Unable to access main activity", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}