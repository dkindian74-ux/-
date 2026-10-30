package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyUtils {

    /**
     * Converts Devanagari numerals (०-९) to ASCII digits ('0'-'9').
     */
    fun normalizeDigits(input: String): String {
        val devanagariDigits = "०१२३४५६७८९"
        return input.map { ch ->
            val index = devanagariDigits.indexOf(ch)
            if (index != -1) ('0' + index) else ch
        }.joinToString("")
    }

    /**
     * Formats a double amount in Indian Rupee format with ₹ prefix.
     * E.g. 14000 -> "₹14,000", 6844 -> "₹6,844", 100000 -> "₹1,00,000"
     * If amount is a whole number, does not show decimal places.
     * If amount has fractional value, shows up to 2 decimal places.
     */
    fun formatIndianRupees(amount: Double): String {
        val isWhole = (amount % 1.0 == 0.0)
        val formattedNumber = formatIndianNumber(amount, if (isWhole) 0 else 2)
        return "₹$formattedNumber"
    }

    fun formatRupees(amount: Double): String = formatIndianRupees(amount)


    /**
     * Formats number in the Indian numbering system (xx,xx,xxx)
     */
    fun formatIndianNumber(amount: Double, decimalPlaces: Int): String {
        val symbols = DecimalFormatSymbols(Locale.ENGLISH)
        val wholePart = amount.toLong()
        val fractionPart = amount - wholePart

        val wholePartString = wholePart.toString()
        val formattedWhole = if (wholePartString.length <= 3) {
            wholePartString
        } else {
            val lastThree = wholePartString.takeLast(3)
            val rest = wholePartString.dropLast(3)
            val regex = "(\\d)(?=(\\d{2})+$)".toRegex()
            val formattedRest = regex.replace(rest, "$1,")
            "$formattedRest,$lastThree"
        }

        return if (decimalPlaces > 0 && fractionPart > 0.0001) {
            val pattern = if (decimalPlaces == 1) "#0.0" else "#0.00"
            val df = DecimalFormat(pattern, symbols)
            val fractionFormatted = df.format(amount)
            val dotIndex = fractionFormatted.indexOf('.')
            if (dotIndex != -1) {
                "$formattedWhole${fractionFormatted.substring(dotIndex)}"
            } else {
                formattedWhole
            }
        } else {
            formattedWhole
        }
    }

    /**
     * Parses a string to valid positive daily wage or returns null if invalid.
     * Handles Devanagari numerals, commas, rupee signs, and decimal amounts.
     */
    fun parseDailyWage(input: String): Double? {
        val normalized = normalizeDigits(input)
            .replace("₹", "")
            .replace(",", "")
            .trim()

        if (normalized.isEmpty()) return null
        return try {
            val value = normalized.toDouble()
            if (value > 0.0) value else null
        } catch (_: Exception) {
            null
        }
    }
}
