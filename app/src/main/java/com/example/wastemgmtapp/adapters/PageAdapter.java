package com.example.wastemgmtapp.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.wastemgmtapp.Fragments.CollectionFragment;
import com.example.wastemgmtapp.Fragments.SortedWasteFragment;


public class PageAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PageAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                CollectionFragment tab1 = new CollectionFragment();
                return tab1;
            case 1:
                SortedWasteFragment tab2 = new SortedWasteFragment();
                return tab2;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
