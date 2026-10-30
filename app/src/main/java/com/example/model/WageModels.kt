package com.example.model

/**
 * Information about a calendar month in Hindi and English.
 */
data class MonthItem(
    val monthNumber: Int, // 1 to 12
    val hindiName: String,
    val englishName: String
)

val HINDI_MONTHS = listOf(
    MonthItem(1, "जनवरी", "January"),
    MonthItem(2, "फरवरी", "February"),
    MonthItem(3, "मार्च", "March"),
    MonthItem(4, "अप्रैल", "April"),
    MonthItem(5, "मई", "May"),
    MonthItem(6, "जून", "June"),
    MonthItem(7, "जुलाई", "July"),
    MonthItem(8, "अगस्त", "August"),
    MonthItem(9, "सितंबर", "September"),
    MonthItem(10, "अक्टूबर", "October"),
    MonthItem(11, "नवंबर", "November"),
    MonthItem(12, "दिसंबर", "December")
)

/**
 * Hindi day of week abbreviations starting with Sunday.
 */
val HINDI_WEEKDAYS = listOf(
    "रवि", // Sunday
    "सोम", // Monday
    "मंगल", // Tuesday
    "बुध", // Wednesday
    "गुरु", // Thursday
    "शुक्र", // Friday
    "शनि"  // Saturday
)

/**
 * Three attendance states: Present, Half-Day, Absent.
 * Interaction cycle: Present -> Absent -> Half-Day -> Present
 */
enum class AttendanceStatus(val code: String, val hindiLabel: String, val wageMultiplier: Double) {
    PRESENT("P", "उपस्थित", 1.0),
    HALF_DAY("H", "आधी मजदूरी", 0.5),
    ABSENT("A", "अनुपस्थित", 0.0);

    fun next(): AttendanceStatus {
        return when (this) {
            PRESENT -> ABSENT
            ABSENT -> HALF_DAY
            HALF_DAY -> PRESENT
        }
    }

    companion object {
        fun fromCode(code: String): AttendanceStatus {
            return when (code) {
                "H" -> HALF_DAY
                "A" -> ABSENT
                else -> PRESENT
            }
        }
    }
}

/**
 * Single day representation in calendar grid.
 */
data class CalendarDayState(
    val dayOfMonth: Int,
    val status: AttendanceStatus
)

/**
 * Full calculation summary result including Advance and Half-Days.
 */
data class CalculationResult(
    val monthName: String,
    val year: Int,
    val totalDays: Int,
    val presentDays: Int,
    val halfDays: Int,
    val absentDays: Int,
    val paidWageDays: Double,
    val dailyWage: Double,
    val grossWages: Double,
    val advanceAmount: Double,
    val remainingPayable: Double,
    val isAdvanceExcess: Boolean,
    val formattedDailyWage: String,
    val formattedGrossWages: String,
    val formattedAdvance: String,
    val formattedRemainingPayable: String,
    val calculationFormulaText: String
) {
    val totalWage: Double
        get() = remainingPayable

    val formattedTotalWage: String
        get() = formattedRemainingPayable
}

/**
 * Result of direct days-based calculation.
 */
data class DaysCalculationResult(
    val dailyWage: Double,
    val fullDays: Double,
    val halfDays: Double,
    val totalEffectiveDays: Double,
    val grossWages: Double,
    val advanceDeducted: Double,
    val netPayable: Double,
    val formattedDailyWage: String,
    val formattedGrossWages: String,
    val formattedAdvance: String,
    val formattedNetPayable: String
)

/**
 * Navigation screen state.
 */
enum class AppScreen {
    MAIN_INPUT,
    MONTH_CALCULATION,
    DAYS_CALCULATION,
    ADVANCE_RECORDS,
    ATTENDANCE_CALENDAR,
    HISTORY
}

