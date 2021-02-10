package com.example.wastemgmtapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RequestsRecyclerAdapter extends RecyclerView.Adapter<RequestsRecyclerAdapter.RequestsViewHolder> {

    Context context;
    ArrayList<String> nameList = new ArrayList<>();
    ArrayList<String> timeList = new ArrayList<>();
    ArrayList<String> locationList = new ArrayList<>();
    ArrayList<String> amountList = new ArrayList<>();

    public RequestsRecyclerAdapter(Context context, ArrayList<String> nameList, ArrayList<String> timeList,
                                ArrayList<String> locationList, ArrayList<String> amountList) {
        this.context = context;
        this.nameList = nameList;
        this.timeList = timeList;
        this.locationList = locationList;
        this.amountList = amountList;
    }
    @Override
    public RequestsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.requests_card_layout, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new RequestsRecyclerAdapter.RequestsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RequestsViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return nameList.size();
    }

    public class RequestsViewHolder extends RecyclerView.ViewHolder {
        // init the item view's
        TextView requester;
        TextView location;
        TextView timeStamp;
        TextView amount;

        public RequestsViewHolder(View itemView) {
            super(itemView);

            // get the reference of item view's

            requester = itemView.findViewById(R.id.requester);
            location = itemView.findViewById(R.id.location_request);
            timeStamp = itemView.findViewById(R.id.request_time);
            amount = itemView.findViewById(R.id.trash_amount);


        }
    }
}
