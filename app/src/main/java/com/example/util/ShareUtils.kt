package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.example.data.local.WageRecord
import com.example.model.CalculationResult

object ShareUtils {

    /**
     * Builds standard Hindi share message for WhatsApp from CalculationResult.
     */
    fun buildShareMessage(result: CalculationResult): String {
        return buildString {
            appendLine("मेरी मजदूरी का हिसाब")
            appendLine()
            appendLine("महीना: ${result.monthName} ${result.year}")
            appendLine("दैनिक मजदूरी: ${result.formattedDailyWage}")
            appendLine("कुल कैलेंडर दिन: ${result.totalDays}")
            appendLine("उपस्थित दिन: ${result.presentDays}")
            appendLine("आधी मजदूरी वाले दिन: ${result.halfDays}")
            appendLine("अनुपस्थित दिन: ${result.absentDays}")
            appendLine("कुल मजदूरी: ${result.formattedGrossWages}")
            appendLine("लिया गया एडवांस: ${result.formattedAdvance}")
            appendLine("बाकी लेने योग्य राशि: ${result.formattedRemainingPayable}")
            appendLine()
            append("यह हिसाब ऐप में दर्ज जानकारी के आधार पर तैयार किया गया है।")
        }
    }

    /**
     * Builds standard Hindi share message from a saved WageRecord.
     */
    fun buildShareMessage(record: WageRecord): String {
        return buildString {
            appendLine("मेरी मजदूरी का हिसाब")
            appendLine()
            appendLine("महीना: ${record.monthHindiName} ${record.year}")
            appendLine("दैनिक मजदूरी: ${record.formattedDailyWage}")
            appendLine("कुल कैलेंडर दिन: ${record.totalCalendarDays}")
            appendLine("उपस्थित दिन: ${record.presentDays}")
            appendLine("आधी मजदूरी वाले दिन: ${record.halfDays}")
            appendLine("अनुपस्थित दिन: ${record.absentDays}")
            appendLine("कुल मजदूरी: ${record.formattedGrossWages}")
            appendLine("लिया गया एडवांस: ${record.formattedAdvance}")
            appendLine("बाकी लेने योग्य राशि: ${record.formattedRemainingPayable}")
            appendLine()
            append("यह हिसाब ऐप में दर्ज जानकारी के आधार पर तैयार किया गया है।")
        }
    }

    /**
     * Shares a CalculationResult as a high-resolution PNG image card via WhatsApp/System Chooser.
     */
    fun shareCalculationAsImage(context: Context, result: CalculationResult) {
        try {
            val summary = WageImageGenerator.fromCalculationResult(result)
            val bitmap = WageImageGenerator.createWageReportBitmap(summary)
            val filePrefix = "${result.monthName}_${result.year}"
            val uri = WageImageGenerator.saveBitmapToCache(context, bitmap, filePrefix)
            val caption = "मेरी मजदूरी का हिसाब — ${result.monthName} ${result.year}\nबाकी लेने योग्य राशि: ${result.formattedRemainingPayable}"
            shareImageUri(context, uri, caption)
        } catch (e: Exception) {
            // Fallback to text sharing if image generation encounters any device error
            shareViaIntent(context, buildShareMessage(result))
        }
    }

    /**
     * Shares a saved WageRecord as a high-resolution PNG image card via WhatsApp/System Chooser.
     */
    fun shareRecordAsImage(context: Context, record: WageRecord) {
        try {
            val summary = WageImageGenerator.fromWageRecord(record)
            val bitmap = WageImageGenerator.createWageReportBitmap(summary)
            val filePrefix = "${record.monthHindiName}_${record.year}"
            val uri = WageImageGenerator.saveBitmapToCache(context, bitmap, filePrefix)
            val caption = "मेरी मजदूरी का हिसाब — ${record.monthHindiName} ${record.year}\nबाकी लेने योग्य राशि: ${record.formattedRemainingPayable}"
            shareImageUri(context, uri, caption)
        } catch (e: Exception) {
            // Fallback to text sharing if image generation encounters any device error
            shareViaIntent(context, buildShareMessage(record))
        }
    }

    /**
     * Launches Android share intent with image/png and FileProvider URI.
     */
    fun shareImageUri(context: Context, imageUri: Uri, caption: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, caption)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(sendIntent, "WhatsApp पर हिसाब भेजें").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            val resInfoList = context.packageManager.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } catch (_: Exception) {
        }

        try {
            context.startActivity(chooser)
        } catch (_: Exception) {
            // Fallback
        }
    }

    /**
     * Launches Android share intent with plain text (for backwards compatibility / fallback).
     */
    fun shareViaIntent(context: Context, text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val chooser = Intent.createChooser(sendIntent, "WhatsApp पर हिसाब भेजें")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(chooser)
        } catch (_: Exception) {
            // Graceful fallback
        }
    }
}
