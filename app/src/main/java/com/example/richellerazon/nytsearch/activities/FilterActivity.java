package com.example.richellerazon.nytsearch.activities;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.richellerazon.nytsearch.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class FilterActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    String beginDate;
    String sortOrder;
    ArrayList<String> newsDesks;
    TextView tvDate;
    Spinner spSortOrder;
    CheckBox cbArt;
    CheckBox cbCars;
    CheckBox cbDining;
    CheckBox cbSports;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        beginDate = getIntent().getStringExtra("beginDate");
        sortOrder = getIntent().getStringExtra("sortOrder");
        newsDesks = getIntent().getStringArrayListExtra("newsDesks");

        tvDate = (TextView) findViewById(R.id.tvDate);
        spSortOrder = (Spinner) findViewById(R.id.spSortOrder);
        cbArt = (CheckBox) findViewById(R.id.cbArt);
        cbCars = (CheckBox) findViewById(R.id.cbCars);
        cbDining = (CheckBox) findViewById(R.id.cbDining);
        cbSports = (CheckBox) findViewById(R.id.cbSports);

        // Set up date picker
        TextView tvDate = (TextView) findViewById(R.id.tvDate);
        if (beginDate.isEmpty()) {
            final Calendar c = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
            beginDate = format.format(c.getTime());
        }
        if (tvDate != null) {
            tvDate.setText(beginDate);
        }

        // Set up spinner
        Spinner spSortOrder = (Spinner) findViewById(R.id.spSortOrder);
        if (spSortOrder != null) {
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.sort_order_array, android.R.layout.simple_spinner_item);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spSortOrder.setAdapter(adapter);
            spSortOrder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    sortOrder = parent.getItemAtPosition(position).toString();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    sortOrder = "newest";
                }
            });

            if(!sortOrder.isEmpty()) {
                int spinnerPosition = adapter.getPosition(sortOrder);
                spSortOrder.setSelection(spinnerPosition);
            }
        }

        CheckBox cb;
        for (int i = 0; i < newsDesks.size(); i++) {
            switch (newsDesks.get(i)) {
                case "Arts":
                    cbArt.setChecked(true);
                    break;
                case "Cars":
                    cbCars.setChecked(true);
                    break;
                case "Dining":
                    cbDining.setChecked(true);
                    break;
                case "Sports":
                    cbSports.setChecked(true);
                    break;
            }

        }
    }

    public void showDatePickerDialog(View view) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Log.d("DEBUG", "onDateSet: year " + year + " month " + monthOfYear + " day " + dayOfMonth);

        final Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, monthOfYear);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
        beginDate = format.format(c.getTime());

        TextView tvDate = (TextView) findViewById(R.id.tvDate);
        if (tvDate != null) {
            tvDate.setText(beginDate);
        }
    }

    public void onSaveFilters(View view) {
        newsDesks.clear();
        if (cbArt.isChecked()) newsDesks.add("Arts");
        if (cbCars.isChecked()) newsDesks.add("Cars");
        if (cbDining.isChecked()) newsDesks.add("Dining");
        if (cbSports.isChecked()) newsDesks.add("Sports");

        Intent i = new Intent();
        i.putExtra("beginDate", beginDate);
        i.putExtra("sortOrder", sortOrder);
        i.putStringArrayListExtra("newsDesks", newsDesks);
        setResult(RESULT_OK, i);
        finish();
    }
}
