package com.example.wastemgmtapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.Staff.RequestDetailsActivity;

import java.util.ArrayList;

public class RequestsRecyclerAdapter extends RecyclerView.Adapter<RequestsRecyclerAdapter.RequestsViewHolder> {

    private final Context context;
    private ArrayList<String> nameList = new ArrayList<>();
    private final String TAG = RequestsRecyclerAdapter.class.getSimpleName();

    public RequestsRecyclerAdapter(Context context, ArrayList<String> nameList) {
        this.context = context;
        this.nameList = nameList;
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


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RequestDetailsActivity.class);
            context.startActivity(intent);
            Log.d(TAG, "onClick: i got clicked" + position);
        });


    }

    @Override
    public int getItemCount() {
        return nameList.size();
    }

    public class RequestsViewHolder extends RecyclerView.ViewHolder {

        public RequestsViewHolder(View itemView) {
            super(itemView);

            // get the reference of item view's

            // init the item view's
            //TextView requester = itemView.findViewById(R.id.requester);
            //TextView location = itemView.findViewById(R.id.location_request);
            //TextView timeStamp = itemView.findViewById(R.id.request_time);
            //TextView amount = itemView.findViewById(R.id.trash_amount);


        }
    }
}
