package com.ax.axsecondaryapp.model.uploadresponse
import com.google.gson.annotations.SerializedName


data class Uploadresponse (
  @SerializedName("status"  ) var status  : Boolean? = null,
  @SerializedName("code"    ) var code    : Int?     = null,
  @SerializedName("message" ) var message : String?  = null,
  @SerializedName("data"    ) var data    : String?  = null
)