package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.WageRecord
import com.example.model.CalculationResult
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates high-quality, professional wage breakdown images (PNG)
 * for sharing via WhatsApp or other messaging apps.
 */
object WageImageGenerator {

    private const val IMAGE_WIDTH = 1080
    private const val IMAGE_HEIGHT = 1440

    // Color Palette
    private val COLOR_CARD_BG = Color.parseColor("#FFFFFF")
    private val COLOR_OUTER_BG = Color.parseColor("#F1F5F9")
    private val COLOR_PRIMARY_NAVY = Color.parseColor("#1E3A8A")
    private val COLOR_PRIMARY_BLUE = Color.parseColor("#2563EB")
    private val COLOR_LIGHT_BLUE_BG = Color.parseColor("#EFF6FF")
    private val COLOR_TEXT_DARK = Color.parseColor("#0F172A")
    private val COLOR_TEXT_MUTED = Color.parseColor("#64748B")
    private val COLOR_TEXT_LIGHT = Color.parseColor("#94A3B8")
    private val COLOR_DIVIDER = Color.parseColor("#E2E8F0")

    // Attendance colors
    private val COLOR_PRESENT_BG = Color.parseColor("#ECFDF5")
    private val COLOR_PRESENT_BORDER = Color.parseColor("#10B981")
    private val COLOR_PRESENT_TEXT = Color.parseColor("#065F46")

    private val COLOR_HALF_BG = Color.parseColor("#FFFBEB")
    private val COLOR_HALF_BORDER = Color.parseColor("#F59E0B")
    private val COLOR_HALF_TEXT = Color.parseColor("#92400E")

    private val COLOR_ABSENT_BG = Color.parseColor("#FEF2F2")
    private val COLOR_ABSENT_BORDER = Color.parseColor("#EF4444")
    private val COLOR_ABSENT_TEXT = Color.parseColor("#991B1B")

    data class SummaryData(
        val monthName: String,
        val year: Int,
        val totalCalendarDays: Int,
        val presentDays: Int,
        val halfDays: Int,
        val absentDays: Int,
        val paidWageDays: Double,
        val formattedDailyWage: String,
        val formattedGrossWages: String,
        val formattedAdvance: String,
        val advanceAmount: Double,
        val formattedRemainingPayable: String,
        val isAdvanceExcess: Boolean,
        val calculationFormulaText: String,
        val savedDateTime: String?
    )

    fun fromCalculationResult(result: CalculationResult): SummaryData {
        return SummaryData(
            monthName = result.monthName,
            year = result.year,
            totalCalendarDays = result.totalDays,
            presentDays = result.presentDays,
            halfDays = result.halfDays,
            absentDays = result.absentDays,
            paidWageDays = result.paidWageDays,
            formattedDailyWage = result.formattedDailyWage,
            formattedGrossWages = result.formattedGrossWages,
            formattedAdvance = result.formattedAdvance,
            advanceAmount = result.advanceAmount,
            formattedRemainingPayable = result.formattedRemainingPayable,
            isAdvanceExcess = result.isAdvanceExcess,
            calculationFormulaText = result.calculationFormulaText,
            savedDateTime = null
        )
    }

    fun fromWageRecord(record: WageRecord): SummaryData {
        return SummaryData(
            monthName = record.monthHindiName,
            year = record.year,
            totalCalendarDays = record.totalCalendarDays,
            presentDays = record.presentDays,
            halfDays = record.halfDays,
            absentDays = record.absentDays,
            paidWageDays = record.paidWageDays,
            formattedDailyWage = record.formattedDailyWage,
            formattedGrossWages = record.formattedGrossWages,
            formattedAdvance = record.formattedAdvance,
            advanceAmount = record.advanceAmount,
            formattedRemainingPayable = record.formattedRemainingPayable,
            isAdvanceExcess = record.advanceAmount > record.grossWages,
            calculationFormulaText = record.calculationFormulaText,
            savedDateTime = record.savedDateTimeText.ifEmpty { null }
        )
    }

    /**
     * Renders a complete, standalone Bitmap containing the wage calculation report.
     */
    fun createWageReportBitmap(summary: SummaryData): Bitmap {
        val bitmap = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Canvas Background
        canvas.drawColor(COLOR_OUTER_BG)

        // 2. Main White Card Container
        val cardRect = RectF(32f, 32f, (IMAGE_WIDTH - 32).toFloat(), (IMAGE_HEIGHT - 32).toFloat())
        paint.color = COLOR_CARD_BG
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(cardRect, 32f, 32f, paint)

        // Border around card
        paint.color = COLOR_DIVIDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawRoundRect(cardRect, 32f, 32f, paint)
        paint.style = Paint.Style.FILL

        // 3. Top Header Banner
        val headerHeight = 200f
        val headerRect = RectF(32f, 32f, (IMAGE_WIDTH - 32).toFloat(), 32f + headerHeight)
        paint.color = COLOR_PRIMARY_NAVY
        canvas.drawRoundRect(headerRect, 32f, 32f, paint)
        // Square off bottom corners of header
        canvas.drawRect(32f, 32f + headerHeight - 32f, (IMAGE_WIDTH - 32).toFloat(), 32f + headerHeight, paint)

        // Header Title: "मेरी मजदूरी"
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 50f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("मेरी मजदूरी", 72f, 105f, paint)

        // Subtitle: "मासिक मजदूरी एवं हाजिरी हिसाब"
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 22f
        paint.color = Color.parseColor("#E0E7FF")
        canvas.drawText("मासिक मजदूरी एवं हाजिरी हिसाब", 72f, 145f, paint)

        // Header Month/Year Pill Badge (Right side of header)
        val monthBadgeText = "${summary.monthName} ${summary.year}"
        paint.textSize = 26f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val badgeTextWidth = paint.measureText(monthBadgeText)
        val badgeWidth = badgeTextWidth + 48f
        val badgeHeight = 56f
        val badgeRight = (IMAGE_WIDTH - 72).toFloat()
        val badgeLeft = badgeRight - badgeWidth
        val badgeTop = 90f
        val badgeRect = RectF(badgeLeft, badgeTop, badgeRight, badgeTop + badgeHeight)

        paint.color = COLOR_PRIMARY_BLUE
        canvas.drawRoundRect(badgeRect, 28f, 28f, paint)

        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        val badgeY = badgeTop + (badgeHeight / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText(monthBadgeText, badgeRect.centerX(), badgeY, paint)

        // 4. Prominent Highlight Card: Net Payable (बाकी लेने योग्य राशि)
        val heroTop = 32f + headerHeight + 24f
        val heroHeight = 175f
        val heroRect = RectF(64f, heroTop, (IMAGE_WIDTH - 64).toFloat(), heroTop + heroHeight)

        paint.color = if (summary.isAdvanceExcess) COLOR_ABSENT_TEXT else COLOR_PRIMARY_NAVY
        canvas.drawRoundRect(heroRect, 20f, 20f, paint)

        // Hero Title
        val heroTitle = if (summary.advanceAmount > 0) "बाकी लेने योग्य राशि (Net Payable)" else "कुल मजदूरी (Total Wages)"
        paint.color = Color.parseColor("#DBEAFE")
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(heroTitle, heroRect.centerX(), heroTop + 42f, paint)

        // Hero Big Amount
        paint.color = Color.WHITE
        paint.textSize = 62f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(summary.formattedRemainingPayable, heroRect.centerX(), heroTop + 110f, paint)

        // Hero Subtext
        val heroSubtext = if (summary.advanceAmount > 0) {
            "कुल कमाई: ${summary.formattedGrossWages}  |  एडवांस कटौती: ${summary.formattedAdvance}"
        } else {
            summary.calculationFormulaText
        }
        paint.color = Color.parseColor("#BFDBFE")
        paint.textSize = 21f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(heroSubtext, heroRect.centerX(), heroTop + 150f, paint)

        // 5. Attendance Summary Section Title
        val attendSecTop = heroTop + heroHeight + 32f
        paint.color = COLOR_TEXT_DARK
        paint.textSize = 26f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("हाजिरी विवरण (Attendance Summary)", 68f, attendSecTop, paint)

        // 3 Attendance Stat Chips side by side
        val chipTop = attendSecTop + 16f
        val chipHeight = 135f
        val totalAvailableWidth = (IMAGE_WIDTH - 128 - 32).toFloat() // 2 gaps of 16px
        val chipWidth = totalAvailableWidth / 3f

        // Chip 1: Present (उपस्थित)
        val chip1Left = 64f
        drawStatChip(
            canvas = canvas,
            paint = paint,
            rect = RectF(chip1Left, chipTop, chip1Left + chipWidth, chipTop + chipHeight),
            bgColor = COLOR_PRESENT_BG,
            borderColor = COLOR_PRESENT_BORDER,
            textColor = COLOR_PRESENT_TEXT,
            title = "उपस्थित दिन",
            value = "${summary.presentDays} दिन",
            subtext = "1 दिन = 1.0 मजदूरी"
        )

        // Chip 2: Half-Day (आधी मजदूरी)
        val chip2Left = chip1Left + chipWidth + 16f
        drawStatChip(
            canvas = canvas,
            paint = paint,
            rect = RectF(chip2Left, chipTop, chip2Left + chipWidth, chipTop + chipHeight),
            bgColor = COLOR_HALF_BG,
            borderColor = COLOR_HALF_BORDER,
            textColor = COLOR_HALF_TEXT,
            title = "आधी मजदूरी",
            value = "${summary.halfDays} दिन",
            subtext = "1 दिन = 0.5 मजदूरी"
        )

        // Chip 3: Absent (अनुपस्थित)
        val chip3Left = chip2Left + chipWidth + 16f
        drawStatChip(
            canvas = canvas,
            paint = paint,
            rect = RectF(chip3Left, chipTop, chip3Left + chipWidth, chipTop + chipHeight),
            bgColor = COLOR_ABSENT_BG,
            borderColor = COLOR_ABSENT_BORDER,
            textColor = COLOR_ABSENT_TEXT,
            title = "अनुपस्थित",
            value = "${summary.absentDays} दिन",
            subtext = "1 दिन = 0 मजदूरी"
        )

        // 6. Detailed Wage Calculation Breakdown Table
        val tableTop = chipTop + chipHeight + 30f
        val tableHeight = 440f
        val tableRect = RectF(64f, tableTop, (IMAGE_WIDTH - 64).toFloat(), tableTop + tableHeight)

        paint.color = COLOR_LIGHT_BLUE_BG
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(tableRect, 18f, 18f, paint)

        paint.color = COLOR_DIVIDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawRoundRect(tableRect, 18f, 18f, paint)
        paint.style = Paint.Style.FILL

        // Table Header
        paint.color = COLOR_PRIMARY_NAVY
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("मजदूरी हिसाब विवरण", 96f, tableTop + 42f, paint)

        // Divider below table title
        paint.color = COLOR_DIVIDER
        paint.strokeWidth = 2f
        canvas.drawLine(96f, tableTop + 58f, (IMAGE_WIDTH - 96).toFloat(), tableTop + 58f, paint)

        val paidDaysStr = if (summary.paidWageDays % 1.0 == 0.0) {
            summary.paidWageDays.toInt().toString()
        } else {
            summary.paidWageDays.toString()
        }

        var rowY = tableTop + 96f
        val rowSpacing = 48f

        drawTableRow(canvas, paint, "कुल कैलेंडर दिन", "${summary.totalCalendarDays} दिन", rowY, COLOR_TEXT_MUTED, COLOR_TEXT_DARK)
        rowY += rowSpacing
        drawTableRow(canvas, paint, "कुल देय मजदूरी दिन", "$paidDaysStr दिन", rowY, COLOR_TEXT_MUTED, COLOR_PRIMARY_NAVY, isBold = true)
        rowY += rowSpacing
        drawTableRow(canvas, paint, "दैनिक मजदूरी दर", "${summary.formattedDailyWage} प्रति दिन", rowY, COLOR_TEXT_MUTED, COLOR_TEXT_DARK)
        rowY += rowSpacing
        drawTableRow(canvas, paint, "कुल मजदूरी (Gross)", summary.formattedGrossWages, rowY, COLOR_TEXT_MUTED, COLOR_TEXT_DARK, isBold = true)
        rowY += rowSpacing
        val advanceText = if (summary.advanceAmount > 0) "− ${summary.formattedAdvance}" else "₹0"
        drawTableRow(canvas, paint, "लिया गया एडवांस (कटौती)", advanceText, rowY, COLOR_TEXT_MUTED, COLOR_HALF_TEXT, isBold = true)

        // Divider before final row
        rowY += 24f
        paint.color = COLOR_DIVIDER
        paint.strokeWidth = 2.5f
        canvas.drawLine(96f, rowY, (IMAGE_WIDTH - 96).toFloat(), rowY, paint)

        // Final Payable Row
        rowY += 46f
        drawTableRow(
            canvas = canvas,
            paint = paint,
            label = "बाकी लेने योग्य शुद्ध राशि",
            value = summary.formattedRemainingPayable,
            y = rowY,
            labelColor = COLOR_PRIMARY_NAVY,
            valueColor = if (summary.isAdvanceExcess) COLOR_ABSENT_TEXT else COLOR_PRIMARY_NAVY,
            isBold = true,
            fontSize = 32f
        )

        // 7. Footer Section
        val footerTop = tableTop + tableHeight + 36f

        paint.color = COLOR_DIVIDER
        paint.strokeWidth = 1.5f
        canvas.drawLine(96f, footerTop, (IMAGE_WIDTH - 96).toFloat(), footerTop, paint)

        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 21f
        paint.color = COLOR_TEXT_MUTED
        canvas.drawText("यह हिसाब 'मेरी मजदूरी' मोबाइल ऐप द्वारा तैयार किया गया है।", (IMAGE_WIDTH / 2).toFloat(), footerTop + 40f, paint)

        val dateText = summary.savedDateTime?.let { "सेव किया गया: $it" } ?: run {
            val now = SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.getDefault()).format(Date())
            "तैयार किया गया: $now"
        }
        paint.textSize = 19f
        paint.color = COLOR_TEXT_LIGHT
        canvas.drawText(dateText, (IMAGE_WIDTH / 2).toFloat(), footerTop + 72f, paint)

        return bitmap
    }

    private fun drawStatChip(
        canvas: Canvas,
        paint: Paint,
        rect: RectF,
        bgColor: Int,
        borderColor: Int,
        textColor: Int,
        title: String,
        value: String,
        subtext: String
    ) {
        // Background
        paint.color = bgColor
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(rect, 16f, 16f, paint)

        // Border
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f
        canvas.drawRoundRect(rect, 16f, 16f, paint)
        paint.style = Paint.Style.FILL

        // Title
        paint.color = textColor
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 21f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(title, rect.centerX(), rect.top + 34f, paint)

        // Value
        paint.textSize = 34f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(value, rect.centerX(), rect.top + 78f, paint)

        // Subtext
        paint.textSize = 17f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(subtext, rect.centerX(), rect.top + 112f, paint)
    }

    private fun drawTableRow(
        canvas: Canvas,
        paint: Paint,
        label: String,
        value: String,
        y: Float,
        labelColor: Int,
        valueColor: Int,
        isBold: Boolean = false,
        fontSize: Float = 22f
    ) {
        // Label (Left)
        paint.color = labelColor
        paint.textSize = fontSize
        paint.typeface = Typeface.create(Typeface.DEFAULT, if (isBold) Typeface.BOLD else Typeface.NORMAL)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(label, 96f, y, paint)

        // Value (Right)
        paint.color = valueColor
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(value, (IMAGE_WIDTH - 96).toFloat(), y, paint)
    }

    /**
     * Saves the generated Bitmap as a PNG file into the application cache directory
     * and returns the content Uri via FileProvider.
     */
    fun saveBitmapToCache(context: Context, bitmap: Bitmap, fileNamePrefix: String): Uri {
        val imagesFolder = File(context.cacheDir, "shared_images").apply {
            if (!exists()) {
                mkdirs()
            }
        }
        val safePrefix = fileNamePrefix.replace("[^a-zA-Z0-9_]".toRegex(), "_")
        val file = File(imagesFolder, "hisab_${safePrefix}.png")

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
        }

        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }
}
