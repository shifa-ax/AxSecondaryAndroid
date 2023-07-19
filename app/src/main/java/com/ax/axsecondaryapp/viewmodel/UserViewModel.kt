package com.ax.axsecondaryapp.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.ax.axsecondaryapp.base.BaseViewModel
import com.ax.axsecondaryapp.db.CallLogDetailsTable
import com.ax.axsecondaryapp.model.store.StoreResponse
import com.ax.axsecondaryapp.network.NetworkStatusLiveData
import com.ax.axsecondaryapp.network.RequestResult
import com.ax.axsecondaryapp.network.performNwOperation
import com.ax.axsecondaryapp.repository.UserRepository
import com.ax.axsecondaryapp.utils.InputEditTextValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(
    application: Application,
    val userRepository: UserRepository
) : BaseViewModel(application) {
    var text = MutableLiveData<String>()
    var agentName: String = ""

    private val networkStatusLiveData: NetworkStatusLiveData by lazy {
        NetworkStatusLiveData(getApplication())
    }

    fun getNetworkStatusLiveData(): LiveData<Boolean> {
        return networkStatusLiveData
    }

    var emailTextObserver: InputEditTextValidator =
        InputEditTextValidator(
            InputEditTextValidator.InputEditTextValidationsEnum.ID,
            true,
            null,
            null
        )

    var passwordTextObserver: InputEditTextValidator =
        InputEditTextValidator(
            InputEditTextValidator.InputEditTextValidationsEnum.ID,
            true,
            null,
            null
        )

    var loading = MutableLiveData<Boolean>()

//    suspend fun getAllCallLog() =
//        callLogDetailsDao.getAllCallLogs()

    val allCallLogs: LiveData<List<CallLogDetailsTable>> = liveData {
        try {
            val callLogs = userRepository.getAllCallLog()
            if (callLogs != null) {
                emit(callLogs)
            }
        } catch (e: Exception) {
            // Handle the exception if needed
        }
    }

    fun getEmail(email: String, password: String) =
        performNwOperation {
            userRepository.getEmail(
                email,
                password
            )
        }


    fun storeDeviceToken(token: String, userId: Int, deviceToken: String) =
        performNwOperation {
            userRepository.storeDeviceToken(
                token,
                userId,
                deviceToken
            )
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
        return userRepository.storeCall(
            token,
            userId,
            callRecording,
            clientNumber,
            callType,
            callDuration,
            callDate,
            callTime,
            callStartedAt,
            callAnsweredAt,
            callEndedAt,
            callerName,
            action
        )
    }

//    suspend fun updateLeads(
//        token:String,userId: Int, deviceToken: String
//    ): RequestResult<TokenExpiredResponse?> {
//        return userRepository.updateLeads(
//            token,
//            userId,
//            deviceToken)
//    }

    fun updateLeads(token: String, userId: Int, deviceToken: String) =
        performNwOperation {
            userRepository.updateLeads(
                token,
                userId,
                deviceToken
            )
        }

    fun sendMsgToDiscordChannel(message:String, userId: Int, userName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.sendMessageToChannel(message,userId, userName)
        }
    }
}

