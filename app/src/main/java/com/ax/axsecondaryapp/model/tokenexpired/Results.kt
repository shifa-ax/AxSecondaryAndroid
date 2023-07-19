package com.ax.axsecondaryapp.model.tokenexpired

import com.google.gson.annotations.SerializedName


data class Results (
    @SerializedName("message_id") var messageId: String = ""
)