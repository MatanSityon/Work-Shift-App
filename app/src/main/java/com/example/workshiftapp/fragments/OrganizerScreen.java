package com.example.workshiftapp.fragments;

import android.app.TimePickerDialog;
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
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.workshiftapp.R;
import com.example.workshiftapp.activities.MainActivity;
import com.example.workshiftapp.models.CalendarShift;
import com.example.workshiftapp.models.Shift;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import android.content.Intent;
import android.provider.CalendarContract;
import java.util.TimeZone;

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


        // Initialize UI elements
        TextView calendarIDText = view.findViewById(R.id.calanderIDTextView);
        CalendarView calendarView = view.findViewById(R.id.calendarView);
        TextView dateTextView = view.findViewById(R.id.selectedDay);
        GridLayout workersGrid = view.findViewById(R.id.workers_textbox);
        Button startTimePicker = view.findViewById(R.id.time_picker_start);
        Button endTimePicker = view.findViewById(R.id.time_picker_end);
        assignShiftBtn = view.findViewById(R.id.submit_time_btn);
        //Button googleSignOutButton = view.findViewById(R.id.logOutBtn);
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
        // Handle shift assignment
        assignShiftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (assignShiftBtn.getText().equals("Shift Me!")) {
                    // Existing functionality for assigning a shift
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




        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchEventsList(new OnEventsFetchedCallback() {
                    @Override
                    public void onEventsFetched(ArrayList<CalendarShift> events) {
                            addEventToCalendar(events);
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
            if (isOnShift) {
                assignShiftBtn.setText("Unshift Me!");
                assignShiftBtn.setBackgroundResource(R.drawable.unshiftme_back);
            }
            else
            {
                assignShiftBtn.setText("Shift Me!");
                assignShiftBtn.setBackgroundResource(R.drawable.login_button);
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
                Toast.makeText(requireContext(), "Start time Should be before end time", Toast.LENGTH_SHORT).show();

            }

        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println("Invalid time format.");
        }

        //addEventToCalendar(year,month,day,start,end); ///////nivs calendar add event
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
                Toast.makeText(requireContext(), "Data deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Failed to delete data", Toast.LENGTH_SHORT).show();
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

            // Start from the root reference of the calendar
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
        else
        {
            Toast.makeText(requireContext(), "Syncing is only available after Google Sign-In", Toast.LENGTH_SHORT).show();
        }

    }
    // Example: Build the Calendar client
    public com.google.api.services.calendar.Calendar getCalendarService() {
        setupGoogleAccountCredential();

        setupGoogleAccountCredential();
        // Use NetHttpTransport.Builder instead of newTrustedTransport()
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

            // You can now pass googleAccountCredential into the Calendar builder if needed
        }
    }

    public void addEventToCalendar(ArrayList<CalendarShift> arrayList) {

        new Thread(() -> {
            try {
                com.google.api.services.calendar.Calendar service = getCalendarService();

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
                Toast.makeText(requireContext(), "Shifts added to Google Calendar!", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
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
