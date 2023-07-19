package com.ax.axsecondaryapp.service

import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.IBinder
import android.util.Log
import com.ax.axsecondaryapp.db.CallLogDetailsTable
import com.ax.axsecondaryapp.db.FileDetails
import com.ax.axsecondaryapp.db.SharedPreferencesManager
import com.ax.axsecondaryapp.model.CallLogDetails
import com.ax.axsecondaryapp.repository.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.log

@AndroidEntryPoint
class ApiDbService : Service() {
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    @ApplicationContext
    lateinit var appContext: Context
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("ApiDbService", "onStartCommand: ", )
        val callLogDetails = intent?.getSerializableExtra("callLogDetails") as? CallLogDetails
        val file = intent?.getSerializableExtra("file") as? File
        var token = intent?.getStringExtra("token").toString()
        var callRecordingPath = intent?.getStringExtra("callRecordingPath").toString()
        val authorizationToken = "Bearer $token"
        val callLogDetailsTableId = intent?.getLongExtra("callLogDetailsTableId", -1)
        sharedPreferencesManager = SharedPreferencesManager(this)

        CoroutineScope(Dispatchers.IO).launch {
            if ( callLogDetails != null) {
                storeDataInRoom(callLogDetails, file, authorizationToken, callRecordingPath,callLogDetailsTableId)
            }
        }
        return START_NOT_STICKY
    }

    private fun isInternetConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }

    private suspend fun storeDataInRoom(
        callLogDetails: CallLogDetails,
        file: File?,
        authorizationToken: String,
        callRecordingPath: String,
        callLogDetailsTableId: Long?
    ) {
        val callLog = CallLogDetailsTable(
            name = callLogDetails.name,
            number = callLogDetails.number,
            date = callLogDetails.date,
            duration = callLogDetails.duration,
            callType = callLogDetails.callType,
            filePath = callRecordingPath
        )

        if (isInternetConnected(appContext)) {
            //get the log and upload once uploaded , delete the file from phone and entry db
            uploadFileApi(callLogDetails, file, authorizationToken,callLogDetailsTableId)
        } else {
            userRepository.saveCallLog(callLog)
            Log.e("TAG", "isInternetConnected: false")
//                storeDataInRoom(callLogDetails, file, authorizationToken, callRecordingPath)
        }


        userRepository.getAllCallLog().let {
            Log.e("TAG", "storeDataInRoom:all call list $it")
        }


        if (callRecordingPath != null) {
            var filePathlog = FileDetails(filePath = callRecordingPath)

            userRepository.saveFilePath(filePathlog)
        }
    }

    private suspend fun uploadFileApi(
        callLogDetails: CallLogDetails,
        file: File?,
        authorizationToken: String,
        callLogDetailsTableId: Long?
    ) {
        val requestBody = file?.asRequestBody("audio/m4a".toMediaType())
        val filePart = requestBody?.let { MultipartBody.Part.createFormData("call_recording", file?.name, it) }

        val uploadResponse = filePart?.let { userRepository.uploadFileAudio(authorizationToken, it) }

        if (uploadResponse?.status == true) {
            val callRec = uploadResponse.data?.data
            callStore(callRec, callLogDetails, authorizationToken,callLogDetailsTableId)
        } else {
            callStore("NA", callLogDetails, authorizationToken,callLogDetailsTableId)
        }
    }

    private suspend fun callStore(
        callRec: String?,
        callLogDetails: CallLogDetails,
        authorizationToken: String,
        callLogDetailsTableId: Long?
    ) {
        val number = callLogDetails.number
        val callType = callLogDetails.callType
        val userId = 172
        val callFinish = "call.finish"
        val callDuration = callLogDetails.duration.toString()
        val callDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(callLogDetails.date))
        val callTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(callLogDetails.date))
        val callStartedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                Date(callLogDetails.date))
        val callAnsweredAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(callLogDetails.date + 1000))
        val callEndedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(callLogDetails.date + callLogDetails.duration * 1000))
        val callerName = callLogDetails.name ?: "NA"
        Log.e("callRec", "callStore:callRec$callRec ", )
        val storeResponse = callRec?.let {
            userRepository.storeCall(
                authorizationToken,
                userId,
                it,
                number,
                callType,
                callDuration,
                callDate,
                callTime,
                callStartedAt,
                callAnsweredAt,
                callEndedAt,
                callerName,
                callFinish
            )
        }
        if (storeResponse?.status == true) {
            Log.e("shifa", "onResponse: StoreResponse  $storeResponse")
            val callLogInDb = callLogDetailsTableId?.let { userRepository.getCallLogById(it) }
            if (callLogInDb != null) {
                // Delete the call log entry from the table
                Log.e("TAG", "callStore: tableid $callLogDetailsTableId" )
                userRepository.deleteCallLog(callLogInDb)
                Log.e("shifa", "onResponse: Call log deleted from table")
            } else {

                Log.e("shifa", "onResponse: Call log not found in table")
            }

            //after upload , check if the item exist in db , if yes delete it

        } else {
            Log.e("shifa", "onResponse: StoreResponse  error")
            if (storeResponse?.message?.equals("Token Expired, Please login again") == true) {
                Log.e(ContentValues.TAG, "updateFirebaseToken: false ${storeResponse.message}")
                //call fcm api to send notification to relogin as token expired
//                var firebaseToken = sharedPreferencesManager.getFirebaseToken()
//                if (firebaseToken != null && userId != -1) {
//                    var tokenresponse = userRepository.handleTokenExpired(userId,firebaseToken)
//                    Log.e("TAG", "callStore:tokenresponse$tokenresponse ", )
//                }

            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}





