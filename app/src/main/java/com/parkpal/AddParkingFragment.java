package com.parkpal;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;


public class AddParkingFragment extends Fragment {
    private EditText field_address;
    private EditText field_timeTo;
    private EditText field_timeFrom;
    private EditText field_initialAmt;
    private EditText field_consecAmt;
    Calendar calendar;
    int currentHour;
    int currentMinute;
    private Button btn_address;
    FragmentManager manager;
    Fragment fragment;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_addparking, null);

    }
    @Override
    public void onViewCreated(@NonNull View myView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(myView, savedInstanceState);

        field_initialAmt = (EditText) myView.findViewById(R.id.field_initialAmt);
        field_consecAmt = (EditText) myView.findViewById(R.id.field_consectAmt);
        field_timeTo = (EditText) myView.findViewById(R.id.field_timeTo);
        field_timeFrom = (EditText) myView.findViewById(R.id.field_timeFrom);
        btn_address = (Button) myView.findViewById(R.id.button_submit_address);
        field_address = (EditText) myView.findViewById(R.id.field_address);
        btn_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(field_address.getText().toString()!=null && field_timeTo.getText().toString()!=null&&
                field_timeTo.getText().toString()!=null && field_initialAmt.getText().toString()!=null &&
                        field_consecAmt.getText().toString() !=null){
                    AddParkingLocationFragment nextFrag= new AddParkingLocationFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("address", field_address.getText().toString());
                    bundle.putString("timeTo", field_timeTo.getText().toString());
                    bundle.putString("timeFrom", field_timeTo.getText().toString());
                    bundle.putString("initialAmt", field_initialAmt.getText().toString());
                    bundle.putString("consecAmt", field_consecAmt.getText().toString());
                    nextFrag.setArguments(bundle);
                    hideKeyboard(getActivity());
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainContent, nextFrag).addToBackStack(null)
                            .commit();

                }
                else{
                    Toast.makeText(getActivity(), "Fill all the required fields", Toast.LENGTH_LONG).show();

                }

            }
        });

        field_timeTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar = Calendar.getInstance();
                currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                currentMinute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {

                        String amPm;
                        if (hourOfDay >= 12) {
                            amPm = "PM";
                        } else {
                            amPm = "AM";
                        }

                        field_timeTo.setText(String.format("%02d:%02d", hourOfDay, minutes) + amPm);
                    }
                }, currentHour, currentMinute, false);
                timePickerDialog.show();
            }
        });
        field_timeFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar = Calendar.getInstance();
                currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                currentMinute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {

                        String amPm;
                        if (hourOfDay >= 12) {
                            amPm = "PM";
                        } else {
                            amPm = "AM";
                        }

                        field_timeTo.setText(String.format("%02d:%02d", hourOfDay, minutes) + amPm);
                    }
                }, currentHour, currentMinute, false);
                timePickerDialog.show();
            }
        });



    }

    public static void hideKeyboard(Context ctx) {
        InputMethodManager inputManager = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
