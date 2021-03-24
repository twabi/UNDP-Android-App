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
import com.example.wastemgmtapp.CreateTrashCollectionMutation;
import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.WasteInstitutionsQuery;
import com.example.wastemgmtapp.type.TrashCollectionInput;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import static com.example.wastemgmtapp.type.TrashCollectionInput.builder;

public class RequestCollection extends AppCompatActivity {

    private final String[] wasteTypes = { "Select Trash Type",
            "Plastic Bottles", "Rubber Waste", "Glass Waste","Thin Plastics", "Recyclable Cans", "Other Waste"};
    String selectedWasteType;

    ArrayList<String> companyNames = new ArrayList<>();
    ArrayList<String> companyIDs = new ArrayList<>();
    String TAG = RequestCollection.class.getSimpleName();
    ApolloClient apolloClient;
    String selectedID;
    EditText inputAmount, inputLocation, inputOther;
    Button buttonSend;
    ProgressBar loading, fetchLoading;
    Spinner companySpinner, wasteSpinner;
    boolean other = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_collection);

        buttonSend = findViewById(R.id.btn_request);
        inputAmount = findViewById(R.id.inputAmount);
        inputLocation = findViewById(R.id.input_loc);
        inputOther = findViewById(R.id.inputOther);
        loading = findViewById(R.id.loads);
        companySpinner = findViewById(R.id.companySpinner);
        Toolbar toolbar = findViewById(R.id.reqToolbar);
        fetchLoading = findViewById(R.id.loading);
        wasteSpinner = findViewById(R.id.wasteSpinner);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //show the back button on the toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        companyNames.add("Select Trash Collection Company");
        Intent intent = getIntent();
        String userID = intent.getStringExtra("id"); //get the productID from the intent
        double latitude = intent.getDoubleExtra("lat", -1);
        double longitude  = intent.getDoubleExtra("long", -1);

        Log.d(TAG, "onCreate: " + userID+"-"+latitude+"-"+longitude);

        fetchLoading.setVisibility(View.VISIBLE);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        apolloClient = ApolloClient.builder().okHttpClient(httpClient)
                .serverUrl("https://waste-mgmt-api.herokuapp.com/graphql")
                .build();

        apolloClient.query(new WasteInstitutionsQuery()).enqueue(wasteCallBack());

        //add the list to the dropdown item in the dialog view
        ArrayAdapter<String> adapter = new ArrayAdapter<>(RequestCollection.this, R.layout.spinner_item, companyNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        companySpinner.setAdapter(adapter);
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

        //add the list to the dropdown item in the dialog view
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(RequestCollection.this, R.layout.spinner_item, wasteTypes);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wasteSpinner.setAdapter(adapter2);
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

        buttonSend.setOnClickListener(view -> {
            if(!validate()){
                Toast.makeText(RequestCollection.this,"Fields Cannot be empty!!!", Toast.LENGTH_SHORT)
                        .show();
                String amount = inputAmount.getText().toString();
                String location = inputLocation.getText().toString();
                Log.d(TAG, "fields: " + amount + location + selectedID);
            } else {
                loading.setVisibility(View.VISIBLE);
                if(other){
                    selectedWasteType = inputOther.getText().toString();
                }
                String amount = inputAmount.getText().toString();
                String location = inputLocation.getText().toString();

                TrashCollectionInput trashCollectionInput = TrashCollectionInput.builder()
                        .amount(amount).creator(userID)
                        .typeOfWaste(selectedWasteType)
                        .institution(selectedID).location(location)
                        .latitude(latitude).longitude(longitude)
                        .build();

                Input<TrashCollectionInput> input = new Input<>(trashCollectionInput, true);

                apolloClient.mutate(new CreateTrashCollectionMutation(input))
                        .enqueue(requestCallBack());
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



                    if(data.wasteInstitutions() == null){

                        if(response.getErrors() == null){
                            Log.e("Apollo", "an Error occurred : " );
                            runOnUiThread(() -> {
                                // Stuff that updates the UI
                                Toast.makeText(RequestCollection.this,
                                        "an Error occurred : " , Toast.LENGTH_LONG).show();
                                //errorText.setText();
                                fetchLoading.setVisibility(View.GONE);
                            });

                        } else{
                            List<Error> error = response.getErrors();
                            String errorMessage = error.get(0).getMessage();
                            Log.e("Apollo", "an Error occurred : " + errorMessage );
                            runOnUiThread(() -> {
                                Toast.makeText(RequestCollection.this,
                                        "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();
                                fetchLoading.setVisibility(View.GONE);
                            });
                        }
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

                        if(response.getErrors() != null){
                            List<Error> error = response.getErrors();
                            String errorMessage = error.get(0).getMessage();
                            Log.e(TAG, "an Error in institutions query : " + errorMessage );
                            runOnUiThread(() -> {
                                Toast.makeText(RequestCollection.this,
                                        "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();

                            });
                        }
                    }

            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e("Apollo", "Error", e);
                Toast.makeText(RequestCollection.this,
                        "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
                fetchLoading.setVisibility(View.GONE);
            }
        };
    }

    public ApolloCall.Callback<CreateTrashCollectionMutation.Data> requestCallBack(){
        return new ApolloCall.Callback<CreateTrashCollectionMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<CreateTrashCollectionMutation.Data> response) {
                CreateTrashCollectionMutation.Data data = response.getData();

                if(response.getErrors() == null){

                    if(data.createTrashCollection() == null){
                        Log.e("Apollo", "an Error occurred : " );
                        runOnUiThread(() -> {
                            // Stuff that updates the UI
                            Toast.makeText(RequestCollection.this,
                                    "an Error occurred : " , Toast.LENGTH_LONG).show();
                            loading.setVisibility(View.GONE);
                            //errorText.setText();
                        });
                    }else{
                        runOnUiThread(() -> {
                            Log.d(TAG, "onResponse: " + data.createTrashCollection()._id());
                            inputAmount.setText("");
                            inputLocation.setText("");
                            companySpinner.setSelection(0);
                            Toast.makeText(RequestCollection.this,
                                    "Request sent successfully", Toast.LENGTH_LONG).show();
                            loading.setVisibility(View.GONE);
                        });
                    }

                } else{
                    List<Error> error = response.getErrors();
                    String errorMessage = error.get(0).getMessage();
                    Log.e("Apollo", "an Error occurred : " + errorMessage );
                    runOnUiThread(() -> {
                        Toast.makeText(RequestCollection.this,
                                "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();
                        loading.setVisibility(View.GONE);
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

    public Boolean validate(){
        boolean valid = true;
        String amount = inputAmount.getText().toString();
        String location = inputLocation.getText().toString();

        if(TextUtils.isEmpty(amount)){
            inputAmount.setError("Required!");
            valid = false;
        }
        if(TextUtils.isEmpty(location)){
            inputLocation.setError("Required!");
            valid = false;
        }

        if(TextUtils.isEmpty(selectedID)){
            Toast.makeText(RequestCollection.this,  "No company was selected. " +
                    "Perhaps reload your app and try again", Toast.LENGTH_LONG).show();
            valid = false;
        }

        return valid;
    }
}