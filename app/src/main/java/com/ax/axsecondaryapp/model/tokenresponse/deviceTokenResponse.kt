package com.ax.axsecondaryapp.model.tokenresponse
import com.google.gson.annotations.SerializedName


data class deviceTokenResponse (

    @SerializedName("status"  ) var status  : Boolean? = null,
    @SerializedName("code"    ) var code    : Int?     = null,
    @SerializedName("message" ) var message : String?  = null,
    @SerializedName("data"    ) var data    : String?  = null

)