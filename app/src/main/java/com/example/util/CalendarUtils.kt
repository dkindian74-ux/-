package com.example.util

import com.example.model.HINDI_MONTHS
import java.util.Calendar
import java.util.GregorianCalendar

object CalendarUtils {

    /**
     * Returns total days in a given month (1..12) and year.
     * Accurately handles leap years for February (29 in leap years, 28 otherwise).
     */
    fun getDaysInMonth(year: Int, month: Int): Int {
        val calendar = GregorianCalendar(year, month - 1, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    /**
     * Returns the 0-based index of the weekday for the 1st of the month.
     * 0 = Sunday (रवि), 1 = Monday (सोम), ..., 6 = Saturday (शनि).
     */
    fun getFirstDayOfWeekOffset(year: Int, month: Int): Int {
        val calendar = GregorianCalendar(year, month - 1, 1)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        // Calendar.SUNDAY == 1, so (dayOfWeek - Calendar.SUNDAY) -> 0..6
        return dayOfWeek - Calendar.SUNDAY
    }

    /**
     * Returns the Hindi name of the month (1..12).
     */
    fun getHindiMonthName(monthNumber: Int): String {
        return HINDI_MONTHS.find { it.monthNumber == monthNumber }?.hindiName ?: ""
    }

    /**
     * Determines if a given year is a leap year.
     */
    fun isLeapYear(year: Int): Boolean {
        val calendar = GregorianCalendar()
        return calendar.isLeapYear(year)
    }

    /**
     * Dynamically calculates the previous calendar month and year based on a given Calendar instance (defaults to current date).
     * Correctly handles year transitions (e.g., January 2027 -> December 2026).
     * Returns Pair(monthNumber 1..12, year).
     */
    fun getPreviousMonthAndYear(calendar: Calendar = Calendar.getInstance()): Pair<Int, Int> {
        val cal = (calendar.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
        }
        val prevMonth = cal.get(Calendar.MONTH) + 1 // 1..12
        val prevYear = cal.get(Calendar.YEAR)
        return Pair(prevMonth, prevYear)
    }
}
