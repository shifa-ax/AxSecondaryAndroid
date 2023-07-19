package com.ax.axsecondaryapp.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers


fun <A> performNwOperation(networkCall: suspend () -> RequestResult<A>): LiveData<RequestResult<A>> =
  liveData(Dispatchers.IO) {
    emit(RequestResult.loading())
    val responseStatus = networkCall.invoke()
    if (responseStatus.status) {
      emit(RequestResult.success(responseStatus.data!!))
    } else {
      emit(RequestResult.error(responseStatus.message ?: "Unknown error", responseStatus.code, responseStatus.data))
    }
  }