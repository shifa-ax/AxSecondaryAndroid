package com.ax.axsecondaryapp.model.tokenexpired

import com.google.gson.annotations.SerializedName


data class TokenExpiredResponse(
    @SerializedName("multicast_id") var multicastId: Long? = null,
    @SerializedName("success") var success: Int? = null,
    @SerializedName("failure") var failure: Int? = null,
    @SerializedName("canonical_ids") var canonicalIds: Long? = null,
    @SerializedName("results") var results: ArrayList<Results> = arrayListOf()
)