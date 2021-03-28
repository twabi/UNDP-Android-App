package com.example.wastemgmtapp.Staff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;
import com.example.wastemgmtapp.Common.LogInActivity;
import com.example.wastemgmtapp.Common.MainActivity;
import com.example.wastemgmtapp.CreateTrashCollectionMutation;
import com.example.wastemgmtapp.DeleteTaskMutation;
import com.example.wastemgmtapp.GetTaskQuery;
import com.example.wastemgmtapp.GetTasksQuery;
import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.UpdateTaskStatusMutation;
import com.example.wastemgmtapp.normalUser.UserHomeActivity;
import com.example.wastemgmtapp.type.UpdateTaskInput;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class RequestDetailsActivity extends AppCompatActivity {

    private MapView mapView;
    String taskID;
    TextView locationText, amountText, nameText, typeText, errorText, qualifierText;
    String TAG = RequestDetailsActivity.class.getSimpleName();
    Double longitude, latitude;
    ApolloClient apolloClient;
    CameraPosition position;
    Button accept, delete;
    ProgressBar taskLoads;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);
        
        locationText = findViewById(R.id.locationTask);
        amountText = findViewById(R.id.amount);
        nameText = findViewById(R.id.creator);
        typeText = findViewById(R.id.trashType);
        errorText = findViewById(R.id.errorText);
        qualifierText = findViewById(R.id.qualifierText);
        mapView = findViewById(R.id.request_map);
        accept = findViewById(R.id.accept);
        delete = findViewById(R.id.delete);
        taskLoads = findViewById(R.id.taskLoads);
        taskLoads.setVisibility(View.VISIBLE);

        //initialize the toolbar
        Toolbar toolbar = findViewById(R.id.detailsToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //show the back button on the toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        taskID = intent.getStringExtra("key");
        Log.d(TAG, "key: " + taskID);

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

        apolloClient.query(new GetTaskQuery(taskID)).enqueue(taskCallback());

        mapView.onCreate(savedInstanceState);
        accept.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(RequestDetailsActivity.this);
            builder.setTitle("Accept Task");
            builder.setMessage("Are you sure?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            UpdateTaskInput taskInput = UpdateTaskInput.builder()
                                    ._id(taskID)
                                    .completed(true)
                                    .build();
                            Input<UpdateTaskInput> input = new Input<>(taskInput, true);
                            apolloClient.mutate(new UpdateTaskStatusMutation(input)).enqueue(updateCallback());
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            dialog.cancel();
                        }
                    });
            builder.show();
        });

        delete.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(RequestDetailsActivity.this);
            builder.setTitle("Delete Task");
            builder.setMessage("Are you sure?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            apolloClient.mutate(new DeleteTaskMutation(taskID)).enqueue(new ApolloCall.Callback<DeleteTaskMutation.Data>() {
                                @Override
                                public void onResponse(@NotNull Response<DeleteTaskMutation.Data> response) {
                                    DeleteTaskMutation.Data data = response.getData();
                                    if(response.getErrors() == null){
                                        runOnUiThread(() -> {
                                            Log.d(TAG, "onResponse: " + data.deleteTask().toString());
                                            Toast.makeText(RequestDetailsActivity.this,
                                                    "Task deleted successfully", Toast.LENGTH_LONG).show();

                                            Intent i = new Intent(RequestDetailsActivity.this, CollectionRequests.class);
                                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(i);

                                        });
                                    }
                                }

                                @Override
                                public void onFailure(@NotNull ApolloException e) {
                                    Log.e("Apollo", "Error", e);
                                    Toast.makeText(RequestDetailsActivity.this,
                                            "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            dialog.cancel();
                        }
                    });
            builder.show();
        });

    }
    
    public ApolloCall.Callback<UpdateTaskStatusMutation.Data> updateCallback(){
        return new ApolloCall.Callback<UpdateTaskStatusMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<UpdateTaskStatusMutation.Data> response) {
                UpdateTaskStatusMutation.Data data = response.getData();
                if(response.getErrors() == null){

                    if(data.updateTask() == null){
                        Log.e("Apollo", "an Error occurred : " );
                        runOnUiThread(() -> {
                            // Stuff that updates the UI
                            Toast.makeText(RequestDetailsActivity.this,
                                    "an unknown Error occurred " , Toast.LENGTH_LONG).show();
                        });
                    }else{
                        runOnUiThread(() -> {
                            Log.d(TAG, "onResponse: " + data.updateTask()._id());
                            Toast.makeText(RequestDetailsActivity.this,
                                    "accepted task successfully", Toast.LENGTH_LONG).show();

                        });
                    }

                } else{
                    List<Error> error = response.getErrors();
                    String errorMessage = error.get(0).getMessage();
                    Log.e("Apollo", "an Error occurred : " + errorMessage );
                    runOnUiThread(() -> {
                        Toast.makeText(RequestDetailsActivity.this,
                                "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e("Apollo", "Error", e);
                Toast.makeText(RequestDetailsActivity.this,
                        "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
    }

    public ApolloCall.Callback<GetTaskQuery.Data> taskCallback(){
        return new ApolloCall.Callback<GetTaskQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<GetTaskQuery.Data> response) {
                GetTaskQuery.Data data = response.getData();


                if(data.task() == null){

                    if(response.getErrors() == null){
                        Log.e(TAG, "an unknown Error in task query : " );
                        runOnUiThread(() -> {
                            taskLoads.setVisibility(View.GONE);
                            errorText.setVisibility(View.VISIBLE);
                            Toast.makeText(RequestDetailsActivity.this,
                                    "an unknown Error occurred : " , Toast.LENGTH_LONG).show();

                        });
                    } else{
                        List<Error> error = response.getErrors();
                        String errorMessage = error.get(0).getMessage();
                        Log.e(TAG, "an Error in task query : " + errorMessage );
                        runOnUiThread(() -> {
                            taskLoads.setVisibility(View.GONE);
                            errorText.setVisibility(View.VISIBLE);
                            Toast.makeText(RequestDetailsActivity.this,
                                    "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();

                        });
                    }
                }else{
                    runOnUiThread(() -> {
                        Log.d(TAG, "task fetched: " + data.task());
                        taskLoads.setVisibility(View.GONE);
                        /*
                        if(data.task().sortedWaste() != null && (data.task().trashcollection() == null)){
                            errorText.setVisibility(View.GONE);
                            locationText.setText(data.task().sortedWaste().location());
                            amountText.setText(data.task().sortedWaste().amount());
                            nameText.setText(data.task().sortedWaste().creator().fullName());
                            typeText.setText(data.task().sortedWaste().typeOfWaste());
                            qualifierText.setText("Sorted Waste Collection");
                            longitude = data.task().sortedWaste().longitude();
                            latitude = data.task().sortedWaste().latitude();

                        } else if(data.task().trashcollection() != null && (data.task().sortedWaste() == null)){
                            errorText.setVisibility(View.GONE);
                            locationText.setText(data.task().trashcollection().location());
                            amountText.setText(data.task().trashcollection().amount());
                            nameText.setText(data.task().trashcollection().creator().fullName());
                            typeText.setText(data.task().trashcollection().typeOfWaste());
                            qualifierText.setText("Trash Collection");
                            longitude = data.task().trashcollection().longitude();
                            latitude = data.task().trashcollection().latitude();

                        } else if(data.task().trashcollection() == null && (data.task().sortedWaste() == null)){
                            Log.d(TAG, "The body: " + data.task().body());
                            errorText.setVisibility(View.VISIBLE);
                            qualifierText.setText("Other");
                            longitude = 0.0;
                            latitude = 0.0;
                        }

                        position = new CameraPosition.Builder()
                                .target(new LatLng(latitude, longitude)).zoom(10).tilt(20)
                                .build();

                        mapView.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(@NonNull MapboxMap mapboxMap) {

                                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                                    @Override
                                    public void onStyleLoaded(@NonNull Style style) {
                                        // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 10);

                                    }
                                });

                            }
                        });
                        */

                    });

                    if(response.getErrors() != null){
                        List<Error> error = response.getErrors();
                        String errorMessage = error.get(0).getMessage();
                        Log.e(TAG, "an Error in task query : " + errorMessage );
                        runOnUiThread(() -> {

                            Toast.makeText(RequestDetailsActivity.this,
                                    "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();

                        });
                    }
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e(TAG, "Error", e);
                runOnUiThread(() -> {
                    taskLoads.setVisibility(View.GONE);
                    errorText.setVisibility(View.VISIBLE);
                    Toast.makeText(RequestDetailsActivity.this,
                            "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();

                });
            }
        };
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}