package com.undp.wastemgmtapp.Common;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.undp.wastemgmtapp.MonitorService;
import com.undp.wastemgmtapp.R;
import com.undp.wastemgmtapp.Staff.StaffHomeActivity;
import com.undp.wastemgmtapp.normalUser.UserHomeActivity;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SessionManager sessionManager = new SessionManager(MainActivity.this);
        boolean valid = sessionManager.checkLogin();

        if(valid){
            HashMap<String, String> user = sessionManager.getUserDetails();
            String status = user.get(SessionManager.KEY_STATUS);
            Log.d(TAG, "onCreate: " + status);

            if(status.equals("User")){
                Intent i = new Intent(MainActivity.this, UserHomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            } else if(status.equals("Staff")){
                Intent i = new Intent(MainActivity.this, StaffHomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startService(new Intent(this, MonitorService.class));
                startActivity(i);
            }
        }
    }

    }