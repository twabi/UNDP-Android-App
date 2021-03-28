package com.example.wastemgmtapp.Common;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.wastemgmtapp.CreateIllegalDumpingMutation;
import com.example.wastemgmtapp.GetStaffQuery;
import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.UpdatePasswordMutation;
import com.example.wastemgmtapp.UpdateStaffPasswordMutation;
import com.example.wastemgmtapp.UpdateUserNameMutation;
import com.example.wastemgmtapp.UserQuery;
import com.example.wastemgmtapp.normalUser.ReportDumping;
import com.example.wastemgmtapp.normalUser.UserHomeActivity;
import com.example.wastemgmtapp.type.ChangePasswordInput;
import com.example.wastemgmtapp.type.UpdateUserInput;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class SettingsActivity extends AppCompatActivity {

    Toolbar toolbar;
    AlertDialog dialog, passDialog;
    CardView shareCard, changePassCard, changeNameCard, viewCard;
    TextView fullname, location, phoneNumber, createdAt, nationalID;
    ApolloClient apolloClient;
    ProgressBar loading, loadChange;
    String TAG = SettingsActivity.class.getSimpleName();
    EditText nameInput, passInput, oldPassInput;
    String userID;
    SessionManager session;
    boolean isStaff = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = findViewById(R.id.setToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        session = new SessionManager(getApplicationContext());

        shareCard = findViewById(R.id.share);
        changeNameCard = findViewById(R.id.changeName);
        changePassCard = findViewById(R.id.changePass);
        viewCard = findViewById(R.id.viewMe);

        Intent intent = getIntent();
        userID = intent.getStringExtra("id");

        HashMap<String, String> user = session.getUserDetails();
        String status = user.get(SessionManager.KEY_STATUS);
        if(!TextUtils.isEmpty(status)){
            isStaff = !status.equals("User");
        }

        if(isStaff){
            changeNameCard.setVisibility(View.GONE);
        }

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        apolloClient = ApolloClient.builder().okHttpClient(httpClient)
                .serverUrl("https://waste-mgmt-api.herokuapp.com/graphql")
                .build();


        shareCard.setOnClickListener(view -> {

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Waste Mgmt App");
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Waste Mgmt App \n Use this app to keep your city clean! find it on google play!");
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        });

        changeNameCard.setOnClickListener(view -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
            builder.setTitle("Change User Name"); //set the title for the dialog
            LayoutInflater inflater = (LayoutInflater) SettingsActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            assert inflater != null;
            //build the dialog and set the view from the layout already created
            view = inflater.inflate(R.layout.change_user, null);
            builder.setView(view);

            nameInput = view.findViewById(R.id.new_username);
            loadChange = view.findViewById(R.id.loadIt);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String name = nameInput.getText().toString();
                    if(TextUtils.isEmpty(name)){
                        nameInput.setError("Required.");
                        Toast.makeText(SettingsActivity.this, "Fields cannot be left empty!!",
                                Toast.LENGTH_SHORT).show();
                    } else{
                        loadChange.setVisibility(View.VISIBLE);

                        UpdateUserInput userInput = UpdateUserInput.builder()
                                .name(name)
                                ._id(userID)
                                .build();
                        Input<UpdateUserInput> input = new Input<>(userInput, true);
                        apolloClient.mutate(new UpdateUserNameMutation(input)).enqueue(changeNameCallback());

                    }
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    dialogInterface.cancel();
                }
            });

            dialog = builder.create();
            dialog.show();

        });

        changePassCard.setOnClickListener(view -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
            builder.setTitle("Change User Password"); //set the title for the dialog
            LayoutInflater inflater = (LayoutInflater) SettingsActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            assert inflater != null;
            //build the dialog and set the view from the layout already created
            view = inflater.inflate(R.layout.change_pass, null);
            builder.setView(view);

            passInput = view.findViewById(R.id.new_password);
            oldPassInput = view.findViewById(R.id.old_pass);
            loadChange = view.findViewById(R.id.loadIt);


            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String oldPassword = oldPassInput.getText().toString();
                    String newPassword = passInput.getText().toString();
                    if(TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword)){
                        Toast.makeText(SettingsActivity.this, "Fields cannot be left empty!!",
                                Toast.LENGTH_SHORT).show();
                    } else{
                        loadChange.setVisibility(View.VISIBLE);
                        if(!isStaff){

                            ChangePasswordInput passwordInput = ChangePasswordInput.builder()
                                    ._id(userID)
                                    .currentPassword(oldPassword)
                                    .newPassword(newPassword)
                                    .build();
                            Input<ChangePasswordInput> input = new Input<>(passwordInput, true);
                            apolloClient.mutate(new UpdatePasswordMutation(input)).enqueue(changePassCallback());

                        }else{
                            ChangePasswordInput passwordInput = ChangePasswordInput.builder()
                                    ._id(userID)
                                    .currentPassword(oldPassword)
                                    .newPassword(newPassword)
                                    .build();
                            Input<ChangePasswordInput> input = new Input<>(passwordInput, true);
                            apolloClient.mutate(new UpdateStaffPasswordMutation(input)).enqueue(changeStaffPass());
                        }
                    }

                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    dialogInterface.cancel();
                }
            });

            passDialog = builder.create();
            passDialog.show();

        });


        viewCard.setOnClickListener(view -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
            builder.setTitle("Profile"); //set the title for the dialog
            LayoutInflater inflater = (LayoutInflater) SettingsActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            assert inflater != null;
            //build the dialog and set the view from the layout already created
            view = inflater.inflate(R.layout.user_details_dialog, null);
            builder.setView(view);

            nationalID = view.findViewById(R.id.id);
            fullname = view.findViewById(R.id.name);
            location = view.findViewById(R.id.locationText);
            phoneNumber = view.findViewById(R.id.phone);
            createdAt = view.findViewById(R.id.createdAt);
            loading = view.findViewById(R.id.nameLoad);

            if(!isStaff){
                apolloClient.query(new UserQuery(userID)).enqueue(userCallback());
            }else {
                apolloClient.query(new GetStaffQuery(userID)).enqueue(staffCallback());
            }

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            dialog = builder.create();
            dialog.show();

        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public ApolloCall.Callback<UserQuery.Data> userCallback(){
        return new ApolloCall.Callback<UserQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<UserQuery.Data> response) {
                UserQuery.Data data = response.getData();

                    if(data.user() == null){
                        if(response.getErrors() == null){
                            Log.e("Apollo", "an Error occurred : " );
                            runOnUiThread(() -> {
                                // Stuff that updates the UI
                                Toast.makeText(SettingsActivity.this,
                                        "an Error occurred : " , Toast.LENGTH_LONG).show();
                                loading.setVisibility(View.GONE);
                            });

                        } else{
                            List<Error> error = response.getErrors();
                            String errorMessage = error.get(0).getMessage();
                            Log.e("Apollo", "an Error occurred : " + errorMessage );
                            runOnUiThread(() -> {
                                Toast.makeText(SettingsActivity.this,
                                        "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();
                                loading.setVisibility(View.GONE);
                            });
                        }
                    }else{
                        runOnUiThread(() -> {
                            loading.setVisibility(View.GONE);
                            Log.d(TAG, "user fetched" + data.user());
                            fullname.setText("User Name:  " + data.user().fullName());
                            nationalID.setText("National ID:  " + data.user().nationalID());
                            location.setText("Location:  " + data.user().location());
                            createdAt.setText("Date Created:  " + data.user().createdAt());
                            phoneNumber.setText("Phone Number:  " + data.user().phoneNumber());

                        });

                        if(response.getErrors() != null){
                            List<Error> error = response.getErrors();
                            String errorMessage = error.get(0).getMessage();
                            Log.e("Apollo", "an Error occurred : " + errorMessage );
                            runOnUiThread(() -> {
                                Toast.makeText(SettingsActivity.this,
                                        "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();

                            });
                        }
                    }



            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e("Apollo", "Error", e);
                Toast.makeText(SettingsActivity.this,
                        "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
                loading.setVisibility(View.GONE);
            }
        };
    }

    public ApolloCall.Callback<UpdatePasswordMutation.Data> changePassCallback(){
        return new ApolloCall.Callback<UpdatePasswordMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<UpdatePasswordMutation.Data> response) {
                UpdatePasswordMutation.Data data = response.getData();

                if(response.getErrors() == null){

                    if(data.changeUserPassword() == null){
                        Log.e("Apollo", "an Error occurred : " );
                        runOnUiThread(() -> {
                            // Stuff that updates the UI
                            Toast.makeText(SettingsActivity.this,
                                    "an Error occurred : " , Toast.LENGTH_LONG).show();
                            //errorText.setText();
                        });
                    }else{
                        runOnUiThread(() -> {
                            Log.d(TAG, "onResponse: " + data.changeUserPassword()._id());
                            passDialog.dismiss();
                            Toast.makeText(SettingsActivity.this,
                                    "password changed successfully", Toast.LENGTH_LONG).show();
                        });
                    }

                } else{
                    List<Error> error = response.getErrors();
                    String errorMessage = error.get(0).getMessage();
                    Log.e("Apollo", "an Error occurred : " + errorMessage );
                    runOnUiThread(() -> {
                        Toast.makeText(SettingsActivity.this,
                                "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e("Apollo", "Error", e);
                Toast.makeText(SettingsActivity.this,
                        "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
    }

    public ApolloCall.Callback<UpdateUserNameMutation.Data> changeNameCallback(){
        return new ApolloCall.Callback<UpdateUserNameMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<UpdateUserNameMutation.Data> response) {
                UpdateUserNameMutation.Data data = response.getData();

                if(response.getErrors() == null){

                    if(data.updateUser() == null){
                        Log.e("Apollo", "an Error occurred : " );
                        runOnUiThread(() -> {
                            // Stuff that updates the UI
                            Toast.makeText(SettingsActivity.this,
                                    "an Error occurred : " , Toast.LENGTH_LONG).show();
                            //errorText.setText();
                        });
                    }else{
                        runOnUiThread(() -> {
                            Log.d(TAG, "onResponse: " + data.updateUser()._id());
                            dialog.dismiss();
                            Toast.makeText(SettingsActivity.this,
                                    "Username changed successfully", Toast.LENGTH_LONG).show();
                        });
                    }

                } else{
                    List<Error> error = response.getErrors();
                    String errorMessage = error.get(0).getMessage();
                    Log.e("Apollo", "an Error occurred : " + errorMessage );
                    runOnUiThread(() -> {
                        Toast.makeText(SettingsActivity.this,
                                "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e("Apollo", "Error", e);
                Toast.makeText(SettingsActivity.this,
                        "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
    }

    public ApolloCall.Callback<UpdateStaffPasswordMutation.Data> changeStaffPass(){
        return new ApolloCall.Callback<UpdateStaffPasswordMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<UpdateStaffPasswordMutation.Data> response) {
                UpdateStaffPasswordMutation.Data data = response.getData();

                if(response.getErrors() == null){

                    if(data.changeStaffPassword() == null){
                        Log.e("Apollo", "an Error occurred : " );
                        runOnUiThread(() -> {
                            // Stuff that updates the UI
                            Toast.makeText(SettingsActivity.this,
                                    "an Error occurred : " , Toast.LENGTH_LONG).show();
                            //errorText.setText();
                        });
                    }else{
                        runOnUiThread(() -> {
                            Log.d(TAG, "onResponse: " + data.changeStaffPassword()._id());
                            passDialog.dismiss();
                            Toast.makeText(SettingsActivity.this,
                                    "password changed successfully", Toast.LENGTH_LONG).show();
                        });
                    }

                } else{
                    List<Error> error = response.getErrors();
                    String errorMessage = error.get(0).getMessage();
                    Log.e("Apollo", "an Error occurred : " + errorMessage );
                    runOnUiThread(() -> {
                        Toast.makeText(SettingsActivity.this,
                                "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e("Apollo", "Error", e);
                Toast.makeText(SettingsActivity.this,
                        "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
    }

    public ApolloCall.Callback<GetStaffQuery.Data> staffCallback(){
        return new ApolloCall.Callback<GetStaffQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<GetStaffQuery.Data> response) {
                GetStaffQuery.Data data = response.getData();

                try{
                    if(data.staff() == null){
                        if(response.getErrors() == null){
                            Log.e("Apollo", "an Error occurred : " );
                            runOnUiThread(() -> {
                                // Stuff that updates the UI
                                Toast.makeText(SettingsActivity.this,
                                        "an Error occurred : " , Toast.LENGTH_LONG).show();
                                loading.setVisibility(View.GONE);
                            });

                        } else{
                            List<Error> error = response.getErrors();
                            String errorMessage = error.get(0).getMessage();
                            Log.e("Apollo", "an Error occurred : " + errorMessage );
                            runOnUiThread(() -> {
                                Toast.makeText(SettingsActivity.this,
                                        "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();
                                loading.setVisibility(View.GONE);
                            });
                        }
                    }else{
                        runOnUiThread(() -> {
                            loading.setVisibility(View.GONE);
                            Log.d(TAG, "staff fetched" + data.staff());
                            fullname.setText("User Name:  " + data.staff().fullName());
                            nationalID.setText("ID:  " + data.staff().employeeID());
                            location.setText("Location:  " + data.staff().location());
                            createdAt.setText("Date Created:  " + data.staff().createdAt());
                            phoneNumber.setText("Phone Number:  " + data.staff().phoneNumber());

                        });

                        if(response.getErrors() != null){
                            List<Error> error = response.getErrors();
                            String errorMessage = error.get(0).getMessage();
                            Log.e("Apollo", "an Error occurred : " + errorMessage );
                            runOnUiThread(() -> {
                                Toast.makeText(SettingsActivity.this,
                                        "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();

                            });
                        }
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e("Apollo", "Error", e);
                Toast.makeText(SettingsActivity.this,
                        "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
                loading.setVisibility(View.GONE);
            }
        };
    }
}