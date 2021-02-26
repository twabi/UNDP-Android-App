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

import com.example.wastemgmtapp.Common.LogInActivity;
import com.example.wastemgmtapp.R;
import com.google.android.material.navigation.NavigationView;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

public class UserHomeActivity extends AppCompatActivity{

    private ActionBarDrawerToggle mToggle;
    private MapView mapView;
    private final String TAG = UserHomeActivity.class.getSimpleName();

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

        setSupportActionBar(toolbar);

        mToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(-15.786111, 35.005833)).zoom(10).tilt(20)
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

        cardRequest.setOnClickListener(v -> {
            Intent intent = new Intent(UserHomeActivity.this, RequestCollection.class);
            startActivity(intent);
        });

        cardReview.setOnClickListener(v -> {
            Intent intent = new Intent(UserHomeActivity.this, ReviewArea.class);
            startActivity(intent);
        });

        cardReport.setOnClickListener(v -> {
            Intent intent = new Intent(UserHomeActivity.this, ReportDumping.class);
            startActivity(intent);
        });

        cardRecord.setOnClickListener(v -> {
            Intent intent = new Intent(UserHomeActivity.this, RecordWaste.class);
            startActivity(intent);
        });

        NavigationView navView = findViewById(R.id.user_navDrawer); // initiate a Navigation View
        // implement setNavigationSelectedListener event
        navView.setNavigationItemSelectedListener(menuItem -> {
            Log.d(TAG, "onOptionsItemSelected: " + menuItem);
            if(TextUtils.equals(menuItem.toString(), "Logout")){
                Intent intent = new Intent(UserHomeActivity.this, LogInActivity.class);
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
}