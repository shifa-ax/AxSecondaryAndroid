package com.ax.axsecondaryapp.model.sports

import com.google.gson.annotations.SerializedName

data class T2 (

	@SerializedName("Nm") val nm : String,
	@SerializedName("ID") val iD : Int,
	@SerializedName("tbd") val tbd : Int,
	@SerializedName("Gd") val gd : Int,
	@SerializedName("Img") val img : String,
//	@SerializedName("Pids") val pids : Pids,
	@SerializedName("HasVideo") val hasVideo : Boolean
)