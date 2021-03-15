package com.example.wastemgmtapp.normalUser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.wastemgmtapp.Common.SessionManager;
import com.example.wastemgmtapp.Fragments.CollectionFragment;
import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.WasteInstitutionsQuery;
import com.example.wastemgmtapp.adapters.InstitutionsAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class InstitutionsActivity extends AppCompatActivity {

    ListView listView;
    String TAG  = CollectionFragment.class.getSimpleName();
    ArrayList<String> nameList = new ArrayList<>();
    ArrayList<String> locationList = new ArrayList<>();
    ArrayList<String> numberList = new ArrayList<>();
    ArrayList<String> emailList = new ArrayList<>();
    ProgressBar fetchLoading;
    LinearLayout noItems, retryNetwork;
    ApolloClient apolloClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_institutions);
        fetchLoading = findViewById(R.id.fetchLoading);
        retryNetwork = findViewById(R.id.retryNetwork);
        noItems = findViewById(R.id.noInsts);
        listView = findViewById(R.id.companyListView);
        Toolbar toolbar = findViewById(R.id.companyToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //show the back button on the toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        Intent intent = getIntent();
        String userID = intent.getStringExtra("id");

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
                            Toast.makeText(InstitutionsActivity.this,
                                    "an Error occurred : " , Toast.LENGTH_LONG).show();
                            //errorText.setText();
                            fetchLoading.setVisibility(View.GONE);
                        });
                    }else{
                        runOnUiThread(() -> {
                            Log.d(TAG, "institutions fetched" + data.wasteInstitutions());

                            fetchLoading.setVisibility(View.GONE);
                            for(int i = 0; i < data.wasteInstitutions().size(); i++){
                                nameList.add(data.wasteInstitutions().get(i).name());
                                emailList.add(data.wasteInstitutions().get(i).email());
                                locationList.add(data.wasteInstitutions().get(i).location());
                                numberList.add(data.wasteInstitutions().get(i).phoneNumber());
                            }
                            InstitutionsAdapter adapter = new InstitutionsAdapter(
                                    InstitutionsActivity.this, nameList, numberList, locationList, emailList);
                            listView.setAdapter(adapter);

                        });
                    }

                } else{
                    List<Error> error = response.getErrors();
                    String errorMessage = error.get(0).getMessage();
                    Log.e("Apollo", "an Error occurred : " + errorMessage );
                    runOnUiThread(() -> {
                        Toast.makeText(InstitutionsActivity.this,
                                "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();
                        fetchLoading.setVisibility(View.GONE);
                    });
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e("Apollo", "Error", e);
                Toast.makeText(InstitutionsActivity.this,
                        "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
                fetchLoading.setVisibility(View.GONE);
            }
        };
    }
}