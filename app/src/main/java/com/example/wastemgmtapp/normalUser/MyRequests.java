package com.example.wastemgmtapp.normalUser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.adapters.PageAdapter;
import com.google.android.material.tabs.TabLayout;

import android.os.Bundle;
import android.os.Parcelable;

public class MyRequests extends AppCompatActivity implements TabLayout.OnTabSelectedListener {
    ViewPager simpleViewPager;
    Parcelable stateMan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        TabLayout tabLayout;

        simpleViewPager = (ViewPager) findViewById(R.id.simpleViewPager);
        simpleViewPager.setOffscreenPageLimit(5);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //show the back button on the toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        tabLayout = (TabLayout) findViewById(R.id.simpleTabLayout);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        //Adding the tabs using addTab() method
        tabLayout.addTab(tabLayout.newTab().setText("Trash Collections"));
        tabLayout.addTab(tabLayout.newTab().setText("Sorted Wastes"));


        //Creating our pager adapter
        PageAdapter adapter = new PageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(simpleViewPager);
                tabLayout.getTabAt(0).setText("Trash Collection");
                tabLayout.getTabAt(1).setText("Sorted Waste");
            }
        });


        //Adding adapter to pager
        simpleViewPager.setAdapter(adapter);

        //Adding onTabSelectedListener to swipe views
        tabLayout.setOnTabSelectedListener(this);


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        simpleViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        stateMan = simpleViewPager.onSaveInstanceState();
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        simpleViewPager.onRestoreInstanceState(stateMan);
    }
}