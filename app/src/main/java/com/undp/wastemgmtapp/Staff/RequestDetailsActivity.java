package com.undp.wastemgmtapp.Staff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.undp.wastemgmtapp.Common.GPSTracker;
import com.undp.wastemgmtapp.DeleteTaskSortedWasteMutation;
import com.undp.wastemgmtapp.DeleteTaskTrashcollectionMutation;
import com.undp.wastemgmtapp.R;
import com.undp.wastemgmtapp.TaskSortedWasteQuery;
import com.undp.wastemgmtapp.TaskTrashCollectionQuery;
import com.undp.wastemgmtapp.UpdateCollectionNotifMutation;
import com.undp.wastemgmtapp.UpdateSortedWasteNotifMutation;
import com.undp.wastemgmtapp.UpdateTaskSortedWasteMutation;
import com.undp.wastemgmtapp.UpdateTaskTrashcollectionMutation;
import com.undp.wastemgmtapp.type.UpdateSortedWasteNotificationInput;
import com.undp.wastemgmtapp.type.UpdateTaskSortedWasteInput;
import com.undp.wastemgmtapp.type.UpdateTaskTrashCollectionInput;
import com.undp.wastemgmtapp.type.UpdateTrashCollectionNoticationInput;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;

public class RequestDetailsActivity extends AppCompatActivity {

    private MapView mapView;
    String taskID, taskType;
    TextView locationText, amountText, nameText, typeText, errorText, qualifierText;
    String TAG = RequestDetailsActivity.class.getSimpleName();
    Double longitude, latitude;
    ApolloClient apolloClient;
    CameraPosition position;
    Button accept, delete;
    ProgressBar taskLoads;
    double userLat, userLong;
    MapboxMap mapboxMap;
    String requestID;
    private NavigationMapRoute navigationMapRoute;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_request_details);

        GPSTracker gpsTracker = new GPSTracker(RequestDetailsActivity.this, RequestDetailsActivity.this);
        userLat = gpsTracker.getLatitude();
        userLong = gpsTracker.getLongitude();
        
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
        taskType = intent.getStringExtra("task type");
        Log.d(TAG, "key: " + taskID);
        Log.d(TAG, "task type: " + taskType);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        apolloClient = ApolloClient.builder().okHttpClient(httpClient)
                .serverUrl("https://waste-mgmt-api.herokuapp.com/graphql")
                .build();

        if(taskType.equals("TrashCollection")){
            apolloClient.query(new TaskTrashCollectionQuery(taskID)).enqueue(taskCollectCallback());
        } else if(taskType.equals("SortedWaste")) {
            apolloClient.query(new TaskSortedWasteQuery(taskID)).enqueue(taskSortedCallback());
        }


        mapView.onCreate(savedInstanceState);
        accept.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(RequestDetailsActivity.this);
            builder.setTitle("Mark Task As complete");
            builder.setMessage("Are you sure you completed the task? The task will be deleted")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            if(taskType.equals("TrashCollection")){

                                UpdateTaskTrashCollectionInput taskInput = UpdateTaskTrashCollectionInput.builder()
                                        ._id(taskID)
                                        .pending(true)
                                        .build();
                                Input<UpdateTaskTrashCollectionInput> input = new Input<>(taskInput, true);
                                apolloClient.mutate(new UpdateTaskTrashcollectionMutation(input)).enqueue(updateTrashCallback());

                            } else if(taskType.equals("SortedWaste")) {

                                UpdateTaskSortedWasteInput taskInput = UpdateTaskSortedWasteInput.builder()
                                        ._id(taskID)
                                        .completed(true)
                                        .build();
                                Input<UpdateTaskSortedWasteInput> input = new Input<>(taskInput, true);
                                apolloClient.mutate(new UpdateTaskSortedWasteMutation(input)).enqueue(updateSortedCallback());

                            }

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

                            if(taskType.equals("TrashCollection")){
                                apolloClient.mutate(new DeleteTaskTrashcollectionMutation(taskID)).enqueue(deleteTrashTaskCallback());
                            } else if(taskType.equals("SortedWaste")) {
                                apolloClient.mutate(new DeleteTaskSortedWasteMutation(taskID)).enqueue(deleteSortedTaskCallback());
                            }
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


    public ApolloCall.Callback<DeleteTaskSortedWasteMutation.Data> deleteSortedTaskCallback(){
        return new ApolloCall.Callback<DeleteTaskSortedWasteMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<DeleteTaskSortedWasteMutation.Data> response) {
                DeleteTaskSortedWasteMutation.Data data = response.getData();
                if(response.getErrors() == null){
                    runOnUiThread(() -> {
                        Log.d(TAG, "onResponse: " + data.deleteTaskSortedWaste());
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
                runOnUiThread(() -> {
                    Toast.makeText(RequestDetailsActivity.this,
                            "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        };
    }

    public ApolloCall.Callback<DeleteTaskTrashcollectionMutation.Data> deleteTrashTaskCallback(){
        return new ApolloCall.Callback<DeleteTaskTrashcollectionMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<DeleteTaskTrashcollectionMutation.Data> response) {
                DeleteTaskTrashcollectionMutation.Data data = response.getData();
                if(response.getErrors() == null){
                    runOnUiThread(() -> {
                        Log.d(TAG, "onResponse: " + data.deleteTrashCollection());
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
                runOnUiThread(() -> {
                    Toast.makeText(RequestDetailsActivity.this,
                            "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        };
    }

    public ApolloCall.Callback<UpdateTaskTrashcollectionMutation.Data> updateTrashCallback(){
        return new ApolloCall.Callback<UpdateTaskTrashcollectionMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<UpdateTaskTrashcollectionMutation.Data> response) {
                UpdateTaskTrashcollectionMutation.Data data = response.getData();
                if(response.getErrors() == null){

                    if(data.updateTaskTrashCollection() == null){
                        Log.e("Apollo", "an Error occurred : " );
                        runOnUiThread(() -> {
                            // Stuff that updates the UI
                            Toast.makeText(RequestDetailsActivity.this,
                                    "an unknown Error occurred " , Toast.LENGTH_LONG).show();
                        });
                    }else{
                        runOnUiThread(() -> {
                            Log.d(TAG, "onResponse: " + data.updateTaskTrashCollection()._id());
                            Toast.makeText(RequestDetailsActivity.this,
                                    "Task Marked as Complete successfully", Toast.LENGTH_LONG).show();

                            UpdateTrashCollectionNoticationInput notifInput =  UpdateTrashCollectionNoticationInput.builder()
                                    .completed(true)
                                    .status("Completed")
                                    ._id(requestID)
                                    .build();
                            Input<UpdateTrashCollectionNoticationInput> input = new Input<>(notifInput, true);
                            apolloClient.mutate(new UpdateCollectionNotifMutation(input)).enqueue(collectionNotifCallback());



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
                runOnUiThread(() -> {
                    Toast.makeText(RequestDetailsActivity.this,
                            "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

            }
        };
    }

    public ApolloCall.Callback<UpdateTaskSortedWasteMutation.Data> updateSortedCallback(){
        return new ApolloCall.Callback<UpdateTaskSortedWasteMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<UpdateTaskSortedWasteMutation.Data> response) {
                UpdateTaskSortedWasteMutation.Data data = response.getData();
                if(response.getErrors() == null){

                    if(data.updateTaskSortedWaste() == null){
                        Log.e("Apollo", "an Error occurred : " );
                        runOnUiThread(() -> {
                            // Stuff that updates the UI
                            Toast.makeText(RequestDetailsActivity.this,
                                    "an unknown Error occurred " , Toast.LENGTH_LONG).show();
                        });
                    }else{
                        runOnUiThread(() -> {
                            Log.d(TAG, "onResponse: " + data.updateTaskSortedWaste()._id());
                            Toast.makeText(RequestDetailsActivity.this,
                                    "Task Marked as Complete successfully", Toast.LENGTH_LONG).show();

                            UpdateSortedWasteNotificationInput notifInput =  UpdateSortedWasteNotificationInput.builder()
                                    .completed(true)
                                    .status("Completed")
                                    ._id(requestID)
                                    .build();
                            Input<UpdateSortedWasteNotificationInput> input = new Input<>(notifInput, true);
                            apolloClient.mutate(new UpdateSortedWasteNotifMutation(input)).enqueue(sortedNotifCallback());



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
                runOnUiThread(() -> {
                    Toast.makeText(RequestDetailsActivity.this,
                            "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

            }
        };
    }

    public ApolloCall.Callback<UpdateCollectionNotifMutation.Data> collectionNotifCallback(){
        return new ApolloCall.Callback<UpdateCollectionNotifMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<UpdateCollectionNotifMutation.Data> response) {
                UpdateCollectionNotifMutation.Data data = response.getData();


                if(data.updateTrashCollectionNotication() == null){

                    if(response.getErrors() == null){
                        Log.e("Apollo", "an Error occurred : " );
                        runOnUiThread(() -> {
                            // Stuff that updates the UI
                            Toast.makeText(RequestDetailsActivity.this,
                                    "an Error occurred : " , Toast.LENGTH_LONG).show();
                        });
                    } else{
                        List<Error> error = response.getErrors();
                        String errorMessage = error.get(0).getMessage();
                        Log.e("Apollo", "an Error occurred : " + errorMessage );
                        runOnUiThread(() -> {
                            Toast.makeText(RequestDetailsActivity.this,
                                    "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();

                        });
                    }
                }else{
                    runOnUiThread(() -> {
                        Log.d(TAG, "notif created" + data.updateTrashCollectionNotication()._id());

                    });

                    if(response.getErrors() != null){
                        List<Error> error = response.getErrors();
                        String errorMessage = error.get(0).getMessage();
                        Log.e("Apollo", "an Error occurred : " + errorMessage );
                        runOnUiThread(() -> {
                            Toast.makeText(RequestDetailsActivity.this,
                                    "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();

                        });
                    }
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e("Apollo", "Error", e);
                runOnUiThread(() -> {
                    Toast.makeText(RequestDetailsActivity.this,
                            "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();

                });
            }
        };
    }

    public ApolloCall.Callback<UpdateSortedWasteNotifMutation.Data> sortedNotifCallback(){
        return new ApolloCall.Callback<UpdateSortedWasteNotifMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<UpdateSortedWasteNotifMutation.Data> response) {
                UpdateSortedWasteNotifMutation.Data data = response.getData();


                if(data.updateSortedWasteNotification() == null){

                    if(response.getErrors() == null){
                        Log.e("Apollo", "an Error occurred : " );
                        runOnUiThread(() -> {
                            // Stuff that updates the UI
                            Toast.makeText(RequestDetailsActivity.this,
                                    "an Error occurred : " , Toast.LENGTH_LONG).show();
                        });
                    } else{
                        List<Error> error = response.getErrors();
                        String errorMessage = error.get(0).getMessage();
                        Log.e("Apollo", "an Error occurred : " + errorMessage );
                        runOnUiThread(() -> {
                            Toast.makeText(RequestDetailsActivity.this,
                                    "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();

                        });
                    }
                }else{
                    runOnUiThread(() -> {
                        Log.d(TAG, "notif created" + data.updateSortedWasteNotification()._id());

                    });

                    if(response.getErrors() != null){
                        List<Error> error = response.getErrors();
                        String errorMessage = error.get(0).getMessage();
                        Log.e("Apollo", "an Error occurred : " + errorMessage );
                        runOnUiThread(() -> {
                            Toast.makeText(RequestDetailsActivity.this,
                                    "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();

                        });
                    }
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e("Apollo", "Error", e);
                runOnUiThread(() -> {
                    Toast.makeText(RequestDetailsActivity.this,
                            "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();

                });
            }
        };
    }

    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(RequestDetailsActivity.this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {

                    @Override
                    public void onResponse(Call<DirectionsResponse> call, retrofit2.Response<DirectionsResponse> response) {
                        if (response.body() == null)
                        {
                            //Usable.logMessage(TAG, "No routes found, Check User and Access Token..");
                            return;
                        } else if (response.body().routes().size() == 0)
                        {
                            //Usable.logMessage(TAG, "No routes found..");
                            return;
                        }


                        DirectionsRoute currentRoute = response.body().routes().get(0);
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Log.e(TAG, "Error: "+ t.getMessage());
                    }
                });

    }


    public ApolloCall.Callback<TaskTrashCollectionQuery.Data> taskCollectCallback(){
        return new ApolloCall.Callback<TaskTrashCollectionQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<TaskTrashCollectionQuery.Data> response) {
                TaskTrashCollectionQuery.Data data = response.getData();


                if(data.taskTrashCollection() == null){

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
                        Log.d(TAG, "task fetched: " + data.taskTrashCollection());
                        taskLoads.setVisibility(View.GONE);
                        errorText.setVisibility(View.GONE);
                        locationText.setText(data.taskTrashCollection().trashcollection().location());
                        amountText.setText(data.taskTrashCollection().trashcollection().amount());
                        nameText.setText(data.taskTrashCollection().trashcollection().creator().fullName());
                        typeText.setText(data.taskTrashCollection().trashcollection().typeOfWaste());
                        qualifierText.setText("Trash Collection");
                        longitude = data.taskTrashCollection().trashcollection().longitude();
                        latitude = data.taskTrashCollection().trashcollection().latitude();
                        requestID = data.taskTrashCollection().trashcollection()._id();

                        position = new CameraPosition.Builder()
                                .target(new LatLng(latitude, longitude)).zoom(10).tilt(20)
                                .build();

                        mapView.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(@NonNull MapboxMap mapboxMap) {

                                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                                    @Override
                                    public void onStyleLoaded(@NonNull Style style) {
                                        RequestDetailsActivity.this.mapboxMap = mapboxMap;
                                        // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 10);
                                        mapboxMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).
                                                        title(nameText.getText().toString()));

                                        IconFactory iconFactory = IconFactory.getInstance(RequestDetailsActivity.this);
                                        Icon userIcon = iconFactory.fromResource(R.drawable.location);

                                        mapboxMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(userLat, userLong)).title("You")
                                                .icon(userIcon));

                                        Point originPosition = Point.fromLngLat(userLong, userLat);
                                        Point  dstPosition = Point.fromLngLat(longitude, latitude);

                                        navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                                        getRoute(originPosition, dstPosition);

                                    }
                                });

                            }
                        });

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

    public ApolloCall.Callback<TaskSortedWasteQuery.Data> taskSortedCallback(){
        return new ApolloCall.Callback<TaskSortedWasteQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<TaskSortedWasteQuery.Data> response) {
                TaskSortedWasteQuery.Data data = response.getData();


                if(data.taskSortedWaste() == null){

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
                        Log.d(TAG, "task fetched: " + data.taskSortedWaste());
                        taskLoads.setVisibility(View.GONE);
                        errorText.setVisibility(View.GONE);
                        try{
                            locationText.setText(data.taskSortedWaste().sortedWaste().location());
                            amountText.setText(data.taskSortedWaste().sortedWaste().amount());
                            nameText.setText(data.taskSortedWaste().sortedWaste().creator().fullName());
                            typeText.setText(data.taskSortedWaste().sortedWaste().typeOfWaste());
                            qualifierText.setText("Sorted Waste Collection");
                            longitude = data.taskSortedWaste().sortedWaste().longitude();
                            latitude = data.taskSortedWaste().sortedWaste().latitude();
                            requestID = data.taskSortedWaste().sortedWaste()._id();

                            position = new CameraPosition.Builder()
                                    .target(new LatLng(latitude, longitude)).zoom(10).tilt(20)
                                    .build();

                            mapView.getMapAsync(new OnMapReadyCallback() {
                                @Override
                                public void onMapReady(@NonNull MapboxMap mapboxMap) {

                                    mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                                        @Override
                                        public void onStyleLoaded(@NonNull Style style) {
                                            RequestDetailsActivity.this.mapboxMap = mapboxMap;
                                            // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                                            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 10);
                                            mapboxMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).
                                                    title(nameText.getText().toString()));

                                            IconFactory iconFactory = IconFactory.getInstance(RequestDetailsActivity.this);
                                            Icon userIcon = iconFactory.fromResource(R.drawable.location);

                                            mapboxMap.addMarker(new MarkerOptions()
                                                    .position(new LatLng(userLat, userLong)).title("You")
                                                    .icon(userIcon));

                                            Point originPosition = Point.fromLngLat(userLong, userLat);
                                            Point  dstPosition = Point.fromLngLat(longitude, latitude);

                                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                                            getRoute(originPosition, dstPosition);
                                        }
                                    });

                                }
                            });
                        }catch (Exception e){
                            e.printStackTrace();
                        }


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