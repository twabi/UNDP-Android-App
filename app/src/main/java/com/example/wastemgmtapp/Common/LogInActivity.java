package com.example.wastemgmtapp.Common;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.Staff.StaffHomeActivity;
import com.example.wastemgmtapp.normalUser.UserHomeActivity;
import com.example.wastemgmtapp.normalUser.UserSignUpActivity;

public class LogInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        TextView noAccountText = findViewById(R.id.noAccount);
        Button login = findViewById(R.id.btn_login);
        EditText numberInput = findViewById(R.id.numberInput);

        noAccountText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LogInActivity.this, UserSignUpActivity.class);
                startActivity(intent);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userNumber = numberInput.getText().toString();
                Intent intent;
                if(userNumber.equals("265")){
                    intent = new Intent(LogInActivity.this, StaffHomeActivity.class);
                } else {
                    intent = new Intent(LogInActivity.this, UserHomeActivity.class);
                }
                startActivity(intent);
            }
        });


    }
}