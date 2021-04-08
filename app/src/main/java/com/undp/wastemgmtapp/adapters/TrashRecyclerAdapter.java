package com.undp.wastemgmtapp.adapters;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.undp.wastemgmtapp.R;
import com.undp.wastemgmtapp.Staff.TrashDetailsActivity;

import java.util.ArrayList;

public class TrashRecyclerAdapter extends RecyclerView.Adapter<TrashRecyclerAdapter.MyViewHolder> {

    private final Context context;
    private ArrayList<String> nameList;
    private ArrayList<Double> statusList;
    private ArrayList<String> keyList;
    private ArrayList<String> zoneNameList;

    String TAG = TrashRecyclerAdapter.class.getSimpleName();

    public TrashRecyclerAdapter(Context context, ArrayList<String> nameList, ArrayList<Double> statusList, ArrayList<String> zoneNameList,
                                ArrayList<String> keyList) {
        this.context = context;
        this.nameList = nameList;
        this.statusList = statusList;
        this.keyList = keyList;
        this.zoneNameList = zoneNameList;
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

        holder.canName.setText(nameList.get(position));
        holder.zoneName.setText(zoneNameList.get(position));
        holder.percentage.setText("" + statusList.get(position));

        Double d = Double.valueOf(statusList.get(position));
        int value = d.intValue();
        if(value > 90){
            Drawable progressDrawable = holder.canLevel.getProgressDrawable().mutate();
            progressDrawable.setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
            holder.canLevel.setProgressDrawable(progressDrawable);
        }

        holder.canLevel.setProgress(value);


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TrashDetailsActivity.class);
            intent.putExtra("key", keyList.get(position));
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return nameList.size();

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // init the item view's
        TextView canName, zoneName, percentage;
        ProgressBar canLevel;
        public MyViewHolder(View itemView) {
            super(itemView);

            // get the reference of item view's
            canName = itemView.findViewById(R.id.can_number);
            zoneName = itemView.findViewById(R.id.zone_name);
            percentage = itemView.findViewById(R.id.can_level);
            canLevel = itemView.findViewById(R.id.can_level_bar);

        }
    }
}
