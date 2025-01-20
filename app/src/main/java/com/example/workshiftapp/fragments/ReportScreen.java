package com.example.workshiftapp.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import com.example.workshiftapp.R;
import com.example.workshiftapp.activities.MainActivity;
import com.example.workshiftapp.adapters.CustomAdapter;
import com.example.workshiftapp.models.CardShift;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReportScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReportScreen extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private MainActivity mainActivity;
    //private FragmentActivity organizerScreen;

    private String fullName;

    public ReportScreen() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReportScreen.
     */
    // TODO: Rename and change types and number of parameters
    public static ReportScreen newInstance(String param1, String param2) {
        ReportScreen fragment = new ReportScreen();
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
        View view =  inflater.inflate(R.layout.fragment_report_screen, container, false);
        mainActivity = (MainActivity) getActivity();
        fullName = mainActivity.getFullName();
        String[] monthSpinnerArray = new String[] {
                "1", "2", "3", "4", "5", "6", "7","8","9","10","11","12"
        };
        String[] yearSpinnerArray = new String[] {
                "2020", "2021", "2022", "2023","2024","2025","2026","2027","2028","2029","2030"
        };
        Spinner month = (Spinner) view.findViewById(R.id.MonthSpinnerView);
        ArrayAdapter<String> adapterMonth = new ArrayAdapter<String>(mainActivity,
                android.R.layout.simple_spinner_item, monthSpinnerArray);
        adapterMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        month.setAdapter(adapterMonth);
        Spinner year = (Spinner) view.findViewById(R.id.YearSpinnerView);
        ArrayAdapter<String> adapterYear = new ArrayAdapter<String>(mainActivity,
                android.R.layout.simple_spinner_item, yearSpinnerArray);
        adapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        year.setAdapter(adapterYear);
        Calendar calendar = Calendar.getInstance();

        // Get the current year
        String currentYear = String.valueOf(calendar.get(Calendar.YEAR));

        // Get the current month (0-based, January = 0)
        String currentMonth = String.valueOf(calendar.get(Calendar.MONTH) + 1); // Add 1 to make it 1-based (January = 1)

        month.setSelection(adapterMonth.getPosition(currentMonth));
        year.setSelection(adapterYear.getPosition(currentYear));

       // RecyclerView recyclerView = view.findViewById(R.id.rvcon);
       // RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
       // recyclerView.setLayoutManager(layoutManager);
       // recyclerView.setItemAnimator(new DefaultItemAnimator());
        RecyclerView recyclerView = view.findViewById(R.id.rvcon);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        ArrayList<CardShift> arr = new ArrayList<>();
        CustomAdapter customAdapter = new CustomAdapter(arr);
        recyclerView.setAdapter(customAdapter); // Set the adapter here

        //CustomAdapter customAdapter = new CustomAdapter(arr);
        Button reportBtn = view.findViewById(R.id.ReportBtn);

        reportBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                String yearSelected = ((Spinner) view.findViewById(R.id.YearSpinnerView)).getSelectedItem().toString();
                String monthSelected = String.valueOf(((Spinner) view.findViewById(R.id.MonthSpinnerView)).getSelectedItemPosition() + 1); // Convert month name to 1-based index
                arr.clear();
                customAdapter.notifyDataSetChanged();

                DatabaseReference monthRef = FirebaseDatabase.getInstance()
                        .getReference("Root")
                        .child("Calendar")
                        .child(yearSelected)
                        .child(monthSelected);

                monthRef.addListenerForSingleValueEvent(new ValueEventListener(){
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //arr.clear(); // Clear the array to avoid duplicate data

                        // Iterate through all days in the month
                        for (DataSnapshot daySnapshot : snapshot.getChildren()) {
                            // Check if the user's name exists under this day
                            DataSnapshot userSnapshot = daySnapshot.child(fullName);
                            if (userSnapshot.exists()) {
                                // Retrieve startTime and endTime
                                String startTime = userSnapshot.child("startTime").getValue(String.class);
                                String endTime = userSnapshot.child("endTime").getValue(String.class);

                                if (startTime != null && endTime != null) {
                                    // Calculate total hours worked
                                    int totalHours = calculateHoursWorked(startTime, endTime);

                                    // Get the date and day of the week
                                    String dayKey = daySnapshot.getKey(); // The day of the month
                                    String date = dayKey + "/" + monthSelected;
                                    String dayOfWeek = getDayOfWeek(yearSelected, monthSelected, dayKey);

                                    // Create a CardShift object
                                    CardShift shift = new CardShift(startTime, endTime,date, String.valueOf(totalHours),dayOfWeek);

                                    // Add to the array
                                    arr.add(shift);
                                }
                            }
                        }

                        // Debug: Log the results
                        Log.d("RecyclerView", "Data size: " + arr.size());

                        // Notify the adapter that the data has changed
                        customAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseError", "Failed to fetch data: " + error.getMessage());
                    }
                });
            }
        });
        return view;

    }
    private int calculateHoursWorked(String startTime, String endTime) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
        try {
            Date start = timeFormat.parse(startTime);
            Date end = timeFormat.parse(endTime);

            if (start != null && end != null) {
                long differenceInMillis = end.getTime() - start.getTime();
                return (int) (differenceInMillis / (1000 * 60 * 60)); // Convert to hours
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0; // Return 0 if parsing fails
    }

    private String getDayOfWeek(String year, String month, String day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(year));
        calendar.set(Calendar.MONTH, Integer.parseInt(month) - 1); // Month is 0-based
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

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






}