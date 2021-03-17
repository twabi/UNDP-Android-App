package com.example.wastemgmtapp.Common;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.UserQuery;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class SettingsActivity extends AppCompatActivity {

    Toolbar toolbar;
    AlertDialog dialog;
    CardView shareCard, changeCard, viewCard;
    TextView fullname, location, phoneNumber, createdAt, nationalID;
    ApolloClient apolloClient;
    ProgressBar loading, loadChange;
    String TAG = SettingsActivity.class.getSimpleName();
    EditText nameInput, passInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = findViewById(R.id.setToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        shareCard = findViewById(R.id.share);
        changeCard = findViewById(R.id.change);
        viewCard = findViewById(R.id.viewMe);

        Intent intent = getIntent();
        String userID = intent.getStringExtra("id");

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

        changeCard.setOnClickListener(view -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
            builder.setTitle("Change User Details"); //set the title for the dialog
            LayoutInflater inflater = (LayoutInflater) SettingsActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            assert inflater != null;
            //build the dialog and set the view from the layout already created
            view = inflater.inflate(R.layout.change_user, null);
            builder.setView(view);

            nameInput = view.findViewById(R.id.new_username);
            passInput = view.findViewById(R.id.new_password);
            loadChange = view.findViewById(R.id.loadIt);

            //apolloClient.query(new UserQuery(userID)).enqueue(userCallback());

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

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

            apolloClient.query(new UserQuery(userID)).enqueue(userCallback());

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

                if(response.getErrors() == null){

                    if(data.user() == null){
                        Log.e("Apollo", "an Error occurred : " );
                        runOnUiThread(() -> {
                            // Stuff that updates the UI
                            Toast.makeText(SettingsActivity.this,
                                    "an Error occurred : " , Toast.LENGTH_LONG).show();
                            loading.setVisibility(View.GONE);
                        });
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
                    }

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