package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AdvanceRecordDao {

    @Query("SELECT * FROM advance_records ORDER BY timestamp DESC")
    fun getAllAdvances(): Flow<List<AdvanceRecord>>

    @Query("SELECT DISTINCT contractorName FROM advance_records WHERE contractorName != '' ORDER BY contractorName ASC")
    fun getAllContractorNames(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvance(record: AdvanceRecord): Long

    @Update
    suspend fun updateAdvance(record: AdvanceRecord)

    @Query("DELETE FROM advance_records WHERE id = :id")
    suspend fun deleteAdvanceById(id: Long)

    @Query("DELETE FROM advance_records")
    suspend fun deleteAllAdvances()
}
