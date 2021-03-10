package com.example.wastemgmtapp.Fragments;

import android.content.Context;
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
import com.example.wastemgmtapp.R;
import com.example.wastemgmtapp.WasteInstitutionsQuery;
import com.example.wastemgmtapp.adapters.RequestsAdapter;
import com.example.wastemgmtapp.normalUser.RequestCollection;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class CollectionFragment extends Fragment {

    String TAG  = CollectionFragment.class.getSimpleName();
    ArrayList<String> headerList = new ArrayList<>();
    ArrayList<String> statusList = new ArrayList<>();
    ArrayList<String> subTextList = new ArrayList<>();
    ListView requestsView;
    ProgressBar fetchLoading;
    LinearLayout noItems, retryNetwork;
    View view;
    ApolloClient apolloClient;
    String userID;
    SessionManager session;

    public CollectionFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_collection, container, false);
        requestsView = view.findViewById(R.id.list_View);
        fetchLoading = view.findViewById(R.id.fetchLoading);
        noItems = view.findViewById(R.id.norequests);
        retryNetwork = view.findViewById(R.id.retryNetwork);
        fetchLoading.setVisibility(View.VISIBLE);

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

        apolloClient.query(new GetCollectionRequestsQuery()).enqueue(requestCallback());



        return view;

    }

    public ApolloCall.Callback<GetCollectionRequestsQuery.Data> requestCallback(){
        return new ApolloCall.Callback<GetCollectionRequestsQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<GetCollectionRequestsQuery.Data> response) {
                GetCollectionRequestsQuery.Data data = response.getData();

                if(response.getErrors() == null){

                    if(data.trashCollections() == null){
                        Log.e("Apollo", "an Error occurred : " );
                        getActivity().runOnUiThread(() -> {
                            // Stuff that updates the UI
                            Toast.makeText(getActivity(),
                                    "an Error occurred : " , Toast.LENGTH_LONG).show();
                            //errorText.setText();
                            retryNetwork.setVisibility(View.VISIBLE);
                            fetchLoading.setVisibility(View.GONE);
                        });
                    }else{
                        Log.d(TAG, "requests fetched" + data.trashCollections().get(0).amount());
                        getActivity().runOnUiThread(() -> {

                            fetchLoading.setVisibility(View.GONE);
                            if(data.trashCollections().size() == 0) {
                                noItems.setVisibility(View.VISIBLE);
                            } else {
                                Log.d(TAG, "onResponse: " + data.trashCollections());
                                for(int i = 0; i < data.trashCollections().size(); i++){
                                    if(userID.equals(data.trashCollections().get(i).creator()._id())){
                                        headerList.add(data.trashCollections().get(i).amount());
                                        statusList.add(data.trashCollections().get(i).location());
                                        subTextList.add(data.trashCollections().get(i).institution().name());
                                    }
                                }

                                RequestsAdapter adapter = new RequestsAdapter(getActivity(), headerList, subTextList, statusList);
                                requestsView.setAdapter(adapter);
                            }

                        });
                    }

                } else{
                    List<Error> error = response.getErrors();
                    String errorMessage = error.get(0).getMessage();
                    Log.e("Apollo", "an Error occurred : " + errorMessage );
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(),
                                "an Error occurred : " + errorMessage, Toast.LENGTH_LONG).show();
                        retryNetwork.setVisibility(View.VISIBLE);
                        fetchLoading.setVisibility(View.GONE);
                    });
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
                });

            }
        };
    }
}