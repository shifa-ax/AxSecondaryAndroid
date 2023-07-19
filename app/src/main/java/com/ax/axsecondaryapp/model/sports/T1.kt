package com.ax.axsecondaryapp.model.sports

import com.google.gson.annotations.SerializedName

data class T1 (

	@SerializedName("Nm") val nm : String,
	@SerializedName("ID") val iD : Int,
	@SerializedName("tbd") val tbd : Int,
	@SerializedName("Img") val img : String,
	@SerializedName("Gd") val gd : Int,
//	@SerializedName("Pids") val pids : Pids,
	@SerializedName("HasVideo") val hasVideo : Boolean
)