package com.example.wastemgmtapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;

public class ZoneTrashcans extends AppCompatActivity {

    private final ArrayList<String> sample = new ArrayList<>(Arrays.asList("one", "two", "three", "four", "five"));
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zone_trashcans);

        //initialize the toolbar
        Toolbar toolbar = findViewById(R.id.zone_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //show the back button on the toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        RecyclerView trashRecyclerview = findViewById(R.id.trash_recyclerview);
        TrashRecyclerAdapter recyclerAdapter = new TrashRecyclerAdapter(ZoneTrashcans.this, sample);

        trashRecyclerview.setAdapter(recyclerAdapter);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}