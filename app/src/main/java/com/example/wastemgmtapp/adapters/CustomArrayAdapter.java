package com.example.wastemgmtapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.normalUser.RequestCollection;

import java.util.ArrayList;
import java.util.HashMap;


public class CustomArrayAdapter extends ArrayAdapter<HashMap<String, String>> {
    Context context;
    ArrayList<HashMap<String, String>> objects;
    LayoutInflater inflater;


    public CustomArrayAdapter(@NonNull Context context, int textViewResourceId, @NonNull ArrayList<HashMap<String, String>> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        this.objects = objects;
        this.inflater = (LayoutInflater.from(context));
    }


    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        // TODO Auto-generated method stub
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        //return super.getView(position, convertView, parent);

        View row = inflater.inflate(R.layout.spinner_item, parent, false);
        //TextView label = (TextView)row.findViewById(R.id.spinnerText);
        //label.setText(objects.get(position).get("name"));

        return row;
    }
}

