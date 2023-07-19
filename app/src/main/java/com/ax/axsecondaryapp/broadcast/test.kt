package com.ax.axsecondaryapp.broadcast


import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CallLog
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat


class test : BroadcastReceiver() {

    companion object {
        const val ACTION_REQUEST_PERMISSION = "com.example.myapplication.REQUEST_PERMISSION"
        private var numSav = false
        private var callonce = false
        private var isCallEnded = false
        private var isgetCallLogCalled = false
        private var isCallAnswered = false
        private var prev_state: String? = ""
        var incomingNumber: String? = null
        var incomingNumberNew: String? = null
        var incomingNumFirst: String? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
//            Log.e("CallBroadcastReceiver", "onReceive: $incomingNumber")
            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    prev_state = state
//                    add prev_state
                    incomingNumberNew = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    if (!numSav) {
                        if (incomingNumber != null) {
                            incomingNumFirst = incomingNumber
                            numSav = true
                        }
                    }
                    if (!callonce) {
                        if (incomingNumFirst != null && incomingNumberNew != null) {
                            callonce = true
                            if (incomingNumFirst == incomingNumberNew) {
                                Log.e(
                                    "EXTRA_STATE_OFFHOOK",
                                    "onReceive:equal $incomingNumber $incomingNumberNew",
                                )
                            } else {
                                val permissionIntent = Intent(ACTION_REQUEST_PERMISSION)
                                permissionIntent.putExtra("missedNum", incomingNumberNew)
                                permissionIntent.putExtra("fromOFFHOOK", true)
                                context?.sendBroadcast(permissionIntent)

                                Log.e(
                                    "EXTRA_STATE_OFFHOOK",
                                    "onReceive:not equal $incomingNumber $incomingNumberNew ",
                                )

                            }
                        }
                    }
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    Log.e("EXTRA_STATE_IDLE", "onReceive: EXTRA_STATE_IDLE")
//                    Log.e("CB", "onReceive: Call ended is CallEnded $isCallEnded")

                    Log.d("CallStateReceiver", "Call ended incoming number $incomingNumber")
//                    if (!isCallEnded) {
//                        isCallEnded = true
                    if (isCallEnded(intent)) {
                        isCallAnswered = context?.let { isCallAnswered(it) } == true
                        Log.e("TAG", "onReceive:isCallAnswered $isCallAnswered", )
                    }
//                    }
                    if (isCallAnswered) {
                        val permissionIntent = Intent(ACTION_REQUEST_PERMISSION)
                        permissionIntent.putExtra(
                            TelephonyManager.EXTRA_INCOMING_NUMBER,
                            incomingNumber
                        )
                        permissionIntent.putExtra("missedCall", false)
                        permissionIntent.putExtra("justMissed", false)
                        context?.sendBroadcast(permissionIntent)
                    } else {
                        //missed call
                        val permissionIntent = Intent(ACTION_REQUEST_PERMISSION)
                        permissionIntent.putExtra(
                            TelephonyManager.EXTRA_INCOMING_NUMBER,
                            incomingNumber
                        )
                        permissionIntent.putExtra("missedCall", false)
                        permissionIntent.putExtra("justMissed", true)
                        context?.sendBroadcast(permissionIntent)
                    }
                }
            }
        }
    }

    private fun isCallEnded(intent: Intent): Boolean {
        return intent.extras?.getString(TelephonyManager.EXTRA_STATE)
            ?.equals(TelephonyManager.EXTRA_STATE_IDLE) == true
    }

//    private fun isCallAnswered(intent: Intent): Boolean {
//        return intent.extras?.getString(TelephonyManager.EXTRA_STATE)
//            ?.equals(TelephonyManager.EXTRA_STATE_OFFHOOK) == true
//    }

    private fun isCallAnswered(context: Context): Boolean {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.callState == TelephonyManager.CALL_STATE_OFFHOOK
    }

    private val phoneStateListener = object : PhoneStateListener() {
        private var isCallAnswered = false

        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            super.onCallStateChanged(state, phoneNumber)
            if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                isCallAnswered = true
            }
        }

        fun getIsCallAnswered(): Boolean {
            return isCallAnswered
        }
    }


}
