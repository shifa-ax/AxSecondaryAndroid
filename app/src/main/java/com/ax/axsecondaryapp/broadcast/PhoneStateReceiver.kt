package com.ax.axsecondaryapp.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log

class PhoneStateReceiver : BroadcastReceiver() {
    var onCallStarted: (() -> Unit)? = null
    var onCallEnded: (() -> Unit)? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        val state = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)

        when (state) {
            TelephonyManager.EXTRA_STATE_IDLE -> {
                Log.e("shifa", "onReceive:End ", )
                // Call ended
                onCallEnded?.invoke()
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                // Call started or answered
                Log.e("shifa", "onReceive: start", )
                onCallStarted?.invoke()
            }
        }
    }
}
