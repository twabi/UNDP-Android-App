package com.example.wastemgmtapp;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TrashRecyclerAdapter extends RecyclerView.Adapter<TrashRecyclerAdapter.MyViewHolder> {

    private final Context context;
    private ArrayList<String> nameList = new ArrayList<>();

    public TrashRecyclerAdapter(Context context, ArrayList<String> nameList) {
        this.context = context;
        this.nameList = nameList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.trash_card_layout, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TrashDetailsActivity.class);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return nameList.size();

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // init the item view's

        public MyViewHolder(View itemView) {
            super(itemView);

            // get the reference of item view's
            //TextView canName = itemView.findViewById(R.id.can_number);
            //TextView zoneName = itemView.findViewById(R.id.zone_name);
            //TextView percentage = itemView.findViewById(R.id.can_level);
            //ProgressBar canLevel = itemView.findViewById(R.id.can_level_bar);

        }
    }
}
