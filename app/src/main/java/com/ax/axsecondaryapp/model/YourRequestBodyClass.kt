package com.ax.axsecondaryapp.model

data class YourRequestBodyClass(
    val userId: Int,
    val callRecording: String,
    val clientNumber: String,
    val callType: Boolean,
    val callDuration: Int,
    val callDate: String,
    val callTime: String,
    val callStartedAt: String,
    val callAnsweredAt: String,
    val callEndedAt: String,
    val callerName: String,
    val action: String
)
