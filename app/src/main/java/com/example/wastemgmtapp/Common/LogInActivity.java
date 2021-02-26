package com.example.wastemgmtapp.Common;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
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
import com.example.wastemgmtapp.GetStaffQuery;
import com.example.wastemgmtapp.LogInAsStaffMutation;
import com.example.wastemgmtapp.LogInMutation;
import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.UsersQuery;
import com.example.wastemgmtapp.normalUser.UserHomeActivity;
import com.example.wastemgmtapp.normalUser.UserSignUpActivity;
import com.example.wastemgmtapp.type.LoginInput;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.Request;

public class LogInActivity extends AppCompatActivity {

    EditText numberInput, passwordInput;
    OkHttpClient client = new OkHttpClient();
    ProgressBar loading;
    ApolloClient apolloClient;
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    String TAG = LogInActivity.class.getSimpleName();
    String url = "https://waste-mgmt-api.herokuapp.com/graphql";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        TextView noAccountText = findViewById(R.id.noAccount);
        Button login = findViewById(R.id.btn_login);
        numberInput = findViewById(R.id.numberInput);
        passwordInput = findViewById(R.id.passwordInput);
        loading = findViewById(R.id.loads);

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


                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
                OkHttpClient httpClient = new OkHttpClient.Builder()
                        .addInterceptor(logging)
                        .build();

                apolloClient = ApolloClient.builder().okHttpClient(httpClient)
                        .serverUrl("https://waste-mgmt-api.herokuapp.com/graphql")
                        .build();

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

                                    Intent intent = new Intent(LogInActivity.this, UserHomeActivity.class);
                                    intent.putExtra("token", token);
                                    intent.putExtra("id", userID);
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
                        });
                    }
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