package com.example.wastemgmtapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class ReviewArea extends AppCompatActivity {

    final String[] zones = { "Select Area Zone","Kanjedza", "Chinyonga", "Blantyre CBD", "Mbayani"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_area);

        //initialize the toolbar
        Toolbar toolbar = findViewById(R.id.recToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //show the back button on the toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        Spinner areaSpinner = findViewById(R.id.areaSpinner);
        //add the list to the dropdown item in the dialog view
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ReviewArea.this, R.layout.spinner_item, zones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        areaSpinner.setAdapter(adapter);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}