package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.RemoveDone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.example.ui.components.AdMobBanner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AttendanceStatus
import com.example.model.HINDI_WEEKDAYS
import com.example.ui.components.ResultsSection
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.AbsentRedBg
import com.example.ui.theme.AbsentRedBorder
import com.example.ui.theme.AbsentRedText
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.HalfDayAmber
import com.example.ui.theme.HalfDayAmberBg
import com.example.ui.theme.HalfDayAmberBorder
import com.example.ui.theme.HalfDayAmberText
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenBg
import com.example.ui.theme.PresentGreenBorder
import com.example.ui.theme.PresentGreenText
import com.example.util.CalendarUtils
import com.example.viewmodel.WageUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceCalendarScreen(
    uiState: WageUiState,
    onDateToggled: (Int) -> Unit,
    onAdvanceChange: (String) -> Unit,
    onSetAllPresent: () -> Unit,
    onSetAllAbsent: () -> Unit,
    onCalculateMonthlyWage: () -> Unit,
    onSaveCalculation: () -> Unit,
    onConfirmUpdateExisting: () -> Unit,
    onDismissDuplicatePrompt: () -> Unit,
    onStartNewCalculation: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    val monthName = CalendarUtils.getHindiMonthName(uiState.selectedMonth)
    val headerTitle = "$monthName ${uiState.selectedYear} का हाजिरी कैलेंडर"
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = headerTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        maxLines = 1,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "वापस जाएं",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                AdMobBanner(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("calendar_banner_ad")
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC)),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Attendance Counters (कुल दिन, उपस्थित, आधी मजदूरी, अनुपस्थित)
                AttendanceCountersRow(
                    totalDays = uiState.totalCalendarDays,
                    presentDays = uiState.presentDaysCount,
                    halfDays = uiState.halfDaysCount,
                    absentDays = uiState.absentDaysCount,
                    paidWageDays = uiState.paidWageDays
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Attendance Actions (सभी उपस्थित / सभी अनुपस्थित)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onSetAllPresent,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PresentGreenText)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = null,
                            tint = PresentGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "सभी उपस्थित",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onSetAllAbsent,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AbsentRedText)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RemoveDone,
                            contentDescription = null,
                            tint = AbsentRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "सभी अनुपस्थित",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Attendance Status Guide / Legend with 3 states
                AttendanceLegendCard()

                Spacer(modifier = Modifier.height(12.dp))

                // Calendar Grid Card
                CalendarGridCard(
                    totalDays = uiState.totalCalendarDays,
                    firstDayOffset = uiState.firstDayOffset,
                    attendanceMap = uiState.attendanceMap,
                    onDateToggled = onDateToggled
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Advance Payment Deduction Input Card
                AdvanceDeductionCard(
                    advanceValue = uiState.advanceInput,
                    onAdvanceChange = onAdvanceChange,
                    onDone = {
                        keyboardController?.hide()
                        onCalculateMonthlyWage()
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Calculate Wage Button ("महीने का हिसाब निकालें")
                Button(
                    onClick = {
                        keyboardController?.hide()
                        onCalculateMonthlyWage()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("calculate_wage_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "महीने का हिसाब निकालें",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Results Section (Visible after calculation, with WhatsApp share & DB save)
                ResultsSection(
                    result = uiState.calculationResult,
                    isStale = uiState.isResultStale,
                    saveConfirmationMessage = uiState.saveConfirmationMessage,
                    duplicatePrompt = uiState.duplicateRecordPrompt,
                    onSaveCalculation = onSaveCalculation,
                    onConfirmUpdateExisting = onConfirmUpdateExisting,
                    onDismissDuplicatePrompt = onDismissDuplicatePrompt,
                    onCalculateAgain = onCalculateMonthlyWage,
                    onStartNewCalculation = onStartNewCalculation
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun AttendanceCountersRow(
    totalDays: Int,
    presentDays: Int,
    halfDays: Int,
    absentDays: Int,
    paidWageDays: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Total Days Counter
                CounterItem(
                    label = "कुल दिन",
                    count = totalDays.toString(),
                    countColor = BrandBlueDark,
                    containerColor = Color(0xFFF1F5F9),
                    testTag = "counter_total_days",
                    modifier = Modifier.weight(1f)
                )

                // Present Days Counter
                CounterItem(
                    label = "उपस्थित",
                    count = presentDays.toString(),
                    countColor = PresentGreenText,
                    containerColor = PresentGreenBg,
                    testTag = "counter_present_days",
                    modifier = Modifier.weight(1f)
                )

                // Half Days Counter
                CounterItem(
                    label = "आधी मजदूरी",
                    count = halfDays.toString(),
                    countColor = HalfDayAmberText,
                    containerColor = HalfDayAmberBg,
                    testTag = "counter_half_days",
                    modifier = Modifier.weight(1f)
                )

                // Absent Days Counter
                CounterItem(
                    label = "अनुपस्थित",
                    count = absentDays.toString(),
                    countColor = AbsentRedText,
                    containerColor = AbsentRedBg,
                    testTag = "counter_absent_days",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Paid Days summary line
            val paidDaysStr = if (paidWageDays % 1.0 == 0.0) paidWageDays.toInt().toString() else paidWageDays.toString()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(vertical = 5.dp, horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "कुल मजदूरी के देय दिन = $paidDaysStr दिन (उपस्थित + 0.5 × आधा दिन)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = BrandBlueDark
                )
            }
        }
    }
}

@Composable
private fun CounterItem(
    label: String,
    count: String,
    countColor: Color,
    containerColor: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = countColor
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF475569),
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AttendanceLegendCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Present Indicator Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(PresentGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(9.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "उपस्थित (1 दिन)",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = PresentGreenText
                    )
                }

                // Absent Indicator Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(AbsentRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(9.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "अनुपस्थित (0)",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = AbsentRedText
                    )
                }

                // Half-Day Indicator Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(HalfDayAmber),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(9.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "आधी मजदूरी (0.5)",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = HalfDayAmberText
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "टच करें: उपस्थित ➔ अनुपस्थित ➔ आधी मजदूरी ➔ उपस्थित",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CalendarGridCard(
    totalDays: Int,
    firstDayOffset: Int,
    attendanceMap: Map<Int, AttendanceStatus>,
    onDateToggled: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calendar_grid"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Weekday Headers (रवि, सोम, मंगल, बुध, गुरु, शुक्र, शनि)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HINDI_WEEKDAYS.forEachIndexed { index, dayName ->
                    val isSunday = (index == 0)
                    Text(
                        text = dayName,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = if (isSunday) AbsentRed else BrandBlueDark
                    )
                }
            }

            // Total slots in grid: leading blank days + total calendar days
            val totalSlots = firstDayOffset + totalDays
            val rows = (totalSlots + 6) / 7

            for (rowIndex in 0 until rows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (colIndex in 0 until 7) {
                        val slotIndex = rowIndex * 7 + colIndex
                        val dayNumber = slotIndex - firstDayOffset + 1

                        if (dayNumber in 1..totalDays) {
                            val status = attendanceMap[dayNumber] ?: AttendanceStatus.PRESENT
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 2.dp)
                            ) {
                                CalendarDayItem(
                                    day = dayNumber,
                                    status = status,
                                    onToggle = { onDateToggled(dayNumber) }
                                )
                            }
                        } else {
                            // Blank cell for alignment
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayItem(
    day: Int,
    status: AttendanceStatus,
    onToggle: () -> Unit
) {
    val bgColor = when (status) {
        AttendanceStatus.PRESENT -> PresentGreenBg
        AttendanceStatus.HALF_DAY -> HalfDayAmberBg
        AttendanceStatus.ABSENT -> AbsentRedBg
    }
    val borderColor = when (status) {
        AttendanceStatus.PRESENT -> PresentGreenBorder
        AttendanceStatus.HALF_DAY -> HalfDayAmberBorder
        AttendanceStatus.ABSENT -> AbsentRedBorder
    }
    val textColor = when (status) {
        AttendanceStatus.PRESENT -> PresentGreenText
        AttendanceStatus.HALF_DAY -> HalfDayAmberText
        AttendanceStatus.ABSENT -> AbsentRedText
    }
    val badgeIcon = when (status) {
        AttendanceStatus.PRESENT -> Icons.Default.Check
        AttendanceStatus.HALF_DAY -> Icons.Default.Schedule
        AttendanceStatus.ABSENT -> Icons.Default.Close
    }
    val badgeBg = when (status) {
        AttendanceStatus.PRESENT -> PresentGreen
        AttendanceStatus.HALF_DAY -> HalfDayAmber
        AttendanceStatus.ABSENT -> AbsentRed
    }
    val statusText = when (status) {
        AttendanceStatus.PRESENT -> "उप."
        AttendanceStatus.HALF_DAY -> "आधा"
        AttendanceStatus.ABSENT -> "अनु."
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(8.dp))
            .clickable(onClick = onToggle)
            .testTag("day_cell_$day"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Day number
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                ),
                color = textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Attendance badge with icon & text (accessible across 3 states)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = status.hindiLabel,
                        tint = Color.White,
                        modifier = Modifier.size(7.dp)
                    )
                }
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = statusText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun AdvanceDeductionCard(
    advanceValue: String,
    onAdvanceChange: (String) -> Unit,
    onDone: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    tint = BrandAmber,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "लिया गया एडवांस (₹)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = BrandBlueDark
                    )
                    Text(
                        text = "यदि मालिक या ठेकेदार से कोई एडवांस लिया है, तो यहाँ भरें",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = advanceValue,
                onValueChange = onAdvanceChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("advance_input"),
                placeholder = {
                    Text("जैसे: 1000 (यदि कोई हो, अन्यथा 0)", color = Color(0xFF94A3B8), fontSize = 14.sp)
                },
                leadingIcon = {
                    Text(
                        text = "₹",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandAmber
                    )
                },
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onDone() }
                ),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandAmber,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            // Quick advance chips
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("0", "500", "1000", "2000").forEach { quickAmount ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (advanceValue == quickAmount) BrandAmber else Color(0xFFF1F5F9))
                            .clickable { onAdvanceChange(quickAmount) }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "₹$quickAmount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (advanceValue == quickAmount) Color.White else Color(0xFF475569)
                        )
                    }
                }
            }
        }
    }
}
