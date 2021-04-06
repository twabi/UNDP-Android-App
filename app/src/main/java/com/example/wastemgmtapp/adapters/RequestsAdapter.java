package com.example.wastemgmtapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.wastemgmtapp.R;

import java.util.ArrayList;

public class RequestsAdapter extends BaseAdapter {

    Context context;
    String TAG = RequestsAdapter.class.getSimpleName();
    ArrayList amountList;
    ArrayList institutionList;
    ArrayList locationList;
    ArrayList createdList;
    ArrayList completedList;
    LayoutInflater inflter;

    public RequestsAdapter(Context applicationContext, ArrayList amountList,
                           ArrayList institutionList, ArrayList locationList, ArrayList completedList, ArrayList createdList) {
        this.context = applicationContext;
        this.amountList = amountList;
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
        view = inflter.inflate(R.layout.requests_row_layout, null);

        LinearLayout listItem = view.findViewById(R.id.item_list);

        TextView settingHead = view.findViewById(R.id.settingHead);
        TextView subText = view.findViewById(R.id.subText);
        TextView statusText = view.findViewById(R.id.statusText);
        TextView completeText = view.findViewById(R.id.completedText);
        TextView createdAtText = view.findViewById(R.id.createdText);

        settingHead.setText("Trash Amount:  " + amountList.get(position).toString());
        subText.setText("institution :  " + institutionList.get(position).toString());
        statusText.setText("location :  " + locationList.get(position).toString());
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
