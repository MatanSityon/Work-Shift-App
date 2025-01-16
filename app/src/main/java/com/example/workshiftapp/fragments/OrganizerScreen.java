package com.example.workshiftapp.fragments;

import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.workshiftapp.R;
import com.example.workshiftapp.activities.MainActivity;
import com.example.workshiftapp.models.Shift;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import android.content.Intent;
import android.provider.CalendarContract;
import android.net.Uri;
import java.util.Calendar;
import java.util.TimeZone;
import android.provider.CalendarContract;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.CalendarContract.Events;

public class OrganizerScreen extends Fragment {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public static String emailUser;
    public static String fullName;

    public interface UserNameCallback {
        void onUserNameRetrieved(String fullName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_organizer_screen, container, false);

        // Initialize UI elements
        CalendarView calendarView = view.findViewById(R.id.calendarView);
        TextView dateTextView = view.findViewById(R.id.selectedDay);
        GridLayout workersGrid = view.findViewById(R.id.workers_textbox);
        Button startTimePicker = view.findViewById(R.id.time_picker_start);
        Button endTimePicker = view.findViewById(R.id.time_picker_end);
        Button assignShiftBtn = view.findViewById(R.id.submit_time_btn);
        Button googleSignOutButton = view.findViewById(R.id.logOutBtn);

        // Set up time pickers
        setupTimePicker(startTimePicker, 8, 0);
        setupTimePicker(endTimePicker, 16, 0);

        // Handle date selection
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) ->
                handleDateChange(year, month, dayOfMonth, dateTextView, workersGrid));

        // Display today's data on launch
        Calendar today = Calendar.getInstance();
        handleDateChange(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH), dateTextView, workersGrid);

        // Handle shift assignment
        assignShiftBtn.setOnClickListener(v -> {
            String start = startTimePicker.getText().toString();
            String end = endTimePicker.getText().toString();
            if (start != null && end != null)
            {
                String selectedDate = dateTextView.getText().toString();

                if (fullName == null) {
                    getUserName(emailUser, name -> {
                        if (name != null) {
                            fullName = name;
                            addShift(selectedDate, start, end, fullName);
                            refreshDataAfterShiftAssignment(selectedDate,dateTextView,workersGrid); // Refresh UI
                        }
                    });
                } else {
                    addShift(selectedDate, start, end, fullName);
                    refreshDataAfterShiftAssignment(selectedDate,dateTextView,workersGrid); // Refresh UI
                }
            }

        });


        // Handle Google sign-out
        googleSignOutButton.setOnClickListener(v -> {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity != null) {
                mainActivity.googleSignInClient.signOut().addOnSuccessListener(unused -> {
                    mAuth.signOut();
                    Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).navigate(R.id.action_generalAppScreen_to_loginScreen);
                });
            } else {
                Toast.makeText(requireContext(), "Unable to sign out", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void handleDateChange(int year, int month, int dayOfMonth, TextView dateTextView, GridLayout workersGrid) {
        // Update the selected date text
        dateTextView.setText(displayDayOfWeek(year, month, dayOfMonth));

        // Clear the GridLayout
        workersGrid.removeAllViews();

        // Fetch and display data from Firebase
        String sYear = String.valueOf(year);
        String sMonth = String.valueOf(month + 1); // Months are zero-based
        String sDay = String.valueOf(dayOfMonth);

        DatabaseReference myRef = FirebaseDatabase.getInstance()
                .getReference("Root")
                .child("Calendar")
                .child(sYear)
                .child(sMonth)
                .child(sDay);

        myRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot snapshot = task.getResult();

                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String name = userSnapshot.child("worker").getValue(String.class);
                        String startTime = userSnapshot.child("startTime").getValue(String.class);
                        String endTime = userSnapshot.child("endTime").getValue(String.class);

                        if (name != null && startTime != null && endTime != null) {
                            LinearLayout workerLayout = new LinearLayout(getActivity());
                            workerLayout.setOrientation(LinearLayout.VERTICAL);
                            workerLayout.setPadding(16, 16, 16, 16);

                            TextView nameTextView = new TextView(getActivity());
                            nameTextView.setText("Worker: " + name);

                            TextView startTextView = new TextView(getActivity());
                            startTextView.setText("Start work at: " + startTime);

                            TextView endTextView = new TextView(getActivity());
                            endTextView.setText("End work at: " + endTime);

                            workerLayout.addView(nameTextView);
                            workerLayout.addView(startTextView);
                            workerLayout.addView(endTextView);

                            workersGrid.addView(workerLayout);
                        }
                    }
                } else {
                    TextView noDataTextView = new TextView(getActivity());
                    noDataTextView.setText("No one is working today! Schedule your shift!");
                    workersGrid.addView(noDataTextView);
                }
            } else {
                Toast.makeText(getActivity(), "Failed to fetch data: " + task.getException(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTimePicker(Button button, int initialHour, int initialMinute) {
        button.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    getActivity(),
                    TimePickerDialog.THEME_HOLO_LIGHT, // Use digital style
                    (view, hourOfDay, minute) -> {
                        // Format the time into 12-hour format
                        String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        SimpleDateFormat f24Hour = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        try {
                            Date date = f24Hour.parse(time);
                            SimpleDateFormat f12Hour = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
                            button.setText(f12Hour.format(date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    },
                    initialHour,
                    initialMinute,
                    false // Use 12-hour format
            );
            timePickerDialog.show();
        });
    }


    private void addShift(String date, String start, String end, String name) {
        String sanitizedDate = date.substring(date.indexOf("\n") + 1);
        String[] dateParts = sanitizedDate.split("/");
        String day = dateParts[0];
        String month = dateParts[1];
        String year = dateParts[2];

        DatabaseReference myRef = FirebaseDatabase.getInstance()
                .getReference("Root")
                .child("Calendar")
                .child(year)
                .child(month)
                .child(day)
                .child(name);

        Shift shift = new Shift(name, start, end);
        myRef.setValue(shift);
        //addEventToCalendar(year,month,day,start,end);
    }

    private void getUserName(String email, UserNameCallback callback) {
        String sanitizedEmail = email.replace(".", "_");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Root");

        ref.child("Users").child(sanitizedEmail).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot snapshot = task.getResult();
                String fullName = snapshot.child("fullName").getValue(String.class);
                callback.onUserNameRetrieved(fullName);
            } else {
                Log.e("Firebase", "Failed to get user name: " + task.getException());
                callback.onUserNameRetrieved(null);
            }
        });
    }

    public String displayDayOfWeek(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return getDayOfWeekString(dayOfWeek) + "\n" + dayOfMonth + "/" + (month + 1) + "/" + year;
    }

    public String getDayOfWeekString(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY: return "Sunday";
            case Calendar.MONDAY: return "Monday";
            case Calendar.TUESDAY: return "Tuesday";
            case Calendar.WEDNESDAY: return "Wednesday";
            case Calendar.THURSDAY: return "Thursday";
            case Calendar.FRIDAY: return "Friday";
            case Calendar.SATURDAY: return "Saturday";
            default: return "Unknown";
        }
    }
    private void refreshDataAfterShiftAssignment(String selectedDate, TextView dateTextView, GridLayout workersGrid) {
        // Parse date string into year, month, and day
        int spaceIndex = selectedDate.indexOf("\n");
        String sanitizedDate = selectedDate.substring(spaceIndex + 1);
        String[] dateParts = sanitizedDate.split("/");
        int day = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]) - 1; // Months are zero-based
        int year = Integer.parseInt(dateParts[2]);

        // Refresh the data for the selected date
        handleDateChange(year, month, day, dateTextView, workersGrid);
    }


    /*public void addEventToCalendar(String year,String month,String day, String startTime,String endTime) {
        // Check if permission is granted

        String[] timeParts_start = startTime.split(" ");
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

        String[] timeParts_end = endTime.split(" ");
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


        // Get calendar times in milliseconds
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(day), startHour, startMin);

        Calendar endTimeCalendar = Calendar.getInstance();
        endTimeCalendar.set(Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(day), endHour, endMin);

        long startMillis = beginTime.getTimeInMillis();
        long endMillis = endTimeCalendar.getTimeInMillis();

        // Insert event using ContentResolver
        if (getActivity() != null) { // Ensure getActivity() is not null
            ContentResolver cr = getActivity().getContentResolver();
            ContentValues values = new ContentValues();

            values.put(CalendarContract.Events.DTSTART, startMillis);
            values.put(CalendarContract.Events.DTEND, endMillis);
            values.put(CalendarContract.Events.TITLE, "Meeting with Team");
            values.put(CalendarContract.Events.DESCRIPTION, "Discuss project updates");
            values.put(CalendarContract.Events.CALENDAR_ID, 1); // Default calendar ID, query for available IDs if needed
            values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

            // Insert event into the calendar
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

            if (uri != null) {
                long eventId = Long.parseLong(uri.getLastPathSegment());
                Toast.makeText(getActivity(), "Event added to calendar with ID: " + eventId, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Failed to add event to calendar", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("addEventToCalendar", "getActivity() returned null");
        }
    }*/

}
