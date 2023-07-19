package com.ax.axsecondaryapp.fragment

import com.ax.axsecondaryapp.model.albumModel
import java.io.Serializable

interface ApiServiceCallback {
    fun onApiResult(result: ArrayList<albumModel>?)
}

class MyCallbackObject(private val callback: ApiServiceCallback) : Serializable {

}

