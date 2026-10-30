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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HINDI_MONTHS
import com.example.model.MonthItem
import com.example.ui.components.AdMobBanner
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueDark
import com.example.viewmodel.WageUiState

@Composable
fun MonthCalculationScreen(
    uiState: WageUiState,
    onWageChange: (String) -> Unit,
    onMonthSelected: (Int) -> Unit,
    onYearSelected: (Int) -> Unit,
    onProceed: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current

    var showMonthDialog by remember { mutableStateOf(false) }
    var showYearDialog by remember { mutableStateOf(false) }

    val selectedMonthItem = remember(uiState.selectedMonth) {
        HINDI_MONTHS.firstOrNull { it.monthNumber == uiState.selectedMonth }
            ?: HINDI_MONTHS[0]
    }

    val availableYears = remember {
        (2020..2030).toList()
    }

    val quickAmounts = listOf("300", "400", "500", "600", "800")

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
                    modifier = Modifier.testTag("month_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "मुख्य मेनू पर वापस",
                        tint = BrandBlue
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "महीने का हिसाब",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle info
            Text(
                text = "दैनिक मजदूरी, महीना और साल चुनें, फिर कैलेंडर से पूरे महीने की हाजिरी निकालें।",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Daily Wage Input Card
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
                    Text(
                        text = "दैनिक मजदूरी (₹)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "एक दिन का तय किया गया वेतन दर्ज करें",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = uiState.dailyWageInput,
                        onValueChange = onWageChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("daily_wage_input"),
                        placeholder = {
                            Text("उदा. 500", color = Color(0xFF94A3B8))
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CurrencyRupee,
                                contentDescription = null,
                                tint = if (uiState.wageInputError != null) MaterialTheme.colorScheme.error else BrandBlue
                            )
                        },
                        isError = uiState.wageInputError != null,
                        supportingText = {
                            if (uiState.wageInputError != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = uiState.wageInputError,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                            }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Selection Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickAmounts.forEach { amount ->
                            val isSelected = uiState.dailyWageInput == amount
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    onWageChange(amount)
                                },
                                label = {
                                    Text(
                                        text = "₹$amount",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Month and Year Selection Card
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
                    Text(
                        text = "समय अवधि चुनें",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Month Selector
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "महीना",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                                .clickable { showMonthDialog = true }
                                .padding(horizontal = 14.dp)
                                .testTag("month_selector_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = BrandBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "${selectedMonthItem.hindiName} (${selectedMonthItem.englishName})",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF0F172A)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "महीना चुनें",
                                tint = Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Year Selector
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "साल",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                                .clickable { showYearDialog = true }
                                .padding(horizontal = 14.dp)
                                .testTag("year_selector_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = BrandBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "${uiState.selectedYear}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF0F172A)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "साल चुनें",
                                tint = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Proceed Button
            Button(
                onClick = {
                    keyboardController?.hide()
                    onProceed()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("btn_proceed_to_calendar"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandBlue,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "हाजिरी कैलेंडर खोलें और हिसाब करें",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AdMob Banner Ad
            AdMobBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("month_banner_ad")
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Month Selection Dialog
    if (showMonthDialog) {
        AlertDialog(
            onDismissRequest = { showMonthDialog = false },
            title = {
                Text(
                    text = "महीना चुनें",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    HINDI_MONTHS.forEach { monthItem ->
                        val isSelected = monthItem.monthNumber == uiState.selectedMonth
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onMonthSelected(monthItem.monthNumber)
                                    showMonthDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${monthItem.hindiName} (${monthItem.englishName})",
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) BrandBlue else Color(0xFF1E293B)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMonthDialog = false }) {
                    Text("रद्द करें")
                }
            }
        )
    }

    // Year Selection Dialog
    if (showYearDialog) {
        AlertDialog(
            onDismissRequest = { showYearDialog = false },
            title = {
                Text(
                    text = "साल चुनें",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    availableYears.forEach { yr ->
                        val isSelected = yr == uiState.selectedYear
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onYearSelected(yr)
                                    showYearDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$yr",
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) BrandBlue else Color(0xFF1E293B)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showYearDialog = false }) {
                    Text("रद्द करें")
                }
            }
        )
    }
}
