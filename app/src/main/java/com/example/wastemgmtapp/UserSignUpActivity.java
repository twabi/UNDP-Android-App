package com.example.wastemgmtapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.wastemgmtapp.type.UserInput;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.List;

public class UserSignUpActivity extends AppCompatActivity {


    EditText inputName, inputPhone, inputID, inputPassword, inputPasswordRepeat;
    ProgressBar loading;
    Button buttonCreateUser;
    ApolloClient apolloClient = ApolloClient.builder()
            .serverUrl("https://waste-mgmt-api.herokuapp.com/graphql")
            .build();
    String TAG = UserSignUpActivity.class.getSimpleName();

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
                        .fullName(name).nationalID(id).latitude(-15.786111).longitude(35.005833)
                        .phoneNumber(phone).password(password1).confirmPassword(password2).build();
                apolloClient.mutate(new CreateUserMutation(userInput)).enqueue(new ApolloCall.Callback<CreateUserMutation.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<CreateUserMutation.Data> response) {

                        CreateUserMutation.Data data = response.getData();
                        List<Error> error = response.getErrors();
                        String errorMessage = error.get(0).getMessage();

                        Log.d(TAG, "onResponse: " + data.createUser + "-" + data.createUser());

                        if(data.createUser == null){
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
        Boolean valid = true;
        String password1 = inputPassword.getText().toString();
        String password2 = inputPasswordRepeat.getText().toString();
        String name = inputName.getText().toString();
        String phone = inputPhone.getText().toString();
        String id = inputID.getText().toString();

        if(TextUtils.isEmpty(password1)){
            inputPassword.setError("Required.");
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

        return valid;
    }
}