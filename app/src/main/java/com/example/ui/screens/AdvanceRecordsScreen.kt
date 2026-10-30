package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AdvanceRecord
import com.example.ui.components.AdMobBanner
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueDark
import com.example.util.CurrencyUtils
import com.example.viewmodel.WageUiState

@Composable
fun AdvanceRecordsScreen(
    uiState: WageUiState,
    advanceRecords: List<AdvanceRecord>,
    onSaveAdvance: (contractor: String, amount: String, date: String, notes: String) -> Unit,
    onDeleteAdvance: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val keyboardController = LocalSoftwareKeyboardController.current

    var contractorInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var dateInput by remember {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        mutableStateOf(sdf.format(java.util.Date()))
    }
    var notesInput by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }
    var deleteCandidate by remember { mutableStateOf<AdvanceRecord?>(null) }

    val totalAdvance = remember(advanceRecords) {
        advanceRecords.sumOf { it.amount }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("advance_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "मुख्य मेनू पर वापस",
                            tint = BrandBlue
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "एडवांस पैसा लिया",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Total Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "कुल एडवांस लिया गया",
                                fontSize = 14.sp,
                                color = Color(0xFF92400E),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = CurrencyUtils.formatRupees(totalAdvance),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFB45309)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Add Advance Form Card
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
                            text = "नया एडवांस दर्ज करें",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Contractor Name
                        OutlinedTextField(
                            value = contractorInput,
                            onValueChange = { contractorInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("advance_contractor_input"),
                            label = { Text("ठेकेदार / मालिक का नाम") },
                            placeholder = { Text("उदा. रमेश ठेकेदार / साइट A") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = BrandBlue)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Advance Amount
                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = {
                                val norm = CurrencyUtils.normalizeDigits(it)
                                val sanitized = norm.filter { ch -> ch.isDigit() || ch == '.' }
                                if (sanitized.count { ch -> ch == '.' } <= 1) {
                                    amountInput = sanitized
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("advance_amount_input"),
                            label = { Text("एडवांस राशि (₹) *") },
                            placeholder = { Text("उदा. 2000") },
                            leadingIcon = {
                                Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = Color(0xFFEF4444))
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Date
                        OutlinedTextField(
                            value = dateInput,
                            onValueChange = { dateInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("advance_date_input"),
                            label = { Text("तारीख") },
                            placeholder = { Text("उदा. 29/10/2026") },
                            leadingIcon = {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = BrandBlue)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Notes
                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("advance_notes_input"),
                            label = { Text("नोट / विवरण (वैकल्पिक)") },
                            placeholder = { Text("उदा. दिवाली खर्चा / घर का काम") },
                            leadingIcon = {
                                Icon(Icons.Default.Notes, contentDescription = null, tint = Color(0xFF64748B))
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (formError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = formError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (amountInput.isBlank() || (amountInput.toDoubleOrNull() ?: 0.0) <= 0.0) {
                                    formError = "कृपया सही एडवांस राशि दर्ज करें"
                                } else {
                                    formError = null
                                    keyboardController?.hide()
                                    onSaveAdvance(
                                        if (contractorInput.isBlank()) "मालिक / ठेकेदार" else contractorInput,
                                        amountInput,
                                        dateInput,
                                        notesInput
                                    )
                                    // Reset inputs
                                    amountInput = ""
                                    notesInput = ""
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_save_advance"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandBlue,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("एडवांस जोड़ें / सेव करें", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Heading for Records List
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "लिए गए एडवांस की सूची (${advanceRecords.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            if (advanceRecords.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "अभी तक कोई एडवांस रिकॉर्ड दर्ज नहीं किया गया है। ऊपर दिए गए फॉर्म से नया एडवांस जोड़ें।",
                            modifier = Modifier.padding(20.dp),
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(advanceRecords, key = { it.id }) { record ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("advance_item_${record.id}"),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = record.contractorName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "तारीख: ${record.dateText}",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                )
                                if (record.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "विवरण: ${record.notes}",
                                        fontSize = 13.sp,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "-${CurrencyUtils.formatRupees(record.amount)}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { deleteCandidate = record }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "हटाएं",
                                        tint = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                AdMobBanner(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("advance_banner_ad")
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Delete Confirmation Dialog
    if (deleteCandidate != null) {
        val record = deleteCandidate!!
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("एडवांस रिकॉर्ड हटाएं?") },
            text = {
                Text("${record.contractorName} से लिया गया ${CurrencyUtils.formatRupees(record.amount)} का एडवांस हटाना चाहते हैं?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAdvance(record.id)
                        deleteCandidate = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("हाँ, हटाएं")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) {
                    Text("रद्द करें")
                }
            }
        )
    }
}
