package com.ax.axsecondaryapp.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ax.axsecondaryapp.MainActivity
import com.ax.axsecondaryapp.R
import com.ax.axsecondaryapp.base.BaseFragment
import com.ax.axsecondaryapp.databinding.Fragment3Binding
import com.ax.axsecondaryapp.db.SharedPreferencesManager
import com.ax.axsecondaryapp.viewmodel.UserViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import java.util.*
import javax.inject.Inject


class Fragment3 : BaseFragment() {
    @Inject
    lateinit var binding: Fragment3Binding
    private val viewModel: UserViewModel by activityViewModels()
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private var token: String = ""
    private val PERMISSION_REQUEST_CODE = 222

    lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_3, container, false
        )
        initUI()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private fun initUI() {
        binding.lifecycleOwner = this
        binding.viewmodel = viewModel
        sharedPreferencesManager = SharedPreferencesManager(requireContext())
        var userid = sharedPreferencesManager.getUserId()?.toInt()
        var firebaseToken = sharedPreferencesManager.getFirebaseToken()
        token = sharedPreferencesManager.getToken().toString()
        val authorizationToken = "Bearer $token"
        firebaseAnalytics = (requireActivity() as MainActivity).firebaseAnalytics
        val bundle = Bundle().apply {
            putString("home_page", "success")
            putString("USERID", "835")
        }

        firebaseAnalytics.logEvent("home_page_opened", bundle)

        if(sharedPreferencesManager.getLead()){
            //call dailog
            Log.e(TAG, "initUI: call dailog", )
            findNavController().navigate(R.id.dialogLeads)
//            findNavController().navigate(
//                R.id.fragment2, RewardsApplyDialog.getDialogApplyBundle(id_reward!!)
//            )
            sharedPreferencesManager.saveLead(false)
        }
        if (!sharedPreferencesManager.getUserName().isNullOrEmpty()) {
            binding.textViewUserName.text = sharedPreferencesManager.getUserName()
        }

        if (firebaseToken != null && authorizationToken != null && userid != -1) {
            updateFirebaseToken(authorizationToken, userid, firebaseToken)
            updateLeads(authorizationToken, userid, firebaseToken)
        }

        if (!checkPermissions()) {
            requestPermissions()
        }
//        viewModel.test()

        var userName = sharedPreferencesManager.getUserName()
        var userId = sharedPreferencesManager.getUserId()
        //custom
        val loginbundle = Bundle().apply {
            putInt(FirebaseAnalytics.Param.ITEM_ID, userId)
            putString(FirebaseAnalytics.Param.ITEM_NAME, userName)
            // Add more parameters if needed
        }

        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, loginbundle)

        //1.Firebase Analytics
        val loginBundle = Bundle().apply {
            putInt("USER ID", userId)
            putString("USER NAME", userName)
        }
        firebaseAnalytics.logEvent(FirebaseTags.LOGGED_IN.name, loginBundle)

    }

    private fun checkPermissions(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE
        )

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) == PackageManager.PERMISSION_DENIED
            ) {
                return false
            }
        }
        return true
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE
        )

        val showRationale = shouldShowRequestPermissionRationale(permissions[0]) ||
                shouldShowRequestPermissionRationale(permissions[1]) ||
                shouldShowRequestPermissionRationale(permissions[2]) ||
                shouldShowRequestPermissionRationale(permissions[3]) ||
                shouldShowRequestPermissionRationale(permissions[4])

        if (showRationale) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Please allow the required permissions")
                .setCancelable(false)
                .setPositiveButton("Settings") { _, _ ->
                    openAppSettings()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }

            val dialog = builder.create()
            dialog.show()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissions,
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted
            } else {
                // Permissions denied
            }
        }
    }
    private fun updateLeads(authToken: String, userId: Int, deviceToken: String) {
        viewModel.getNetworkStatusLiveData().observe(viewLifecycleOwner) { isConnected ->
            if (isConnected) {
                viewModel.updateLeads(authToken, userId, deviceToken)
                    .observe(viewLifecycleOwner) { result ->
                        result?.let {
                            Log.e(TAG, "updateFirebaseToken: result${result.data?.multicastId}")

                            when (result.status) {

                                true -> {
                                    Log.e(TAG, "updateFirebaseToken: success")
                                    Log.e(TAG, "updateFirebaseToken: result$result")
                                }
                                false -> {
                                    Log.e(TAG, "updateFirebaseToken: false ${it.message}")

                                    if (result.message?.equals("Token Expired, Please login again") == true) {
                                        Log.e(TAG, "updateFirebaseToken: false ${result.message}")
                                        findNavController().navigate(R.id.fragment2)
                                    }
                                }

                            }

                        }
                    }

            } else {
                // No internet connection
                // Handle the lack of internet connection
                Toast.makeText(
                    requireContext(),
                    "No internet connection",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }

    }
    private fun updateFirebaseToken(authToken: String, userId: Int, deviceToken: String) {
        viewModel.getNetworkStatusLiveData().observe(viewLifecycleOwner) { isConnected ->
            if (isConnected) {
                viewModel.storeDeviceToken(authToken, userId, deviceToken)
                    .observe(viewLifecycleOwner) { result ->
                        result?.let {
                            when (result.status) {
                                true -> {
                                    Log.e(TAG, "updateFirebaseToken: success")
                                }
                                false -> {
                                    Log.e(TAG, "updateFirebaseToken: false ${it.message}")

                                    if (result.message?.equals("Token Expired, Please login again") == true) {
                                       var userName = sharedPreferencesManager.getUserName().toString()
                                        viewModel.sendMsgToDiscordChannel("Token Expired",userId,userName )
                                        Log.e(TAG, "updateFirebaseToken: false ${result.message}")
                                        findNavController().navigate(R.id.fragment2)
                                    }
                                }

                            }

                        }
                    }

            } else {
                // No internet connection
                // Handle the lack of internet connection
                Toast.makeText(
                    requireContext(),
                    "No internet connection",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }

    }


}






