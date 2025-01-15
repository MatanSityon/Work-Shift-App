package com.example.workshiftapp.fragments;

import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.workshiftapp.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TimeSelectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TimeSelectFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TimeSelectFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TimeSelectFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TimeSelectFragment newInstance(String param1, String param2) {
        TimeSelectFragment fragment = new TimeSelectFragment();
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
        View view = inflater.inflate(R.layout.fragment_time_select, container, false);
        Button start = view.findViewById(R.id.startTime);
        Button end = view.findViewById(R.id.endTime);
        setupTimePicker(start, 8, 0);
        setupTimePicker(end, 16, 0);


        return view;
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
}