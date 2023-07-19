package com.ax.axsecondaryapp.model

import java.io.Serializable

data class CallLogDetails(
    val number: String,
    val date: Long,
    var duration: Long,
    val name: String?,
    var callType: Int
) : Serializable