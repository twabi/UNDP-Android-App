package com.example.wastemgmtapp.normalUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.wastemgmtapp.Common.GPSTracker;
import com.example.wastemgmtapp.Common.LogInActivity;
import com.example.wastemgmtapp.Common.SessionManager;
import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.UserQuery;
import com.example.wastemgmtapp.ZonesQuery;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.Style;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class UserHomeActivity extends AppCompatActivity{

    private ActionBarDrawerToggle mToggle;
    private MapView mapView;
    private final String TAG = UserHomeActivity.class.getSimpleName();
    double userLat, userLong;
    ApolloClient apolloClient;
    TextView textUserName, locationName, ratingText;
    Double maxRating;
    String maxLocation;
    SessionManager session;
    FusedLocationProviderClient mFusedLocationClient;
    double zoneLat,zoneLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_user_home);

        Toolbar toolbar = findViewById(R.id.nav_action);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        CardView cardRequest = findViewById(R.id.cardRequest);
        CardView cardReview = findViewById(R.id.cardReview);
        CardView cardReport = findViewById(R.id.cardReport);
        CardView cardRecord = findViewById(R.id.cardRecord);

        Button rate = findViewById(R.id.btn_rate);
        Button share = findViewById(R.id.btn_share);
        locationName = findViewById(R.id.locationName);
        ratingText = findViewById(R.id.averageRating);

        session = new SessionManager(getApplicationContext());

        NavigationView navView = findViewById(R.id.user_navDrawer); // initiate a Navigation View

        View headerView = navView.getHeaderView(0);
        TextView text_support = (TextView) headerView.findViewById(R.id.text_support);
        textUserName = (TextView) headerView.findViewById(R.id.userName);
        text_support.setText("");

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        apolloClient = ApolloClient.builder().okHttpClient(httpClient)
                .serverUrl("https://waste-mgmt-api.herokuapp.com/graphql")
                .build();

        HashMap<String, String> user = session.getUserDetails();
        String userID = user.get(SessionManager.KEY_USERID);

        setSupportActionBar(toolbar);

        mToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        drawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        apolloClient.query(new UserQuery(userID)).enqueue(usersCallBack());
        apolloClient.query(new ZonesQuery()).enqueue(zonesQuery());

        GPSTracker gpsTracker = new GPSTracker(UserHomeActivity.this, UserHomeActivity.this);
        userLat = gpsTracker.getLatitude();
        userLong = gpsTracker.getLongitude();

        if(userLong == 0.0 && userLat == 0.0 ){
            Toast.makeText(UserHomeActivity.this,
                    "Could not obtain location! Enable the gps location or network on your phone and try again!", Toast.LENGTH_LONG).show();
        }
        Log.d(TAG, "Latitude: " + gpsTracker.getLatitude() +"-Longitude: "+ gpsTracker.getLongitude());


        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(zoneLat, zoneLong)).zoom(15).tilt(20)
                .build();
        mapView.getMapAsync(mapboxMap -> mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 10);

            }
        }));

        cardRequest.setOnClickListener(v -> {
            Intent intent = new Intent(UserHomeActivity.this, RequestCollection.class);
            intent.putExtra("id", userID);
            intent.putExtra("lat", userLat);
            intent.putExtra("long", userLong);
            startActivity(intent);
        });

        cardReview.setOnClickListener(v -> {
            Intent intent = new Intent(UserHomeActivity.this, ReviewArea.class);
            intent.putExtra("id", userID);
            intent.putExtra("lat", userLat);
            intent.putExtra("long", userLong);
            startActivity(intent);
        });

        cardReport.setOnClickListener(v -> {
            Intent intent = new Intent(UserHomeActivity.this, ReportDumping.class);
            intent.putExtra("id", userID);
            intent.putExtra("lat", userLat);
            intent.putExtra("long", userLong);
            startActivity(intent);
        });

        cardRecord.setOnClickListener(v -> {
            Intent intent = new Intent(UserHomeActivity.this, RecordWaste.class);
            intent.putExtra("id", userID);
            intent.putExtra("lat", userLat);
            intent.putExtra("long", userLong);
            startActivity(intent);
        });

        rate.setOnClickListener( view -> {
            Intent intent = new Intent(UserHomeActivity.this, ReviewArea.class);
            intent.putExtra("id", userID);
            intent.putExtra("lat", userLat);
            intent.putExtra("long", userLong);
            startActivity(intent);
        });

        share.setOnClickListener( view -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    maxLocation + " is the cleanest zone in the city with a Rating of " + maxRating+"!");
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, "Share News");
            startActivity(shareIntent);
        });

        // implement setNavigationSelectedListener event
        navView.setNavigationItemSelectedListener(menuItem -> {
            Log.d(TAG, "onOptionsItemSelected: " + menuItem);
            if(TextUtils.equals(menuItem.toString(), "Logout")){
                Intent intent = new Intent(UserHomeActivity.this, LogInActivity.class);
                startActivity(intent);
            } else if((TextUtils.equals(menuItem.toString(), "My Requests"))){
                Intent intent = new Intent(UserHomeActivity.this, MyRequests.class);
                //intent.putExtra("id", userID);
                //intent.putExtra("lat", userLat);
                //intent.putExtra("long", userLong);
                startActivity(intent);
            }else if((TextUtils.equals(menuItem.toString(), "Report Illegal Waste"))){
                Intent intent = new Intent(UserHomeActivity.this, ReportDumping.class);
                intent.putExtra("id", userID);intent.putExtra("lat", userLat);intent.putExtra("long", userLong);
                startActivity(intent);
            }else if((TextUtils.equals(menuItem.toString(), "Review Area"))){
                Intent intent = new Intent(UserHomeActivity.this, ReviewArea.class);
                intent.putExtra("id", userID);intent.putExtra("lat", userLat);intent.putExtra("long", userLong);
                startActivity(intent);
            }else if((TextUtils.equals(menuItem.toString(), "Record Sorted Waste"))){
                Intent intent = new Intent(UserHomeActivity.this, RecordWaste.class);
                intent.putExtra("id", userID);intent.putExtra("lat", userLat);intent.putExtra("long", userLong);
                startActivity(intent);
            }
            // add code here what you need on click of items.
            return false;
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)){

            return true;
        }

        return super.onOptionsItemSelected(item);
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

    public ApolloCall.Callback<ZonesQuery.Data> zonesQuery(){
        return new ApolloCall.Callback<ZonesQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<ZonesQuery.Data> response) {
                ZonesQuery.Data data = response.getData();

                if(response.getErrors() == null){

                    if(data.zones() == null){
                        Log.e("Apollo", "an Error occurred : " );
                        runOnUiThread(() -> {
                            // Stuff that updates the UI
                            Toast.makeText(UserHomeActivity.this,
                                    "an Error occurred : " , Toast.LENGTH_LONG).show();
                            //errorText.setText();
                        });
                    }else{
                        runOnUiThread(() -> {
                            // Stuff that updates the UI
                            //Toast.makeText(UserHomeActivity.this,
                            //"User fetched!", Toast.LENGTH_LONG).show();
                            try{
                                Log.d(TAG, "zones fetched" + data.zones());
                                ArrayList<Double> ratings = new ArrayList<>();
                                ArrayList<String> locations = new ArrayList<>();
                                ArrayList<String> lat = new ArrayList<>();
                                ArrayList<String> longitudes = new ArrayList<>();
                                for(int i =0; i < data.zones().size(); i++){
                                    ratings.add(data.zones().get(i).averageRating());
                                    locations.add(data.zones().get(i).location());
                                    longitudes.add(data.zones().get(i).longitude());
                                    lat.add(data.zones().get(i).latitude());
                                }

                                Double maxVal = Collections.max(ratings);
                                int maxIdx = ratings.indexOf(maxVal);
                                ratingText.setText("Rating : " + maxVal);
                                String locale = locations.get(maxIdx);
                                locationName.setText(locale);

                                maxLocation = locations.get(maxIdx);
                                maxRating = Collections.max(ratings);
                                zoneLat = Double.parseDouble(lat.get(maxIdx));
                                zoneLong = Double.parseDouble(longitudes.get(maxIdx));

                                Log.d(TAG, "onResponse: " + zoneLat + "-" + zoneLong);
                            } catch (Exception e){
                                e.printStackTrace();
                                Toast.makeText(UserHomeActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }


                        });
                    }

                } else{
                    List<Error> error = response.getErrors();
                    String errorMessage = error.get(0).getMessage();
                    Log.e("Apollo", "an Error occurred : " + errorMessage );
                    runOnUiThread(() -> {
                        Toast.makeText(UserHomeActivity.this,
                                "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();

                    });
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e("Apollo", "Error", e);
                Toast.makeText(UserHomeActivity.this,
                        "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
    }

    public ApolloCall.Callback<UserQuery.Data> usersCallBack(){
        return new ApolloCall.Callback<UserQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<UserQuery.Data> response) {
                UserQuery.Data data = response.getData();

                if(response.getErrors() == null){

                    if(data.user() == null){
                        Log.e("Apollo", "an Error occurred : " );
                        runOnUiThread(() -> {
                            // Stuff that updates the UI
                            Toast.makeText(UserHomeActivity.this,
                                    "an Error occurred : " , Toast.LENGTH_LONG).show();
                            //errorText.setText();
                        });
                    }else{
                        runOnUiThread(() -> {
                            // Stuff that updates the UI
                            //Toast.makeText(UserHomeActivity.this,
                            //"User fetched!", Toast.LENGTH_LONG).show();
                            Log.d(TAG, "user fetched" + data.user());
                            //userLat = data.user().latitude();
                            //userLong = data.user().longitude();
                            textUserName.setText(data.user().fullName());

                        });
                    }

                } else{
                    List<Error> error = response.getErrors();
                    String errorMessage = error.get(0).getMessage();
                    Log.e("Apollo", "an Error occurred : " + errorMessage );
                    runOnUiThread(() -> {
                        Toast.makeText(UserHomeActivity.this,
                                "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();

                    });
                }

            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e("Apollo", "Error", e);
                Toast.makeText(UserHomeActivity.this,
                        "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };


    }
}