package com.ax.axsecondaryapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import javax.inject.Singleton

@Database(
    entities = [CallLogDetailsTable::class, FileDetails::class],
    version = 2,
    exportSchema = false
)
@Singleton
abstract class CallLogDatabase : RoomDatabase(){

    abstract fun callLogDetailsDao() : CallLogDetailsDao


    companion object {
        @Volatile private var instance : CallLogDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            CallLogDatabase::class.java,
            "notedatabase"
        ).build()

    }
}