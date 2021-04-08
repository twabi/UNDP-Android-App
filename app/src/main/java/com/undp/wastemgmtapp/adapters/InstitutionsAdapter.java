package com.undp.wastemgmtapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.undp.wastemgmtapp.R;
import java.util.ArrayList;


public class InstitutionsAdapter extends BaseAdapter {
    Context context;
    ArrayList<String> nameList;
    ArrayList<String>  numberList;
    ArrayList<String>  locationList;
    ArrayList<String>  emailList;
    LayoutInflater inflter;

    public InstitutionsAdapter(Context applicationContext, ArrayList<String> nameList, ArrayList<String> numberList,
                       ArrayList<String> locationList, ArrayList<String> emailList) {
        this.context = applicationContext;
        this.emailList = emailList;
        this.nameList = nameList;
        this.numberList = numberList;
        this.locationList = locationList;

        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return nameList.size();
    }

    @Override
    public Object getItem(int i) {
        return nameList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return Long.parseLong(numberList.get(i));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.company_row_layout, null);


        LinearLayout listItem = view.findViewById(R.id.item_list);
        TextView nameText = view.findViewById(R.id.name);
        TextView numberText = view.findViewById(R.id.phoneNumber);
        TextView locationText = view.findViewById(R.id.location);
        TextView emailText = view.findViewById(R.id.email);
        TextView companyInitials = view.findViewById(R.id.companyInitials);

        //set the arraylists to the list item textviews
        nameText.setText("Name:  " + nameList.get(position));
        numberText.setText("Phone:  " + numberList.get(position));
        locationText.setText("Location:  " + locationList.get(position));
        emailText.setText("Email:  " + emailList.get(position));

        final String name = nameList.get(position).toString(); //get the username from the database
        String ini = name.substring(0,1).toUpperCase(); //get the first letter from the username
        companyInitials.setText(ini); //set the first letter of the username to the initials text view


        return view;
    }
}
