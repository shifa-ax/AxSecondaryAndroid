package com.ax.axsecondaryapp.model.sports

import com.google.gson.annotations.SerializedName

data class Pids (

	@SerializedName("8") var val8 : ArrayList<Int>? = ArrayList(),
	@SerializedName("11") val val11 : ArrayList<Int>? = ArrayList(),
	@SerializedName("12") val val12 : ArrayList<Int>? = ArrayList()
)