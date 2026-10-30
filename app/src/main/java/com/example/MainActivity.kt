package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppScreen
import com.example.ui.screens.AdvanceRecordsScreen
import com.example.ui.screens.AttendanceCalendarScreen
import com.example.ui.screens.DaysCalculationScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MainInputScreen
import com.example.ui.screens.MonthCalculationScreen
import com.example.ui.theme.MeriMazdooriTheme
import com.example.viewmodel.WageCalculatorViewModel
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Google Mobile Ads (AdMob) SDK with test configuration
        try {
            MobileAds.initialize(this) {}
        } catch (e: Exception) {
            // Safe fallback if Play Services is absent in test environment
        }

        setContent {
            MeriMazdooriTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) {
                    val app = application
                    val viewModel: WageCalculatorViewModel = viewModel(
                        factory = WageCalculatorViewModel.Factory(app)
                    )
                    MeriMazdooriApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MeriMazdooriApp(
    viewModel: WageCalculatorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val historyRecords by viewModel.historyRecords.collectAsStateWithLifecycle()
    val allAdvances by viewModel.allAdvances.collectAsStateWithLifecycle()

    when (uiState.currentScreen) {
        AppScreen.MAIN_INPUT -> {
            MainInputScreen(
                historyCount = historyRecords.size,
                onOpenDaysCalculation = viewModel::openDaysCalculation,
                onOpenAdvanceRecords = viewModel::openAdvanceRecords,
                onOpenMonthCalculation = viewModel::openMonthCalculation,
                onOpenHistory = viewModel::openHistoryScreen
            )
        }
        AppScreen.MONTH_CALCULATION -> {
            MonthCalculationScreen(
                uiState = uiState,
                onWageChange = viewModel::onWageInputChange,
                onMonthSelected = viewModel::onMonthSelected,
                onYearSelected = viewModel::onYearSelected,
                onProceed = {
                    viewModel.proceedToCalendar()
                },
                onBack = viewModel::openHomeScreen
            )
        }
        AppScreen.DAYS_CALCULATION -> {
            DaysCalculationScreen(
                uiState = uiState,
                onDailyWageChange = viewModel::onDaysDailyWageChange,
                onFullDaysChange = viewModel::onDaysFullDaysChange,
                onHalfDaysChange = viewModel::onDaysHalfDaysChange,
                onAdvanceChange = viewModel::onDaysAdvanceChange,
                onCalculate = viewModel::calculateDaysWage,
                onReset = viewModel::resetDaysCalculation,
                onBack = viewModel::openHomeScreen
            )
        }
        AppScreen.ADVANCE_RECORDS -> {
            AdvanceRecordsScreen(
                uiState = uiState,
                advanceRecords = allAdvances,
                onSaveAdvance = viewModel::saveAdvanceRecord,
                onDeleteAdvance = viewModel::deleteAdvanceRecord,
                onBack = viewModel::openHomeScreen
            )
        }
        AppScreen.ATTENDANCE_CALENDAR -> {
            AttendanceCalendarScreen(
                uiState = uiState,
                onDateToggled = viewModel::toggleAttendance,
                onAdvanceChange = viewModel::onAdvanceInputChange,
                onSetAllPresent = viewModel::setAllPresent,
                onSetAllAbsent = viewModel::setAllAbsent,
                onCalculateMonthlyWage = viewModel::calculateMonthlyWage,
                onSaveCalculation = viewModel::onSaveCalculationClick,
                onConfirmUpdateExisting = viewModel::confirmUpdateExistingRecord,
                onDismissDuplicatePrompt = viewModel::dismissDuplicatePrompt,
                onStartNewCalculation = viewModel::startNewCalculation,
                onBack = viewModel::openMonthCalculation
            )
        }
        AppScreen.HISTORY -> {
            HistoryScreen(
                records = historyRecords,
                selectedRecord = uiState.selectedHistoryRecord,
                showDeleteConfirmDialog = uiState.showDeleteConfirmDialog,
                showDeleteAllConfirmDialog = uiState.showDeleteAllConfirmDialog,
                onOpenRecordCalendar = viewModel::openRecordFromHistory,
                onOpenRecordDetail = viewModel::openRecordDetail,
                onCloseRecordDetail = viewModel::closeRecordDetail,
                onRequestDeleteRecord = viewModel::requestDeleteRecord,
                onConfirmDeleteRecord = viewModel::confirmDeleteRecord,
                onDismissDeleteRecord = viewModel::dismissDeleteConfirm,
                onRequestDeleteAll = viewModel::requestDeleteAllRecords,
                onConfirmDeleteAll = viewModel::confirmDeleteAllRecords,
                onDismissDeleteAll = viewModel::dismissDeleteAllConfirm,
                onBack = viewModel::openHomeScreen
            )
        }
    }
}
