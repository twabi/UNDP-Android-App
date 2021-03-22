package com.example.wastemgmtapp.Common;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.wastemgmtapp.LogInAsStaffMutation;
import com.example.wastemgmtapp.LogInMutation;
import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.Staff.StaffHomeActivity;
import com.example.wastemgmtapp.normalUser.UserHomeActivity;
import com.example.wastemgmtapp.normalUser.UserSignUpActivity;
import com.example.wastemgmtapp.type.LoginInput;
import com.example.wastemgmtapp.type.LoginStaffInput;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class LogInActivity extends AppCompatActivity {

    boolean loginStatus = false;
    EditText numberInput, passwordInput;
    ProgressBar loading;
    ApolloClient apolloClient;
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    String TAG = LogInActivity.class.getSimpleName();

    TextView loginAsStaffText, loginText, errorText, loginAsUserText;
    LinearLayout errorTextLayout;
    // Session Manager Class
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        TextView noAccountText = findViewById(R.id.noAccount);
        Button login = findViewById(R.id.btn_login);
        numberInput = findViewById(R.id.numberInput);
        passwordInput = findViewById(R.id.passwordInput);
        loading = findViewById(R.id.loads);
        loginAsStaffText = findViewById(R.id.loginStaff);
        loginText = findViewById(R.id.txt_user);
        errorTextLayout = findViewById(R.id.errorB);
        errorText = findViewById(R.id.errorText);
        loginAsUserText = findViewById(R.id.loginUser);

        session = new SessionManager(LogInActivity.this);
        //session.checkLogin();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        apolloClient = ApolloClient.builder().okHttpClient(httpClient)
                .serverUrl("https://waste-mgmt-api.herokuapp.com/graphql")
                .build();

        loginAsStaffText.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(LogInActivity.this);
            builder.setTitle(R.string.log_in_as_staff);
            builder.setMessage("Are you sure?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            loginStatus = true;
                            loginText.setText(R.string.log_in_Staff);
                            loginAsStaffText.setVisibility(View.GONE);
                            loginAsUserText.setVisibility(View.VISIBLE);
                        }
                    })
                    .setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            dialog.cancel();
                        }
                    });
            builder.show();
        });

        loginAsUserText.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(LogInActivity.this);
            builder.setTitle("Login As a User");
            builder.setMessage("Are you sure?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            loginStatus = false;
                            loginText.setText("Log In");
                            loginAsStaffText.setVisibility(View.VISIBLE);
                            loginAsUserText.setVisibility(View.GONE);
                        }
                    })
                    .setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            dialog.cancel();
                        }
                    });
            builder.show();
        });

        noAccountText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogInActivity.this, UserSignUpActivity.class);
                startActivity(intent);
            }
        });

        login.setOnClickListener(v -> {
            if(!validateForm()){
                Toast.makeText(LogInActivity.this,"Fields Cannot be empty!!!", Toast.LENGTH_SHORT).show();
            } else {
                if(!loginStatus){
                    logInUser();
                }else{
                    logInStaff();
                }
            }
        });
    }

    public void logInStaff(){

        loading.setVisibility(View.VISIBLE);
        String userNumber = numberInput.getText().toString();
        String password = passwordInput.getText().toString();

        LoginStaffInput input = LoginStaffInput.builder()
                .employeeID(userNumber)
                .password(password)
                .build();

        apolloClient.mutate(new LogInAsStaffMutation(input)).enqueue(new ApolloCall.Callback<LogInAsStaffMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<LogInAsStaffMutation.Data> response) {
                if(response.getErrors() == null){
                    LogInAsStaffMutation.Data data = response.getData();
                    Log.d(TAG, "onResponse: " + data.loginAsStaff());
                    String token = data.loginAsStaff().token();
                    String userID = data.loginAsStaff()._id();
                    String otherID = numberInput.getText().toString();
                    int expiration = data.loginAsStaff().tokenExpiration();

                    if(data.loginAsStaff() == null){
                        Log.e("Apollo", "an Error occurred : " );
                        runOnUiThread(() -> {
                            // Stuff that updates the UI
                            loading.setVisibility(View.GONE);
                            Toast.makeText(LogInActivity.this,
                                    "an Error occurred : " , Toast.LENGTH_LONG).show();
                            //errorText.setText();
                        });
                    } else {
                        runOnUiThread(() -> {
                            // Stuff that updates the UI
                            loading.setVisibility(View.GONE);
                            Toast.makeText(LogInActivity.this,
                                    "Login successful!", Toast.LENGTH_LONG).show();
                            loginStatus = false;
                            loginText.setText("Log In");
                            session.createLoginSession(userID, otherID, "Staff");

                            Intent intent = new Intent(LogInActivity.this, StaffHomeActivity.class);
                            intent.putExtra("tokenExpiration", expiration);
                            intent.putExtra("token", token);
                            intent.putExtra("id", userID);
                            startActivity(intent);
                        });
                    }
                } else {
                    List<Error> error = response.getErrors();
                    String errorMessage = error.get(0).getMessage();
                    Log.e("Apollo", "an Error occurred : " + errorMessage );
                    loginStatus = false;
                    runOnUiThread(() -> {
                        loginText.setText("Log In");
                        // Stuff that updates the UI
                        loading.setVisibility(View.GONE);
                        Toast.makeText(LogInActivity.this,
                                "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();
                        errorText.setText(errorMessage);
                        errorTextLayout.setVisibility(View.VISIBLE);

                    });
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                runOnUiThread(() -> {
                    e.getCause();
                    Log.e("Apollo", "Error", e);
                    Log.e("More details", e.getLocalizedMessage() , e);
                    Toast.makeText(LogInActivity.this,
                            "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
                    loading.setVisibility(View.GONE);
                    errorText.setText(e.getLocalizedMessage());
                    errorTextLayout.setVisibility(View.VISIBLE);
                });
            }
        });
    }


    public void logInUser(){

        loading.setVisibility(View.VISIBLE);
        String userNumber = numberInput.getText().toString();
        String password = passwordInput.getText().toString();

        LoginInput input = LoginInput.builder()
                .nationalID(userNumber)
                .password(password)
                .build();

        apolloClient.mutate(new LogInMutation(input)).enqueue(new ApolloCall.Callback<LogInMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<LogInMutation.Data> response) {
                if(response.getErrors() == null){
                    LogInMutation.Data data = response.getData();
                    Log.d(TAG, "onResponse: " + data.login());
                    String token = data.login().token();
                    String userID = data.login()._id();
                    int expiration = data.login().tokenExpiration();

                    if(data.login() == null){
                        Log.e("Apollo", "an Error occurred : " );
                        runOnUiThread(() -> {
                            // Stuff that updates the UI
                            loading.setVisibility(View.GONE);
                            Toast.makeText(LogInActivity.this,
                                    "an Error occurred : " , Toast.LENGTH_LONG).show();
                        });
                    } else {
                        runOnUiThread(() -> {
                            // Stuff that updates the UI
                            loading.setVisibility(View.GONE);
                            Toast.makeText(LogInActivity.this,
                                    "Login successful!", Toast.LENGTH_LONG).show();

                            session.createLoginSession(userID, userNumber, "User");

                            Intent intent = new Intent(LogInActivity.this, UserHomeActivity.class);
                            intent.putExtra("token", token);
                            intent.putExtra("id", userID);
                            intent.putExtra("tokenExpiraton", expiration);
                            startActivity(intent);
                        });
                    }
                } else {
                    List<Error> error = response.getErrors();
                    String errorMessage = error.get(0).getMessage();
                    Log.e("Apollo", "an Error occurred : " + errorMessage );
                    runOnUiThread(() -> {
                        // Stuff that updates the UI
                        loading.setVisibility(View.GONE);
                        Toast.makeText(LogInActivity.this,
                                "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();
                        errorText.setText(errorMessage);
                        errorTextLayout.setVisibility(View.VISIBLE);
                    });
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                runOnUiThread(() -> {
                    e.getCause();
                    Log.e("Apollo", "Error", e);
                    Log.e("More details", e.getLocalizedMessage() , e);
                    Toast.makeText(LogInActivity.this,
                            "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
                    loading.setVisibility(View.GONE);
                    errorText.setText(e.getLocalizedMessage());
                    errorTextLayout.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    public Boolean validateForm(){
        boolean valid = true;
        String number = numberInput.getText().toString();
        String password = passwordInput.getText().toString();

        if(TextUtils.isEmpty(number)){
            numberInput.setError("Required.");
            valid = false;
        }

        if(TextUtils.isEmpty(password)){
            passwordInput.setError("Required.");
            valid = false;
        }
        return valid;
    }
}