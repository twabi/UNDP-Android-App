package com.example.wastemgmtapp.Staff;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.wastemgmtapp.Common.SessionManager;
import com.example.wastemgmtapp.GetStaffQuery;
import com.example.wastemgmtapp.GetTaskSortedWastesQuery;
import com.example.wastemgmtapp.GetTrashcansQuery;
import com.example.wastemgmtapp.GetZoneTrashcansQuery;
import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.adapters.RequestsRecyclerAdapter;
import com.example.wastemgmtapp.adapters.TrashRecyclerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class ZoneTrashcans extends AppCompatActivity {

    String userID;
    private final ArrayList<String> sample = new ArrayList<>(Arrays.asList("one", "two", "three", "four", "five"));
    ApolloClient apolloClient;
    LinearLayout errorLayout, noItems;
    ProgressBar loadCans;
    RecyclerView trashRecyclerview;
    TrashRecyclerAdapter recyclerAdapter;
    String zoneID;
    ArrayList<String> keyList = new ArrayList<>();
    ArrayList<String> nameList = new ArrayList<>();
    ArrayList<Double> statusList = new ArrayList<>();
    ArrayList<String> zoneNameList = new ArrayList<>();

    String TAG = ZoneTrashcans.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zone_trashcans);

        errorLayout = findViewById(R.id.Retry2);
        noItems = findViewById(R.id.noitems2);
        loadCans = findViewById(R.id.load2);
        trashRecyclerview = findViewById(R.id.trash_recyclerview);

        //initialize the toolbar
        Toolbar toolbar = findViewById(R.id.zone_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //show the back button on the toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent  = getIntent();
        userID = intent.getStringExtra("id");
        zoneID = intent.getStringExtra("zoneID");

        Log.d(TAG, "IDs: " + userID + "-" + zoneID);


        loadCans.setVisibility(View.VISIBLE);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        apolloClient = ApolloClient.builder().okHttpClient(httpClient)
                .serverUrl("https://waste-mgmt-api.herokuapp.com/graphql")
                .build();


        apolloClient.query(new GetTrashcansQuery()).enqueue(trashCallback());

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public ApolloCall.Callback<GetTrashcansQuery.Data> trashCallback(){
        return new ApolloCall.Callback<GetTrashcansQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<GetTrashcansQuery.Data> response) {
                GetTrashcansQuery.Data data = response.getData();

                if(data.trashcans() == null){

                    if(response.getErrors() == null){
                        Log.e(TAG, "an Error in trashcans query : " );
                        runOnUiThread(() -> {
                            loadCans.setVisibility(View.GONE);
                            errorLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(ZoneTrashcans.this,
                                    "zone: Error occurred : " , Toast.LENGTH_LONG).show();
                        });
                    } else{
                        List<Error> error = response.getErrors();
                        String errorMessage = error.get(0).getMessage();
                        Log.e(TAG, "an Error in trashcans query : " + errorMessage );
                        runOnUiThread(() -> {
                            loadCans.setVisibility(View.GONE);
                            errorLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(ZoneTrashcans.this,
                                    "zone: error occurred : " + errorMessage, Toast.LENGTH_LONG).show();

                        });
                    }
                }else{
                    runOnUiThread(() -> {
                        loadCans.setVisibility(View.GONE);
                        noItems.setVisibility(View.GONE);
                        errorLayout.setVisibility(View.GONE);
                        Log.d(TAG, "trashcans fetched: " + data.trashcans());
                        if(!TextUtils.isEmpty(zoneID)){
                            for(int i=0; i < data.trashcans().size(); i++){
                                if(zoneID.equals(data.trashcans().get(i).zone()._id())){
                                    Log.d(TAG, "onResponse: " + data.trashcans().get(i));
                                    nameList.add(data.trashcans().get(i).trashcanId());
                                    statusList.add(data.trashcans().get(i).status());
                                    zoneNameList.add(data.trashcans().get(i).zone().name());
                                    keyList.add(data.trashcans().get(i)._id());
                                }
                            }

                            recyclerAdapter = new TrashRecyclerAdapter(ZoneTrashcans.this, nameList, statusList, zoneNameList, keyList);
                            trashRecyclerview.setAdapter(recyclerAdapter);

                            if(recyclerAdapter.getItemCount() == 0){
                                noItems.setVisibility(View.VISIBLE);
                            }
                        }


                    });

                    if(response.getErrors() != null){
                        List<Error> error = response.getErrors();
                        String errorMessage = error.get(0).getMessage();
                        Log.e(TAG, "an Error in staff query : " + errorMessage );
                        runOnUiThread(() -> {
                            Toast.makeText(ZoneTrashcans.this,
                                    "zone: error occurred : " + errorMessage, Toast.LENGTH_LONG).show();

                        });
                    }
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e(TAG, "Error", e);
                runOnUiThread(() -> {
                    errorLayout.setVisibility(View.VISIBLE);
                    loadCans.setVisibility(View.GONE);
                    Toast.makeText(ZoneTrashcans.this,
                            "zone: error : " + e.getMessage(), Toast.LENGTH_LONG).show();

                });
            }
        };
    }
}