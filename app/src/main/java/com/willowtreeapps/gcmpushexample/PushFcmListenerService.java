package com.willowtreeapps.gcmpushexample;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class PushFcmListenerService extends FirebaseMessagingService {

    public static final int NOTIFICATION_ID = "FirebaseMessageNotification".hashCode();
    public static final String TOPICS_FIELD = "/topics/chat";
    public static final String TEXT_KEY = "message";
    public static final String MSG_DELIVERY = "asyncform";


    public PushFcmListenerService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getFrom().equals(TOPICS_FIELD)) {
            Intent intent = new Intent(MSG_DELIVERY);
            intent.putExtra(TEXT_KEY, remoteMessage.getData().get(TEXT_KEY));

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else {
            createNotification(remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody());
        }

    }

    private void createNotification(String title, String body) {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentText(body)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification.build());
    }

}
