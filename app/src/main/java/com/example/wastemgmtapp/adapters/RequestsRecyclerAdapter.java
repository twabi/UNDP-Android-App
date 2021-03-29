package com.example.wastemgmtapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.Staff.RequestDetailsActivity;

import java.util.ArrayList;

public class RequestsRecyclerAdapter extends RecyclerView.Adapter<RequestsRecyclerAdapter.RequestsViewHolder> {

    private final Context context;
    ArrayList<String> keyList = new ArrayList<>();
    ArrayList<Boolean> statusList = new ArrayList<>();
    ArrayList<String> createdAtList = new ArrayList<>();
    ArrayList<String> taskType = new ArrayList<>();

    public RequestsRecyclerAdapter(Context context, ArrayList<String> keyList,
                                   ArrayList<Boolean> statusList, ArrayList<String> createdAtList, ArrayList<String> taskType) {
        this.context = context;
        this.keyList = keyList;
        this.statusList = statusList;
        this.createdAtList = createdAtList;
        this.taskType = taskType;
    }

    private final String TAG = RequestsRecyclerAdapter.class.getSimpleName();

    @Override
    public RequestsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.requests_card_layout, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new RequestsRecyclerAdapter.RequestsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RequestsViewHolder holder, int position) {

        holder.completed.setText("Completed:  " + statusList.get(position));
        holder.date.setText("Date Added:  " + createdAtList.get(position));
        holder.taskID.setText("Task ID:  " + keyList.get(position));
        holder.type.setText("Task Type:  " + taskType.get(position));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RequestDetailsActivity.class);
            intent.putExtra("key", keyList.get(position));
            intent.putExtra("task type", taskType.get(position));
            context.startActivity(intent);
            Log.d(TAG, "onClick: i got clicked" + position);
        });
    }

    @Override
    public int getItemCount() {
        return keyList.size();
    }

    public void clear(){
        taskType.clear();
        statusList.clear();
        createdAtList.clear();
        keyList.clear();
    }

    public class RequestsViewHolder extends RecyclerView.ViewHolder {

        TextView type;
        TextView completed;
        TextView taskID;
        TextView date;
        public RequestsViewHolder(View itemView) {
            super(itemView);

            // get the reference of item view's

            // init the item view's
             type = itemView.findViewById(R.id.type);
             completed = itemView.findViewById(R.id.completed);
             taskID = itemView.findViewById(R.id.task_id);
             date = itemView.findViewById(R.id.createTime);


        }
    }
}
