package com.example.richellerazon.nytsearch.activities;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.example.richellerazon.nytsearch.R;

import java.util.ArrayList;

public class FilterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    String beginDate;
    String sortOrder;
    ArrayList<String> newsDesks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        beginDate = getIntent().getStringExtra("beginDate");
        sortOrder = getIntent().getStringExtra("sortOrder");
        newsDesks = getIntent().getStringArrayListExtra("newsDesks");

        Spinner spSortOrder = (Spinner) findViewById(R.id.spSortOrder);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_order_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSortOrder.setAdapter(adapter);

        if(!sortOrder.equals(null)) {
            int spinnerPosition = adapter.getPosition(sortOrder);
            spSortOrder.setSelection(spinnerPosition);
        }
    }

    public void showDatePickerDialog(View view) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        sortOrder = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        sortOrder = "newest";
    }

    public void onSaveFilters(View view) {
        beginDate = findViewById(R.id.tvDate).toString();
        newsDesks.clear();
        CheckBox cbArts = (CheckBox) findViewById(R.id.cbArt);
        CheckBox cbCars = (CheckBox) findViewById(R.id.cbCars);
        CheckBox cbDining = (CheckBox) findViewById(R.id.cbDining);
        CheckBox cbSports = (CheckBox) findViewById(R.id.cbSports);
        if (cbArts.isChecked()) newsDesks.add("Arts");
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
