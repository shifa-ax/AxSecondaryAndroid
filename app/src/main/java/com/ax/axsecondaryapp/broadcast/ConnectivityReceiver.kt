package com.ax.axsecondaryapp.broadcast

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.ax.axsecondaryapp.db.MainWorker
import com.ax.axsecondaryapp.db.SharedPreferencesManager

class ConnectivityReceiver : BroadcastReceiver() {
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    var token: String = ""
    var firebaseToken: String = ""

    override fun onReceive(context: Context?, intent: Intent?) {
        sharedPreferencesManager = context?.let { SharedPreferencesManager(it) }!!
        token = sharedPreferencesManager.getToken().toString()
        var userId = sharedPreferencesManager.getUserId()
        firebaseToken = sharedPreferencesManager.getFirebaseToken().toString()

        val authorizationToken = "Bearer $token"

        Log.e("ConnectivityReceiver", "authorizationToken  $authorizationToken")
        if (!checkPermissions(context)) {
            //if permission not given call fcm api to send notification with username and token
            val inputData = Data.Builder()
                .putBoolean("permission", true)
                .putString("authorizationToken", authorizationToken)
                .putString("firebaseToken", firebaseToken)
                .putBoolean("isInternetConnected", true)
                .putInt("userid", userId)
                .build()

            val workRequest = OneTimeWorkRequest.Builder(MainWorker::class.java)
                .setInputData(inputData)
                .build()

            WorkManager.getInstance(context)
                .beginUniqueWork("unique_work", ExistingWorkPolicy.APPEND, workRequest)
                .enqueue()
        }

        if (intent?.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            val connectivityManager =
                context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            if (networkInfo != null && networkInfo.isConnected) {
                Log.d("ConnectivityReceiver", "Internet is connected")
                val inputData = Data.Builder()
                    .putBoolean("isInternetConnected", true)
                    .putInt("userid", userId)
                    .putString("authorizationToken", authorizationToken)
                    .putString("firebaseToken", firebaseToken)
                    .putBoolean("fromForeground", true)
                    .build()


                Log.e("ConnectivityReceiver", "onReceive:userId $userId")
                Log.e("ConnectivityReceiver", "onReceive:authorizationToken $authorizationToken")

                val workRequest2 = OneTimeWorkRequest.Builder(MainWorker::class.java)
                    .setInputData(inputData)
                    .build()

                WorkManager.getInstance(context)
                    .beginUniqueWork("unique_work", ExistingWorkPolicy.APPEND, workRequest2)
                    .enqueue()

            } else {
                Log.d("Service", "Internet is disconnected")
            }
        }
    }

    private fun checkPermissions(context: Context): Boolean {
        val permissions = arrayOf(
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE
        )

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_DENIED
            ) {
                return false
            }
        }
        return true
    }

}
