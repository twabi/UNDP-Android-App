package com.example.wastemgmtapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class UserSignUpActivity extends AppCompatActivity {

    TextView alreadyAccount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sign_up);

        alreadyAccount = findViewById(R.id.already);
        alreadyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserSignUpActivity.this, LogInActivity.class);
                startActivity(intent);
            }
        });
    }
}