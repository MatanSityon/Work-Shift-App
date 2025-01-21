package com.example.workshiftapp.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshiftapp.R;
import com.example.workshiftapp.activities.MainActivity;
import com.example.workshiftapp.adapters.ShiftAdapter;
import com.example.workshiftapp.models.CardShift;
import com.example.workshiftapp.models.PayslipGenerator;
import com.google.android.material.snackbar.Snackbar;
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
import java.util.Locale;

public class ReportScreen extends Fragment {
    private static final int REQUEST_WRITE_STORAGE = 112;
    private MainActivity mainActivity;
    private String fullName;
    private double wage;
    private double totalSalary;
    private String calendarID;
    private double totalMonthHours;
    private String selectedMonth;
    private String selectedYear;
    private Spinner monthSpinner;
    private Spinner yearSpinner;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report_screen, container, false);
        // Initialize main activity and user info
        mainActivity = (MainActivity) getActivity();
        fullName = mainActivity.getFullName();
        wage = mainActivity.getWage();
        calendarID = mainActivity.getCalendarID();
        // Spinners for selecting month and year
        monthSpinner = view.findViewById(R.id.MonthSpinnerView);
        yearSpinner = view.findViewById(R.id.YearSpinnerView);
        setupSpinners(monthSpinner, yearSpinner);
        // RecyclerView setup
        RecyclerView recyclerView = view.findViewById(R.id.rvcon);
        TextView salaryTextView = view.findViewById(R.id.TotalSalaryTextView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        ArrayList<CardShift> shifts = new ArrayList<>();
        ShiftAdapter shiftAdapter = new ShiftAdapter(shifts);
        recyclerView.setAdapter(shiftAdapter);
        // Buttons
        Button reportBtn = view.findViewById(R.id.ReportBtn);
        Button downloadBtn = view.findViewById(R.id.DownloadBtn);
        downloadBtn.setVisibility(View.GONE); // Initially hide the Download button
        // Generate Report Button
        reportBtn.setOnClickListener(v -> {
            selectedYear = yearSpinner.getSelectedItem().toString();
            selectedMonth = String.valueOf(monthSpinner.getSelectedItemPosition() + 1);
            shifts.clear();
            shiftAdapter.notifyDataSetChanged();
            fetchShifts(selectedYear, selectedMonth, shifts, shiftAdapter, salaryTextView, downloadBtn);
        });
        // Download Report Button
        downloadBtn.setOnClickListener(v -> {
            selectedMonth = monthSpinner.getSelectedItem().toString();
            selectedYear = yearSpinner.getSelectedItem().toString();

            if (shouldRequestStoragePermission()) {
                requestStoragePermission();
            } else {
                generateAndDownloadPDF(selectedMonth, selectedYear);
            }
        });
        return view;
    }
    private boolean shouldRequestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // For Android 11 (R) and above
            return !Environment.isExternalStorageManager();
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // For Android 6.0 (M) to Android 10
            return ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }
    private void requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // For Android 11 and above
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // For Android 6.0 to Android 10
            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE
            );
        }
    }
    private void setupSpinners(Spinner monthSpinner, Spinner yearSpinner) {
        String[] months = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
        String[] years = {"2020", "2021", "2022", "2023", "2024", "2025"};
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);
        // Set current month and year as default
        Calendar calendar = Calendar.getInstance();
        monthSpinner.setSelection(calendar.get(Calendar.MONTH));
        yearSpinner.setSelection(yearAdapter.getPosition(String.valueOf(calendar.get(Calendar.YEAR))));
    }
    private void fetchShifts(String year, String month, ArrayList<CardShift> shifts, ShiftAdapter shiftAdapter,
                             TextView salaryTextView, Button downloadBtn) {
        DatabaseReference monthRef = FirebaseDatabase.getInstance()
                .getReference("Root")
                .child(calendarID)
                .child("Calendar")
                .child(year)
                .child(month);

        monthRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalMonthHours = 0;
                totalSalary = 0;
                for (DataSnapshot daySnapshot : snapshot.getChildren()) {
                    DataSnapshot userSnapshot = daySnapshot.child(fullName);
                    if (userSnapshot.exists()) {
                        String startTime = userSnapshot.child("startTime").getValue(String.class);
                        String endTime = userSnapshot.child("endTime").getValue(String.class);
                        if (startTime != null && endTime != null) {
                            double totalHours = calculateHoursWorked(startTime, endTime);
                            totalMonthHours += totalHours;
                            String dayKey = daySnapshot.getKey();
                            String date = dayKey + "/" + month;
                            String dayOfWeek = getDayOfWeek(year, month, dayKey);
                            CardShift shift = new CardShift(startTime, endTime, date, String.valueOf(totalHours), dayOfWeek);
                            shifts.add(shift);
                        }
                    }
                }
                // Update RecyclerView and total salary
                shiftAdapter.notifyDataSetChanged();
                totalSalary = Math.round(totalMonthHours * wage * 100.0) / 100.0;
                salaryTextView.setText("Total salary: " + totalSalary + "₪");
                // Show the Download button after fetching shifts
                downloadBtn.setVisibility(View.VISIBLE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Failed to fetch data: " + error.getMessage());
            }
        });
    }
    private void generateAndDownloadPDF(String month, String year) {
        try {
            PayslipGenerator.generatePayslip(
                    requireContext(),
                    "ACME Corporation", // Replace with actual company name
                    "123 Business Ave, Suite 100, Business City, ST 12345", // Replace with actual company address
                    fullName, // Employee name
                    "IT", // Employee position or department
                    month + year, // Pay period
                    "12345", // Check number
                    totalMonthHours, // Regular hours worked
                    0.00, // Overtime hours worked
                    wage, // Regular hourly rate
                    wage * 1.5, // Overtime hourly rate
                    totalMonthHours * wage, // Gross pay
                    230.40, // Federal tax
                    76.80, // State tax
                    95.23, // Social security
                    22.27, // Medicare
                    76.80, // Retirement contributions (401k)
                    totalSalary // Net pay
            );
            Snackbar snackbar = Snackbar.make(requireView(), "PDF generated successfully!", Snackbar.LENGTH_LONG);
            snackbar.setBackgroundTint(Color.parseColor("#FFFFFF"));
            snackbar.setTextColor(Color.BLACK);
            snackbar.setAction("Dismiss", x -> {
            });
            snackbar.show();
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar snackbar = Snackbar.make(requireView(), "Error generating PDF: " + e.getMessage(), Snackbar.LENGTH_LONG);
            snackbar.setBackgroundTint(Color.parseColor("#FFFFFF"));
            snackbar.setTextColor(Color.RED);
            snackbar.setAction("Dismiss", x -> {
            });
            snackbar.show();
        }
    }
    private double calculateHoursWorked(String startTime, String endTime) {
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
            Date start = timeFormat.parse(startTime);
            Date end = timeFormat.parse(endTime);
            if (start != null && end != null) {
                if (end.before(start)) {
                    end = new Date(end.getTime() + 24 * 60 * 60 * 1000); // Add 24 hours for overnight shifts
                }
                long differenceInMillis = end.getTime() - start.getTime();
                return Math.round((differenceInMillis / (1000.0 * 60 * 60)) * 100.0) / 100.0;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
    private String getDayOfWeek(String year, String month, String day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(year));
        calendar.set(Calendar.MONTH, Integer.parseInt(month) - 1); // Month is 0-based
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar snackbar = Snackbar.make(requireView(), "Permission granted. Downloading PDF...", Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(Color.parseColor("#FFFFFF"));
                snackbar.setTextColor(Color.BLACK);
                snackbar.setAction("Dismiss", x -> {
                });
                snackbar.show();
                // Actually generate and download the PDF after permission is granted
                generateAndDownloadPDF(selectedMonth, selectedYear);
            } else {
                Snackbar snackbar = Snackbar.make(requireView(), "Storage permission is required to download the PDF.", Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(Color.parseColor("#FFFFFF"));
                snackbar.setTextColor(Color.RED);
                snackbar.setAction("Dismiss", x -> {
                });
                snackbar.show();
            }
        }
    }
}