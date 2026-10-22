package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WageRecord
import com.example.model.CalculationResult
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.AbsentRedBg
import com.example.ui.theme.AbsentRedText
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.HalfDayAmber
import com.example.ui.theme.HalfDayAmberText
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenBg
import com.example.ui.theme.PresentGreenText
import com.example.util.ShareUtils

@Composable
fun ResultsSection(
    result: CalculationResult?,
    isStale: Boolean,
    saveConfirmationMessage: String?,
    duplicatePrompt: WageRecord?,
    onSaveCalculation: () -> Unit,
    onConfirmUpdateExisting: () -> Unit,
    onDismissDuplicatePrompt: () -> Unit,
    onCalculateAgain: () -> Unit,
    onStartNewCalculation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (isStale && result == null) {
        // Warning banner when attendance changed after calculation
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("result_stale_warning"),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFEF3C7)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = BrandAmber,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "हाजिरी में बदलाव हुआ है",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF78350F)
                    )
                    Text(
                        text = "नया हिसाब देखने के लिए नीचे बटन दबाएं",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF92400E)
                    )
                }
                Button(
                    onClick = onCalculateAgain,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandAmber),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("हिसाब करें")
                }
            }
        }
        return
    }

    if (result == null) return

    Column(modifier = modifier.fillMaxWidth()) {

        // Confirmation banner if calculation was just saved or updated
        if (saveConfirmationMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("save_success_banner"),
                colors = CardDefaults.cardColors(containerColor = PresentGreenBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PresentGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = saveConfirmationMessage,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PresentGreenText
                    )
                }
            }
        }

        // Advance Excess Warning banner
        if (result.isAdvanceExcess) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("advance_excess_warning"),
                colors = CardDefaults.cardColors(containerColor = AbsentRedBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = AbsentRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "एडवांस, कुल मजदूरी से अधिक है।",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AbsentRedText
                        )
                        Text(
                            text = "लिया गया एडवांस (${result.formattedAdvance}) कुल कमाई (${result.formattedGrossWages}) से ज्यादा है।",
                            style = MaterialTheme.typography.bodySmall,
                            color = AbsentRedText
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("result_card"),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BrandBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = BrandBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.size(12.dp))
                        Column {
                            Text(
                                text = "आपकी महीने की मजदूरी का हिसाब",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlueDark
                            )
                            Text(
                                text = "${result.monthName} ${result.year}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Main Total Highlight Banner: Remaining Payable (or Gross if no advance)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (result.isAdvanceExcess) AbsentRed else BrandBlue)
                        .padding(vertical = 18.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (result.advanceAmount > 0) "बाकी लेने योग्य राशि" else "कुल मजदूरी",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.88f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result.formattedRemainingPayable,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color.White,
                            modifier = Modifier.testTag("result_total_wage")
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (result.advanceAmount > 0) {
                            Text(
                                text = "कुल कमाई: ${result.formattedGrossWages} − एडवांस: ${result.formattedAdvance}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.92f),
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = result.calculationFormulaText,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.92f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                Spacer(modifier = Modifier.height(12.dp))

                // Detailed Breakdown
                BreakdownRow(
                    label = "महीना",
                    value = "${result.monthName} ${result.year}"
                )
                BreakdownRow(
                    label = "कुल कैलेंडर दिन",
                    value = "${result.totalDays} दिन"
                )
                BreakdownRow(
                    label = "उपस्थित दिन (पूरे)",
                    value = "${result.presentDays} दिन",
                    valueColor = PresentGreen
                )
                if (result.halfDays > 0) {
                    BreakdownRow(
                        label = "आधी मजदूरी वाले दिन",
                        value = "${result.halfDays} दिन",
                        valueColor = HalfDayAmber
                    )
                }
                BreakdownRow(
                    label = "अनुपस्थित दिन",
                    value = "${result.absentDays} दिन",
                    valueColor = AbsentRed
                )
                val paidDaysStr = if (result.paidWageDays % 1.0 == 0.0) {
                    result.paidWageDays.toInt().toString()
                } else {
                    result.paidWageDays.toString()
                }
                BreakdownRow(
                    label = "कुल देय मजदूरी दिन",
                    value = "$paidDaysStr दिन",
                    valueColor = BrandBlueDark
                )
                BreakdownRow(
                    label = "दैनिक मजदूरी",
                    value = result.formattedDailyWage
                )
                BreakdownRow(
                    label = "कुल मजदूरी (Gross)",
                    value = result.formattedGrossWages,
                    valueColor = BrandBlueDark
                )
                if (result.advanceAmount > 0) {
                    BreakdownRow(
                        label = "लिया गया एडवांस",
                        value = "− ${result.formattedAdvance}",
                        valueColor = BrandAmber
                    )
                    BreakdownRow(
                        label = "बाकी लेने योग्य राशि",
                        value = result.formattedRemainingPayable,
                        valueColor = if (result.isAdvanceExcess) AbsentRed else PresentGreen
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 1. WhatsApp Share Button ("WhatsApp पर हिसाब भेजें")
                Button(
                    onClick = {
                        ShareUtils.shareCalculationAsImage(context, result)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("whatsapp_share_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF25D366) // Official WhatsApp Green
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "WhatsApp पर हिसाब भेजें",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Save Calculation Button ("हिसाब सेव करें")
                Button(
                    onClick = onSaveCalculation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_calculation_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "हिसाब सेव करें",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Start New Calculation Button
                OutlinedButton(
                    onClick = onStartNewCalculation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("new_calculation_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = BrandBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "नया हिसाब करें",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    // Duplicate Record Alert Dialog
    if (duplicatePrompt != null) {
        AlertDialog(
            onDismissRequest = onDismissDuplicatePrompt,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = BrandAmber,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "इस महीने का हिसाब पहले से मौजूद है",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueDark
                )
            },
            text = {
                Text(
                    text = "${duplicatePrompt.monthHindiName} ${duplicatePrompt.year} का पुराना हिसाब पहले से सेव है (कुल: ${duplicatePrompt.formattedTotalWage})।\n\nक्या आप पुराने रिकॉर्ड को नए हिसाब (${result.formattedRemainingPayable}) से अपडेट करना चाहते हैं?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF334155),
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmUpdateExisting,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Text("अपडेट करें", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDuplicatePrompt) {
                    Text("रद्द करें", fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF1E293B)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF64748B)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
