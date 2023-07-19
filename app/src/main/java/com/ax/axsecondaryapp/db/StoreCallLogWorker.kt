package com.ax.axsecondaryapp.db
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.withContext
import com.ax.axsecondaryapp.model.CallLogDetails
import com.ax.axsecondaryapp.repository.UserRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers

import javax.inject.Inject

class StoreCallLogWorker(
    @ApplicationContext private val appContext: Context,
    private val workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @Inject
    lateinit var userRepository: UserRepository

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.e("StoreCallLogWorker", "doWork: ", )
//            // Get the call log details from workerParams
//            val callLogDetailsString = workerParams.inputData.getString("callLogDetails")
//            val gson = Gson()
//            val callLogDetails = gson.fromJson(callLogDetailsString, CallLogDetails::class.java)
//            Log.e("StoreCallLogWorker", "doWork: ${callLogDetails.number}" )
            // Check if callLogDetails is null or not
//            if (callLogDetails != null) {
                // Store the call log details in the Room database
                val callLogDao = CallLogDatabase.invoke(appContext).callLogDetailsDao()
//                val callLogTable = CallLogDetailsTable(
//                    name = "shifa",
//                    number = "0522222222",
//                    date = callLogDetails.date,
//                    duration = callLogDetails.duration,
//                    filePath = "callLogDetails.filePath"
//                )
                var data = callLogDao.getAllCallLogs()
                Log.e("StoreCallLogWorker", "doWork:data $data", )
//                callLogDao.addCallLog(callLogTable)
//            }

            // Return success
            Result.success()
        } catch (e: Exception) {
            // Return failure
            Result.failure()
        }
    }

    private fun isInternetConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }
}


