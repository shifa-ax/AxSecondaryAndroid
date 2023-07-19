package com.ax.axsecondaryapp.db

import androidx.room.*

@Dao
interface CallLogDetailsDao {

    @Insert
    suspend fun addCallLog(callLog: CallLogDetailsTable)

    @Insert
    suspend fun addFilepath(fileDetails: FileDetails)

    @Query("SELECT * FROM call_log_details ORDER BY id DESC")
    suspend fun getAllCallLogs() : List<CallLogDetailsTable>?

    @Query("SELECT * FROM call_log_details WHERE id = :id")
    suspend fun getCallLogById(id: Long): CallLogDetailsTable?

    @Insert
    suspend fun addMultipleCall(vararg note: CallLogDetailsTable)

    @Update
    suspend fun updateCallLog(note: CallLogDetailsTable)

    @Delete
    suspend fun deleteCallLog(note: CallLogDetailsTable)
}