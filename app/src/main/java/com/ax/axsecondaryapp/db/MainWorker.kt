package com.ax.axsecondaryapp.db

import android.content.ContentValues
import android.content.Context
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ax.axsecondaryapp.model.CallLogDetails
import com.ax.axsecondaryapp.repository.UserRepository
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.abs


@HiltWorker
class MainWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val userRepository: UserRepository
) : CoroutineWorker(context, workerParameters) {
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        sharedPreferencesManager = SharedPreferencesManager(applicationContext)

        val bundle = Bundle().apply {
            putString("main_worker", "open")
        }
        firebaseAnalytics.logEvent("main_worker", bundle)
        val callLogDetailsJson = inputData.getString("callLogDetailsJson")
        val gson = Gson()
        val callLogDetails = gson.fromJson(callLogDetailsJson, CallLogDetails::class.java)
        val callRecordingPath = inputData.getString("filepath")
        val userId = inputData.getInt("userid", 0)
        val authorizationToken = inputData.getString("authorizationToken")
        val firebaseToken = inputData.getString("firebaseToken")
        val internet = inputData.getBoolean("isInternetConnected", false)

        Log.e("TAG", "doWork:callRecordingPath$callRecordingPath ")
        Log.e("TAG", "doWork:internet$internet ")


        val permission = inputData.getBoolean("permission", false)
        val fromForegroundService = inputData.getBoolean("fromForeground", false)
        val internetConnected: Boolean = isInternetConnected(applicationContext)
        Log.e("MainWorker", "doWork:internetConnected$internetConnected ")
        if (!permission) {
            if (fromForegroundService) {
                userRepository.getAllCallLog()?.let { callLogTbl ->
                    if (callLogTbl.isNotEmpty()) {
                        callLogTbl.reversed().forEach { callLogTblItem ->
                            var callLogDetails = CallLogDetails(
                                number = callLogTblItem.number,
                                date = callLogTblItem.date,
                                duration = callLogTblItem.duration,
                                name = callLogTblItem.name,
                                callType = callLogTblItem.callType
                            )
                            var callRecordingPath = callLogTblItem.filePath
                            val callLogDetailsTableId = callLogTblItem?.id ?: -1
                            var file = callRecordingPath?.let { getFileFromPath(it) }

                            if (authorizationToken != null) {
                                if (!callRecordingPath.isNullOrEmpty()) {
                                    storeDataInRoom(
                                        callLogDetails,
                                        userId,
                                        file,
                                        authorizationToken,
                                        firebaseToken,
                                        callRecordingPath,
                                        callLogDetailsTableId
                                    )
                                } else {
//                                    callLogDetails.duration = 0L shifatest
                                    callStore(
                                        "MISSED CALL",
                                        callLogDetails,
                                        authorizationToken,
                                        firebaseToken,
                                        userId,
                                        callLogDetailsTableId
                                    )
                                }


                            }
                        }
                    }
                }
            } else {
                var file = callRecordingPath?.let { getFileFromPath(it) }
                if (callLogDetails != null) {
                    if (authorizationToken != null) {
                        Log.e("MainWorker", "doWork: callRecordingPath$callRecordingPath")
                        if (callRecordingPath != null) {
                            storeDataInRoom(
                                callLogDetails,
                                userId,
                                file,
                                authorizationToken,
                                firebaseToken,
                                callRecordingPath,
                                -1
                            )
                        } else {
                            Log.e("MainWorker", "doWork: No callRecordingPath")
                            if (internetConnected) {
                                callStore(
                                    "MISSED CALL",
                                    callLogDetails,
                                    authorizationToken,
                                    firebaseToken,
                                    userId,
                                    -1
                                )
                            } else {
                                val callLog = CallLogDetailsTable(
                                    name = callLogDetails.name,
                                    number = callLogDetails.number,
                                    date = callLogDetails.date,
                                    duration = callLogDetails.duration,
                                    callType = callLogDetails.callType,
                                    filePath = callRecordingPath
                                )
                                userRepository.saveCallLog(callLog)
                                Log.e("MainWorker", "isInternetConnected: false")
//                storeDataInRoom(callLogDetails, file, authorizationToken, callRecordingPath)
                            }

                        }


                    }
                }
            }
        } else {
            Log.e(
                "MainWorker",
                "doWork:firebaseToken$firebaseToken userId$userId authorizationToken$authorizationToken",
            )
            if (firebaseToken != null && userId != -1 && authorizationToken != null) {
                try {
                    var tokenResponseBody =
                        userRepository.handlePermission(authorizationToken, userId, firebaseToken)

                    val success = tokenResponseBody.data?.success
                    Log.e("TAG", "handlePermission: tokenResponseBody $tokenResponseBody")
                    if (success != null) {
                        if (success == 1) {
                            Log.e("TAG", "handleTokenExpired: $success")
                        }
                    }
                } catch (e: Exception) {
                    // Handle the exception
                }
            }
        }

//        userRepository.saveCallLog(callLog)
//        callStore()
//        userRepository.getAllCallLog().let {
//            Log.e("MainWorker", "storeDataInRoom:MainWorker all call list $it")
//        }
        Result.success()
    }

    private fun isInternetConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }

    private suspend fun storeDataInRoom(
        callLogDetails: CallLogDetails,
        userId: Int,
        file: File?,
        authorizationToken: String,
        firebaseToken: String?,
        callRecordingPath: String?,
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
        val internetConnected: Boolean = isInternetConnected(applicationContext)
        if (internetConnected) {
            Log.e("MainWorker", "isInternetConnected: true")
            //get the log and upload once uploaded , delete the file from phone and entry db
            uploadFileApi(
                callLogDetails,
                file,
                userId,
                authorizationToken,
                firebaseToken,
                callLogDetailsTableId
            )
        } else {
            userRepository.saveCallLog(callLog)
            Log.e("MainWorker", "isInternetConnected: false")
//                storeDataInRoom(callLogDetails, file, authorizationToken, callRecordingPath)
        }


//        userRepository.getAllCallLog().let {
//            Log.e("TAG", "storeDataInRoom:all call list $it")
//        }

    }

    private fun getMediaTypeFromFileExtension(file: File?): okhttp3.MediaType? {
        val extension = file?.extension?.toLowerCase()
        return when (extension) {
            "m4a" -> "audio/m4a".toMediaTypeOrNull()
            "mp3" -> "audio/mpeg".toMediaTypeOrNull()
            else -> null
        }
    }

    private suspend fun uploadFileApi(
        callLogDetails: CallLogDetails,
        file: File?,
        userId: Int,
        authorizationToken: String,
        firebaseToken: String?,
        callLogDetailsTableId: Long?
    ) {
        val mediaType = getMediaTypeFromFileExtension(file)
        val requestBody = file?.asRequestBody(mediaType)

//        val requestBody = file?.asRequestBody("audio/m4a".toMediaType())
        val filePart = requestBody?.let {
            MultipartBody.Part.createFormData(
                "call_recording",
                file.name, it
            )
        }

        val uploadResponse =
            filePart?.let { userRepository.uploadFileAudio(authorizationToken, it) }

        when (uploadResponse?.status) {
            true -> {
                val mediaPlayer = MediaPlayer()
                mediaPlayer.setDataSource(file.path)
                mediaPlayer.prepare()
                val duration = mediaPlayer.duration // Get the duration in milliseconds
                mediaPlayer.release()

                val fileDurInSec = duration / 1000
                val callDurInSec = callLogDetails.duration

                val differenceInSeconds = abs(fileDurInSec - callDurInSec)
                Log.e("TAG", "uploadFileApi:differenceInSeconds$differenceInSeconds $callDurInSec")
                if (differenceInSeconds > 4) {
                    Log.e("TAG", "uploadFileApi:duration greater than 4  ")
                    sharedPreferencesManager.saveCallFileDurDiff(true)
                    // The difference is less than 4 seconds
                    // Perform your desired action here
                }
                val callRec = uploadResponse.data?.data
                callStore(
                    callRec,
                    callLogDetails,
                    authorizationToken,
                    firebaseToken,
                    userId,
                    callLogDetailsTableId
                )
            }
            false -> {
                if (uploadResponse.message?.equals(", Please login again") == true) {
                    if (firebaseToken != null && userId != -1) {
                        try {
                            var tokenResponseBody =
                                userRepository.handleTokenExpired(userId, firebaseToken)
                            if (tokenResponseBody != null) {
                                val success = tokenResponseBody.data?.success
                                Log.e(
                                    "TAG",
                                    "handleTokenExpired: tokenResponseBody $tokenResponseBody",
                                )
                                if (success != null) {
                                    if (success == 1) {
                                        Log.e("TAG", "handleTokenExpired: $success")
                                    }
                                }
                            } else {
                                Log.e("TAG", "handleTokenExpired:FAIL ")
                            }
                        } catch (e: Exception) {
                            // Handle the exception
                        }
                    }
                } else {
                    callStore(
                        "NA",
                        callLogDetails,
                        authorizationToken,
                        firebaseToken,
                        userId,
                        callLogDetailsTableId
                    )
                }
            }
        }
    }


    private fun getFileFromPath(filePath: String): File? {
        val file = File(filePath)
        return if (file.exists()) {
            file
        } else {
            null
        }
    }


    private suspend fun callStore(
        callRec: String?,
        callLogDetails: CallLogDetails,
        authorizationToken: String,
        firebaseToken: String?,
        userId: Int,
        callLogDetailsTableId: Long?
    ) {
        val number = callLogDetails.number
        val callType = callLogDetails.callType
        val userId = userId
        val callFinish = "call.finish"
        val callDuration = callLogDetails.duration.toString()
        val callDate =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(Date(callLogDetails.date))
        val callTime =
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(callLogDetails.date))
        val callStartedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
            Date(callLogDetails.date)
        )
        var callAnsweredAt = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()
        ).format(Date(callLogDetails.date + 1000))
        val callEndedAt = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()
        ).format(Date(callLogDetails.date + callLogDetails.duration * 1000))
        val callerName = callLogDetails.name ?: "NA"
        Log.e("MainWorker", "callStore:callType$callType ")
        var callRectest = callRec.toString()
        if (callRec.equals("MISSED CALL")) {
            Log.e("TAG", "callStore: missed call ")
            callAnsweredAt = ""
            callRectest = ""
        }

        val callDur = callLogDetails.duration
        if (callDur > 0 && (callRectest.isEmpty() || sharedPreferencesManager.getCallFileDurDiff()) && sharedPreferencesManager.getCallAns()) {
            Log.e("TAG", "callStore: inside incoming ")
            var username = sharedPreferencesManager.getUserName().toString()
            userRepository.sendMessageToChannel("Call Recording Disabled", userId, username)
        }
        sharedPreferencesManager.savecallAns(false) //reset
        sharedPreferencesManager.saveCallFileDurDiff(false) //reset
        val storeResponse =
            userRepository.storeCall(
                authorizationToken,
                userId,
                callRectest,
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

        if (storeResponse.status) {
            val callLogInDb = callLogDetailsTableId?.let { userRepository.getCallLogById(it) }
            if (callLogInDb != null) {
                //after upload , check if the item exist in db , if yes delete it                Log.e("TAG", "callStore: tableid $callLogDetailsTableId" )
                userRepository.deleteCallLog(callLogInDb)
                Log.e("MainWorker", "callStore:deleteCallLog DB SUCCESS ")
            } else {
                Log.e("MainWorker", "onResponse: Call log not found in table")
            }
            if (firebaseToken != null && userId != -1 && authorizationToken != null) {
                //lead notification call
//                try {
//                    var tokenResponseBody = userRepository.updateLeads(authorizationToken, userId, firebaseToken)
//
//                    val success = tokenResponseBody.data?.success
//                    Log.e("TAG", "handlePermission: tokenResponseBody ${tokenResponseBody.message}")
//                    if (success != null) {
//                        if (success == 1) {
//                            Log.e("TAG", "handleTokenExpired: $success")
//                        }
//                    }
//                } catch (e: Exception) {
//                    // Handle the exception
//                }
            }
        } else {
            if (storeResponse.message?.equals("Token Expired, Please login again") == true) {
                Log.e(ContentValues.TAG, "updateFirebaseToken: false ${storeResponse.message}")
                //call fcm api to send notification to relogin as token expired
                var userName= sharedPreferencesManager.getUserName().toString()
                if (firebaseToken != null && userId != -1) {
                    //message in discord for token expired
                    userRepository.sendMessageToChannel("Token Expired", userId,userName)

                    try {
                        var tokenResponseBody =
                            userRepository.handleTokenExpired(userId, firebaseToken)

                        if (tokenResponseBody != null) {
                            // Process the TokenExpiredResponse
                            val success = tokenResponseBody.data?.success

                            Log.e("TAG", "callStore: tokenResponseBody $tokenResponseBody")
                            if (success != null) {
                                if (success == 1) {
                                    Log.e("TAG", "callStore: $success")
                                }
                            }

                            // Use the extracted values as needed
                        } else {
                            // Handle the case where the response body is null
                        }
                    } catch (e: Exception) {
                        // Handle the exception
                    }
                }

            }
            Log.e("MainWorker", "callStore:API CALL FAIL${storeResponse?.status}")
        }
    }


    companion object {
        const val KEY_NAME = "name"
        const val KEY_NUMBER = "number"
        const val KEY_DATE = "date"
        const val KEY_DURATION = "duration"
        const val KEY_FILE_PATH = "file_path"
    }
}