package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.AbsentRedBg
import com.example.ui.theme.AbsentRedText
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.HalfDayAmber
import com.example.ui.theme.HalfDayAmberBg
import com.example.ui.theme.HalfDayAmberText
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenBg
import com.example.ui.theme.PresentGreenText
import com.example.util.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    records: List<WageRecord>,
    selectedRecord: WageRecord?,
    showDeleteConfirmDialog: WageRecord?,
    showDeleteAllConfirmDialog: Boolean,
    onOpenRecordCalendar: (WageRecord) -> Unit,
    onOpenRecordDetail: (WageRecord) -> Unit,
    onCloseRecordDetail: () -> Unit,
    onRequestDeleteRecord: (WageRecord) -> Unit,
    onConfirmDeleteRecord: () -> Unit,
    onDismissDeleteRecord: () -> Unit,
    onRequestDeleteAll: () -> Unit,
    onConfirmDeleteAll: () -> Unit,
    onDismissDeleteAll: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "हिसाब हिस्ट्री (${records.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("history_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "वापस जाएं",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (records.isNotEmpty()) {
                        IconButton(
                            onClick = onRequestDeleteAll,
                            modifier = Modifier.testTag("delete_all_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "सभी हटाएं",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
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
            if (records.isEmpty()) {
                EmptyHistoryState(
                    onBack = onBack,
                    modifier = Modifier.widthIn(max = 600.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp)
                        .testTag("history_records_list"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(
                        items = records,
                        key = { it.id }
                    ) { record ->
                        HistoryRecordCard(
                            record = record,
                            onClick = { onOpenRecordCalendar(record) },
                            onShare = {
                                ShareUtils.shareRecordAsImage(context, record)
                            },
                            onDelete = { onRequestDeleteRecord(record) }
                        )
                    }
                }
            }
        }
    }

    // Detail Dialog
    if (selectedRecord != null) {
        RecordDetailDialog(
            record = selectedRecord,
            onOpenCalendar = {
                onCloseRecordDetail()
                onOpenRecordCalendar(selectedRecord)
            },
            onShare = {
                ShareUtils.shareRecordAsImage(context, selectedRecord)
            },
            onDismiss = onCloseRecordDetail
        )
    }

    // Single Record Delete Confirmation Dialog
    if (showDeleteConfirmDialog != null) {
        AlertDialog(
            onDismissRequest = onDismissDeleteRecord,
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = AbsentRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "हिसाब हटाएं?",
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueDark
                )
            },
            text = {
                Text(
                    text = "क्या आप ${showDeleteConfirmDialog.monthHindiName} ${showDeleteConfirmDialog.year} का हिसाब (कुल: ${showDeleteConfirmDialog.formattedRemainingPayable}) हटाना चाहते हैं?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF334155)
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmDeleteRecord,
                    colors = ButtonDefaults.buttonColors(containerColor = AbsentRed)
                ) {
                    Text("हटाएं", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteRecord) {
                    Text("रद्द करें", fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    // Delete All Confirmation Dialog
    if (showDeleteAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = onDismissDeleteAll,
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    tint = AbsentRed,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "सभी हिसाब हटाएं?",
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueDark
                )
            },
            text = {
                Text(
                    text = "क्या आप सभी ${records.size} सेव किए गए हिसाब हटाना चाहते हैं? यह क्रिया वापस नहीं ली जा सकती।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF334155)
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmDeleteAll,
                    colors = ButtonDefaults.buttonColors(containerColor = AbsentRed)
                ) {
                    Text("सभी हटाएं", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteAll) {
                    Text("रद्द करें", fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}

@Composable
private fun HistoryRecordCard(
    record: WageRecord,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("history_item_${record.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Month/Year and Delete & Share Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BrandBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = BrandBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${record.monthHindiName} ${record.year}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = BrandBlueDark
                        )
                        if (record.savedDateTimeText.isNotEmpty()) {
                            Text(
                                text = "सेव किया: ${record.savedDateTimeText}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // WhatsApp Share quick button
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "WhatsApp पर भेजें",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Delete button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "रिकॉर्ड हटाएं",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Wage and Attendance Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "दैनिक मजदूरी",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                    Text(
                        text = record.formattedDailyWage,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "उपस्थित",
                        style = MaterialTheme.typography.bodySmall,
                        color = PresentGreenText,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${record.presentDays} दिन",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = PresentGreenText
                    )
                }

                if (record.halfDays > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "आधी मजदूरी",
                            style = MaterialTheme.typography.bodySmall,
                            color = HalfDayAmberText,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${record.halfDays} दिन",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = HalfDayAmberText
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "अनुपस्थित",
                        style = MaterialTheme.typography.bodySmall,
                        color = AbsentRedText,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${record.absentDays} दिन",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AbsentRedText
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Total / Remaining Payable Highlight
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (record.advanceAmount > 0) "बाकी लेने योग्य राशि" else "कुल मजदूरी",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                    if (record.advanceAmount > 0) {
                        Text(
                            text = "कुल: ${record.formattedGrossWages} − एडवांस: ${record.formattedAdvance}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Text(
                    text = record.formattedRemainingPayable,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    ),
                    color = BrandBlue
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Tap hint to open in calendar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.EditCalendar,
                    contentDescription = null,
                    tint = BrandBlue,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "कैलेंडर में देखने/बदलने के लिए टच करें",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = BrandBlue,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun EmptyHistoryState(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(BrandBlue.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = BrandBlue,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "कोई सेव किया गया हिसाब नहीं है",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = BrandBlueDark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "कैलेंडर में हिसाब निकालने के बाद 'हिसाब सेव करें' बटन दबाकर यहाँ सुरक्षित रख सकते हैं।",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBack,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
        ) {
            Text("हिसाब निकालें", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RecordDetailDialog(
    record: WageRecord,
    onOpenCalendar: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = BrandBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${record.monthHindiName} ${record.year}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlueDark
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (record.savedDateTimeText.isNotEmpty()) {
                    Text(
                        text = "सेव किया गया: ${record.savedDateTimeText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Total Wage Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BrandBlue)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (record.advanceAmount > 0) "बाकी लेने योग्य राशि" else "कुल मजदूरी",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = record.formattedRemainingPayable,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp
                            ),
                            color = Color.White
                        )
                        if (record.advanceAmount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "कुल: ${record.formattedGrossWages} − एडवांस: ${record.formattedAdvance}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                DetailItemRow("कुल कैलेंडर दिन", "${record.totalCalendarDays} दिन")
                DetailItemRow("उपस्थित दिन", "${record.presentDays} दिन", PresentGreen)
                if (record.halfDays > 0) {
                    DetailItemRow("आधी मजदूरी वाले दिन", "${record.halfDays} दिन", HalfDayAmber)
                }
                DetailItemRow("अनुपस्थित दिन", "${record.absentDays} दिन", AbsentRed)
                DetailItemRow("दैनिक मजदूरी", record.formattedDailyWage)
                DetailItemRow("कुल मजदूरी (Gross)", record.formattedGrossWages)
                if (record.advanceAmount > 0) {
                    DetailItemRow("लिया गया एडवांस", "− ${record.formattedAdvance}", BrandAmber)
                    DetailItemRow("बाकी लेने योग्य राशि", record.formattedRemainingPayable, PresentGreen)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // WhatsApp share button inside dialog
                Button(
                    onClick = onShare,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp पर शेयर करें", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onOpenCalendar,
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Text("कैलेंडर में खोलें", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("बंद करें", fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@Composable
private fun DetailItemRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF1E293B)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
