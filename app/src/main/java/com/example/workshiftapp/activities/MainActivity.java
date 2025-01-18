package com.example.workshiftapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.Navigation;

import com.example.workshiftapp.R;
import com.example.workshiftapp.fragments.OrganizerScreen;
import com.example.workshiftapp.models.CalendarShift;
import com.example.workshiftapp.models.Worker;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.calendar.Calendar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;


import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    public  GoogleSignInClient googleSignInClient;

    // Credential for Calendar
    public static GoogleAccountCredential googleAccountCredential;

    // ActivityResultLauncher to handle sign-in result
    private final ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == RESULT_OK) {
                                Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                                View btn = findViewById(R.id.signIn);
                                try {
                                    GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                                    if (signInAccount != null) {
                                        OrganizerScreen.fullName = signInAccount.getDisplayName();
                                        OrganizerScreen.emailUser = signInAccount.getEmail();
                                        String userPhoto = (signInAccount.getPhotoUrl() != null)
                                                ? signInAccount.getPhotoUrl().toString()
                                                : "No photo available";
                                    }

                                    // Firebase sign-in with the Google ID token
                                    AuthCredential authCredential = GoogleAuthProvider.getCredential(
                                            signInAccount.getIdToken(), null);
                                    mAuth.signInWithCredential(authCredential)
                                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (task.isSuccessful()) {
                                                        mAuth = FirebaseAuth.getInstance();
                                                        Toast.makeText(MainActivity.this,
                                                                "Signed in successfully!",
                                                                Toast.LENGTH_SHORT).show();

                                                        // Now that user is signed in, set up the Calendar credential
                                                        setupGoogleAccountCredential();

                                                        // Navigate to next screen
                                                        Navigation.findNavController(btn)
                                                                .navigate(R.id.action_loginScreen_to_generalAppScreen);
                                                    } else {
                                                        Toast.makeText(MainActivity.this,
                                                                "Failed to sign in: " + task.getException(),
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } catch (ApiException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();



    }

    // Method to start Google Sign-In (called from Fragment, presumably)
    public void startGoogleSignIn() {
        // Configure GoogleSignInOptions to request Calendar scope
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .requestScopes(new com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/calendar"))
                .build();

        googleSignInClient = GoogleSignIn.getClient(MainActivity.this, options);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        activityResultLauncher.launch(signInIntent);
    }

    // Once sign-in is successful, set up the Calendar credential
    private void setupGoogleAccountCredential() {
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (signInAccount != null) {
            googleAccountCredential = GoogleAccountCredential.usingOAuth2(
                    this,
                    Collections.singleton("https://www.googleapis.com/auth/calendar")
            );
            googleAccountCredential.setSelectedAccount(signInAccount.getAccount());

            // You can now pass googleAccountCredential into the Calendar builder if needed
        }
    }

    // Example: Build the Calendar client
    public Calendar getCalendarService() {
        if (googleAccountCredential == null) {
            setupGoogleAccountCredential();
        }

        // Use NetHttpTransport.Builder instead of newTrustedTransport()
        NetHttpTransport httpTransport = new NetHttpTransport.Builder().build();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        return new Calendar.Builder(httpTransport, jsonFactory, googleAccountCredential)
                .setApplicationName("WorkShiftApp")
                .build();
    }

    // Example method to add an event (could be called from a Fragment)
    // Example method to add an event (could be called from a Fragment)
    public void addEventToCalendar(ArrayList<CalendarShift> arrayList) {
        new Thread(() -> {
            try {
                Calendar service = getCalendarService();

                for (CalendarShift shift : arrayList) {

                    com.google.api.services.calendar.model.Event event = new com.google.api.services.calendar.model.Event()
                            .setSummary("Shift");


                    String[] timeParts_start = shift.getStartTime().split(" ");
                    String startPartTime = timeParts_start[0];
                    String startPeriod = timeParts_start[1]; // AM/PM
                    String[] datePartsStart = startPartTime.split(":");
                    int startHour = Integer.parseInt(datePartsStart[0]);
                    int startMin = Integer.parseInt(datePartsStart[1]);

                    if (startPeriod.equalsIgnoreCase("PM") && startHour != 12) {
                        startHour += 12;
                    } else if (startPeriod.equalsIgnoreCase("AM") && startHour == 12) {
                        startHour = 0; // Midnight case
                    }

                    // Ensure startHour and startMin are formatted as two digits
                    String startHourStr = String.format("%02d", startHour); // Format hour with leading zero if needed
                    String startMinStr = String.format("%02d", startMin); // Format minute with leading zero if needed

                    String[] timeParts_end = shift.getEndTime().split(" ");
                    String endPartTime = timeParts_end[0];
                    String endPeriod = timeParts_end[1]; // AM/PM
                    String[] datePartsEnd = endPartTime.split(":");
                    int endHour = Integer.parseInt(datePartsEnd[0]);
                    int endMin = Integer.parseInt(datePartsEnd[1]);

                    if (endPeriod.equalsIgnoreCase("PM") && endHour != 12) {
                        endHour += 12;
                    } else if (endPeriod.equalsIgnoreCase("AM") && endHour == 12) {
                        endHour = 0; // Midnight case
                    }

                    // Ensure endHour and endMin are formatted as two digits
                    String endHourStr = String.format("%02d", endHour); // Format hour with leading zero if needed
                    String endMinStr = String.format("%02d", endMin); // Format minute with leading zero if needed

                    // You can now use startHourStr, startMinStr, endHourStr, and endMinStr as formatted values


                    // Example start/end times (RFC3339)
                    String startDateTimeStr = shift.getDate() + startHourStr +":"+ startMinStr + ":00+02:00";
                    String endDateTimeStr = shift.getDate()+ endHourStr +":"+ endMinStr + ":00+02:00";


                    com.google.api.client.util.DateTime startDateTime = new com.google.api.client.util.DateTime(startDateTimeStr);
                    com.google.api.services.calendar.model.EventDateTime start =
                            new com.google.api.services.calendar.model.EventDateTime().setDateTime(startDateTime);
                    event.setStart(start);

                    com.google.api.client.util.DateTime endDateTime = new com.google.api.client.util.DateTime(endDateTimeStr);
                    com.google.api.services.calendar.model.EventDateTime end =
                            new com.google.api.services.calendar.model.EventDateTime().setDateTime(endDateTime);
                    event.setEnd(end);

                    // Insert event
                    String calendarId = "primary";
                    event = service.events().insert(calendarId, event).execute();

                    Log.d("MainActivity", "Event created: " + event.getHtmlLink());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void login(View v) {
        String email = ((EditText) findViewById(R.id.login_TextEmail)).getText().toString();
        String password = ((EditText) findViewById(R.id.login_TextPassword)).getText().toString();
        OrganizerScreen.emailUser = email;

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "login ok", Toast.LENGTH_LONG).show();
                            Navigation.findNavController(v).navigate(R.id.action_loginScreen_to_generalAppScreen);
                        } else {
                            Toast.makeText(MainActivity.this, "login fail", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void register() {
        String email = ((EditText) findViewById(R.id.reg_TextEmail)).getText().toString();
        String password = ((EditText) findViewById(R.id.reg_TextPassword)).getText().toString();
        String fullName = ((EditText) findViewById(R.id.reg_FullName)).getText().toString();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "register ok", Toast.LENGTH_LONG).show();
                            addData(email, fullName);
                        } else {
                            Toast.makeText(MainActivity.this, "register fail", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void addData(String email, String fullName) {
        String sanitizedEmail = email.replace(".", "_");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Root").child("Users").child(sanitizedEmail);
        Worker worker = new Worker(email, fullName);
        myRef.setValue(worker);
    }

    public GoogleAccountCredential getGoogleAccountCredential() {
        return googleAccountCredential;
    }
}