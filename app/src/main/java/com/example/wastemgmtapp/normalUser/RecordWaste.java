package com.example.wastemgmtapp.normalUser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.wastemgmtapp.CreateSortedWasteMutation;
import com.example.wastemgmtapp.CreateTrashCollectionMutation;
import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.WasteInstitutionsQuery;
import com.example.wastemgmtapp.type.SortedWasteInput;
import com.example.wastemgmtapp.type.TrashCollectionInput;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class RecordWaste extends AppCompatActivity {

    private final String[] wasteTypes = { "Select Trash Type",
            "Plastic Bottles", "Rubber Waste", "Glass Waste","Thin Plastics", "Recyclable Cans", "Other Waste"};

    ArrayList<String> companyNames = new ArrayList<>();
    ArrayList<String> companyIDs = new ArrayList<>();
    ApolloClient apolloClient;
    String selectedID;
    String selectedWasteType;
    Spinner companySpinner, wasteSpinner;
    String TAG = RecordWaste.class.getSimpleName();
    ProgressBar fetchLoading, sendLoading;
    EditText inputTrashAmount, inputOther, inputLocation;
    boolean other = false;
    Button recordWaste;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_waste);

        companySpinner = findViewById(R.id.companySpinner);
        fetchLoading = findViewById(R.id.comLoads);
        inputTrashAmount = findViewById(R.id.inputTrashAmount);
        inputLocation = findViewById(R.id.input_loc);
        inputOther = findViewById(R.id.inputOther);
        recordWaste = findViewById(R.id.btn_record);
        sendLoading = findViewById(R.id.recLoads);

        //initialize the toolbar
        Toolbar toolbar = findViewById(R.id.recToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //show the back button on the toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        companyNames.add("Select Trash Collection Company");
        Intent intent = getIntent();
        String userID = intent.getStringExtra("id"); //get the productID from the intent
        double latitude = intent.getDoubleExtra("lat", -1);
        double longitude  = intent.getDoubleExtra("long", -1);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        apolloClient = ApolloClient.builder().okHttpClient(httpClient)
                .serverUrl("https://waste-mgmt-api.herokuapp.com/graphql")
                .build();

        apolloClient.query(new WasteInstitutionsQuery()).enqueue(wasteCallBack());

        wasteSpinner = findViewById(R.id.wasteSpinner);
        //add the list to the dropdown item in the dialog view
        ArrayAdapter<String> adapter = new ArrayAdapter<>(RecordWaste.this, R.layout.spinner_item, wasteTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wasteSpinner.setAdapter(adapter);
        wasteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0){
                    selectedWasteType =  parent.getSelectedItem().toString();
                    if(parent.getSelectedItem().toString().equals("Other Waste")){
                        other = true;
                        inputOther.setVisibility(View.VISIBLE);
                    } else {
                        other = false;
                        inputOther.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(RecordWaste.this, R.layout.spinner_item, companyNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        companySpinner.setAdapter(spinnerAdapter);
        companySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: " + parent.getSelectedItem());
                Log.d(TAG, "index: " + position);

                if(position != 0){
                    Log.d(TAG, "index twice: " + position);
                    selectedID = companyIDs.get(position - 1);
                    Log.d(TAG, "id: " + selectedID);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        recordWaste.setOnClickListener(view -> {
            if(!validate()){
                Toast.makeText(RecordWaste.this, "Fields cannot be left empty", Toast.LENGTH_LONG)
                        .show();
            } else {
                sendLoading.setVisibility(View.VISIBLE);
                if(other){
                    selectedWasteType = inputOther.getText().toString();
                }

                String amount = inputTrashAmount.getText().toString();
                String location = inputLocation.getText().toString();

                SortedWasteInput wasteInput = SortedWasteInput.builder()
                        .amount(amount).institution(selectedID)
                        .location(location).typeOfWaste(selectedWasteType)
                        .latitude(latitude).longitude(longitude).creator(userID)
                        .build();
                Input<SortedWasteInput> input = new Input<>(wasteInput, true);

                apolloClient.mutate(new CreateSortedWasteMutation(input)).enqueue(recordCallBack());
            }
        });

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
                            Toast.makeText(RecordWaste.this,
                                    "an Error occurred : " , Toast.LENGTH_LONG).show();
                            //errorText.setText();
                            fetchLoading.setVisibility(View.GONE);
                        });
                    }else{
                        runOnUiThread(() -> {
                            Log.d(TAG, "institutions fetched" + data.wasteInstitutions());

                            fetchLoading.setVisibility(View.GONE);
                            for(int i = 0; i < data.wasteInstitutions().size(); i++){
                                //Institution company = new Institution(data.wasteInstitutions().get(i).name(),
                                //data.wasteInstitutions().get(i)._id());
                                companyNames.add(data.wasteInstitutions().get(i).name());
                                companyIDs.add(data.wasteInstitutions().get(i)._id());
                                //companyIDs.add(company);
                            }
                        });
                    }

                } else{
                    List<Error> error = response.getErrors();
                    String errorMessage = error.get(0).getMessage();
                    Log.e("Apollo", "an Error occurred : " + errorMessage );
                    runOnUiThread(() -> {
                        Toast.makeText(RecordWaste.this,
                                "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();
                        fetchLoading.setVisibility(View.GONE);
                    });
                }

            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e("Apollo", "Error", e);
                Toast.makeText(RecordWaste.this,
                        "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
                fetchLoading.setVisibility(View.GONE);
            }
        };
    }


    public ApolloCall.Callback<CreateSortedWasteMutation.Data> recordCallBack(){
        return new ApolloCall.Callback<CreateSortedWasteMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<CreateSortedWasteMutation.Data> response) {
                CreateSortedWasteMutation.Data data = response.getData();

                if(response.getErrors() == null){

                    if(data.createSortedWaste() == null){
                        Log.e("Apollo", "an Error occurred : " );
                        runOnUiThread(() -> {
                            // Stuff that updates the UI
                            Toast.makeText(RecordWaste.this,
                                    "an Error occurred : " , Toast.LENGTH_LONG).show();
                            sendLoading.setVisibility(View.GONE);
                            //errorText.setText();
                        });
                    }else{
                        runOnUiThread(() -> {
                            Log.d(TAG, "onResponse: " + data.createSortedWaste()._id());
                            inputTrashAmount.setText(""); inputOther.setText("");
                            inputLocation.setText("");
                            companySpinner.setSelection(0);
                            wasteSpinner.setSelection(0);

                            Toast.makeText(RecordWaste.this,
                                    "Record sent successfully", Toast.LENGTH_LONG).show();
                            sendLoading.setVisibility(View.GONE);
                        });
                    }

                } else{
                    List<Error> error = response.getErrors();
                    String errorMessage = error.get(0).getMessage();
                    Log.e("Apollo", "an Error occurred : " + errorMessage );
                    runOnUiThread(() -> {
                        Toast.makeText(RecordWaste.this,
                                "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();
                        sendLoading.setVisibility(View.GONE);
                    });
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e("Apollo", "Error", e);
                Toast.makeText(RecordWaste.this,
                        "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
    }


    public Boolean validate(){
        boolean valid = true;
        String amount = inputTrashAmount.getText().toString();
        String location = inputLocation.getText().toString();
        String otherWaste = inputOther.getText().toString();

        if(TextUtils.isEmpty(amount)){
            inputTrashAmount.setError("Required!");
            valid = false;
        }
        if(TextUtils.isEmpty(location)){
            inputLocation.setError("Required!");
            valid = false;
        }

        if(TextUtils.isEmpty(selectedID)){
            Toast.makeText(RecordWaste.this,  "No company was selected. " +
                    "Perhaps reload your app and try again", Toast.LENGTH_LONG).show();
            valid = false;
        }

        if(TextUtils.isEmpty(selectedWasteType)){
            Toast.makeText(RecordWaste.this,  "No company was selected. " +
                    "Perhaps reload your app and try again", Toast.LENGTH_LONG).show();
            valid = false;
        }

        if(other){
            if(TextUtils.isEmpty(otherWaste)){
                inputOther.setError("Required!");
                valid = false;
            }
        }

        return valid;
    }
}