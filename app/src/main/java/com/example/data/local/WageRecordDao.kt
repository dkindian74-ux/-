package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WageRecordDao {

    @Query("SELECT * FROM wage_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<WageRecord>>

    @Query("SELECT * FROM wage_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): WageRecord?

    @Query("SELECT * FROM wage_records WHERE month = :month AND year = :year LIMIT 1")
    suspend fun getRecordByMonthAndYear(month: Int, year: Int): WageRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: WageRecord): Long

    @Update
    suspend fun updateRecord(record: WageRecord)

    @Query("DELETE FROM wage_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM wage_records")
    suspend fun deleteAllRecords()
}
