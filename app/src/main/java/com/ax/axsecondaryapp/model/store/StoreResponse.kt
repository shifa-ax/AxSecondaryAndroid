package com.ax.axsecondaryapp.model.store

import com.google.gson.annotations.SerializedName


data class StoreResponse (

  @SerializedName("status"  ) var status  : Boolean? = null,
  @SerializedName("code"    ) var code    : Int?     = null,
  @SerializedName("message" ) var message : String?  = null,
  @SerializedName("data"    ) var data    : StoreData?    = StoreData()

)