package com.example.wastemgmtapp.Staff;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.adapters.RequestsRecyclerAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class CollectionRequests extends AppCompatActivity {

    private final ArrayList<String> sample = new ArrayList<>(Arrays.asList("one", "two", "three", "four", "five"));
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_requests);

        //initialize the toolbar
        Toolbar toolbar = findViewById(R.id.request_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //show the back button on the toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        RecyclerView requestsRecyclerview = findViewById(R.id.requests_recyclerview);
        RequestsRecyclerAdapter recyclerAdapter = new RequestsRecyclerAdapter(CollectionRequests.this, sample);

        requestsRecyclerview.setAdapter(recyclerAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}