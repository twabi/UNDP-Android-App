package com.example.wastemgmtapp.normalUser;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.wastemgmtapp.Common.GPSTracker;
import com.example.wastemgmtapp.Common.SessionManager;
import com.example.wastemgmtapp.GetCollectionNotifsQuery;
import com.example.wastemgmtapp.GetSortedWasteNotifsQuery;
import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.Common.SettingsActivity;
import com.example.wastemgmtapp.UserQuery;
import com.example.wastemgmtapp.ZonesQuery;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;
import com.mapbox.mapboxsdk.Mapbox;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class UserHomeActivity extends AppCompatActivity{

    private ActionBarDrawerToggle mToggle;
    private final String TAG = UserHomeActivity.class.getSimpleName();
    double userLat, userLong;
    ApolloClient apolloClient;
    Double maxRating;
    String maxLocation;
    TextView textUserName;
    LinearLayout linearCollect, linearSorted, gotoSettings, gotoRequests, gotoInstitutions;
    TextView collectNumber, sortedNumber;
    SessionManager session;
    FusedLocationProviderClient mFusedLocationClient;
    TextView sortNumber1, collNumber1;
    double zoneLat,zoneLong;
    String userID;

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
        linearCollect = findViewById(R.id.trash_collect);
        linearSorted = findViewById(R.id.sorted_waste);
        collectNumber = findViewById(R.id.collNumber);
        sortedNumber = findViewById(R.id.sortNumber);
        gotoSettings = findViewById(R.id.gotoSettings);
        gotoInstitutions = findViewById(R.id.gotoCompanies);
        gotoRequests = findViewById(R.id.gotoRequests);
        sortNumber1 = findViewById(R.id.sortNumber1);
        collNumber1 = findViewById(R.id.collNumber1);

        session = new SessionManager(getApplicationContext());

        NavigationView navView = findViewById(R.id.user_navDrawer); // initiate a Navigation View

        View headerView = navView.getHeaderView(0);
        TextView text_support = headerView.findViewById(R.id.text_support);
        textUserName = headerView.findViewById(R.id.userName);
        text_support.setText("User");

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        apolloClient = ApolloClient.builder().okHttpClient(httpClient)
                .serverUrl("https://waste-mgmt-api.herokuapp.com/graphql")
                .build();

        HashMap<String, String> user = session.getUserDetails();
        userID = user.get(SessionManager.KEY_USERID);

        setSupportActionBar(toolbar);

        mToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        drawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        apolloClient.query(new UserQuery(userID)).enqueue(usersCallBack());
        //apolloClient.query(new ZonesQuery()).enqueue(zonesQuery());
        apolloClient.query(new GetCollectionNotifsQuery()).enqueue(collectCallback());
        apolloClient.query(new GetSortedWasteNotifsQuery()).enqueue(sortedCallback());

        GPSTracker gpsTracker = new GPSTracker(UserHomeActivity.this, UserHomeActivity.this);
        userLat = gpsTracker.getLatitude();
        userLong = gpsTracker.getLongitude();

        if(userLong == 0.0 && userLat == 0.0 ){
            Toast.makeText(UserHomeActivity.this,
                    "Could not obtain location! Enable the gps location or network on your phone and try again!", Toast.LENGTH_LONG).show();
        }
        Log.d(TAG, "Latitude: " + gpsTracker.getLatitude() +"-Longitude: "+ gpsTracker.getLongitude());

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

        gotoRequests.setOnClickListener(view -> {
            Intent intent = new Intent(UserHomeActivity.this, MyRequests.class);
            intent.putExtra("id", userID);
            startActivity(intent);
        });

        gotoInstitutions.setOnClickListener(view ->{
            Intent intent = new Intent(UserHomeActivity.this, InstitutionsActivity.class);
            intent.putExtra("id", userID);
            startActivity(intent);
        });

        gotoSettings.setOnClickListener(view -> {
            Intent intent = new Intent(UserHomeActivity.this, SettingsActivity.class);
            intent.putExtra("id", userID);
            startActivity(intent);
        });

        // implement setNavigationSelectedListener event
        navView.setNavigationItemSelectedListener(menuItem -> {
            Log.d(TAG, "onOptionsItemSelected: " + menuItem);
            if(TextUtils.equals(menuItem.toString(), "Logout")){
                AlertDialog.Builder builder = new AlertDialog.Builder(UserHomeActivity.this);
                builder.setTitle("Log Out").setMessage("Are you sure you want to log out?");

                builder.setPositiveButton("Log Out", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        session.logoutUser();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.cancel();
                    }
                });
                builder.show(); //show the alert dialog

                //Intent intent = new Intent(UserHomeActivity.this, LogInActivity.class);
                //startActivity(intent);
            } else if((TextUtils.equals(menuItem.toString(), "Request Collection"))){
                Intent intent = new Intent(UserHomeActivity.this, RequestCollection.class);
                intent.putExtra("id", userID);
                intent.putExtra("lat", userLat);
                intent.putExtra("long", userLong);
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
                                ArrayList<Double> lat = new ArrayList<>();
                                ArrayList<String> longitudes = new ArrayList<>();
                                for(int i =0; i < data.zones().size(); i++){
                                    ratings.add(data.zones().get(i).averageRating());
                                    locations.add(data.zones().get(i).location());
                                    longitudes.add(data.zones().get(i).longitude());
                                    lat.add(data.zones().get(i).latitude());
                                }

                                Double maxVal = Collections.max(ratings);
                                int maxIdx = ratings.indexOf(maxVal);
                                //ratingText.setText("Rating : " + maxVal);
                                String locale = locations.get(maxIdx);
                                //locationName.setText(locale);

                                maxLocation = locations.get(maxIdx);
                                maxRating = Collections.max(ratings);
                                zoneLat = Double.parseDouble(String.valueOf(lat.get(maxIdx)));
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
                runOnUiThread(() -> {
                    Toast.makeText(UserHomeActivity.this,
                            "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

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
                runOnUiThread(() -> {
                    Toast.makeText(UserHomeActivity.this,
                            "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();

                });
            }
        };


    }


    public ApolloCall.Callback<GetSortedWasteNotifsQuery.Data> sortedCallback(){
        return new ApolloCall.Callback<GetSortedWasteNotifsQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<GetSortedWasteNotifsQuery.Data> response) {
                GetSortedWasteNotifsQuery.Data data = response.getData();

                if(response.getErrors() == null){

                    if(data.sortedWasteNotications() == null){
                        Log.e("Apollo", "an Error occurred : " );
                        runOnUiThread(() -> {
                            // Stuff that updates the UI
                            Toast.makeText(UserHomeActivity.this,
                                    "an Error occurred : " , Toast.LENGTH_LONG).show();
                        });
                    }else{
                        runOnUiThread(() -> {
                            Log.d(TAG, "notifs fetched" + data.sortedWasteNotications());
                            Log.d(TAG, "notifs fetched" + data.sortedWasteNotications());
                            ArrayList complete = new ArrayList<>();
                            ArrayList incomplete = new ArrayList<>();

                            for(int i =0; i < data.sortedWasteNotications().size(); i++){
                                if(data.sortedWasteNotications().get(i).status().equals("accepted") &&
                                userID.equals(data.sortedWasteNotications().get(i).creator()._id())){
                                    complete.add(data.sortedWasteNotications().get(i));
                                } else {
                                    incomplete.add(data.sortedWasteNotications().get(i));
                                }
                            }
                            int completeSize = complete.size();
                            int pendingSize = incomplete.size();
                            Log.d(TAG, "onResponse: " + completeSize +"-"+ pendingSize);
                            sortedNumber.setText(String.valueOf(completeSize));
                            sortNumber1.setText(String.valueOf(pendingSize));

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
                runOnUiThread(() -> {
                    Toast.makeText(UserHomeActivity.this,
                            "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();

                });
            }
        };
    }

    public ApolloCall.Callback<GetCollectionNotifsQuery.Data> collectCallback(){
        return new ApolloCall.Callback<GetCollectionNotifsQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<GetCollectionNotifsQuery.Data> response) {
                GetCollectionNotifsQuery.Data data = response.getData();

                if(response.getErrors() == null){

                    if(data.trashCollectionNotications() == null){
                        Log.e("Apollo", "an Error occurred : " );
                        runOnUiThread(() -> {
                            // Stuff that updates the UI
                            Toast.makeText(UserHomeActivity.this,
                                    "an Error occurred : " , Toast.LENGTH_LONG).show();
                        });
                    }else{
                        runOnUiThread(() -> {
                            Log.d(TAG, "notifs fetched" + data.trashCollectionNotications());
                            ArrayList complete = new ArrayList<>();
                            ArrayList incomplete = new ArrayList<>();

                            for(int i =0; i < data.trashCollectionNotications().size(); i++){
                                if(data.trashCollectionNotications().get(i).status().equals("accepted")&&
                                        userID.equals(data.trashCollectionNotications().get(i).creator()._id())){
                                    complete.add(data.trashCollectionNotications().get(i));
                                } else {
                                    incomplete.add(data.trashCollectionNotications().get(i));
                                }
                            }
                            int completeSize = complete.size();
                            int pendingSize = incomplete.size();
                            Log.d(TAG, "onResponse: " + completeSize +"-"+ pendingSize);
                            collectNumber.setText(String.valueOf(completeSize));
                            collNumber1.setText(String.valueOf(pendingSize));

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
                runOnUiThread(() -> {
                    Toast.makeText(UserHomeActivity.this,
                            "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();

                });
            }
        };


    }

}