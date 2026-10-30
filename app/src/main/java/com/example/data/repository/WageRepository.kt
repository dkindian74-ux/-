package com.example.data.repository

import com.example.data.local.AdvanceRecord
import com.example.data.local.AdvanceRecordDao
import com.example.data.local.WageRecord
import com.example.data.local.WageRecordDao
import kotlinx.coroutines.flow.Flow

class WageRepository(
    private val wageDao: WageRecordDao,
    private val advanceDao: AdvanceRecordDao? = null
) {

    val allRecords: Flow<List<WageRecord>> = wageDao.getAllRecords()

    val allAdvances: Flow<List<AdvanceRecord>>? = advanceDao?.getAllAdvances()

    val allContractors: Flow<List<String>>? = advanceDao?.getAllContractorNames()

    suspend fun getRecordById(id: Long): WageRecord? {
        return wageDao.getRecordById(id)
    }

    suspend fun getRecordByMonthAndYear(month: Int, year: Int): WageRecord? {
        return wageDao.getRecordByMonthAndYear(month, year)
    }

    suspend fun saveRecord(record: WageRecord): Long {
        return wageDao.insertRecord(record)
    }

    suspend fun updateRecord(record: WageRecord) {
        wageDao.updateRecord(record)
    }

    suspend fun deleteRecord(id: Long) {
        wageDao.deleteRecordById(id)
    }

    suspend fun deleteAllRecords() {
        wageDao.deleteAllRecords()
    }

    // Advance record methods
    suspend fun saveAdvance(record: AdvanceRecord): Long {
        return advanceDao?.insertAdvance(record) ?: -1L
    }

    suspend fun updateAdvance(record: AdvanceRecord) {
        advanceDao?.updateAdvance(record)
    }

    suspend fun deleteAdvance(id: Long) {
        advanceDao?.deleteAdvanceById(id)
    }

    suspend fun deleteAllAdvances() {
        advanceDao?.deleteAllAdvances()
    }
}
