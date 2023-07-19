package com.ax.axsecondaryapp.repository

import android.util.Log
import com.ax.axsecondaryapp.base.BaseRepository
import com.ax.axsecondaryapp.api.Api
import com.ax.axsecondaryapp.db.CallLogDetailsDao
import com.ax.axsecondaryapp.db.CallLogDetailsTable
import com.ax.axsecondaryapp.db.FileDetails
import com.ax.axsecondaryapp.model.login.LoginBase
import com.ax.axsecondaryapp.model.store.StoreResponse
import com.ax.axsecondaryapp.model.tokenexpired.TokenExpiredResponse
import com.ax.axsecondaryapp.network.RequestResult
import okhttp3.*
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val api: Api,
    private val callLogDetailsDao: CallLogDetailsDao
) : BaseRepository() {

    suspend fun createLogs() {
        Log.d("TAG", "createLogs: mainrepository called")
    }

    suspend fun getEmail(email: String, password: String) =
        getResult {
            api.login(
                email,
                password
            )
        }

    suspend fun storeDeviceToken(token: String, userId: Int, deviceToken: String) =
        getResult {
            api.storeDeviceToken(
                token,
                userId,
                deviceToken
            )
        }


    suspend fun updateLeads(
        token: String,
        userId: Int,
        deviceToken: String
    ): RequestResult<TokenExpiredResponse?> {
        return try {
            val response = api.updateLeads(token, userId, deviceToken)
            val tokenResponseBody = response.body()

            if (response.isSuccessful && tokenResponseBody != null) {
                RequestResult.success(tokenResponseBody)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                RequestResult.error(errorMessage, response.code(), tokenResponseBody)
            }
        } catch (e: Exception) {
            RequestResult.error(e.message ?: "Unknown error", -1, null)
        }
    }

    suspend fun handlePermission(
        token: String,
        userId: Int,
        deviceToken: String
    ): RequestResult<TokenExpiredResponse?> {
        return try {
            val response = api.handlePermission(token, userId, deviceToken)
            val tokenResponseBody = response.body()

            if (response.isSuccessful && tokenResponseBody != null) {
                RequestResult.success(tokenResponseBody)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                RequestResult.error(errorMessage, response.code(), tokenResponseBody)
            }
        } catch (e: Exception) {
            RequestResult.error(e.message ?: "Unknown error", -1, null)
        }
    }

    suspend fun handleTokenExpired(
        userId: Int,
        deviceToken: String
    ): RequestResult<TokenExpiredResponse?> {
        return try {
            val response = api.handleTokenExpired(userId, deviceToken)
            val tokenResponseBody = response.body()

            if (response.isSuccessful && tokenResponseBody != null) {
                RequestResult.success(tokenResponseBody)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                RequestResult.error(errorMessage, response.code(), tokenResponseBody)
            }
        } catch (e: Exception) {
            RequestResult.error(e.message ?: "Unknown error", -1, null)
        }
    }


    suspend fun handleTokenExpirede(
        userId: Int,
        deviceToken: String
    ): Response<TokenExpiredResponse?> {
        return try {
            val response = api.handleTokenExpired(userId, deviceToken)
            response
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun storeCall(
        token: String,
        userId: Int,
        callRecording: String,
        clientNumber: String,
        callType: Int,
        callDuration: String,
        callDate: String,
        callTime: String,
        callStartedAt: String,
        callAnsweredAt: String,
        callEndedAt: String,
        callerName: String,
        action: String
    ): RequestResult<StoreResponse?> {
//        val tokenPart = createPartFromString(token)
//        val userIdPart = createPartFromString(userId)
        val callRecordingPart = createPartFromString(callRecording)
        val clientNumberPart = createPartFromString(clientNumber)
//        val callTypePart = createPartFromString(callType)
        val callDurationPart = createPartFromString(callDuration)
        val callDatePart = createPartFromString(callDate)
        val callTimePart = createPartFromString(callTime)
        val callStartedAtPart = createPartFromString(callStartedAt)
        val callAnsweredAtPart = createPartFromString(callAnsweredAt)
        val callEndedAtPart = createPartFromString(callEndedAt)
        val callerNamePart = createPartFromString(callerName)
        val actionPart = createPartFromString(action)

        return getResultNew {
            api.storeCall(
                token,
                userId,
                callRecordingPart,
                clientNumberPart,
                callType,
                callDurationPart,
                callDatePart,
                callTimePart,
                callStartedAtPart,
                callAnsweredAtPart,
                callEndedAtPart,
                callerNamePart,
                actionPart
            )
        }
    }

    private fun createPartFromString(string: String): RequestBody {
        return string.toRequestBody(MultipartBody.FORM)
    }

    suspend fun uploadFileAudio(authorizationToken: String, file: MultipartBody.Part) =
        getResult { api.uploadFileAudio(authorizationToken, file) }

    suspend fun saveCallLog(callLog: CallLogDetailsTable) =
        callLogDetailsDao.addCallLog(callLog)

    suspend fun saveFilePath(fileDetails: FileDetails) =
        callLogDetailsDao.addFilepath(fileDetails)

    suspend fun getAllCallLog() =
        callLogDetailsDao.getAllCallLogs()

    suspend fun deleteCallLog(callLog: CallLogDetailsTable) =
        callLogDetailsDao.deleteCallLog(callLog)

    suspend fun getCallLogById(id: Long): CallLogDetailsTable? =
        callLogDetailsDao.getCallLogById(id)

    suspend fun getTodo() =
        getResult { api.getTodo() }

    suspend fun getPhoto() =
        getResult { api.getPhoto() }


    //    fun sendMessageToChannel(channelId: String, message: String) {
//        val client = OkHttpClient()
//        val url = "https://discord.com/api/channels/$channelId/messages"
//        val requestBody = "{\"content\": \"$message\"}".toRequestBody("application/json".toMediaType())
//        val request = Request.Builder()
//            .url(url)
//            .header("Authorization", "Bot MTEzMDc2NjU2MTM0MzQzODkxOA.GRUxch.dwMnYTeaGf7t0qp176i350el0OlDYce-hnuHJk")
//            .post(requestBody)
//            .build()
//
//        client.newCall(request).execute().use { response: okhttp3.Response ->
//            if (response.isSuccessful) {
//                Log.e("main", "sendMessageToChannel: success", )
//                // Message sent successfully
//            } else {
//                Log.e("main", "sendMessageToChannel:error ", )
//
//                // Handle error
//            }
//        }
//    }

    fun sendMessageToChannel(message:String?,userId: Int, userName: String) {
        try {
            val webhookUrl = "https://discord.com/api/webhooks/1130805755973476403/"
            val json =
                "{\"content\":\"$message userId : $userId userName : $userName\"}" // JSON payload to send
            val mediaType = "application/json".toMediaTypeOrNull()

            val requestBody = json.toRequestBody(mediaType)
            val retrofit = Retrofit.Builder()
                .baseUrl(webhookUrl)
                .addConverterFactory(GsonConverterFactory.create()) // Use ScalarsConverterFactory
                .build()

            val service = retrofit.create(DiscordService::class.java)

            val call: Call<Unit> = service.sendMessageToWebhook(requestBody)

            call.enqueue(object : Callback<Unit> {
                override fun onResponse(
                    call: Call<Unit>,
                    response: Response<Unit>
                ) {
                    if (response.isSuccessful) {
                        val responseCode = response.code()
                        if (responseCode == 204) {
                            // Handle the response code 204 if needed
                            Log.e("TAG", "onResponse:responseCode$responseCode ",)
                        }
                    }
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {

                }
            })
        }catch (e:Exception){
            e.printStackTrace()
        }
    }


    interface DiscordService {
        @retrofit2.http.Headers("Content-Type: application/json")
        @retrofit2.http.POST("Zkd2mNQ4CN4AfuBHfvtwDin6IZV-A7cMSx_U6k4_OtBtEX9krLnD7eeQnCZiP6ZouqpN")
        fun sendMessageToWebhook(@retrofit2.http.Body requestBody: okhttp3.RequestBody): retrofit2.Call<Unit>
    }
}