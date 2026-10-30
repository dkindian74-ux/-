package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AdvanceRecord
import com.example.data.local.WageDatabase
import com.example.data.local.WageRecord
import com.example.data.repository.WageRepository
import com.example.model.AppScreen
import com.example.model.AttendanceStatus
import com.example.model.CalculationResult
import com.example.model.DaysCalculationResult
import com.example.util.AttendanceSerializer
import com.example.util.CalendarUtils
import com.example.util.CurrencyUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class WageUiState(
    val dailyWageInput: String = "",
    val advanceInput: String = "0",
    val selectedMonth: Int = CalendarUtils.getPreviousMonthAndYear().first, // Dynamic previous month (1..12)
    val selectedYear: Int = CalendarUtils.getPreviousMonthAndYear().second, // Dynamic previous year
    val wageInputError: String? = null,
    val advanceInputError: String? = null,
    val currentScreen: AppScreen = AppScreen.MAIN_INPUT,
    val attendanceMap: Map<Int, AttendanceStatus> = emptyMap(), // dayOfMonth -> AttendanceStatus
    val totalCalendarDays: Int = 31,
    val firstDayOffset: Int = 4, // 0-based offset for 1st of month
    val calculationResult: CalculationResult? = null,
    val isResultStale: Boolean = false,
    val saveConfirmationMessage: String? = null,
    val duplicateRecordPrompt: WageRecord? = null,
    val selectedHistoryRecord: WageRecord? = null,
    val showDeleteConfirmDialog: WageRecord? = null,
    val showDeleteAllConfirmDialog: Boolean = false,
    // Days Calculation state
    val daysDailyWageInput: String = "",
    val daysFullDaysInput: String = "",
    val daysHalfDaysInput: String = "",
    val daysAdvanceInput: String = "",
    val daysCalculationResult: DaysCalculationResult? = null
) {
    val presentDaysCount: Int
        get() = attendanceMap.values.count { it == AttendanceStatus.PRESENT }

    val halfDaysCount: Int
        get() = attendanceMap.values.count { it == AttendanceStatus.HALF_DAY }

    val absentDaysCount: Int
        get() = attendanceMap.values.count { it == AttendanceStatus.ABSENT }

    val paidWageDays: Double
        get() = presentDaysCount + (halfDaysCount * 0.5)
}

class WageCalculatorViewModel @JvmOverloads constructor(
    application: Application,
    customRepository: WageRepository? = null
) : AndroidViewModel(application) {

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WageCalculatorViewModel::class.java)) {
                return WageCalculatorViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    private val db = WageDatabase.getDatabase(application)
    private val repository: WageRepository = customRepository ?: WageRepository(
        db.wageRecordDao(),
        db.advanceRecordDao()
    )

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<WageUiState> = _uiState.asStateFlow()

    val historyRecords: StateFlow<List<WageRecord>> = repository.allRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allAdvances: StateFlow<List<AdvanceRecord>> = (repository.allAdvances ?: emptyFlow())
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun createInitialState(): WageUiState {
        val (prevMonth, prevYear) = CalendarUtils.getPreviousMonthAndYear()
        return WageUiState(
            dailyWageInput = "",
            advanceInput = "0",
            selectedMonth = prevMonth,
            selectedYear = prevYear,
            wageInputError = null,
            advanceInputError = null,
            currentScreen = AppScreen.MAIN_INPUT,
            attendanceMap = emptyMap(),
            calculationResult = null,
            isResultStale = false,
            saveConfirmationMessage = null,
            duplicateRecordPrompt = null,
            selectedHistoryRecord = null,
            showDeleteConfirmDialog = null,
            showDeleteAllConfirmDialog = false
        )
    }

    // Navigation functions
    fun openHomeScreen() {
        _uiState.update { it.copy(currentScreen = AppScreen.MAIN_INPUT) }
    }

    fun openDaysCalculation() {
        _uiState.update { it.copy(currentScreen = AppScreen.DAYS_CALCULATION) }
    }

    fun openAdvanceRecords() {
        _uiState.update { it.copy(currentScreen = AppScreen.ADVANCE_RECORDS) }
    }

    fun openMonthCalculation() {
        _uiState.update { it.copy(currentScreen = AppScreen.MONTH_CALCULATION) }
    }

    fun openHistoryScreen() {
        _uiState.update { it.copy(currentScreen = AppScreen.HISTORY) }
    }

    // Days Calculation logic
    fun onDaysDailyWageChange(value: String) {
        val normalized = CurrencyUtils.normalizeDigits(value).filter { it.isDigit() || it == '.' }
        if (normalized.count { it == '.' } <= 1) {
            _uiState.update { it.copy(daysDailyWageInput = normalized) }
        }
    }

    fun onDaysFullDaysChange(value: String) {
        val normalized = CurrencyUtils.normalizeDigits(value).filter { it.isDigit() || it == '.' }
        if (normalized.count { it == '.' } <= 1) {
            _uiState.update { it.copy(daysFullDaysInput = normalized) }
        }
    }

    fun onDaysHalfDaysChange(value: String) {
        val normalized = CurrencyUtils.normalizeDigits(value).filter { it.isDigit() || it == '.' }
        if (normalized.count { it == '.' } <= 1) {
            _uiState.update { it.copy(daysHalfDaysInput = normalized) }
        }
    }

    fun onDaysAdvanceChange(value: String) {
        val normalized = CurrencyUtils.normalizeDigits(value).filter { it.isDigit() || it == '.' }
        if (normalized.count { it == '.' } <= 1) {
            _uiState.update { it.copy(daysAdvanceInput = normalized) }
        }
    }

    fun calculateDaysWage() {
        val state = _uiState.value
        val dailyWage = state.daysDailyWageInput.toDoubleOrNull() ?: 0.0
        val fullDays = state.daysFullDaysInput.toDoubleOrNull() ?: 0.0
        val halfDays = state.daysHalfDaysInput.toDoubleOrNull() ?: 0.0
        val advance = state.daysAdvanceInput.toDoubleOrNull() ?: 0.0

        val totalEffectiveDays = fullDays + (halfDays * 0.5)
        val grossWages = totalEffectiveDays * dailyWage
        val netPayable = grossWages - advance

        val result = DaysCalculationResult(
            dailyWage = dailyWage,
            fullDays = fullDays,
            halfDays = halfDays,
            totalEffectiveDays = totalEffectiveDays,
            grossWages = grossWages,
            advanceDeducted = advance,
            netPayable = netPayable,
            formattedDailyWage = CurrencyUtils.formatRupees(dailyWage),
            formattedGrossWages = CurrencyUtils.formatRupees(grossWages),
            formattedAdvance = CurrencyUtils.formatRupees(advance),
            formattedNetPayable = CurrencyUtils.formatRupees(netPayable)
        )

        _uiState.update { it.copy(daysCalculationResult = result) }
    }

    fun resetDaysCalculation() {
        _uiState.update {
            it.copy(
                daysDailyWageInput = "",
                daysFullDaysInput = "",
                daysHalfDaysInput = "",
                daysAdvanceInput = "",
                daysCalculationResult = null
            )
        }
    }

    // Advance record methods
    fun saveAdvanceRecord(contractor: String, amount: String, date: String, notes: String) {
        val parsedAmount = amount.toDoubleOrNull() ?: 0.0
        if (parsedAmount <= 0.0) return

        viewModelScope.launch {
            repository.saveAdvance(
                AdvanceRecord(
                    contractorName = contractor,
                    amount = parsedAmount,
                    dateText = date,
                    notes = notes
                )
            )
        }
    }

    fun deleteAdvanceRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteAdvance(id)
        }
    }

    // Month calculation logic
    fun onWageInputChange(newValue: String) {
        val normalized = CurrencyUtils.normalizeDigits(newValue)
        val sanitized = normalized.filter { it.isDigit() || it == '.' }
        if (sanitized.count { it == '.' } > 1) return

        _uiState.update {
            it.copy(
                dailyWageInput = sanitized,
                wageInputError = null
            )
        }
    }

    fun onAdvanceInputChange(newValue: String) {
        val normalized = CurrencyUtils.normalizeDigits(newValue)
        val sanitized = normalized.filter { it.isDigit() || it == '.' }
        if (sanitized.count { it == '.' } > 1) return

        _uiState.update {
            it.copy(
                advanceInput = sanitized,
                advanceInputError = null
            )
        }
        calculateMonthlyWage()
    }

    fun onMonthSelected(month: Int) {
        if (month !in 1..12) return
        _uiState.update {
            it.copy(
                selectedMonth = month,
                calculationResult = null,
                isResultStale = false,
                saveConfirmationMessage = null
            )
        }
    }

    fun onYearSelected(year: Int) {
        if (year <= 1900) return
        _uiState.update {
            it.copy(
                selectedYear = year,
                calculationResult = null,
                isResultStale = false,
                saveConfirmationMessage = null
            )
        }
    }

    fun proceedToCalendar(): Boolean {
        val currentState = _uiState.value
        val wage = currentState.dailyWageInput.toDoubleOrNull()

        if (wage == null || wage <= 0.0) {
            _uiState.update {
                it.copy(wageInputError = "कृपया सही दैनिक मजदूरी दर्ज करें")
            }
            return false
        }

        val totalDaysInMonth = CalendarUtils.getDaysInMonth(currentState.selectedYear, currentState.selectedMonth)
        val offset = CalendarUtils.getFirstDayOfWeekOffset(currentState.selectedYear, currentState.selectedMonth)

        viewModelScope.launch {
            val existingRecord = repository.getRecordByMonthAndYear(
                currentState.selectedMonth,
                currentState.selectedYear
            )

            if (existingRecord != null) {
                val parsedMap = AttendanceSerializer.deserialize(existingRecord.attendanceData, totalDaysInMonth)
                _uiState.update {
                    it.copy(
                        dailyWageInput = if (existingRecord.dailyWage > 0) existingRecord.dailyWage.toString() else currentState.dailyWageInput,
                        advanceInput = existingRecord.advanceAmount.toString(),
                        totalCalendarDays = totalDaysInMonth,
                        firstDayOffset = offset,
                        attendanceMap = parsedMap,
                        currentScreen = AppScreen.ATTENDANCE_CALENDAR,
                        wageInputError = null,
                        advanceInputError = null,
                        isResultStale = false
                    )
                }
            } else {
                val initialAttendance = (1..totalDaysInMonth).associateWith { AttendanceStatus.PRESENT }
                _uiState.update {
                    it.copy(
                        totalCalendarDays = totalDaysInMonth,
                        firstDayOffset = offset,
                        attendanceMap = initialAttendance,
                        currentScreen = AppScreen.ATTENDANCE_CALENDAR,
                        wageInputError = null,
                        advanceInputError = null,
                        isResultStale = false
                    )
                }
            }
            calculateMonthlyWage()
        }
        return true
    }

    fun toggleAttendance(dayOfMonth: Int) {
        _uiState.update { current ->
            val oldStatus = current.attendanceMap[dayOfMonth] ?: AttendanceStatus.PRESENT
            val newStatus = oldStatus.next()
            val updatedMap = current.attendanceMap.toMutableMap()
            updatedMap[dayOfMonth] = newStatus
            current.copy(
                attendanceMap = updatedMap,
                isResultStale = true,
                saveConfirmationMessage = null
            )
        }
        calculateMonthlyWage()
    }

    fun setAllPresent() {
        val totalDays = _uiState.value.totalCalendarDays
        val newMap = (1..totalDays).associateWith { AttendanceStatus.PRESENT }
        _uiState.update {
            it.copy(
                attendanceMap = newMap,
                isResultStale = true,
                saveConfirmationMessage = null
            )
        }
        calculateMonthlyWage()
    }

    fun setAllAbsent() {
        val totalDays = _uiState.value.totalCalendarDays
        val newMap = (1..totalDays).associateWith { AttendanceStatus.ABSENT }
        _uiState.update {
            it.copy(
                attendanceMap = newMap,
                isResultStale = true,
                saveConfirmationMessage = null
            )
        }
        calculateMonthlyWage()
    }

    fun calculateMonthlyWage() {
        val state = _uiState.value
        val dailyWage = state.dailyWageInput.toDoubleOrNull() ?: 0.0
        val advance = state.advanceInput.toDoubleOrNull() ?: 0.0

        val presentCount = state.presentDaysCount
        val halfDaysCount = state.halfDaysCount
        val absentCount = state.absentDaysCount
        val totalDays = state.totalCalendarDays
        val paidWageDays = state.paidWageDays

        val grossWages = paidWageDays * dailyWage
        val remainingPayable = grossWages - advance
        val isAdvanceExcess = remainingPayable < 0.0

        val monthItem = com.example.model.HINDI_MONTHS.firstOrNull { it.monthNumber == state.selectedMonth }
        val monthName = monthItem?.hindiName ?: "महीना"

        val formulaText = buildString {
            append("$paidWageDays दिन ($presentCount पूरा + $halfDaysCount आधा) × ₹${dailyWage.toInt()} = ₹${grossWages.toInt()}")
            if (advance > 0) {
                append(" - एडवांस ₹${advance.toInt()} = ₹${remainingPayable.toInt()}")
            }
        }

        val result = CalculationResult(
            monthName = monthName,
            year = state.selectedYear,
            totalDays = totalDays,
            presentDays = presentCount,
            halfDays = halfDaysCount,
            absentDays = absentCount,
            paidWageDays = paidWageDays,
            dailyWage = dailyWage,
            grossWages = grossWages,
            advanceAmount = advance,
            remainingPayable = remainingPayable,
            isAdvanceExcess = isAdvanceExcess,
            formattedDailyWage = CurrencyUtils.formatRupees(dailyWage),
            formattedGrossWages = CurrencyUtils.formatRupees(grossWages),
            formattedAdvance = CurrencyUtils.formatRupees(advance),
            formattedRemainingPayable = CurrencyUtils.formatRupees(remainingPayable),
            calculationFormulaText = formulaText
        )

        _uiState.update {
            it.copy(
                calculationResult = result,
                isResultStale = false
            )
        }
    }

    fun onSaveCalculationClick() {
        val state = _uiState.value
        val result = state.calculationResult ?: return

        viewModelScope.launch {
            val existing = repository.getRecordByMonthAndYear(state.selectedMonth, state.selectedYear)
            if (existing != null) {
                _uiState.update { it.copy(duplicateRecordPrompt = existing) }
            } else {
                doSaveNewRecord(state, result)
            }
        }
    }

    private suspend fun doSaveNewRecord(state: WageUiState, result: CalculationResult) {
        val record = buildWageRecord(state, result, existingId = 0)
        repository.saveRecord(record)
        _uiState.update {
            it.copy(
                saveConfirmationMessage = "हिसाब सुरक्षित हो गया!",
                duplicateRecordPrompt = null
            )
        }
    }

    fun confirmUpdateExistingRecord() {
        val state = _uiState.value
        val result = state.calculationResult ?: return
        val existing = state.duplicateRecordPrompt ?: return

        viewModelScope.launch {
            val record = buildWageRecord(state, result, existingId = existing.id)
            repository.updateRecord(record)
            _uiState.update {
                it.copy(
                    saveConfirmationMessage = "मौजूदा हिसाब अपडेट हो गया!",
                    duplicateRecordPrompt = null
                )
            }
        }
    }

    fun dismissDuplicatePrompt() {
        _uiState.update { it.copy(duplicateRecordPrompt = null) }
    }

    private fun buildWageRecord(state: WageUiState, result: CalculationResult, existingId: Long): WageRecord {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale("hi", "IN"))
        val formattedDate = sdf.format(Date())

        return WageRecord(
            id = existingId,
            month = state.selectedMonth,
            monthHindiName = result.monthName,
            year = state.selectedYear,
            dailyWage = result.dailyWage,
            totalCalendarDays = result.totalDays,
            presentDays = result.presentDays,
            halfDays = result.halfDays,
            absentDays = result.absentDays,
            paidWageDays = result.paidWageDays,
            grossWages = result.grossWages,
            advanceAmount = result.advanceAmount,
            remainingPayable = result.remainingPayable,
            attendanceData = AttendanceSerializer.serialize(state.attendanceMap),
            formattedDailyWage = result.formattedDailyWage,
            formattedGrossWages = result.formattedGrossWages,
            formattedAdvance = result.formattedAdvance,
            formattedRemainingPayable = result.formattedRemainingPayable,
            calculationFormulaText = result.calculationFormulaText,
            timestamp = System.currentTimeMillis(),
            savedDateTimeText = formattedDate
        )
    }

    fun openRecordFromHistory(record: WageRecord) {
        val totalDays = record.totalCalendarDays
        val offset = CalendarUtils.getFirstDayOfWeekOffset(record.year, record.month)
        val parsedMap = AttendanceSerializer.deserialize(record.attendanceData, totalDays)

        _uiState.update {
            it.copy(
                dailyWageInput = record.dailyWage.toInt().toString(),
                advanceInput = record.advanceAmount.toInt().toString(),
                selectedMonth = record.month,
                selectedYear = record.year,
                totalCalendarDays = totalDays,
                firstDayOffset = offset,
                attendanceMap = parsedMap,
                currentScreen = AppScreen.ATTENDANCE_CALENDAR,
                saveConfirmationMessage = null,
                selectedHistoryRecord = null
            )
        }
        calculateMonthlyWage()
    }

    fun openRecordDetail(record: WageRecord) {
        _uiState.update { it.copy(selectedHistoryRecord = record) }
    }

    fun closeRecordDetail() {
        _uiState.update { it.copy(selectedHistoryRecord = null) }
    }

    fun requestDeleteRecord(record: WageRecord) {
        _uiState.update { it.copy(showDeleteConfirmDialog = record) }
    }

    fun confirmDeleteRecord() {
        val record = _uiState.value.showDeleteConfirmDialog ?: return
        viewModelScope.launch {
            repository.deleteRecord(record.id)
            _uiState.update {
                it.copy(
                    showDeleteConfirmDialog = null,
                    selectedHistoryRecord = if (it.selectedHistoryRecord?.id == record.id) null else it.selectedHistoryRecord
                )
            }
        }
    }

    fun dismissDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirmDialog = null) }
    }

    fun requestDeleteAllRecords() {
        _uiState.update { it.copy(showDeleteAllConfirmDialog = true) }
    }

    fun confirmDeleteAllRecords() {
        viewModelScope.launch {
            repository.deleteAllRecords()
            _uiState.update {
                it.copy(
                    showDeleteAllConfirmDialog = false,
                    selectedHistoryRecord = null
                )
            }
        }
    }

    fun dismissDeleteAllConfirm() {
        _uiState.update { it.copy(showDeleteAllConfirmDialog = false) }
    }

    fun startNewCalculation() {
        val (prevMonth, prevYear) = CalendarUtils.getPreviousMonthAndYear()
        _uiState.update {
            it.copy(
                dailyWageInput = "",
                advanceInput = "0",
                selectedMonth = prevMonth,
                selectedYear = prevYear,
                wageInputError = null,
                advanceInputError = null,
                currentScreen = AppScreen.MAIN_INPUT,
                attendanceMap = emptyMap(),
                calculationResult = null,
                isResultStale = false,
                saveConfirmationMessage = null,
                duplicateRecordPrompt = null
            )
        }
    }

    fun navigateBackToInput() {
        _uiState.update {
            it.copy(
                currentScreen = AppScreen.MAIN_INPUT,
                saveConfirmationMessage = null
            )
        }
    }
}
