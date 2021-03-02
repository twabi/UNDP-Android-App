package com.example.wastemgmtapp.Staff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.apollographql.apollo.ApolloClient;
import com.example.wastemgmtapp.Common.LogInActivity;
import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.normalUser.UserHomeActivity;
import com.google.android.material.navigation.NavigationView;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class StaffHomeActivity extends AppCompatActivity {

    private ActionBarDrawerToggle mToggle;
    private MapView mapRequests;
    private MapView mapTrash;
    ApolloClient apolloClient;
    String TAG = StaffHomeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_staff_home);

        Toolbar toolbar = findViewById(R.id.nav_action);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout2);
        TextView seeMoreRequests = findViewById(R.id.see_more_requests);
        TextView seeMoreTrash = findViewById(R.id.see_more_trash);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        apolloClient = ApolloClient.builder().okHttpClient(httpClient)
                .serverUrl("https://waste-mgmt-api.herokuapp.com/graphql")
                .build();

        setSupportActionBar(toolbar);

        mToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mapRequests = findViewById(R.id.map_requests);
        mapTrash = findViewById(R.id.map_trash);

        mapRequests.onCreate(savedInstanceState);
        mapTrash.onCreate(savedInstanceState);

        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(-15.786111, 35.005833)).zoom(10).tilt(20)
                .build();
        mapTrash.getMapAsync(new OnMapReadyCallback() {
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

        CameraPosition position2 = new CameraPosition.Builder()
                .target(new LatLng(-15.3766, 35.3357)).zoom(10).tilt(20)
                .build();
        mapRequests.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {

                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position2), 10);

                    }
                });

            }
        });

        seeMoreTrash.setOnClickListener(v -> {
            Intent intent = new Intent(StaffHomeActivity.this, ZoneTrashcans.class);
            startActivity(intent);
        });

        seeMoreRequests.setOnClickListener(v -> {
            Intent intent = new Intent(StaffHomeActivity.this, CollectionRequests.class);
            startActivity(intent);
        });

        NavigationView navView = findViewById(R.id.staff_navDrawer); // initiate a Navigation View
        // implement setNavigationSelectedListener event
        navView.setNavigationItemSelectedListener(menuItem -> {
            Log.d(TAG, "onOptionsItemSelected: " + menuItem);
            if(TextUtils.equals(menuItem.toString(), "Logout")){
                Intent intent = new Intent(StaffHomeActivity.this, LogInActivity.class);
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
        mapRequests.onStart();
        mapTrash.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapTrash.onResume();
        mapRequests.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapRequests.onPause();
        mapTrash.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapTrash.onStop();
        mapRequests.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapTrash.onSaveInstanceState(outState);
        mapRequests.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapRequests.onLowMemory();
        mapTrash.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapRequests.onDestroy();
        mapTrash.onDestroy();
    }

}