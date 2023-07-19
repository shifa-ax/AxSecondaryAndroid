package com.ax.axsecondaryapp.api

import com.ax.axsecondaryapp.Urls.LEADS
import com.ax.axsecondaryapp.Urls.LOGIN
import com.ax.axsecondaryapp.Urls.PERMISSION
import com.ax.axsecondaryapp.Urls.PHOTO
import com.ax.axsecondaryapp.Urls.STORE
import com.ax.axsecondaryapp.Urls.TODO
import com.ax.axsecondaryapp.Urls.TOKEN
import com.ax.axsecondaryapp.Urls.TOKENEXPIRED
import com.ax.axsecondaryapp.Urls.UPLOAD
import com.ax.axsecondaryapp.Urls.USER
import com.ax.axsecondaryapp.model.Photo
import com.ax.axsecondaryapp.model.TodoResponseModel
import com.ax.axsecondaryapp.model.YourRequestBodyClass
import com.ax.axsecondaryapp.model.login.LoginBase
import com.ax.axsecondaryapp.model.sports.StagesBase
import com.ax.axsecondaryapp.model.store.StoreResponse
import com.ax.axsecondaryapp.model.tokenexpired.TokenExpiredResponse
import com.ax.axsecondaryapp.model.tokenresponse.deviceTokenResponse
import com.ax.axsecondaryapp.model.uploadresponse.Uploadresponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface Api {
    @Multipart
    @POST(UPLOAD)
    @Headers("Accept: application/json")
    suspend fun uploadFileAudio( @Header("Authorization") auth: String?,@Part image: MultipartBody.Part): Response<Uploadresponse?>


    @FormUrlEncoded
    @POST(TOKENEXPIRED)
    @Headers("Accept: application/json")
    suspend fun handleTokenExpired(
        @Field("user_id") userId: Int,
        @Field("device_token") deviceToken: String
    ): Response<TokenExpiredResponse?>


    @FormUrlEncoded
    @POST(PERMISSION)
    @Headers("Accept: application/json")
    suspend fun handlePermission(
        @Header("Authorization") token: String,
        @Field("user_id") userId: Int,
        @Field("device_token") deviceToken: String
    ): Response<TokenExpiredResponse?>


    @FormUrlEncoded
    @POST(LEADS)
    @Headers("Accept: application/json")
    suspend fun updateLeads(
        @Header("Authorization") authToken: String,
        @Field("user_id") userId: Int,
        @Field("device_token") deviceToken: String
    ): Response<TokenExpiredResponse?>

    @Multipart
    @POST("/api/v1/calls/store")
    @Headers("Accept: application/json")
    suspend fun storeCall(
        @Header("Authorization") token: String,
        @Part("user_id") userId: Int,
        @Part("call_recording") callRecording: RequestBody,
        @Part("client_number") clientNumber: RequestBody,
        @Part("call_type") callType: Int,
        @Part("call_duration") callDuration: RequestBody,
        @Part("call_date") callDate: RequestBody,
        @Part("call_time") callTime: RequestBody,
        @Part("call_started_at") callStartedAt: RequestBody,
        @Part("call_answered_at") callAnsweredAt: RequestBody,
        @Part("call_ended_at") callEndedAt: RequestBody,
        @Part("caller_name") callerName: RequestBody,
        @Part("action") action: RequestBody
    ): Response<StoreResponse>

    @FormUrlEncoded
    @POST(LOGIN)
    @Headers("Accept: application/json")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<LoginBase?>

    @FormUrlEncoded
    @POST(TOKEN)
    @Headers("Accept: application/json")
    suspend fun storeDeviceToken(
        @Header("Authorization") authToken: String,
        @Field("user_id") userId: Int,
        @Field("device_token") deviceToken: String
    ): Response<deviceTokenResponse?>



    @GET(TODO)
    suspend fun getTodo(): Response<ArrayList<TodoResponseModel>?>

    @GET(PHOTO)
    suspend fun getPhoto(): Response<ArrayList<Photo>?>

}


//@Multipart
//@POST("v1/calls/store")
//suspend fun callLogDetail( @Header("Authorization") auth: String?,userId:Int): Response<StoreResponse?>


