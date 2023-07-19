package com.ax.axsecondaryapp.db
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ax.axsecondaryapp.model.CallLogDetails

@Entity(tableName = "call_log_details")
data class CallLogDetailsTable(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String?,
    val number: String,
    val date: Long,
    val duration: Long,
    val callType: Int,
    val filePath: String?
)



@Entity(tableName = "file_details")
data class FileDetails(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String
)