package com.example.wastemgmtapp.normalUser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.adapters.PageAdapter;
import com.google.android.material.tabs.TabLayout;

import android.os.Bundle;

public class MyRequests extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);
        ViewPager simpleViewPager;
        TabLayout tabLayout;

        simpleViewPager = (ViewPager) findViewById(R.id.simpleViewPager);
        tabLayout = (TabLayout) findViewById(R.id.simpleTabLayout);
        tabLayout.setupWithViewPager(simpleViewPager);

        simpleViewPager.addOnPageChangeListener(
                new TabLayout.TabLayoutOnPageChangeListener(tabLayout)
        );

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //show the back button on the toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final TabLayout.Tab firstTab = tabLayout.newTab();
        firstTab.setText("Trash Collections");
        tabLayout.addTab(firstTab); // add  the tab at in the TabLayout

        final TabLayout.Tab secondTab = tabLayout.newTab();
        secondTab.setText("Sorted Wastes");
        tabLayout.addTab(secondTab); // add  the tab  in the TabLayout

        PageAdapter adapter = new PageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        simpleViewPager.setAdapter(adapter);
        // addOnPageChangeListener event change the tab on slide
        simpleViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}