package com.ax.axsecondaryapp.model.login

import com.google.gson.annotations.SerializedName


data class LoginBase (
	@SerializedName("status"  ) var status  : Boolean? = null,
	@SerializedName("code"    ) var code    : Int?     = null,
	@SerializedName("message" ) var message : String?  = null,
	@SerializedName("data"    ) var data    : Data?    = Data()

)