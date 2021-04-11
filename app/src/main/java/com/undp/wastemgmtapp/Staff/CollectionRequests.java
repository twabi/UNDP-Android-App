package com.undp.wastemgmtapp.Staff;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

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
import com.undp.wastemgmtapp.Common.SessionManager;
import com.undp.wastemgmtapp.GetTaskSortedWastesQuery;
import com.undp.wastemgmtapp.GetTaskTrashCollectionsQuery;
import com.undp.wastemgmtapp.R;
import com.undp.wastemgmtapp.adapters.RequestsRecyclerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class CollectionRequests extends AppCompatActivity {

    String userID;
    SessionManager session;
    String TAG = CollectionRequests.class.getSimpleName();
    ArrayList<String> keyList = new ArrayList<>();
    ArrayList<Boolean> statusList = new ArrayList<>();
    ArrayList<String> createdAtList = new ArrayList<>();
    ArrayList<String> taskType = new ArrayList<>();
    RecyclerView tasksView;
    LinearLayout errorLayout, noItems;
    ProgressBar loadTasks;
    ArrayList<Object> tasks = new ArrayList<>();
    ApolloClient apolloClient;
    RequestsRecyclerAdapter recyclerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_requests);
        session = new SessionManager(getApplicationContext());
        HashMap<String, String> user = session.getUserDetails();
        userID = user.get(SessionManager.KEY_USERID);

        //initialize the toolbar
        Toolbar toolbar = findViewById(R.id.request_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //show the back button on the toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        tasksView = findViewById(R.id.requests_recyclerview);
        loadTasks = findViewById(R.id.loadRequests);
        errorLayout = findViewById(R.id.errorLayout);
        noItems = findViewById(R.id.noTasks);

        loadTasks.setVisibility(View.VISIBLE);
        noItems.setVisibility(View.GONE);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        apolloClient = ApolloClient.builder().okHttpClient(httpClient)
                .serverUrl("https://waste-mgmt-api.herokuapp.com/graphql")
                .build();

        tasks.clear();
        taskType.clear();
        keyList.clear();
        createdAtList.clear();
        statusList.clear();
        apolloClient.query(new GetTaskSortedWastesQuery()).enqueue(taskSortedCallback());
        apolloClient.query(new GetTaskTrashCollectionsQuery()).enqueue(taskCollectCallback());

        errorLayout.setOnClickListener(view -> {
            clearDS();
            apolloClient.query(new GetTaskSortedWastesQuery()).enqueue(taskSortedCallback());
            apolloClient.query(new GetTaskTrashCollectionsQuery()).enqueue(taskCollectCallback());
        });

        if(keyList.size() == 0){
            loadTasks.setVisibility(View.VISIBLE);
            noItems.setVisibility(View.VISIBLE);
        } else {
            errorLayout.setVisibility(View.GONE);
            noItems.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        taskType.clear();
        keyList.clear();
        createdAtList.clear();
        statusList.clear();
        tasks.clear();
        apolloClient.query(new GetTaskSortedWastesQuery()).enqueue(taskSortedCallback());
        apolloClient.query(new GetTaskTrashCollectionsQuery()).enqueue(taskCollectCallback());

    }

    public ApolloCall.Callback<GetTaskSortedWastesQuery.Data> taskSortedCallback(){
        return new ApolloCall.Callback<GetTaskSortedWastesQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<GetTaskSortedWastesQuery.Data> response) {
                GetTaskSortedWastesQuery.Data data = response.getData();


                if(data.taskSortedWastes() == null){

                    if(response.getErrors() == null){
                        Log.e(TAG, "an unknown Error in tasks query : " );
                        runOnUiThread(() -> {
                            loadTasks.setVisibility(View.GONE);
                            errorLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(CollectionRequests.this,
                                    "an unknown Error occurred : " , Toast.LENGTH_LONG).show();

                        });
                    } else{
                        List<Error> error = response.getErrors();
                        String errorMessage = error.get(0).getMessage();
                        Log.e(TAG, "an Error in tasks query : " + errorMessage );
                        runOnUiThread(() -> {
                            loadTasks.setVisibility(View.GONE);
                            errorLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(CollectionRequests.this,
                                    "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();

                        });
                    }
                }else{
                    runOnUiThread(() -> {
                        loadTasks.setVisibility(View.GONE);
                        noItems.setVisibility(View.GONE);
                        errorLayout.setVisibility(View.GONE);
                        Log.d(TAG, "tasks fetched: " + data.taskSortedWastes());

                        try{
                            if(!TextUtils.isEmpty(userID)){
                                for(int i=0; i < data.taskSortedWastes().size(); i++){
                                    if((userID.equals(data.taskSortedWastes().get(i).staff()._id()) && (data.taskSortedWastes().get(i).completed() == false))){
                                        tasks.add(data.taskSortedWastes().get(i));
                                        keyList.add(data.taskSortedWastes().get(i)._id());
                                        statusList.add(data.taskSortedWastes().get(i).completed());
                                        createdAtList.add(data.taskSortedWastes().get(i).createdAt());
                                        taskType.add("SortedWaste");
                                    }
                                }

                                recyclerAdapter = new RequestsRecyclerAdapter(CollectionRequests.this, keyList, statusList, createdAtList, taskType);
                                tasksView.setAdapter(recyclerAdapter);

                                if(recyclerAdapter.getItemCount() == 0){
                                    noItems.setVisibility(View.VISIBLE);
                                }
                            }
                        }catch (Exception e){
                            errorLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(CollectionRequests.this,
                                    "an Error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                    });

                    if(response.getErrors() != null){
                        List<Error> error = response.getErrors();
                        String errorMessage = error.get(0).getMessage();
                        Log.e(TAG, "an Error in tasks query : " + errorMessage );
                        runOnUiThread(() -> {
                            Toast.makeText(CollectionRequests.this,
                                    "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();

                        });
                    }
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e(TAG, "Error", e);
                runOnUiThread(() -> {
                    errorLayout.setVisibility(View.VISIBLE);
                    loadTasks.setVisibility(View.GONE);
                    Toast.makeText(CollectionRequests.this,
                            "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();

                });
            }
        };
    }


    public ApolloCall.Callback<GetTaskTrashCollectionsQuery.Data> taskCollectCallback(){
        return new ApolloCall.Callback<GetTaskTrashCollectionsQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<GetTaskTrashCollectionsQuery.Data> response) {
                GetTaskTrashCollectionsQuery.Data data = response.getData();


                if(data.taskTrashCollections() == null){

                    if(response.getErrors() == null){
                        Log.e(TAG, "an unknown Error in tasks query : " );
                        runOnUiThread(() -> {
                            loadTasks.setVisibility(View.GONE);
                            errorLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(CollectionRequests.this,
                                    "an unknown Error occurred : " , Toast.LENGTH_LONG).show();

                        });
                    } else{
                        List<Error> error = response.getErrors();
                        String errorMessage = error.get(0).getMessage();
                        Log.e(TAG, "an Error in tasks query : " + errorMessage );
                        runOnUiThread(() -> {
                            loadTasks.setVisibility(View.GONE);
                            errorLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(CollectionRequests.this,
                                    "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();

                        });
                    }
                }else{
                    runOnUiThread(() -> {
                        loadTasks.setVisibility(View.GONE);
                        noItems.setVisibility(View.GONE);
                        errorLayout.setVisibility(View.GONE);
                        Log.d(TAG, "tasks fetched: " + data.taskTrashCollections());

                        try{
                            if(!TextUtils.isEmpty(userID)){
                                for(int i=0; i < data.taskTrashCollections().size(); i++){
                                    if((userID.equals(data.taskTrashCollections().get(i).staff()._id()) && (data.taskTrashCollections().get(i).completed() == false))){
                                        tasks.add(data.taskTrashCollections().get(i));
                                        keyList.add(data.taskTrashCollections().get(i)._id());
                                        statusList.add(data.taskTrashCollections().get(i).completed());
                                        createdAtList.add(data.taskTrashCollections().get(i).createdAt());
                                        taskType.add("TrashCollection");
                                    }
                                }

                                recyclerAdapter = new RequestsRecyclerAdapter(CollectionRequests.this, keyList, statusList, createdAtList, taskType);
                                tasksView.setAdapter(recyclerAdapter);

                                if(recyclerAdapter.getItemCount() == 0){
                                    noItems.setVisibility(View.VISIBLE);
                                }
                            }
                        }catch (Exception e){
                            errorLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(CollectionRequests.this,
                                    "an Error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                    });

                    if(response.getErrors() != null){
                        List<Error> error = response.getErrors();
                        String errorMessage = error.get(0).getMessage();
                        Log.e(TAG, "an Error in tasks query : " + errorMessage );
                        runOnUiThread(() -> {
                            Toast.makeText(CollectionRequests.this,
                                    "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();

                        });
                    }
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e(TAG, "Error", e);
                runOnUiThread(() -> {
                    errorLayout.setVisibility(View.VISIBLE);
                    loadTasks.setVisibility(View.GONE);
                    Toast.makeText(CollectionRequests.this,
                            "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();

                });
            }
        };
    }


    public void clearDS(){
        taskType.clear();
        statusList.clear();
        createdAtList.clear();
        keyList.clear();

        recyclerAdapter.clear();
    }
}