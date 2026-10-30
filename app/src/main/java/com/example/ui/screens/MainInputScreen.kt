package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AdMobBanner
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueDark

/**
 * Main Home Screen of "मेरी मजदूरी" App.
 * Displays the 3 primary options cleanly:
 * 1. दिनों का हिसाब
 * 2. एडवांस पैसा लिया
 * 3. महीने का हिसाब
 * Plus the preserved History button at top-right.
 */
@Composable
fun MainInputScreen(
    historyCount: Int,
    onOpenDaysCalculation: () -> Unit,
    onOpenAdvanceRecords: () -> Unit,
    onOpenMonthCalculation: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

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
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top action bar with existing History button preserved at original top-right location
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onOpenHistory,
                    modifier = Modifier.testTag("history_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = BrandBlue
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "हिस्ट्री खोलें",
                        tint = BrandBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (historyCount > 0) "हिस्ट्री ($historyCount)" else "हिस्ट्री",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // App Header
            HomeHeader()

            Spacer(modifier = Modifier.height(26.dp))

            // Section Label
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "मुख्य विकल्प चुनें:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Option 1: दिनों का हिसाब
            HomeFeatureCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_days_calculation"),
                badgeNumber = "1",
                title = "दिनों का हिसाब",
                subtitle = "पूरे दिन, आधे दिन/OT और एडवांस से तुरंत हिसाब करें (बिना कैलेंडर)",
                icon = Icons.Default.Calculate,
                iconBg = Color(0xFFEFF6FF),
                iconTint = BrandBlue,
                borderColor = Color(0xFFBFDBFE),
                onClick = onOpenDaysCalculation
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Option 2: एडवांस पैसा लिया
            HomeFeatureCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_advance_records"),
                badgeNumber = "2",
                title = "एडवांस पैसा लिया",
                subtitle = "ठेकेदारों या मालिकों से लिए एडवांस पैसे का निजी रिकॉर्ड रखें",
                icon = Icons.Default.AccountBalanceWallet,
                iconBg = Color(0xFFFEF3C7),
                iconTint = Color(0xFFD97706),
                borderColor = Color(0xFFFDE68A),
                onClick = onOpenAdvanceRecords
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Option 3: महीने का हिसाब
            HomeFeatureCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_month_calculation"),
                badgeNumber = "3",
                title = "महीने का हिसाब",
                subtitle = "महीने और साल का चयन करके पूरे महीने की हाजिरी कैलेंडर से निकालें",
                icon = Icons.Default.CalendarMonth,
                iconBg = Color(0xFFF0FDF4),
                iconTint = Color(0xFF16A34A),
                borderColor = Color(0xFFBBF7D0),
                onClick = onOpenMonthCalculation
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Helpful Guide Note for Workers
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "ऊपर दिए गए किसी भी विकल्प पर टच करके अपना हिसाब शुरू करें। आपका पुराना डेटा सुरक्षित रहता है।",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1E3A8A),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AdMob Banner Ad
            AdMobBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("main_banner_ad")
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HomeHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(BrandBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Payments,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "मेरी मजदूरी",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "मजदूरों का सबसे सरल और भरोसेमंद डिजिटल हिसाब",
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HomeFeatureCard(
    badgeNumber: String,
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with badge
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Title & Subtitle
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(iconTint),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeNumber,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
