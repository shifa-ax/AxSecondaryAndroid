package com.ax.axsecondaryapp.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel

abstract class BaseViewModel(application: Application) :
  AndroidViewModel(application) {

  var viewModelContext: Application = application
  fun logOut() {

  }
}
