package com.example.wastemgmtapp.normalUser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.UserQuery;
import com.example.wastemgmtapp.WasteInstitutionsQuery;
import com.example.wastemgmtapp.adapters.CustomArrayAdapter;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RequestCollection extends AppCompatActivity {

    private final String[] companies = { "Select Trash Collection Company","City Council", "Zipatso", "Cleanex", "Mr. Muscle"};
    ArrayList<String> companyNames = new ArrayList<>();
    ArrayList<String> companyIDs = new ArrayList<>();
    String TAG = RequestCollection.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_collection);

        //initialize the toolbar
        Toolbar toolbar = findViewById(R.id.reqToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //show the back button on the toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        String userID = intent.getStringExtra("id"); //get the productID from the intent
        double latitude = intent.getDoubleExtra("lat", -1);
        double longitude  = intent.getDoubleExtra("long", -1);

        Log.d(TAG, "onCreate: " + userID+"-"+latitude+"-"+longitude);

        Spinner companySpinner = findViewById(R.id.companySpinner);
        //add the list to the dropdown item in the dialog view
        ArrayAdapter<String> adapter = new ArrayAdapter<>(RequestCollection.this, R.layout.spinner_item, companies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        companySpinner.setAdapter(adapter);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public ApolloCall.Callback<WasteInstitutionsQuery.Data> wasteCallBack(){
        return new ApolloCall.Callback<WasteInstitutionsQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<WasteInstitutionsQuery.Data> response) {
                WasteInstitutionsQuery.Data data = response.getData();

                if(response.getErrors() == null){

                    if(data.wasteInstitutions() == null){
                        Log.e("Apollo", "an Error occurred : " );
                        runOnUiThread(() -> {
                            // Stuff that updates the UI
                            Toast.makeText(RequestCollection.this,
                                    "an Error occurred : " , Toast.LENGTH_LONG).show();
                            //errorText.setText();
                        });
                    }else{
                        runOnUiThread(() -> {
                            Log.d(TAG, "institutions fetched" + data.wasteInstitutions());
                            HashMap<String, String> tempMap = new HashMap<>();

                            for(int i = 0; i < data.wasteInstitutions().size(); i++){
                                tempMap.put("name", data.wasteInstitutions().get(i).name());
                                tempMap.put("id", data.wasteInstitutions().get(i)._id());
                                //companyList.add(tempMap);
                                companyNames.add(data.wasteInstitutions().get(i).name());
                                companyNames.add(data.wasteInstitutions().get(i)._id());
                            }

                        });
                    }

                } else{
                    List<Error> error = response.getErrors();
                    String errorMessage = error.get(0).getMessage();
                    Log.e("Apollo", "an Error occurred : " + errorMessage );
                    runOnUiThread(() -> {
                        Toast.makeText(RequestCollection.this,
                                "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();

                    });
                }

            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e("Apollo", "Error", e);
                Toast.makeText(RequestCollection.this,
                        "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
    }
}