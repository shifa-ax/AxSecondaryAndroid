package com.ax.axsecondaryapp

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.ax.axsecondaryapp.databinding.ActivityMainBinding
import com.ax.axsecondaryapp.db.SharedPreferencesManager
import com.ax.axsecondaryapp.service.MyForegroundService
import com.ax.axsecondaryapp.viewmodel.UserViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val viewModel: UserViewModel by viewModels()
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        sharedPreferencesManager = SharedPreferencesManager(this)

        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)

//        firebaseAnalytics = Firebase.analytics
//testpush
        val bundle = Bundle()
        bundle.putBoolean("APP_OPEN", true)
        bundle.putString("USERID", "835")
        firebaseAnalytics.logEvent("APP_OPEN", bundle)

        val bundle2 = Bundle()
        bundle2.putString("MAIN", "MainActivity")
        bundle.putString("USERID", "835")
        firebaseAnalytics.logEvent("main_activity_opened", bundle2)

        val phoneNumber = intent.getStringExtra("phone")
        if (!phoneNumber.isNullOrEmpty()) {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phoneNumber")
            startActivity(intent)
            finish()
        }
        val loginToken = intent.getStringExtra("loginToken")

        if (!loginToken.isNullOrEmpty()) {
            if (loginToken == "Expired") {
                Log.e("MAIN", "onCreate:loginToken --$loginToken")
                sharedPreferencesManager.clearToken()
//                navigateToLogin()
            }
            Log.e("TAG", "onCreate:loginToken ")
        }
        val permission = intent.getStringExtra("AccessDenied")
        if (!permission.isNullOrEmpty()) {
            if (permission == "AccessDenied") {
                Log.e("MAIN", "onCreate:permission --$permission")
//                sharedPreferencesManager.clearToken()
//                navigateToLogin()
            }
            Log.e("TAG", "onCreate:loginToken ")
        }
        val lead = intent.getStringExtra("lead")
        if (!lead.isNullOrEmpty()) {
            if (lead == "CreateLead") {
                sharedPreferencesManager.saveLead(true)
                Log.e("MAIN", "onCreate:permission --$lead")
            }

        }
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener<String?> { task: Task<String?> ->
                if (task.isSuccessful) {
                    val token = task.result
                    if (token != null) {
                        sharedPreferencesManager.saveFirebaseToken(token)
                        Log.e("MainActivity", "onCreate:Firebase token $token")
                    }
                } else {
                    Log.e("MainActivity", "onCreate:Firebase token FAILED")
                }
            })

        var token = sharedPreferencesManager.getToken()


        if (!token.isNullOrEmpty()) {
            navigateToHome()
        } else {
            navigateToLogin()
        }

        Log.e("MAIN", "onCreate:foregroundServiceRunning${foregroundServiceRunning()} ")
        if (!foregroundServiceRunning()) {
            Log.e("MAIN", "onCreate:inside foregroundServiceRunning ")
            val serviceIntent = Intent(this, MyForegroundService::class.java)
            startForegroundService(serviceIntent)
        }

    }

    private fun navigateToHome() {
        val navController = Navigation.findNavController(this, R.id.fragment_container_view)
        navController.navigate(R.id.nav_home)
    }

    private fun navigateToLogin() {
        val navController = Navigation.findNavController(this, R.id.fragment_container_view)
        navController.navigate(R.id.nav_login)
    }


    private fun foregroundServiceRunning(): Boolean {
        val deviceBrand: String = Build.BRAND
        if (deviceBrand == "xiaomi") {
            Thread.sleep(5000)
        }
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (MyForegroundService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

}