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
import com.example.wastemgmtapp.CreateIllegalDumpingMutation;
import com.example.wastemgmtapp.CreateSortedWasteMutation;
import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.WasteInstitutionsQuery;
import com.example.wastemgmtapp.type.IllegalDumpingInput;
import com.example.wastemgmtapp.type.SortedWasteInput;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class ReportDumping extends AppCompatActivity {

    private final String[] zones = { "Select Area Zone","Kanjedza", "Chinyonga", "Blantyre CBD", "Mbayani"};

    ArrayList<String> companyNames = new ArrayList<>();
    ArrayList<String> companyIDs = new ArrayList<>();
    Button reportBtn;
    EditText inputComment, inputLocation;
    String selectedID;
    ProgressBar btn_loading, fetchLoading;
    ApolloClient apolloClient;
    Spinner areaSpinner;
    String TAG = ReportDumping.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_dumping);
        fetchLoading = findViewById(R.id.comLoads);
        btn_loading = findViewById(R.id.loads);
        inputComment = findViewById(R.id.inputReportComment);
        inputLocation = findViewById(R.id.inputLocation);
        reportBtn = findViewById(R.id.btn_report);

        fetchLoading.setVisibility(View.VISIBLE);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        apolloClient = ApolloClient.builder().okHttpClient(httpClient)
                .serverUrl("https://waste-mgmt-api.herokuapp.com/graphql")
                .build();

        companyNames.add("Select Trash Collection Company");
        Intent intent = getIntent();
        String userID = intent.getStringExtra("id"); //get the productID from the intent
        double latitude = intent.getDoubleExtra("lat", -1);
        double longitude  = intent.getDoubleExtra("long", -1);

        apolloClient.query(new WasteInstitutionsQuery()).enqueue(wasteCallBack());

        //initialize the toolbar
        Toolbar toolbar = findViewById(R.id.repToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //show the back button on the toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        areaSpinner = findViewById(R.id.areaSpinner);
        //add the list to the dropdown item in the dialog view
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ReportDumping.this, R.layout.spinner_item, companyNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        areaSpinner.setAdapter(adapter);
        areaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
                parent.setSelection(0);
            }
        });

        reportBtn.setOnClickListener(view -> {
            if(!validate()){
                Toast.makeText(ReportDumping.this, "Fields cannot be left empty", Toast.LENGTH_LONG)
                        .show();
            } else {
                btn_loading.setVisibility(View.VISIBLE);

                String comment = inputComment.getText().toString();
                String location = inputLocation.getText().toString();

                IllegalDumpingInput dumpingInput = IllegalDumpingInput.builder()
                        .creator(userID)
                        .institution(selectedID)
                        .latitude(latitude).longitude(longitude)
                        .location(location)
                        .build();
                Input<IllegalDumpingInput> input = new Input<>(dumpingInput, true);

                apolloClient.mutate(new CreateIllegalDumpingMutation(input)).enqueue(reportCallback());
            }
        });
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
                            Toast.makeText(ReportDumping.this,
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
                        Toast.makeText(ReportDumping.this,
                                "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();
                        fetchLoading.setVisibility(View.GONE);
                    });
                }

            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e("Apollo", "Error", e);
                Toast.makeText(ReportDumping.this,
                        "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
                fetchLoading.setVisibility(View.GONE);
            }
        };
    }

    public ApolloCall.Callback<CreateIllegalDumpingMutation.Data> reportCallback(){
        return new ApolloCall.Callback<CreateIllegalDumpingMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<CreateIllegalDumpingMutation.Data> response) {
                CreateIllegalDumpingMutation.Data data = response.getData();

                if(response.getErrors() == null){

                    if(data.createIllegalDumping() == null){
                        Log.e("Apollo", "an Error occurred : " );
                        runOnUiThread(() -> {
                            // Stuff that updates the UI
                            Toast.makeText(ReportDumping.this,
                                    "an Error occurred : " , Toast.LENGTH_LONG).show();
                            btn_loading.setVisibility(View.GONE);
                            //errorText.setText();
                        });
                    }else{
                        runOnUiThread(() -> {
                            Log.d(TAG, "onResponse: " + data.createIllegalDumping()._id());
                            inputComment.setText("");
                            inputLocation.setText("");
                            areaSpinner.setSelection(0);

                            Toast.makeText(ReportDumping.this,
                                    "Report sent successfully", Toast.LENGTH_LONG).show();
                            btn_loading.setVisibility(View.GONE);
                        });
                    }

                } else{
                    List<Error> error = response.getErrors();
                    String errorMessage = error.get(0).getMessage();
                    Log.e("Apollo", "an Error occurred : " + errorMessage );
                    runOnUiThread(() -> {
                        Toast.makeText(ReportDumping.this,
                                "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();
                        btn_loading.setVisibility(View.GONE);
                    });
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e("Apollo", "Error", e);
                Toast.makeText(ReportDumping.this,
                        "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public Boolean validate(){
        boolean valid = true;
        String comment = inputComment.getText().toString();
        String location = inputLocation.getText().toString();

        if(TextUtils.isEmpty(comment)){
            inputComment.setError("Required!");
            valid = false;
        }

        if(TextUtils.isEmpty(location)){
            inputLocation.setError("Required!");
            valid = false;
        }

        if(TextUtils.isEmpty(selectedID)){
            Toast.makeText(ReportDumping.this,  "No company was selected. " +
                    "Perhaps reload your app and try again", Toast.LENGTH_LONG).show();
            valid = false;
        }

        return valid;
    }
}