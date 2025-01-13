package com.example.workshiftapp.fragments;

import static android.text.format.DateUtils.getDayOfWeekString;
import static androidx.navigation.fragment.FragmentKt.findNavController;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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




        Spinner spinner_morning_hours = view.findViewById(R.id.start_morning_spinner);
        Spinner spinner_morning_minutes = view.findViewById(R.id.end_morning_spinner);
        Spinner spinner_night_hours = view.findViewById(R.id.start_night_spinner);
        Spinner spinner_night_minutes = view.findViewById(R.id.end_night_spinner);

        spinner_morning_hours.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner_morning_minutes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner_night_hours.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner_night_minutes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayList<String> morning_hours = new ArrayList<>(),morning_minutes = new ArrayList<>(), night_hours = new ArrayList<>(),night_minutes = new ArrayList<>();
        Collections.addAll(morning_hours, "08", "09", "10", "11", "12", "13", "14", "15", "16");
        Collections.addAll(morning_minutes, "00","10", "20", "30", "40", "50", "60");
        Collections.addAll(night_hours, "16","17", "18", "19", "20","21", "22", "23", "24");
        Collections.addAll(night_minutes, "00","10", "20", "30", "40", "50", "60");
        ArrayAdapter<String> adapter_morning_hours = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, morning_hours);
        adapter_morning_hours.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner_morning_hours.setAdapter(adapter_morning_hours);
        ArrayAdapter<String> adapter_morning_minutes = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, morning_minutes);
        adapter_morning_minutes.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner_morning_minutes.setAdapter(adapter_morning_minutes);
        ArrayAdapter<String> adapter_night_hours = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, night_hours);
        adapter_night_hours.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner_night_hours.setAdapter(adapter_night_hours);
        ArrayAdapter<String> adapter_night_minutes = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, night_minutes);
        adapter_night_minutes.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner_night_minutes.setAdapter(adapter_night_minutes);


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