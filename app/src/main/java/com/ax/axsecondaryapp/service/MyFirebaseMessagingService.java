package com.ax.axsecondaryapp.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.ax.axsecondaryapp.MainActivity;
import com.ax.axsecondaryapp.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String phoneNumber = remoteMessage.getData().get("phone");
        String loginToken = remoteMessage.getData().get("loginToken");
        String permission = remoteMessage.getData().get("AccessDenied");
        String lead = remoteMessage.getData().get("lead");

        Log.e("TAG", "onMessageReceived: phoneNumber " + phoneNumber);
        Log.e("TAG", "onMessageReceived: loginToken " + loginToken);
        Log.e("TAG", "onMessageReceived: permission " + permission);
        Log.e("TAG", "onMessageReceived: lead " + lead);
        Log.e("TAG", "onMessageReceived: remoteMessage " + remoteMessage.getData());

        if (phoneNumber != null) {
            openDialer(phoneNumber);
        }
        //it is working now but check music-firebase2 code and add appforeground check

        String title = remoteMessage.getNotification().getTitle();
        String msg = remoteMessage.getNotification().getBody();
        if (phoneNumber != null) {
            getFirebaseMessage(title, msg,phoneNumber);
        }
        if (loginToken  != null) {
            if (loginToken.equals("Expired")) {
                getFirebaseMessageToken(title, msg,loginToken);
            }
        }

        if (permission  != null) {
            if (permission.equals("AccessDenied")) {
                getFirebasePermission(title, msg,permission);
            }
        }
        if (lead  != null) {
            Log.e("TAG", "onMessageReceived: "+lead );
            if (lead.equals("CreateLead")) {
                Log.e("TAG", "onMessageReceived: equal" );
                getFirebaseLead(title, msg,lead);
            }
        }
    }

    private void openDialer(String phoneNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("TAG", "Error opening dialer: " + e.getMessage());
        }


    }
    private void getFirebaseLead(String title, String message, String lead) {
        String channelId = "myFirebaseChannel";
        int notificationId = (int) System.currentTimeMillis() + new Random().nextInt(1000);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("lead", lead);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_star_empty)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(notificationId, builder.build());
    }
    private void getFirebasePermission(String title, String message, String permission) {
        String channelId = "myFirebaseChannel";
        int notificationId = (int) System.currentTimeMillis() + new Random().nextInt(1000);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("AccessDenied", permission);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_star_empty)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(notificationId, builder.build());
    }

    private void getFirebaseMessageToken(String title, String message, String loginToken) {
        String channelId = "myFirebaseChannel";
        int notificationId = (int) System.currentTimeMillis() + new Random().nextInt(1000);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("loginToken", loginToken);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_star_empty)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(notificationId, builder.build());
    }

    private void getFirebaseMessage(String title, String message, String phoneNumber) {
        String channelId = "myFirebaseChannel";
        int notificationId = (int) System.currentTimeMillis() + new Random().nextInt(1000);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("phone", phoneNumber);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_star_empty)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(notificationId, builder.build());

    }


}



