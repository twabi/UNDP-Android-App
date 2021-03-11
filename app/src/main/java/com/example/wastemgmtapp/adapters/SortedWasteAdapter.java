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

public class SortedWasteAdapter extends BaseAdapter {

    Context context;
    String TAG = RequestsAdapter.class.getSimpleName();
    ArrayList headerList = new ArrayList();
    ArrayList descriptionList = new ArrayList();
    ArrayList statusList = new ArrayList<>();
    ArrayList priceList;
    LayoutInflater inflter;

    public SortedWasteAdapter(Context applicationContext, ArrayList headerList, ArrayList priceList,
                           ArrayList descriptionList, ArrayList statusList) {
        this.context = applicationContext;
        this.headerList = headerList;
        this.priceList = priceList;
        this.descriptionList = descriptionList;
        this.statusList = statusList;

        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return headerList.size();
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

        settingHead.setText("Trash Amount: " + headerList.get(position).toString());
        subText.setText("institution : " + descriptionList.get(position).toString());
        statusText.setText("location : " + statusList.get(position).toString());
        otherText.setText("Price: K" + priceList.get(position).toString());

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
