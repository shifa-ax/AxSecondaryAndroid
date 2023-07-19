package com.ax.axsecondaryapp.base

import androidx.lifecycle.MutableLiveData
import com.ax.axsecondaryapp.network.BaseDataSource
import kotlinx.coroutines.CompletableJob

abstract class BaseRepository : BaseDataSource() {


  var job: CompletableJob? = null

  private val isLoading = MutableLiveData<Boolean>()


  fun cancelJobs() {
    job?.cancel()
  }

  fun setLoading(value: Boolean) {
    isLoading.value = value
  }

  fun getLoading(): MutableLiveData<Boolean> {
    return isLoading
  }

}

