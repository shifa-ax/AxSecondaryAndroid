package com.ax.axsecondaryapp.db

import androidx.room.TypeConverter
import com.ax.axsecondaryapp.model.CallLogDetails
import com.google.gson.Gson

class CallLogDetailsConverter {
    @TypeConverter
    fun toDatabaseValue(callLogDetails: CallLogDetails): String {
        // Convert CallLogDetails object to a JSON string
        return Gson().toJson(callLogDetails)
    }

    @TypeConverter
    fun toModelValue(value: String): CallLogDetails {
        // Convert JSON string to a CallLogDetails object
        return Gson().fromJson(value, CallLogDetails::class.java)
    }
}
