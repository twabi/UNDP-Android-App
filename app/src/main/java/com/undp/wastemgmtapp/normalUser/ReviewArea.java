package com.undp.wastemgmtapp.normalUser;

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
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.undp.wastemgmtapp.CreateReviewMutation;
import com.undp.wastemgmtapp.R;
import com.undp.wastemgmtapp.WasteInstitutionsQuery;
import com.undp.wastemgmtapp.type.ReviewInput;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class ReviewArea extends AppCompatActivity {

    private final String[] zones = { "Select Area Zone","Kanjedza", "Chinyonga", "Blantyre CBD", "Mbayani"};
    EditText inputComment, inputLocation;
    Button reviewBtn;
    RatingBar ratingBar;
    ProgressBar progressBar, loadingCompanies;
    ArrayList<String> companyNames =  new ArrayList<>();
    ArrayList<String> companyIDs = new ArrayList<>();
    ApolloClient apolloClient;
    String selectedID;
    Spinner companySpinner;
    String TAG = ReviewArea.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_area);

        //initialize the toolbar
        Toolbar toolbar = findViewById(R.id.recToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //show the back button on the toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        inputComment = findViewById(R.id.inputComment);
        reviewBtn = findViewById(R.id.btn_review);
        ratingBar = findViewById(R.id.barRating);
        loadingCompanies = findViewById(R.id.comLoads);
        progressBar = findViewById(R.id.loads);
        companySpinner = findViewById(R.id.comSpinner);
        inputLocation = findViewById(R.id.inputloca);
        
        Intent intent = getIntent();
        String userID = intent.getStringExtra("id");
        
        loadingCompanies.setVisibility(View.VISIBLE);
        
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        apolloClient = ApolloClient.builder().okHttpClient(httpClient)
                .serverUrl("https://waste-mgmt-api.herokuapp.com/graphql")
                .build();

        companyNames.add("Select Trash Collection Company");
        apolloClient.query(new WasteInstitutionsQuery()).enqueue(wasteCallBack());

        //add the list to the dropdown item in the dialog view
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ReviewArea.this, R.layout.spinner_item, companyNames);
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
                parent.setSelection(0);
            }
        });

        reviewBtn.setOnClickListener(view -> {
            
            if(!validate()){
                Toast.makeText(ReviewArea.this, "Fields cannot be left empty", Toast.LENGTH_SHORT)
                        .show();
            } else {
                String comment  = inputComment.getText().toString();
                float rating = ratingBar.getRating();
                String location = inputLocation.getText().toString();


                ReviewInput reviewInput = ReviewInput.builder()
                        .comment(comment)
                        .institution(selectedID)
                        .rating(rating)
                        .location(location)
                        .creator(userID)
                        .build();
                Input<ReviewInput> input = new Input<>(reviewInput, true);
                apolloClient.mutate(new CreateReviewMutation(input)).enqueue(reviewCallBack());
            }
        });
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
        String rating = String.valueOf(ratingBar.getRating());

        if(TextUtils.isEmpty(comment)){
            inputComment.setError("Required!");
            valid = false;
        }
        if(TextUtils.isEmpty(location)){
            inputLocation.setError("Required!");
            valid = false;
        }
        
        if(TextUtils.isEmpty(rating)){
            Toast.makeText(ReviewArea.this, "Rating cannot be left empty. Choose number of stars",
                    Toast.LENGTH_LONG).show();
            valid = false;
        }

        if(TextUtils.isEmpty(selectedID)){
            Toast.makeText(ReviewArea.this, "Company has not been selected yet!",
                    Toast.LENGTH_SHORT).show();
            valid = false;
        }


        return valid;
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
                                Toast.makeText(ReviewArea.this,
                                        "an Error occurred : " , Toast.LENGTH_LONG).show();
                                //errorText.setText();
                                loadingCompanies.setVisibility(View.GONE);
                            });
                        } else{
                            List<Error> error = response.getErrors();
                            String errorMessage = error.get(0).getMessage();
                            Log.e("Apollo", "an Error occurred : " + errorMessage );
                            runOnUiThread(() -> {
                                Toast.makeText(ReviewArea.this,
                                        "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();
                                loadingCompanies.setVisibility(View.GONE);
                            });
                        }
                    }else{
                        runOnUiThread(() -> {
                            Log.d(TAG, "institutions fetched" + data.wasteInstitutions());

                            loadingCompanies.setVisibility(View.GONE);
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
                                Toast.makeText(ReviewArea.this,
                                        "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();

                            });
                        }
                    }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e("Apollo", "Error", e);
                Toast.makeText(ReviewArea.this,
                        "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
                loadingCompanies.setVisibility(View.GONE);
            }
        };
    }

    public ApolloCall.Callback<CreateReviewMutation.Data> reviewCallBack(){
        return new ApolloCall.Callback<CreateReviewMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<CreateReviewMutation.Data> response) {
                CreateReviewMutation.Data data = response.getData();

                if(response.getErrors() == null){

                    if(data.createReview() == null){
                        Log.e("Apollo", "an Error occurred : " );
                        runOnUiThread(() -> {
                            // Stuff that updates the UI
                            Toast.makeText(ReviewArea.this,
                                    "an Error occurred : " , Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                            //errorText.setText();
                        });
                    }else{
                        runOnUiThread(() -> {
                            Log.d(TAG, "onResponse: " + data.createReview()._id());
                            inputComment.setText("");ratingBar.setRating(0);
                            inputLocation.setText("");
                            companySpinner.setSelection(0);

                            Toast.makeText(ReviewArea.this,
                                    "Review made successfully", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        });
                    }

                } else{
                    List<Error> error = response.getErrors();
                    String errorMessage = error.get(0).getMessage();
                    Log.e("Apollo", "an Error occurred : " + errorMessage );
                    runOnUiThread(() -> {
                        Toast.makeText(ReviewArea.this,
                                "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    });
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e("Apollo", "Error", e);
                Toast.makeText(ReviewArea.this,
                        "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
    }

}