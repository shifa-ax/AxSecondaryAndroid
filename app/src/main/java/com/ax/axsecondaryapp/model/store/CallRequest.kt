package com.ax.axsecondaryapp.model.store

import okhttp3.MultipartBody

data class CallRequest(
    val user_id: Int,
    val call_recording: MultipartBody.Part,
    val client_number: String,
    val call_type: Boolean,
    val call_duration: String,
    val call_date: String,
    val call_time: String,
    val call_started_at: String,
    val call_answered_at: String,
    val call_ended_at: String,
    val caller_name: String,
    val action: String
)
