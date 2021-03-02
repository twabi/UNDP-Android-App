package com.example.wastemgmtapp.normalUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.wastemgmtapp.Common.LogInActivity;
import com.example.wastemgmtapp.CreateUserMutation;
import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.type.UserInput;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UserSignUpActivity extends AppCompatActivity {


    EditText inputName, inputPhone, inputID, inputPassword, inputPasswordRepeat;
    ProgressBar loading;
    Button buttonCreateUser;
    ApolloClient apolloClient = ApolloClient.builder()
            .serverUrl("https://waste-mgmt-api.herokuapp.com/graphql")
            .build();
    String TAG = UserSignUpActivity.class.getSimpleName();
    FusedLocationProviderClient mFusedLocationClient;

    // Initializing other items
    // from layout file
    double lat = Double.parseDouble(null);
    double longitude = Double.parseDouble(null);
    int PERMISSION_ID = 44;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sign_up);

        inputID = findViewById(R.id.nationalID);
        inputName = findViewById(R.id.username);
        inputPhone = findViewById(R.id.phone);
        inputPassword = findViewById(R.id.passText);
        inputPasswordRepeat = findViewById(R.id.passText1);

        buttonCreateUser = findViewById(R.id.btn_sign);
        loading = findViewById(R.id.loads);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // method to get the location
        getLastLocation();

        TextView alreadyAccount = findViewById(R.id.already);
        alreadyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserSignUpActivity.this, LogInActivity.class);
                startActivity(intent);
            }
        });


        buttonCreateUser.setOnClickListener(v -> {
            if (!validate()) {
                Toast.makeText(UserSignUpActivity.this,"Fields Cannot be empty!!!", Toast.LENGTH_SHORT).show();
            } else {
                loading.setVisibility(View.VISIBLE);

                String password1 = inputPassword.getText().toString();
                String password2 = inputPasswordRepeat.getText().toString();
                String name = inputName.getText().toString();
                String phone = inputPhone.getText().toString();
                String id = inputID.getText().toString();

                UserInput userInput = UserInput.builder()
                        .fullName(name).nationalID(id).latitude(lat).longitude(longitude)
                        .phoneNumber(phone).password(password1).confirmPassword(password2).build();
                apolloClient.mutate(new CreateUserMutation(userInput)).enqueue(new ApolloCall.Callback<CreateUserMutation.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<CreateUserMutation.Data> response) {

                        CreateUserMutation.Data data = response.getData();
                        List<Error> error = response.getErrors();
                        assert error != null;
                        String errorMessage = error.get(0).getMessage();

                        assert data != null;
                        Log.d(TAG, "onResponse: " + data.createUser() + "-" + data.createUser());

                        if(data.createUser() == null){
                            Log.e("Apollo", "an Error occurred : " + errorMessage);
                            runOnUiThread(() -> {
                                // Stuff that updates the UI
                                loading.setVisibility(View.GONE);
                                Toast.makeText(UserSignUpActivity.this,
                                       errorMessage, Toast.LENGTH_LONG).show();
                            });
                        } else {
                            runOnUiThread(() -> {
                                // Stuff that updates the UI
                                loading.setVisibility(View.GONE);
                                Toast.makeText(UserSignUpActivity.this,
                                        "New User Created!", Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(UserSignUpActivity.this, LogInActivity.class);
                                startActivity(intent);
                            });
                        }

                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        Log.e("Apollo", "Error", e);
                        Toast.makeText(UserSignUpActivity.this,
                                "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
                        loading.setVisibility(View.GONE);
                    }
                });
            }

        });
    }

    public Boolean validate(){
        boolean valid = true;
        String password1 = inputPassword.getText().toString();
        String password2 = inputPasswordRepeat.getText().toString();
        String name = inputName.getText().toString();
        String phone = inputPhone.getText().toString();
        String id = inputID.getText().toString();

        if(TextUtils.isEmpty(password1)){
            inputPassword.setError("Required.");
            valid = false;
        }

        if(password1.length() < 8){
            inputPassword.setError("password should have 8 characters.");
            valid = false;
        }

        if(TextUtils.isEmpty(name)){
            inputName.setError("Required.");
            valid = false;
        }
        if(TextUtils.isEmpty(password2)){
            inputPasswordRepeat.setError("Required.");
            valid = false;
        }
        if(TextUtils.isEmpty(phone)){
            inputPhone.setError("Required.");
            valid = false;
        }

        if(TextUtils.isEmpty(id)){
            inputID.setError("Required.");
            valid = false;
        }

        if(!TextUtils.equals(password1, password2)){
            inputPasswordRepeat.setError("passwords don't match!");
            valid = false;
        }

        if(phone.contains("+")){
            inputPhone.setError("remove + on phone number.");
            valid = false;
        }

        if(!phone.contains("265")){
            inputPhone.setError("phone number invalid");
            valid = false;
        }

        if(lat == Double.parseDouble(null)  || longitude ==  Double.parseDouble(null)){
            Toast.makeText(UserSignUpActivity.this, "Location details empty! Enable location and try again!", Toast.LENGTH_LONG)
                    .show();
            valid = false;
        }

        return valid;
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last
                // location from
                // FusedLocationClient
                // object
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            //latitudeTextView.setText(location.getLatitude() + "");
                            //longitTextView.setText(location.getLongitude() + "");
                            lat = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            //latitudeTextView.setText("Latitude: " + mLastLocation.getLatitude() + "");
            //longitTextView.setText("Longitude: " + mLastLocation.getLongitude() + "");
            lat = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
        }
    };

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    // method to check
    // if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // If everything is alright then
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
    }
}
