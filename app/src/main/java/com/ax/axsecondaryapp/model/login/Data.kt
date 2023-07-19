package com.ax.axsecondaryapp.model.login

import com.google.gson.annotations.SerializedName


data class Data (

    @SerializedName("token" ) var token : String? = null,
    @SerializedName("user"  ) var user  : User?   = User()

)