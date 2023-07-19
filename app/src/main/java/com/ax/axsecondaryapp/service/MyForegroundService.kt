package com.ax.axsecondaryapp.service

import android.Manifest
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ax.axsecondaryapp.R
import com.ax.axsecondaryapp.broadcast.CallBroadcastReceiver
import com.ax.axsecondaryapp.broadcast.ConnectivityReceiver
import com.google.firebase.analytics.FirebaseAnalytics

class MyForegroundService : Service() {

    private lateinit var callStateReceiver: CallBroadcastReceiver
    private lateinit var connectivityReceiver: ConnectivityReceiver
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle().apply {
            putString("foreground_service ", "running")
        }
        firebaseAnalytics.logEvent("foreground_service", bundle)

        Thread {
            while (true) {
                Log.e("Service", "Service is running Foreground...")
                try {
                    Thread.sleep(2000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()

        val channelId = "Foreground Service ID"
        val channel = NotificationChannel(
            channelId,
            channelId,
            NotificationManager.IMPORTANCE_LOW
        )

        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val notification: Notification = Notification.Builder(this, channelId)
            .setContentText("AX Service is running")
            .setContentTitle("AX Service enabled")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        // Register the call state receiver
        callStateReceiver = CallBroadcastReceiver()
        val intentFilter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        val additionalIntentFilter = IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL)

        registerReceiver(callStateReceiver, intentFilter)
        registerReceiver(callStateReceiver, additionalIntentFilter)

        // Start the foreground service
        startForeground(911, notification)

        // Register the connectivity receiver
        connectivityReceiver = ConnectivityReceiver()
        val intentFilter2 = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(connectivityReceiver, intentFilter2)

        // Get the ConnectivityManager instance
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return super.onStartCommand(intent, flags, startId)
    }



    override fun onDestroy() {
        super.onDestroy()

        // Unregister the call state receiver
        unregisterReceiver(callStateReceiver)
        // Unregister the network callback
        unregisterReceiver(connectivityReceiver)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}


