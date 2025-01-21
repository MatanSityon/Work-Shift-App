package com.example.workshiftapp.fragments;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.workshiftapp.R;
import com.example.workshiftapp.activities.MainActivity;
import com.example.workshiftapp.models.CalendarShift;
import com.example.workshiftapp.models.Shift;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
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
import java.util.Collections;
import java.util.Date;
import java.util.Locale;


public class OrganizerScreen extends Fragment {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String emailUser;
    public static String fullName;
    private boolean isOnShift;
    private double wage;
    private String calendarID;
    private MainActivity mainActivity;
    Button assignShiftBtn;
    GoogleSignInClient googleSignInClient;
    GoogleAccountCredential googleAccountCredential;
    public interface OnEventsFetchedCallback {
        void onEventsFetched(ArrayList<CalendarShift> events);
    }
    public interface CalendarSyncCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
         mainActivity= (MainActivity) getActivity();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_organizer_screen, container, false);
        if (mainActivity != null)
        {
            googleAccountCredential = mainActivity.getGoogleAccountCredential();
            googleSignInClient = mainActivity.getGetGoogleSignInClient();
            emailUser = mainActivity.getEmailUser();
            fullName = mainActivity.getFullName();
            calendarID = mainActivity.getCalendarID();
        }
        TextView calendarIDText = view.findViewById(R.id.calanderIDTextView);
        CalendarView calendarView = view.findViewById(R.id.calendarView);
        TextView dateTextView = view.findViewById(R.id.selectedDay);
        GridLayout workersGrid = view.findViewById(R.id.workers_textbox);
        Button startTimePicker = view.findViewById(R.id.time_picker_start);
        Button endTimePicker = view.findViewById(R.id.time_picker_end);
        assignShiftBtn = view.findViewById(R.id.submit_time_btn);
        FloatingActionButton syncBtn = view.findViewById(R.id.SyncGoogleBtn);
        // Set up time pickers
        setupTimePicker(startTimePicker, 8, 0);
        setupTimePicker(endTimePicker, 16, 0);
        // Handle date selection
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) ->
                handleDateChange(year, month, dayOfMonth, dateTextView, workersGrid));
        // Display today's data on launch
        Calendar today = Calendar.getInstance();
        handleDateChange(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH), dateTextView, workersGrid);
        calendarIDText.setText("Calendar ID: "+calendarID);
        calendarIDText.setBackgroundResource(R.drawable.rounded_box);
        // Handle shift assignment
        assignShiftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (assignShiftBtn.getText().equals("Shift Me!")) {
                    String selectedDate = dateTextView.getText().toString();
                    String start = startTimePicker.getText().toString();
                    String end = endTimePicker.getText().toString();
                    if (fullName == null) {
                        fullName = mainActivity.getFullName();
                        addShift(selectedDate, start, end, fullName);
                        refreshDataAfterShiftAssignment(selectedDate, dateTextView, workersGrid); // Refresh UI
                    } else {
                        addShift(selectedDate, start, end, fullName);
                        refreshDataAfterShiftAssignment(selectedDate, dateTextView, workersGrid); // Refresh UI
                    }
                }
                else {
                    String selectedDate = dateTextView.getText().toString();
                    if (fullName == null) {
                        fullName = mainActivity.getFullName();
                        removeShift(selectedDate, fullName);
                        refreshDataAfterShiftAssignment(selectedDate, dateTextView, workersGrid); // Refresh UI
                    }
                    else {
                        removeShift(selectedDate, fullName);
                        refreshDataAfterShiftAssignment(selectedDate, dateTextView, workersGrid); // Refresh UI
                    }
                }
            }
        });
        //Sync button handle
        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchEventsList(new OnEventsFetchedCallback() {
                    @Override
                    public void onEventsFetched(ArrayList<CalendarShift> events) {
                        addEventToCalendar(events, new CalendarSyncCallback() {
                            @Override
                            public void onSuccess() {
                                Snackbar snackbar = Snackbar.make(requireView(),
                                        "Shifts added or updated in Google Calendar!",
                                        Snackbar.LENGTH_LONG);
                                snackbar.setBackgroundTint(Color.parseColor("#FFFFFF"));
                                snackbar.setTextColor(Color.BLACK);
                                snackbar.show();
                            }

                            @Override
                            public void onFailure(Exception e) {

                            }
                        });
                    }
                });
            }
        });
        return view;
    }
    private void handleDateChange(int year, int month, int dayOfMonth, TextView dateTextView, GridLayout workersGrid) {
        // Update the selected date text
        dateTextView.setText(displayDayOfWeek(year, month, dayOfMonth));
        dateTextView.setBackgroundResource(R.drawable.rounded_box);
        // Clear the GridLayout
        workersGrid.removeAllViews();
        String sYear = String.valueOf(year);
        String sMonth = String.valueOf(month + 1); // Months are zero-based
        String sDay = String.valueOf(dayOfMonth);
        // Fetch and display data from Firebase
        DatabaseReference myRef = FirebaseDatabase.getInstance()
                .getReference("Root")
                .child(calendarID)
                .child("Calendar")
                .child(sYear)
                .child(sMonth)
                .child(sDay);

        myRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot snapshot = task.getResult();
                isOnShift = false;
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String name = userSnapshot.child("worker").getValue(String.class);
                        String startTime = userSnapshot.child("startTime").getValue(String.class);
                        String endTime = userSnapshot.child("endTime").getValue(String.class);
                        if(name.equals(fullName))
                        {
                            isOnShift = true;
                        }
                        if (name != null && startTime != null && endTime != null) {
                            LinearLayout workerLayout = new LinearLayout(getActivity());
                            workerLayout.setOrientation(LinearLayout.VERTICAL);
                            workerLayout.setPadding(16, 16, 16, 16);
                            TextView nameTextView = new TextView(getActivity());
                            nameTextView.setTextColor(Color.BLACK);
                            nameTextView.setText("Worker: " + name);
                            TextView startTextView = new TextView(getActivity());
                            startTextView.setTextColor(Color.BLACK);
                            startTextView.setText("Start work at: " + startTime);
                            TextView endTextView = new TextView(getActivity());
                            endTextView.setTextColor(Color.BLACK);
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
                Snackbar snackbar = Snackbar.make(requireView(), "Failed to fetch data: " + task.getException(), Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(Color.parseColor("#FFFFFF"));
                snackbar.setTextColor(Color.RED);
                snackbar.setAction("Dismiss", x -> {
                });
                snackbar.show();
            }
            if (isOnShift) {
                assignShiftBtn.setText("Unshift Me!");
                assignShiftBtn.setBackgroundResource(R.drawable.unshiftme_back);
            }
            else
            {
                assignShiftBtn.setText("Shift Me!");
                assignShiftBtn.setBackgroundResource(R.drawable.rounded_box);
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
                .child(calendarID)
                .child("Calendar")
                .child(year)
                .child(month)
                .child(day)
                .child(name);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        try {
            // Parse the times into Date objects
            Date startDate = sdf.parse(start);
            Date endDate = sdf.parse(end);
            // Compare the times
            if (startDate.before(endDate)) {
                Shift shift = new Shift(name, start, end);
                myRef.setValue(shift);
            } else {
                Snackbar snackbar = Snackbar.make(requireView(), "Start time Should be before end time ", Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(Color.parseColor("#FFFFFF"));
                snackbar.setTextColor(Color.RED);
                snackbar.setAction("Dismiss", x -> {
                });
                snackbar.show();
            }
        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println("Invalid time format.");
        }
    }
    private void removeShift(String date, String name){
        String sanitizedDate = date.substring(date.indexOf("\n") + 1);
        String[] dateParts = sanitizedDate.split("/");
        String day = dateParts[0];
        String month = dateParts[1];
        String year = dateParts[2];
        DatabaseReference myRef = FirebaseDatabase.getInstance()
                .getReference("Root")
                .child(calendarID)
                .child("Calendar")
                .child(year)
                .child(month)
                .child(day)
                .child(name);
        // To delete the data at this reference
        myRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Snackbar snackbar = Snackbar.make(requireView(), "Data deleted successfully ", Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(Color.parseColor("#FFFFFF"));
                snackbar.setTextColor(Color.BLACK);
                snackbar.setAction("Dismiss", x -> {
                });
                snackbar.show();
            } else {
                Snackbar snackbar = Snackbar.make(requireView(), "Failed to delete data", Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(Color.parseColor("#FFFFFF"));
                snackbar.setTextColor(Color.RED);
                snackbar.setAction("Dismiss", x -> {
                });
                snackbar.show();
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
        if (googleSignInClient != null)
        {
            ArrayList<CalendarShift> arr = new ArrayList<>();
            DatabaseReference calendarRef = FirebaseDatabase.getInstance()
                    .getReference("Root")
                    .child(calendarID)
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
                                DataSnapshot userSnapshot = daySnapshot.child(fullName);
                                if (userSnapshot.exists()) {
                                    // Retrieve startTime and endTime
                                    String startTime = userSnapshot.child("startTime").getValue(String.class);
                                    String endTime = userSnapshot.child("endTime").getValue(String.class);
                                    String date = "";
                                    if (startTime != null && endTime != null) {
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
        else
        {
            Snackbar snackbar = Snackbar.make(requireView(), "Syncing is only available after Google Sign-In", Snackbar.LENGTH_LONG);
            snackbar.setBackgroundTint(Color.parseColor("#FFFFFF"));
            snackbar.setTextColor(Color.RED);
            snackbar.setAction("Dismiss", x -> {
            });
            snackbar.show();
        }
    }
    public com.google.api.services.calendar.Calendar getCalendarService() {
        setupGoogleAccountCredential();
        setupGoogleAccountCredential();
        NetHttpTransport httpTransport = new NetHttpTransport.Builder().build();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        return new com.google.api.services.calendar.Calendar.Builder(httpTransport, jsonFactory, googleAccountCredential)
                .setApplicationName("WorkShiftApp")
                .build();
    }
    // Once sign-in is successful, set up the Calendar credential
    private void setupGoogleAccountCredential() {
        assert mainActivity != null;
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(mainActivity);
        if (signInAccount != null) {
            googleAccountCredential = GoogleAccountCredential.usingOAuth2(
                    mainActivity,
                    Collections.singleton("https://www.googleapis.com/auth/calendar")
            );
            googleAccountCredential.setSelectedAccount(signInAccount.getAccount());
        }
    }
    public void addEventToCalendar(ArrayList<CalendarShift> arrayList, CalendarSyncCallback callback) {
        new Thread(() -> {
            try {
                com.google.api.services.calendar.Calendar service = getCalendarService();
                // Fetch and delete all existing events with the summary "Shift"
                String calendarId = "primary";
                com.google.api.services.calendar.model.Events existingEvents = service.events().list(calendarId)
                        .setQ("Shift") // Search for events with the summary "Shift"
                        .setSingleEvents(true)
                        .execute();
                for (com.google.api.services.calendar.model.Event existingEvent : existingEvents.getItems()) {
                    if ("Shift".equals(existingEvent.getSummary())) {
                        service.events().delete(calendarId, existingEvent.getId()).execute();
                        Log.d("MainActivity", "Deleted existing event: " + existingEvent.getHtmlLink());
                    }
                }
                // Insert new events from the arrayList
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
                    String endHourStr = String.format("%02d", endHour); // Format hour with leading zero if needed
                    String endMinStr = String.format("%02d", endMin); // Format minute with leading zero if needed
                    // Correct RFC3339 datetime strings
                    String startDateTimeStr = shift.getDate() + startHourStr + ":" + startMinStr + ":00+02:00";
                    String endDateTimeStr = shift.getDate() +  endHourStr + ":" + endMinStr + ":00+02:00";
                    com.google.api.client.util.DateTime startDateTime = new com.google.api.client.util.DateTime(startDateTimeStr);
                    com.google.api.client.util.DateTime endDateTime = new com.google.api.client.util.DateTime(endDateTimeStr);
                    event.setStart(new com.google.api.services.calendar.model.EventDateTime().setDateTime(startDateTime));
                    event.setEnd(new com.google.api.services.calendar.model.EventDateTime().setDateTime(endDateTime));
                    event = service.events().insert(calendarId, event).execute();
                    Log.d("MainActivity", "Event created: " + event.getHtmlLink());
                }
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onSuccess();
                });

            } catch (Exception e) {
                Log.e("MainActivity", "Error occurred while adding events to calendar.", e);
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onFailure(e);
                });
            }
        }).start();
    }
}
