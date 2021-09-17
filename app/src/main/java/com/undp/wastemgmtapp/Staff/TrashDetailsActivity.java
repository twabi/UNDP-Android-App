package com.undp.wastemgmtapp.Staff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.undp.wastemgmtapp.Common.GPSTracker;
import com.undp.wastemgmtapp.GetTrashcanQuery;
import com.undp.wastemgmtapp.R;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;

public class TrashDetailsActivity extends AppCompatActivity {

    private MapView mapView;
    String TAG = TrashDetailsActivity.class.getSimpleName();
    String canID;
    double userLat, userLong;
    TextView zoneNameText, canNameText, canIDText, levelText, errorText;
    ProgressBar canLevel;
    ApolloClient apolloClient;
    ProgressBar canLoads;
    double longitude, latitude;
    CameraPosition position;
    MapboxMap mapboxMap;
    private NavigationMapRoute navigationMapRoute;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_trash_details);

        mapView = findViewById(R.id.trash_map);
        canLevel = findViewById(R.id.progress);
        levelText = findViewById(R.id.percentage);
        zoneNameText = findViewById(R.id.zoneName);
        canNameText = findViewById(R.id.canName);
        canIDText = findViewById(R.id.canID);
        canLoads = findViewById(R.id.canLoads);
        errorText = findViewById(R.id.errorText);

        int viewHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        mapView.getLayoutParams().height = viewHeight - 800;
        GPSTracker gpsTracker = new GPSTracker(TrashDetailsActivity.this, TrashDetailsActivity.this);
        userLat = gpsTracker.getLatitude();
        userLong = gpsTracker.getLongitude();

        //initialize the toolbar
        Toolbar toolbar = findViewById(R.id.detailsToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //show the back button on the toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();
        canID = intent.getStringExtra("key");
        Log.d(TAG, "key: " + canID);
        canIDText.setText(canID);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        apolloClient = ApolloClient.builder().okHttpClient(httpClient)
                .serverUrl("https://waste-mgmt-api.herokuapp.com/graphql")
                .build();


        mapView.onCreate(savedInstanceState);
        apolloClient.query(new GetTrashcanQuery(canID)).enqueue(trashCallBack());

    }


    public ApolloCall.Callback<GetTrashcanQuery.Data> trashCallBack(){
        return new ApolloCall.Callback<GetTrashcanQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<GetTrashcanQuery.Data> response) {
                GetTrashcanQuery.Data data = response.getData();


                if(data.trashcan() == null){

                    if(response.getErrors() == null){
                        Log.e(TAG, "an unknown Error in task query : " );
                        runOnUiThread(() -> {
                            canLoads.setVisibility(View.GONE);
                            errorText.setVisibility(View.VISIBLE);
                            Toast.makeText(TrashDetailsActivity.this,
                                    "an unknown Error occurred : " , Toast.LENGTH_LONG).show();

                        });
                    } else{
                        List<Error> error = response.getErrors();
                        String errorMessage = error.get(0).getMessage();
                        Log.e(TAG, "an Error in task query : " + errorMessage );
                        runOnUiThread(() -> {
                            canLoads.setVisibility(View.GONE);
                            errorText.setVisibility(View.VISIBLE);
                            Toast.makeText(TrashDetailsActivity.this,
                                    "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();

                        });
                    }
                }else{
                    runOnUiThread(() -> {
                        Log.d(TAG, "task fetched: " + data.trashcan());
                        canLoads.setVisibility(View.GONE);
                        errorText.setVisibility(View.GONE);
                        canNameText.setText(data.trashcan().trashcanId());
                        zoneNameText.setText(data.trashcan().zone().name());
                        levelText.setText(String.valueOf(data.trashcan().status()));
                        longitude = data.trashcan().longitude();
                        latitude = data.trashcan().latitude();

                        Log.d(TAG, "latitude: " + latitude + "-" + "longitude: " + longitude);

                        Double d = Double.valueOf(data.trashcan().status());
                        int value = d.intValue();
                        if(value > 90){
                            Drawable progressDrawable = canLevel.getProgressDrawable().mutate();
                            progressDrawable.setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
                            canLevel.setProgressDrawable(progressDrawable);
                        }

                        canLevel.setProgress(value);

                        position = new CameraPosition.Builder()
                                .target(new LatLng(latitude, longitude)).zoom(14).tilt(20)
                                .build();
                        mapView.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(@NonNull MapboxMap mapboxMap) {

                                TrashDetailsActivity.this.mapboxMap = mapboxMap;
                                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                                    @Override
                                    public void onStyleLoaded(@NonNull Style style) {
                                        // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 10);

                                        // Create an Icon object for the marker to use
                                        IconFactory iconFactory = IconFactory.getInstance(TrashDetailsActivity.this);
                                        Icon icon = iconFactory.fromResource(R.drawable.bin);
                                        Icon userIcon = iconFactory.fromResource(R.drawable.location);

                                        mapboxMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(userLat, userLong)).title("You")
                                                .icon(userIcon));

                                        mapboxMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(latitude, longitude))
                                                .title(canNameText.getText().toString())
                                                .icon(icon));

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
                            Toast.makeText(TrashDetailsActivity.this,
                                    "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();

                        });
                    }
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e(TAG, "Error", e);
                runOnUiThread(() -> {
                    canLoads.setVisibility(View.GONE);
                    errorText.setVisibility(View.VISIBLE);
                    Toast.makeText(TrashDetailsActivity.this,
                            "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();

                });
            }
        };
    }


    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(TrashDetailsActivity.this)
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


    private void showToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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