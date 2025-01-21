package com.example.workshiftapp.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.workshiftapp.R;
import com.example.workshiftapp.activities.MainActivity;
import com.example.workshiftapp.classes.EmailValidator;

public class RegistrationScreen extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_registration_screen, container, false);

        // Input fields
        EditText regFullNameEditText = view.findViewById(R.id.reg_FullName);
        EditText regEmailEditText = view.findViewById(R.id.reg_TextEmail);
        EditText regPassEditText = view.findViewById(R.id.reg_TextPassword);
        EditText regRePassEditText = view.findViewById(R.id.reg_ReEnterPassword);
        EditText regCalendarIDText = view.findViewById(R.id.reg_CalendarID);

        // Error labels
        TextView errorFullName = view.findViewById(R.id.errorFullName);
        TextView errorEmail = view.findViewById(R.id.errorEmail);
        TextView errorPassword = view.findViewById(R.id.errorPassword);
        TextView errorRePassword = view.findViewById(R.id.errorRePassword);
        TextView errorCalendarID = view.findViewById(R.id.errorCalendarID);

        Button button_Register = view.findViewById(R.id.cirRegButton);
        Button BackToLogin = view.findViewById(R.id.BackToLoginBtn);

        button_Register.setOnClickListener(v -> {
            // Retrieve input values
            String regFullName = regFullNameEditText.getText().toString().trim();
            String regEmail = regEmailEditText.getText().toString().trim();
            String regPass = regPassEditText.getText().toString();
            String regRePass = regRePassEditText.getText().toString();
            String regCalendarID = regCalendarIDText.getText().toString().trim();

            // Reset error messages
            errorFullName.setVisibility(View.GONE);
            errorEmail.setVisibility(View.GONE);
            errorPassword.setVisibility(View.GONE);
            errorRePassword.setVisibility(View.GONE);
            errorCalendarID.setVisibility(View.GONE);

            boolean isValid = true;

            // Validation checks
            if (regFullName.isEmpty()) {
                errorFullName.setText("Full name is required");
                errorFullName.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if (regEmail.isEmpty() || !EmailValidator.isValidEmail(regEmail)) {
                errorEmail.setText(regEmail.isEmpty() ? "Email is required" : "Invalid email format");
                errorEmail.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if (regPass.isEmpty() || regPass.length() < 6) {
                errorPassword.setText(regPass.isEmpty() ? "Password is required" : "Password must be at least 6 characters");
                errorPassword.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if (regRePass.isEmpty() || !regPass.equals(regRePass)) {
                errorRePassword.setText(regRePass.isEmpty() ? "Confirm your password" : "Passwords do not match");
                errorRePassword.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if (regCalendarID.isEmpty() || !(regCalendarID.matches("\\d{6}"))) {
                errorCalendarID.setText(regCalendarID.isEmpty() ? "Calendar ID is required" : "Calendar ID must be exactly 6 digits" );
                errorCalendarID.setVisibility(View.VISIBLE);
                isValid = false;
            }

            if (isValid) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.register();
                if (!mainActivity.getuserExists()) {
                    Navigation.findNavController(view).navigate(R.id.action_registrationScreen_to_loginScreen);
                }
            }
        });

        BackToLogin.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_registrationScreen_to_loginScreen)
        );

        return view;
    }
}
