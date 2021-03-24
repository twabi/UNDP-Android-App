package com.example.wastemgmtapp.Staff;

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
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.subscription.SubscriptionTransport;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;
import com.example.wastemgmtapp.Common.SessionManager;
import com.example.wastemgmtapp.GetTasksQuery;
import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.TaskAddedSubscription;
import com.example.wastemgmtapp.adapters.RequestsRecyclerAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
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

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        apolloClient = ApolloClient.builder().okHttpClient(httpClient)
                .serverUrl("https://waste-mgmt-api.herokuapp.com/graphql")
                .subscriptionTransportFactory(
                       new WebSocketSubscriptionTransport.Factory("wss://waste-mgmt-api.herokuapp.com/graphql", httpClient))
                .build();

        apolloClient.query(new GetTasksQuery()).enqueue(taskCallback());
        apolloClient.subscribe(new TaskAddedSubscription()).execute(subscriptionCallback());

        errorLayout.setOnClickListener(view -> {
            clearDS();
            apolloClient.query(new GetTasksQuery()).enqueue(taskCallback());
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public ApolloCall.Callback<GetTasksQuery.Data> taskCallback(){
        return new ApolloCall.Callback<GetTasksQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<GetTasksQuery.Data> response) {
                GetTasksQuery.Data data = response.getData();


                if(data.tasks() == null){

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
                        Log.d(TAG, "tasks fetched: " + data.tasks());
                        ArrayList<Object> tasks = new ArrayList<>();
                        if(!TextUtils.isEmpty(userID)){
                            for(int i=0; i < data.tasks().size(); i++){
                                if((userID.equals(data.tasks().get(i).staff()._id()) && (data.tasks().get(i).completed() == false))){
                                    tasks.add(data.tasks().get(i));
                                    keyList.add(data.tasks().get(i)._id());
                                    statusList.add(data.tasks().get(i).completed());
                                    createdAtList.add(data.tasks().get(i).createdAt());

                                    Log.d(TAG, "requests: " + data.tasks().get(i).sortedWaste() + "-" + data.tasks().get(i).trashcollection());

                                    if(data.tasks().get(i).sortedWaste() != null && data.tasks().get(i).trashcollection() == null){
                                        taskType.add("Sorted Waste Collection");
                                    } else if(data.tasks().get(i).trashcollection() != null && data.tasks().get(i).sortedWaste() == null){
                                        taskType.add("Trash Collection");
                                    } else if(data.tasks().get(i).trashcollection() == null && data.tasks().get(i).sortedWaste() == null){
                                        Log.d(TAG, "The body: " + data.tasks().get(i).body());
                                        if(data.tasks().get(i).body().equals("not available")){
                                            taskType.add("Unknown Task");
                                        } else {
                                            taskType.add("Other");
                                        }

                                    }
                                }
                            }

                            recyclerAdapter = new RequestsRecyclerAdapter(CollectionRequests.this, keyList, statusList, createdAtList, taskType);
                            tasksView.setAdapter(recyclerAdapter);

                            if(recyclerAdapter.getItemCount() == 0){
                                noItems.setVisibility(View.VISIBLE);
                            }
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


    public ApolloSubscriptionCall.Callback<TaskAddedSubscription.Data> subscriptionCallback(){
        return new ApolloSubscriptionCall.Callback<TaskAddedSubscription.Data>() {

            @Override
            public void onResponse(@NotNull Response response) {
                TaskAddedSubscription.Data data = (TaskAddedSubscription.Data) response.getData();
                Log.d(TAG, "onResponse: " + data);


                if(data.getClass() == null){

                    if(response.getErrors() == null){
                        Log.e(TAG, "an unknown Error in tasks query : " );
                        runOnUiThread(() -> {
                            loadTasks.setVisibility(View.GONE);
                            Toast.makeText(CollectionRequests.this,
                                    "an unknown Error occurred : " , Toast.LENGTH_LONG).show();

                        });
                    } else{
                        List<Error> error = response.getErrors();
                        String errorMessage = error.get(0).getMessage();
                        Log.e(TAG, "an Error in tasks query : " + errorMessage );
                        runOnUiThread(() -> {
                            loadTasks.setVisibility(View.GONE);
                            Toast.makeText(CollectionRequests.this,
                                    "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();

                        });
                    }
                }else{
                    runOnUiThread(() -> {
                        /*
                        loadTasks.setVisibility(View.GONE);
                        noItems.setVisibility(View.GONE);
                        errorLayout.setVisibility(View.GONE);
                        Log.d(TAG, "tasks fetched: " + data.tasks());
                        ArrayList<Object> tasks = new ArrayList<>();
                        if(!TextUtils.isEmpty(userID)){
                            for(int i=0; i < data.tasks().size(); i++){
                                if(userID.equals(data.tasks().get(i).staff()._id())){
                                    tasks.add(data.tasks().get(i));
                                    keyList.add(data.tasks().get(i)._id());
                                    statusList.add(data.tasks().get(i).completed());
                                    createdAtList.add(data.tasks().get(i).createdAt());

                                    if(data.tasks().get(i).sortedWaste() != null && data.tasks().get(i).trashcollection() == null){
                                        taskType.add("Sorted Waste Collection");
                                    } else if(data.tasks().get(i).trashcollection() != null && data.tasks().get(i).sortedWaste() == null){
                                        taskType.add("Trash Collection");
                                    } else if(data.tasks().get(i).trashcollection() == null && data.tasks().get(i).sortedWaste() == null){
                                        Log.d(TAG, "The body: " + data.tasks().get(i).body());
                                        if(data.tasks().get(i).body().equals("not available")){
                                            taskType.add("Unknown Task");
                                        } else {
                                            taskType.add("Other");
                                        }

                                    }
                                }
                            }

                            recyclerAdapter = new RequestsRecyclerAdapter(CollectionRequests.this, keyList, statusList, createdAtList, taskType);
                            tasksView.setAdapter(recyclerAdapter);

                            if(recyclerAdapter.getItemCount() == 0){
                                noItems.setVisibility(View.VISIBLE);
                            }

                            }
                         */

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
                    loadTasks.setVisibility(View.GONE);
                    Toast.makeText(CollectionRequests.this,
                            "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();

                });
            }

            @Override
            public void onCompleted() {

            }

            @Override
            public void onTerminated() {

            }

            @Override
            public void onConnected() {

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