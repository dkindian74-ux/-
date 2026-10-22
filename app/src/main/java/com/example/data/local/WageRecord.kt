package com.example.data.local

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "wage_records")
data class WageRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val month: Int, // 1 to 12
    val monthHindiName: String,
    val year: Int,
    val dailyWage: Double,
    val totalCalendarDays: Int,
    val presentDays: Int,
    val halfDays: Int = 0,
    val absentDays: Int,
    val paidWageDays: Double = presentDays.toDouble(),
    val grossWages: Double,
    val advanceAmount: Double = 0.0,
    val remainingPayable: Double = grossWages,
    val attendanceData: String = "", // e.g. "1:P,2:H,3:A"
    val formattedDailyWage: String,
    val formattedGrossWages: String,
    val formattedAdvance: String = "₹0",
    val formattedRemainingPayable: String = formattedGrossWages,
    val calculationFormulaText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val savedDateTimeText: String = ""
) {
    @get:Ignore
    val totalWage: Double
        get() = remainingPayable

    @get:Ignore
    val formattedTotalWage: String
        get() = formattedRemainingPayable
}
