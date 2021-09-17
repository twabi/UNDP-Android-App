package com.undp.wastemgmtapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;
import com.undp.wastemgmtapp.Common.MainActivity;
import com.undp.wastemgmtapp.Staff.StaffHomeActivity;
import com.undp.wastemgmtapp.Staff.TrashDetailsActivity;

import org.jetbrains.annotations.NotNull;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import timber.log.Timber;

public class MonitorService extends Service {

    String TAG = MonitorService.class.getSimpleName();
    NotificationCompat.Builder notifBuilder;
    String CHANNEL_ID = "R2";
    private static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "Channel_Id";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        startForeground();
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        ApolloClient subscriptionClient = ApolloClient.builder()
                .serverUrl("https://waste-mgmt-api.herokuapp.com/graphql")
                .subscriptionTransportFactory(
                        new WebSocketSubscriptionTransport.Factory("wss://waste-mgmt-api.herokuapp.com/subscriptions", httpClient))
                .okHttpClient(httpClient)
                .build();
        subscriptionClient.subscribe(new GetCanUpdateSubscription()).execute(canUpdateCallback());
        // do your jobs here
        return super.onStartCommand(intent, flags, startId);
    }

    public ApolloSubscriptionCall.Callback<GetCanUpdateSubscription.Data> canUpdateCallback(){
        return new ApolloSubscriptionCall.Callback<GetCanUpdateSubscription.Data>() {

            @Override
            public void onResponse(@NotNull Response<GetCanUpdateSubscription.Data> response) {
                Log.d(TAG, "onResponse: " + response.getData());
                GetCanUpdateSubscription.Data data = response.getData();

                Log.d(TAG, "data: " + data.updateTrashcan());
                if(data.updateTrashcan().status() > 90){
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

// notificationId is a unique int for each notification that you must define
                    Intent pendIntent = new Intent(getApplicationContext(), TrashDetailsActivity.class);
                    pendIntent.putExtra("key", data.updateTrashcan()._id());
                    pendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, pendIntent, 0);

                    Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    notifBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                            .setSmallIcon(R.drawable.can)
                            .setContentTitle("Bin Full")
                            .setContentText("trashcan at "+data.updateTrashcan().trashcanId()+" is ready for collection")
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent)
                            .setSound(uri)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                    int b = (int)(Math.random()*(10000000-1+1)+1);
                    notificationManager.notify(b, notifBuilder.build());
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onCompleted() {
                Timber.e( "Subscription completed");
            }

            @Override
            public void onTerminated() {
                Timber.e( "Subscription terminated");
            }

            @Override
            public void onConnected() {
                Timber.e( "Subscription connected");
            }
        };
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = "notif channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void startForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID) // don't forget create a notification channel first
                .setOngoing(true)
                .setSmallIcon(R.drawable.can)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Service is running background")
                .setContentIntent(pendingIntent);


        startForeground(NOTIF_ID, builder.build());
    }
}