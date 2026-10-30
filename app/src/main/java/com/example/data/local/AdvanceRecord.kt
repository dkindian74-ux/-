package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "advance_records")
data class AdvanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contractorName: String,
    val amount: Double,
    val dateText: String, // e.g. "29/10/2026"
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
