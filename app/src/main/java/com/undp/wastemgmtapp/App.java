package com.undp.wastemgmtapp;

import android.app.Application;
import android.content.Intent;

import androidx.core.content.ContextCompat;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //startService(new Intent(this, MonitorService.class));
        ContextCompat.startForegroundService(this, new Intent(this, MonitorService.class) );
    }
}
