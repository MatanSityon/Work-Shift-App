package com.example.workshiftapp.fragments;

//import static android.text.format.DateUtils.getDayOfWeekString;
//import static androidx.navigation.fragment.FragmentKt.findNavController;

import android.app.TimePickerDialog;
//import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
//import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.workshiftapp.R;
import com.example.workshiftapp.activities.MainActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import androidx.navigation.Navigation;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


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

        Button morningTimePicker = view.findViewById(R.id.time_picker_morning);
        Button eveningTimePicker = view.findViewById(R.id.time_picker_evening);
        morningTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_generalAppScreen_to_timeSelectFragment);
            }
        });
        eveningTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_generalAppScreen_to_timeSelectFragment);
            }
        });

        //setupTimePicker(morningTimePicker,8,0);
        //setupTimePicker(eveningTimePicker,16,0);

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




}