package com.example.workshiftapp.fragments;

import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
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
import com.example.workshiftapp.models.CalendarShift;
import com.example.workshiftapp.models.CardShift;
import com.example.workshiftapp.models.Shift;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
import android.provider.CalendarContract;

public class OrganizerScreen extends Fragment {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public static String emailUser;
    public static String fullName;

    public interface UserNameCallback {
        void onUserNameRetrieved(String fullName);
    }
    public interface OnEventsFetchedCallback {
        void onEventsFetched(ArrayList<CalendarShift> events);
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
            if (MainActivity.googleAccountCredential == null)
            {
                Navigation.findNavController(view).navigate(R.id.action_generalAppScreen_to_loginScreen);
                Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show();
            }
            else
            {
                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity != null) {
                    mainActivity.googleSignInClient.signOut().addOnSuccessListener(unused -> {
                        mAuth.signOut();
                        MainActivity.googleAccountCredential = null;
                        Toast.makeText(requireContext(), "Signed out from Google account", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(view).navigate(R.id.action_generalAppScreen_to_loginScreen);
                    });
                } else {
                    Toast.makeText(requireContext(), "Unable to sign out", Toast.LENGTH_SHORT).show();
                }
            }

        });

        FloatingActionButton syncBtn = view.findViewById(R.id.SyncGoogleBtn);
        MainActivity mainActivity = (MainActivity) getActivity();


        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchEventsList(new OnEventsFetchedCallback() {
                    @Override
                    public void onEventsFetched(ArrayList<CalendarShift> events) {
                            mainActivity.addEventToCalendar(events);
                    }
                });
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
        //addEventToCalendar(year,month,day,start,end); ///////nivs calendar add event
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

    private void fetchEventsList(OnEventsFetchedCallback callback) {
        ArrayList<CalendarShift> arr = new ArrayList<>();

        // Start from the root reference of the calendar
        DatabaseReference calendarRef = FirebaseDatabase.getInstance()
                .getReference("Root")
                .child("Calendar");

        calendarRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Iterate over all years
                for (DataSnapshot yearSnapshot : snapshot.getChildren()) {
                    String year = yearSnapshot.getKey(); // Year key (e.g., "2025")

                    // Iterate over all months in the year
                    for (DataSnapshot monthSnapshot : yearSnapshot.getChildren()) {
                        String month = monthSnapshot.getKey(); // Month key (e.g., "1", "2", ... "12")

                        // Iterate over all days in the month
                        for (DataSnapshot daySnapshot : monthSnapshot.getChildren()) {
                            String day = daySnapshot.getKey(); // Day key (e.g., "1", "2", ... "31")

                            // Check if the user's name exists under this day
                            DataSnapshot userSnapshot = daySnapshot.child(OrganizerScreen.fullName);
                            if (userSnapshot.exists()) {
                                // Retrieve startTime and endTime
                                String startTime = userSnapshot.child("startTime").getValue(String.class);
                                String endTime = userSnapshot.child("endTime").getValue(String.class);
                                String date = "";
                                if (startTime != null && endTime != null) {
                                    // Format the date (e.g., "DD/MM/YYYY")
                                    //"2025-01-19T10:00:00";
                                    if(Integer.parseInt(month)<10) {
                                         date = year + "-" + "0" + month + "-" + day + "T";
                                    }
                                    else{
                                         date = year + "-" + month + "-" + day + "T";
                                    }

                                    // Create a CalendarShift object and add it to the list
                                    CalendarShift shift = new CalendarShift(date, startTime, endTime);
                                    arr.add(shift);
                                }
                            }
                        }
                    }
                }

                // Notify the callback with the fetched events
                callback.onEventsFetched(arr);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Failed to fetch data: " + error.getMessage());
                callback.onEventsFetched(new ArrayList<>()); // Return empty list on error
            }
        });
    }

    public void addEventToCalendar(String year,String month,String day, String startTime,String endTime) {
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
        Calendar beginTimeCalendar = Calendar.getInstance();
        beginTimeCalendar.set(Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(day), startHour, startMin);

        Calendar endTimeCalendar = Calendar.getInstance();
        endTimeCalendar.set(Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(day), endHour, endMin);

        long startMillis = beginTimeCalendar.getTimeInMillis();
        long endMillis = endTimeCalendar.getTimeInMillis();

        Date startDate = new Date(startMillis);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedStartTime = sdf.format(startDate);
        Date endDate = new Date(endMillis);
        SimpleDateFormat sdf_2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedEndTime = sdf_2.format(endDate);


        // Insert event using Intent
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.Events.ALL_DAY, false);
        intent.putExtra(CalendarContract.Events.TITLE, "Shift");
        intent.putExtra(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis);

        String[] calendarPackages = {
                "com.google.android.calendar",     // Google's official calendar app
                "com.google.android.apps.calendar", // Alternate package for some devices
                "com.android.calendar"             // Default Android calendar package
        };

        PackageManager packageManager = getActivity().getPackageManager();
        boolean calendarAppFound = false;

        for (String packageName : calendarPackages) {
            Intent launchIntent = packageManager.getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                calendarAppFound = true;
                intent.setPackage(packageName); // Set the package name for the calendar app
                break;
            }
        }
        startActivity(intent);
    }

}
