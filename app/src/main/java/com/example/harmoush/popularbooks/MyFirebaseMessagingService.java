package com.example.harmoush.popularbooks;


import android.app.Notification;
import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Harmoush on 2/2/2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    public MyFirebaseMessagingService() {
    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, R.string.from + remoteMessage.getFrom());
        if (remoteMessage.getNotification() != null)
            Log.d(TAG, R.string.notificationBody + remoteMessage.getNotification().getBody());
        sendNotification(remoteMessage.getNotification().getBody());

    }

    private void sendNotification(String body) {
        String notification =getString(R.string.notification);
        String notificationBody = getString(R.string.notificationText);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(android.R.drawable.stat_notify_chat)
                        .setContentTitle(notification)
                        .setContentText(notificationBody);
        notificationBuilder.setDefaults(Notification.DEFAULT_ALL);
        NotificationManagerCompat notificationManagerCompat =  NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(1,notificationBuilder.build());
    }

}
