package com.example.workshiftapp.fragments;

//import static android.text.format.DateUtils.getDayOfWeekString;
//import static androidx.navigation.fragment.FragmentKt.findNavController;

//import android.content.Intent;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
//import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
        import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.workshiftapp.R;
import com.example.workshiftapp.activities.MainActivity;
import com.example.workshiftapp.models.Shift;
import com.example.workshiftapp.models.Worker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrganizerScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrganizerScreen extends Fragment {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    public static String emailUser;
    private String fullName;
    public interface UserNameCallback {
        void onUserNameRetrieved(String fullName);
    }


    public OrganizerScreen() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OrganizerScreen.
     */
    // TODO: Rename and change types and number of parameters
    public static OrganizerScreen newInstance(String param1, String param2) {
        OrganizerScreen fragment = new OrganizerScreen();
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
        View view =inflater.inflate(R.layout.fragment_organizer_screen, container, false);
        GeneralAppScreen generalAppScreen = new GeneralAppScreen();
        //emailUser = generalAppScreen.emailUser;
        Button googleSignOutButton = view.findViewById(R.id.logOutBtn);

            googleSignOutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity != null) {
                    mainActivity.googleSignInClient.signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(view).navigate(R.id.action_generalAppScreen_to_loginScreen);

                        }
                    });
                } else {
                    Toast.makeText(requireContext(), "Unable to sign out", Toast.LENGTH_SHORT).show();

                }
            }
        });


        CalendarView calendar = view.findViewById(R.id.calendarView);
        TextView date = view.findViewById(R.id.selectedDay);
        CalendarView calendarView = view.findViewById(R.id.calendarView);
        

        // Get current date
        Calendar calendarInstance = Calendar.getInstance();
        int year = calendarInstance.get(Calendar.YEAR);
        int month = calendarInstance.get(Calendar.MONTH);
        int dayOfMonth = calendarInstance.get(Calendar.DAY_OF_MONTH);

        // Display day of the week for current day
        date.setText(displayDayOfWeek(year, month, dayOfMonth));

        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {


            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month,
                                            int dayOfMonth) {
                date.setText(displayDayOfWeek(year, month, dayOfMonth));

            }


        });

        Button startTimePicker = view.findViewById(R.id.time_picker_start);
        Button endTimePicker = view.findViewById(R.id.time_picker_end);


        setupTimePicker(startTimePicker,8,0);
        setupTimePicker(endTimePicker,16,0);

        Button assignShiftBtn = view.findViewById(R.id.submit_time_btn);
        assignShiftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String start = startTimePicker.getText().toString();
                String end = endTimePicker.getText().toString();
                //addShift(v,start,end,emailUser);
                String date = ((TextView) view.findViewById(R.id.selectedDay)).getText().toString();
                getUserName(emailUser, new UserNameCallback() {
                    @Override
                    public void onUserNameRetrieved(String fullName) {
                        if (fullName != null) {
                            // Use the fullName here, now that it's available
                            addShift(date, start, end, fullName);
                        }
                    }
                });

            }
        });


        return view;
    }




    public String displayDayOfWeek(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);


        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String curDate;
        String dayOfWeekString = getDayOfWeekString(dayOfWeek);
        curDate = String.valueOf(dayOfWeekString + "\n" + dayOfMonth + "/" + (month + 1) + "/" + year);
        return curDate.toString();
    }
    public String getDayOfWeekString(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                return "Sunday";
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";
            default:
                return "Unknown";
        }
    }
    private void setupTimePicker(Button button, int initialHour, int initialMinute) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        getActivity(),
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                // Formatting the time to 12-hour format
                                String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                                SimpleDateFormat f24Hour = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                try {
                                    Date date = f24Hour.parse(time);
                                    SimpleDateFormat f12Hour = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
                                    String time12hr = f12Hour.format(date);
                                    button.setText(time12hr);  // Set button text or handle time as needed
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, initialHour, initialMinute, false
                );
                timePickerDialog.show();
            }
        });
    }

    private void addShift(String date,String start, String end, String name)
    {
        //String date = view.findViewById(R.id.selectedDay).toString();
        int space = date.indexOf("\n");
        String sanitizedDate = date.substring(space + 1);
        int slash = sanitizedDate.indexOf("/");
        String day = sanitizedDate.substring(0,slash);
        int lastSlash = sanitizedDate.lastIndexOf("/");
        String month = sanitizedDate.substring(slash+1,lastSlash);
        String year = sanitizedDate.substring(lastSlash+1);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Root").child("Calendar").child(year).child(month).child(day);
        //Worker worker = new Worker(email, fullName);
        //myRef.setValue(date+" Start time:"+start+" End time: "+end);
        Shift shift = new Shift(name,start,end);
        myRef.setValue(shift);

    }

    private void getUserName(String email, UserNameCallback callback) {
        String sanitizedEmail = email.replace(".", "_");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Root");

        ref.child("Users").child(sanitizedEmail).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    String fullName = String.valueOf(dataSnapshot.child("fullName").getValue());

                   // String start = startTimePicker.getText().toString();


                    callback.onUserNameRetrieved(fullName);  // Pass the result via callback
                } else {
                    Log.e("Firebase", "Failed to get data: " + task.getException());
                    callback.onUserNameRetrieved(null);  // Handle failure (optional)
                }
            }
        });
    }


}