package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DaysCalculationResult
import com.example.ui.components.AdMobBanner
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueDark
import com.example.viewmodel.WageUiState

@Composable
fun DaysCalculationScreen(
    uiState: WageUiState,
    onDailyWageChange: (String) -> Unit,
    onFullDaysChange: (String) -> Unit,
    onHalfDaysChange: (String) -> Unit,
    onAdvanceChange: (String) -> Unit,
    onCalculate: () -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    val result = uiState.daysCalculationResult

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .verticalScroll(scrollState)
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("days_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "मुख्य मेनू पर वापस",
                        tint = BrandBlue
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "दिनों का हिसाब",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "काम के दिनों और हाफ डे (आधे दिन) को दर्ज करके तुरंत अपनी कुल मजदूरी और बाकी पैसा निकालें।",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Input Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Daily Wage Input
                    Text(
                        text = "दैनिक मजदूरी (₹)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = uiState.daysDailyWageInput,
                        onValueChange = onDailyWageChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("days_daily_wage_input"),
                        placeholder = { Text("उदा. 500", color = Color(0xFF94A3B8)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CurrencyRupee,
                                contentDescription = null,
                                tint = BrandBlue
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Full Days Input
                    Text(
                        text = "पूरे काम के दिन (Full Days)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = uiState.daysFullDaysInput,
                        onValueChange = onFullDaysChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("days_full_days_input"),
                        placeholder = { Text("उदा. 20", color = Color(0xFF94A3B8)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = BrandBlue
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Half Days Input
                    Text(
                        text = "आधे दिन / हाफ डे (Half Days)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = uiState.daysHalfDaysInput,
                        onValueChange = onHalfDaysChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("days_half_days_input"),
                        placeholder = { Text("उदा. 2 (आधा दिन = 0.5)", color = Color(0xFF94A3B8)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B)
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Advance Amount Input
                    Text(
                        text = "एडवांस में लिए गए पैसे (₹)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = uiState.daysAdvanceInput,
                        onValueChange = onAdvanceChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("days_advance_input"),
                        placeholder = { Text("अगर कोई एडवांस नहीं तो 0 रखें", color = Color(0xFF94A3B8)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = Color(0xFFEF4444)
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                onCalculate()
                            }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Buttons: Calculate and Reset
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onReset,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("btn_days_reset"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("रीसेट")
                        }

                        Button(
                            onClick = {
                                keyboardController?.hide()
                                onCalculate()
                            },
                            modifier = Modifier
                                .weight(2f)
                                .height(50.dp)
                                .testTag("btn_days_calculate"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandBlue,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("हिसाब निकालें", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Calculation Result Section
            if (result != null) {
                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("days_result_card"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "हिसाब का विवरण",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlue
                            )

                            // WhatsApp Share Button
                            IconButton(
                                onClick = {
                                    val shareText = """
                                        *मेरी मजदूरी - दिनों का हिसाब*
                                        ---------------------------
                                        दैनिक मजदूरी: ${result.formattedDailyWage}
                                        पूरे दिन: ${result.fullDays.toInt()} दिन
                                        आधे दिन: ${result.halfDays.toInt()} दिन
                                        कुल गिने गए दिन: ${result.totalEffectiveDays} दिन
                                        कुल मजदूरी: ${result.formattedGrossWages}
                                        काटा गया एडवांस: -${result.formattedAdvance}
                                        ---------------------------
                                        *बाकी मिलने वाली मजदूरी: ${result.formattedNetPayable}*
                                    """.trimIndent()

                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "हिसाब शेयर करें"))
                                },
                                modifier = Modifier.testTag("btn_days_share")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "हिसाब शेयर करें",
                                    tint = BrandBlue
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFFF1F5F9)
                        )

                        // Row: Total Days
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("कुल गिने गए दिन:", color = Color(0xFF64748B), fontSize = 15.sp)
                            Text(
                                text = "${result.totalEffectiveDays} दिन",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Row: Gross Wages
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("कुल मजदूरी (Gross):", color = Color(0xFF64748B), fontSize = 15.sp)
                            Text(
                                text = result.formattedGrossWages,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Row: Advance Deducted
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("काटा गया एडवांस:", color = Color(0xFF64748B), fontSize = 15.sp)
                            Text(
                                text = "-${result.formattedAdvance}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFFDC2626)
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color(0xFFCBD5E1)
                        )

                        // Highlight Row: Net Payable
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "बाकी मिलने वाली मजदूरी:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = if (result.netPayable >= 0) "(मालिक द्वारा देय)" else "(आपने ज्यादा लिया है)",
                                    fontSize = 12.sp,
                                    color = if (result.netPayable >= 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                                )
                            }
                            Text(
                                text = result.formattedNetPayable,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = if (result.netPayable >= 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AdMob Banner Ad
            AdMobBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("days_banner_ad")
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
