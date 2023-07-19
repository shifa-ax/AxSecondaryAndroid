package com.ax.axsecondaryapp.model.sports

import com.google.gson.annotations.SerializedName

data class Stages (

	@SerializedName("Sid") val sid : Int,
	@SerializedName("Snm") val snm : String,
	@SerializedName("sportselected") var sportselected : String = "cricket",
	@SerializedName("Sds") val sds : String,
	@SerializedName("Scd") val scd : String,
	@SerializedName("Cid") val cid : Int,
	@SerializedName("Cnm") val cnm : String,
	@SerializedName("Csnm") val csnm : String,
	@SerializedName("Ccd") val ccd : String,
	@SerializedName("Ccdiso") val ccdiso : String,
	@SerializedName("Scu") val scu : Int,
	@SerializedName("Chi") val chi : Int,
	@SerializedName("Shi") val shi : Int,
	@SerializedName("Sdn") val sdn : String,
	@SerializedName("Events") val events : ArrayList<Events>? = ArrayList()
)