package com.undp.wastemgmtapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.undp.wastemgmtapp.R;

import java.util.ArrayList;

public class SortedWasteAdapter extends BaseAdapter {

    Context context;
    String TAG = RequestsAdapter.class.getSimpleName();
    ArrayList amountList = new ArrayList();
    ArrayList institutionList = new ArrayList();
    ArrayList locationList = new ArrayList<>();
    ArrayList createdList;
    ArrayList completedList;
    ArrayList priceList;
    LayoutInflater inflter;

    public SortedWasteAdapter(Context applicationContext, ArrayList amountList, ArrayList priceList,
                           ArrayList institutionList, ArrayList locationList, ArrayList completedList, ArrayList createdList) {
        this.context = applicationContext;
        this.amountList = amountList;
        this.priceList = priceList;
        this.institutionList = institutionList;
        this.locationList = locationList;
        this.completedList = completedList;
        this.createdList = createdList;

        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return amountList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflter.inflate(R.layout.sorted_row_layout, null);

        LinearLayout listItem = view.findViewById(R.id.item_list);

        TextView settingHead = view.findViewById(R.id.settingHead);
        TextView subText = view.findViewById(R.id.subText);
        TextView statusText = view.findViewById(R.id.statusText);
        TextView otherText = view.findViewById(R.id.otherText);
        TextView completeText = view.findViewById(R.id.completedText);
        TextView createdAtText = view.findViewById(R.id.createdText);

        settingHead.setText("Trash Amount:  " + amountList.get(position).toString());
        subText.setText("institution :  " + institutionList.get(position).toString());
        statusText.setText("location :  " + locationList.get(position).toString());
        otherText.setText("Price:  K" + priceList.get(position).toString());
        completeText.setText("Status:  " + completedList.get(position).toString());
        createdAtText.setText("Date Created:  " + createdList.get(position).toString());

        listItem.setOnClickListener(view1 -> {
            if(position == 0) {
                Log.d(TAG, "onClick: " + position + "-i got clicked");
            } else if(position == 1){
                Log.d(TAG, "onClick: " + position + "-i got clicked");
            } else if (position == 2){
                Log.d(TAG, "onClick: " + position + "-i got clicked");
            } else if (position == 3){
                Log.d(TAG, "onClick: " + position + "-i got clicked");
            }
        });

        return view;
    }
}
