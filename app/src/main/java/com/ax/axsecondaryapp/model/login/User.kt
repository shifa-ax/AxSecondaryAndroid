package com.ax.axsecondaryapp.model.login

import com.google.gson.annotations.SerializedName

data class User (

    @SerializedName("id"                ) var id               : Int?    = null,
    @SerializedName("name"              ) var name             : String? = null,
    @SerializedName("full_name"         ) var fullName         : String? = null,
    @SerializedName("first_name"        ) var firstName        : String? = null,
    @SerializedName("last_name"         ) var lastName         : String? = null,
    @SerializedName("position"          ) var position         : String? = null,
    @SerializedName("email"             ) var email            : String? = null,
    @SerializedName("secondary_email"   ) var secondaryEmail   : String? = null,
    @SerializedName("mobile"            ) var mobile           : String? = null,
    @SerializedName("thumbnail"         ) var thumbnail        : String? = null,
    @SerializedName("image"             ) var image            : String? = null,
    @SerializedName("avatar"            ) var avatar           : String? = null,
    @SerializedName("telegram_username" ) var telegramUsername : String? = null,
    @SerializedName("telegram_chatid"   ) var telegramChatid   : String? = null,
    @SerializedName("status"            ) var status           : Int?    = null

)
