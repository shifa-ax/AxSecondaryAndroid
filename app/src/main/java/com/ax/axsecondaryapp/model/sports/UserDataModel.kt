package com.ax.axsecondaryapp.model.sports

import com.google.gson.annotations.SerializedName

data class UserDataModel(
    @SerializedName("Stages") val stages : ArrayList<Stages>? = ArrayList()
)