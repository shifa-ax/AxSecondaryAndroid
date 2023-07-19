package com.ax.axsecondaryapp.model.store

import com.google.gson.annotations.SerializedName



data class StoreData (

  @SerializedName("id"                   ) var id                : Int?    = null,
  @SerializedName("lead_id"              ) var leadId            : Int?    = null,
  @SerializedName("lead_follow_up_id"    ) var leadFollowUpId    : String? = null,
  @SerializedName("contact_id"           ) var contactId         : String? = null,
  @SerializedName("contact_follow_up_id" ) var contactFollowUpId : String? = null,
  @SerializedName("client_number"        ) var clientNumber      : String? = null,
  @SerializedName("call_type"            ) var callType          : Boolean = true,
  @SerializedName("internal_user_id"     ) var internalUserId    : String? = null,
  @SerializedName("user_id"              ) var userId            : Int?    = null,
  @SerializedName("call_started_at"      ) var callStartedAt     : String? = null,
  @SerializedName("call_answered_at"     ) var callAnsweredAt    : String? = null,
  @SerializedName("call_ended_at"        ) var callEndedAt       : String? = null,
  @SerializedName("call_duration"        ) var callDuration      : String? = null,
  @SerializedName("call_status"          ) var callStatus        : String? = null,
  @SerializedName("call_sid"             ) var callSid           : String? = null,
  @SerializedName("call_recording_url"   ) var callRecordingUrl  : String? = null,
  @SerializedName("call_recording_sid"   ) var callRecordingSid  : String? = null,
  @SerializedName("json_dump"            ) var jsonDump          : String? = null,
  @SerializedName("created_at"           ) var createdAt         : String? = null,
  @SerializedName("updated_at"           ) var updatedAt         : String? = null,
  @SerializedName("deleted_at"           ) var deletedAt         : String? = null,
  @SerializedName("need_to_be_destroyed" ) var needToBeDestroyed : Int?    = null

)