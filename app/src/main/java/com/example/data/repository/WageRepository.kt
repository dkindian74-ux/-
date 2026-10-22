package com.example.data.repository

import com.example.data.local.WageRecord
import com.example.data.local.WageRecordDao
import kotlinx.coroutines.flow.Flow

class WageRepository(private val dao: WageRecordDao) {

    val allRecords: Flow<List<WageRecord>> = dao.getAllRecords()

    suspend fun getRecordById(id: Long): WageRecord? {
        return dao.getRecordById(id)
    }

    suspend fun getRecordByMonthAndYear(month: Int, year: Int): WageRecord? {
        return dao.getRecordByMonthAndYear(month, year)
    }

    suspend fun saveRecord(record: WageRecord): Long {
        return dao.insertRecord(record)
    }

    suspend fun updateRecord(record: WageRecord) {
        dao.updateRecord(record)
    }

    suspend fun deleteRecord(id: Long) {
        dao.deleteRecordById(id)
    }

    suspend fun deleteAllRecords() {
        dao.deleteAllRecords()
    }
}
