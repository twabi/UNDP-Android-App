package com.example.wastemgmtapp.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Error;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.wastemgmtapp.Common.SessionManager;
import com.example.wastemgmtapp.GetCollectionRequestsQuery;
import com.example.wastemgmtapp.GetSortedWasteRequestsQuery;
import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.adapters.RequestsAdapter;
import com.example.wastemgmtapp.adapters.SortedWasteAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class SortedWasteFragment extends Fragment {

    String TAG  = CollectionFragment.class.getSimpleName();
    ArrayList<String> headerList = new ArrayList<>();
    ArrayList<String> statusList = new ArrayList<>();
    ArrayList<String> subTextList = new ArrayList<>();
    ArrayList<String> priceList =  new ArrayList<>();
    ListView requestsView;
    ProgressBar fetchLoading;
    LinearLayout noItems, retryNetwork;
    View view;
    ApolloClient apolloClient;
    String userID;
    SessionManager session;

    public SortedWasteFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_sorted_waste, container, false);

        requestsView = view.findViewById(R.id.list_View);
        fetchLoading = view.findViewById(R.id.fetchLoading);
        noItems = view.findViewById(R.id.norequests);
        retryNetwork = view.findViewById(R.id.retryNetwork);

        fetchLoading.setVisibility(View.VISIBLE);
        retryNetwork.setVisibility(View.GONE);
        noItems.setVisibility(View.GONE);

        session = new SessionManager(getActivity().getApplicationContext());

        HashMap<String, String> user = session.getUserDetails();
        userID = user.get(SessionManager.KEY_USERID);


        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        apolloClient = ApolloClient.builder().okHttpClient(httpClient)
                .serverUrl("https://waste-mgmt-api.herokuapp.com/graphql")
                .build();

        apolloClient.query(new GetSortedWasteRequestsQuery()).enqueue(requestCallback());


        return view;
    }

    public ApolloCall.Callback<GetSortedWasteRequestsQuery.Data> requestCallback(){
        return new ApolloCall.Callback<GetSortedWasteRequestsQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<GetSortedWasteRequestsQuery.Data> response) {
                GetSortedWasteRequestsQuery.Data data = response.getData();

                if(response.getErrors() == null){

                    if(data.sortedWastes() == null){
                        Log.e("Apollo", "an Error occurred : " );
                        getActivity().runOnUiThread(() -> {
                            // Stuff that updates the UI
                            Toast.makeText(getActivity(),
                                    "an Error occurred : " , Toast.LENGTH_LONG).show();
                            //errorText.setText();
                            retryNetwork.setVisibility(View.VISIBLE);
                            fetchLoading.setVisibility(View.GONE);
                            noItems.setVisibility(View.GONE);
                        });
                    }else{
                        try {
                            getActivity().runOnUiThread(() -> {
                                Log.d(TAG, "requests fetched" + data.sortedWastes());
                                fetchLoading.setVisibility(View.GONE);
                                if(data.sortedWastes().size() == 0) {
                                    noItems.setVisibility(View.VISIBLE);
                                    retryNetwork.setVisibility(View.GONE);
                                } else {
                                    for(int i = 0; i < data.sortedWastes().size(); i++){
                                        if(userID.equals(data.sortedWastes().get(i).creator()._id())){
                                            headerList.add(data.sortedWastes().get(i).amount());
                                            priceList.add(data.sortedWastes().get(i).price().toString());
                                            statusList.add(data.sortedWastes().get(i).institution().name());
                                            subTextList.add(data.sortedWastes().get(i).location());
                                        }
                                    }
                                    SortedWasteAdapter adapter = new SortedWasteAdapter(getActivity(), headerList, priceList, statusList, subTextList);
                                    requestsView.setAdapter(adapter);

                                }

                            });
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }

                } else{
                    try{
                        List<Error> error = response.getErrors();
                        String errorMessage = error.get(0).getMessage();
                        Log.e("Apollo", "an Error occurred : " + errorMessage );
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getActivity(),
                                    "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();
                            retryNetwork.setVisibility(View.VISIBLE);
                            fetchLoading.setVisibility(View.GONE);
                            noItems.setVisibility(View.GONE);
                        });
                    } catch (Exception e){
                        e.printStackTrace();
                        retryNetwork.setVisibility(View.VISIBLE);
                        fetchLoading.setVisibility(View.GONE);
                        noItems.setVisibility(View.GONE);
                    }

                }

            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e("Apollo", "Error", e);
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(),
                            "An error occurred : " + e.getMessage(), Toast.LENGTH_LONG).show();
                    retryNetwork.setVisibility(View.VISIBLE);
                    fetchLoading.setVisibility(View.GONE);
                    noItems.setVisibility(View.GONE);
                });

            }
        };
    }
}