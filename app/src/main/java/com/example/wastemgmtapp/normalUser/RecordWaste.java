package com.example.wastemgmtapp.normalUser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.wastemgmtapp.R;

public class RecordWaste extends AppCompatActivity {

    private final String[] wasteTypes = { "Select Trash Type","Plastics", "Bio-degradable", "Recyclables", "Other Waste"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_waste);

        //initialize the toolbar
        Toolbar toolbar = findViewById(R.id.recToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //show the back button on the toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Spinner wasteSpinner = findViewById(R.id.wasteSpinner);
        //add the list to the dropdown item in the dialog view
        ArrayAdapter<String> adapter = new ArrayAdapter<>(RecordWaste.this, R.layout.spinner_item, wasteTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wasteSpinner.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}