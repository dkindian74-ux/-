package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WageRecord::class], version = 2, exportSchema = false)
abstract class WageDatabase : RoomDatabase() {

    abstract fun wageRecordDao(): WageRecordDao

    companion object {
        @Volatile
        private var INSTANCE: WageDatabase? = null

        fun getDatabase(context: Context): WageDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WageDatabase::class.java,
                    "meri_mazdoori_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
