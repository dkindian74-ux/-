package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.WageDatabase
import com.example.data.local.WageRecord
import com.example.data.repository.WageRepository
import com.example.model.AppScreen
import com.example.model.AttendanceStatus
import com.example.model.CalculationResult
import com.example.util.AttendanceSerializer
import com.example.util.CalendarUtils
import com.example.util.CurrencyUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val selectedMonth: Int = 10, // Default October (अक्टूबर)
    val selectedYear: Int = 2026, // Default 2026
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
    val showDeleteAllConfirmDialog: Boolean = false
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

    private val repository: WageRepository = customRepository ?: WageRepository(
        WageDatabase.getDatabase(application).wageRecordDao()
    )

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<WageUiState> = _uiState.asStateFlow()

    val historyRecords: StateFlow<List<WageRecord>> = repository.allRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun createInitialState(): WageUiState {
        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        val currentMonth = now.get(Calendar.MONTH) + 1 // 1..12
        return WageUiState(
            dailyWageInput = "",
            advanceInput = "0",
            selectedMonth = currentMonth,
            selectedYear = currentYear,
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

    /**
     * Updates daily wage input, converting any Devanagari numerals to ASCII digits.
     */
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

    /**
     * Updates advance input field, converting Devanagari numerals to ASCII digits.
     */
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
        // Recalculate wages immediately when advance changes
        calculateMonthlyWage()
    }

    /**
     * Updates selected month (1..12).
     */
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

    /**
     * Updates selected year.
     */
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

    /**
     * Validates input and transitions to calendar screen.
     * Loads existing persistent record if previously saved for this month/year.
     */
    fun proceedToCalendar(): Boolean {
        val currentState = _uiState.value
        val wageTrimmed = currentState.dailyWageInput.trim()

        if (wageTrimmed.isEmpty()) {
            _uiState.update { it.copy(wageInputError = "कृपया दैनिक मजदूरी दर्ज करें।") }
            return false
        }

        val parsedWage = CurrencyUtils.parseDailyWage(wageTrimmed)
        if (parsedWage == null || parsedWage <= 0.0) {
            _uiState.update { it.copy(wageInputError = "कृपया सही मजदूरी दर्ज करें।") }
            return false
        }

        val daysCount = CalendarUtils.getDaysInMonth(currentState.selectedYear, currentState.selectedMonth)
        val firstDayOffset = CalendarUtils.getFirstDayOfWeekOffset(currentState.selectedYear, currentState.selectedMonth)
        val defaultAttendance = (1..daysCount).associateWith { AttendanceStatus.PRESENT }

        _uiState.update {
            it.copy(
                wageInputError = null,
                totalCalendarDays = daysCount,
                firstDayOffset = firstDayOffset,
                attendanceMap = defaultAttendance,
                currentScreen = AppScreen.ATTENDANCE_CALENDAR,
                saveConfirmationMessage = null,
                isResultStale = false
            )
        }
        calculateMonthlyWage()

        // Check if saved persistent record already exists for this month and year
        viewModelScope.launch {
            val existingRecord = repository.getRecordByMonthAndYear(
                currentState.selectedMonth,
                currentState.selectedYear
            )

            if (existingRecord != null) {
                val restoredAttendance = if (existingRecord.attendanceData.isNotEmpty()) {
                    AttendanceSerializer.deserialize(existingRecord.attendanceData, daysCount)
                } else {
                    defaultAttendance
                }

                val advanceStr = if (existingRecord.advanceAmount % 1.0 == 0.0) {
                    existingRecord.advanceAmount.toInt().toString()
                } else {
                    existingRecord.advanceAmount.toString()
                }

                _uiState.update {
                    it.copy(
                        advanceInput = advanceStr,
                        attendanceMap = restoredAttendance
                    )
                }
                calculateMonthlyWage()
            }
        }
        return true
    }

    /**
     * Cycles attendance for a single day:
     * 1st tap: Present -> Absent
     * 2nd tap: Absent -> Half-Day
     * 3rd tap: Half-Day -> Present
     */
    fun toggleAttendance(dayOfMonth: Int) {
        _uiState.update { state ->
            val currentStatus = state.attendanceMap[dayOfMonth] ?: AttendanceStatus.PRESENT
            val newStatus = currentStatus.next()
            val updatedMap = state.attendanceMap.toMutableMap()
            updatedMap[dayOfMonth] = newStatus
            state.copy(
                attendanceMap = updatedMap,
                saveConfirmationMessage = null
            )
        }
        calculateMonthlyWage()
        autoPersistCurrentCalculation()
    }

    /**
     * Marks all days in the month as Present.
     */
    fun setAllPresent() {
        _uiState.update { state ->
            val allPresent = (1..state.totalCalendarDays).associateWith { AttendanceStatus.PRESENT }
            state.copy(
                attendanceMap = allPresent,
                saveConfirmationMessage = null
            )
        }
        calculateMonthlyWage()
        autoPersistCurrentCalculation()
    }

    /**
     * Marks all days in the month as Absent.
     */
    fun setAllAbsent() {
        _uiState.update { state ->
            val allAbsent = (1..state.totalCalendarDays).associateWith { AttendanceStatus.ABSENT }
            state.copy(
                attendanceMap = allAbsent,
                saveConfirmationMessage = null
            )
        }
        calculateMonthlyWage()
        autoPersistCurrentCalculation()
    }

    /**
     * Calculates the monthly wage including Half-Days and Advance deduction.
     * Paid Wage Days = Present Days + (Half-Days × 0.5)
     * Gross Wages = Paid Wage Days × Daily Wage
     * Remaining Payable Amount = Gross Wages − Advance
     */
    fun calculateMonthlyWage() {
        val state = _uiState.value
        val parsedWage = CurrencyUtils.parseDailyWage(state.dailyWageInput) ?: return
        val advance = CurrencyUtils.parseDailyWage(state.advanceInput) ?: 0.0

        val presentDays = state.presentDaysCount
        val halfDays = state.halfDaysCount
        val absentDays = state.absentDaysCount

        val paidDays = presentDays + (halfDays * 0.5)
        val grossWages = paidDays * parsedWage
        val remainingPayable = grossWages - advance
        val isAdvanceExcess = advance > grossWages

        val formattedDaily = CurrencyUtils.formatIndianRupees(parsedWage)
        val formattedGross = CurrencyUtils.formatIndianRupees(grossWages)
        val formattedAdv = CurrencyUtils.formatIndianRupees(advance)
        val formattedRemaining = CurrencyUtils.formatIndianRupees(remainingPayable)
        val monthHindi = CalendarUtils.getHindiMonthName(state.selectedMonth)

        val paidDaysDisplay = if (paidDays % 1.0 == 0.0) paidDays.toInt().toString() else paidDays.toString()
        val formulaText = "$paidDaysDisplay दिन × $formattedDaily = $formattedGross"

        val result = CalculationResult(
            monthName = monthHindi,
            year = state.selectedYear,
            totalDays = state.totalCalendarDays,
            presentDays = presentDays,
            halfDays = halfDays,
            absentDays = absentDays,
            paidWageDays = paidDays,
            dailyWage = parsedWage,
            grossWages = grossWages,
            advanceAmount = advance,
            remainingPayable = remainingPayable,
            isAdvanceExcess = isAdvanceExcess,
            formattedDailyWage = formattedDaily,
            formattedGrossWages = formattedGross,
            formattedAdvance = formattedAdv,
            formattedRemainingPayable = formattedRemaining,
            calculationFormulaText = formulaText
        )

        _uiState.update {
            it.copy(
                calculationResult = result,
                isResultStale = false
            )
        }
    }

    /**
     * Saves or updates the current month's calculation to Room database automatically.
     */
    private fun autoPersistCurrentCalculation() {
        val result = _uiState.value.calculationResult ?: return
        val month = _uiState.value.selectedMonth
        val year = _uiState.value.selectedYear
        val attendanceStr = AttendanceSerializer.serialize(_uiState.value.attendanceMap)

        viewModelScope.launch {
            val existing = repository.getRecordByMonthAndYear(month, year)
            saveRecordInternal(
                result = result,
                month = month,
                year = year,
                attendanceData = attendanceStr,
                existingId = existing?.id ?: 0,
                showFeedback = false
            )
        }
    }

    /**
     * Directly saves calculation (suspend, useful for synchronous persistence or programmatic save).
     */
    suspend fun saveCalculationDirectly() {
        val result = _uiState.value.calculationResult ?: return
        val month = _uiState.value.selectedMonth
        val year = _uiState.value.selectedYear
        val attendanceStr = AttendanceSerializer.serialize(_uiState.value.attendanceMap)
        val existing = repository.getRecordByMonthAndYear(month, year)
        saveRecordInternal(
            result = result,
            month = month,
            year = year,
            attendanceData = attendanceStr,
            existingId = existing?.id ?: 0,
            showFeedback = true
        )
    }

    /**
     * Explicit save triggered by "हिसाब सेव करें" button.
     */
    fun onSaveCalculationClick() {
        val result = _uiState.value.calculationResult ?: return
        val month = _uiState.value.selectedMonth
        val year = _uiState.value.selectedYear
        val attendanceStr = AttendanceSerializer.serialize(_uiState.value.attendanceMap)

        viewModelScope.launch {
            val existing = repository.getRecordByMonthAndYear(month, year)
            if (existing != null) {
                // If record exists, prompt for update
                _uiState.update { it.copy(duplicateRecordPrompt = existing) }
            } else {
                saveRecordInternal(
                    result = result,
                    month = month,
                    year = year,
                    attendanceData = attendanceStr,
                    existingId = 0,
                    showFeedback = true
                )
            }
        }
    }

    /**
     * Confirms updating the existing record for the same month and year.
     */
    fun confirmUpdateExistingRecord() {
        val prompt = _uiState.value.duplicateRecordPrompt ?: return
        val result = _uiState.value.calculationResult ?: return
        val month = _uiState.value.selectedMonth
        val year = _uiState.value.selectedYear
        val attendanceStr = AttendanceSerializer.serialize(_uiState.value.attendanceMap)

        viewModelScope.launch {
            saveRecordInternal(
                result = result,
                month = month,
                year = year,
                attendanceData = attendanceStr,
                existingId = prompt.id,
                showFeedback = true
            )
            _uiState.update { it.copy(duplicateRecordPrompt = null) }
        }
    }

    /**
     * Dismisses duplicate record prompt.
     */
    fun dismissDuplicatePrompt() {
        _uiState.update { it.copy(duplicateRecordPrompt = null) }
    }

    private suspend fun saveRecordInternal(
        result: CalculationResult,
        month: Int,
        year: Int,
        attendanceData: String,
        existingId: Long,
        showFeedback: Boolean
    ) {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale("hi", "IN"))
        val formattedDate = sdf.format(Date())

        val record = WageRecord(
            id = existingId,
            month = month,
            monthHindiName = result.monthName,
            year = year,
            dailyWage = result.dailyWage,
            totalCalendarDays = result.totalDays,
            presentDays = result.presentDays,
            halfDays = result.halfDays,
            absentDays = result.absentDays,
            paidWageDays = result.paidWageDays,
            grossWages = result.grossWages,
            advanceAmount = result.advanceAmount,
            remainingPayable = result.remainingPayable,
            attendanceData = attendanceData,
            formattedDailyWage = result.formattedDailyWage,
            formattedGrossWages = result.formattedGrossWages,
            formattedAdvance = result.formattedAdvance,
            formattedRemainingPayable = result.formattedRemainingPayable,
            calculationFormulaText = result.calculationFormulaText,
            timestamp = System.currentTimeMillis(),
            savedDateTimeText = formattedDate
        )

        if (existingId > 0) {
            repository.updateRecord(record)
            if (showFeedback) {
                _uiState.update {
                    it.copy(saveConfirmationMessage = "हिसाब सफलतापूर्वक अपडेट हो गया।")
                }
            }
        } else {
            repository.saveRecord(record)
            if (showFeedback) {
                _uiState.update {
                    it.copy(saveConfirmationMessage = "हिसाब सफलतापूर्वक सेव हो गया।")
                }
            }
        }
    }

    /**
     * Clears confirmation message snackbar/banner.
     */
    fun clearConfirmationMessage() {
        _uiState.update { it.copy(saveConfirmationMessage = null) }
    }

    /**
     * Navigates to the History screen.
     */
    fun openHistoryScreen() {
        _uiState.update {
            it.copy(currentScreen = AppScreen.HISTORY)
        }
    }

    /**
     * Opens a saved monthly record from History directly into the Attendance Calendar screen.
     */
    fun openRecordFromHistory(record: WageRecord) {
        val daysCount = record.totalCalendarDays
        val firstDayOffset = CalendarUtils.getFirstDayOfWeekOffset(record.year, record.month)
        val attendance = AttendanceSerializer.deserialize(record.attendanceData, daysCount)

        val wageStr = if (record.dailyWage % 1.0 == 0.0) {
            record.dailyWage.toInt().toString()
        } else {
            record.dailyWage.toString()
        }

        val advanceStr = if (record.advanceAmount % 1.0 == 0.0) {
            record.advanceAmount.toInt().toString()
        } else {
            record.advanceAmount.toString()
        }

        _uiState.update {
            it.copy(
                selectedMonth = record.month,
                selectedYear = record.year,
                dailyWageInput = wageStr,
                advanceInput = advanceStr,
                totalCalendarDays = daysCount,
                firstDayOffset = firstDayOffset,
                attendanceMap = attendance,
                currentScreen = AppScreen.ATTENDANCE_CALENDAR,
                saveConfirmationMessage = null,
                selectedHistoryRecord = null
            )
        }
        calculateMonthlyWage()
    }

    /**
     * Opens record details modal.
     */
    fun openRecordDetail(record: WageRecord) {
        _uiState.update {
            it.copy(selectedHistoryRecord = record)
        }
    }

    /**
     * Closes record details modal.
     */
    fun closeRecordDetail() {
        _uiState.update {
            it.copy(selectedHistoryRecord = null)
        }
    }

    /**
     * Prompts confirmation dialog to delete a single history record.
     */
    fun requestDeleteRecord(record: WageRecord) {
        _uiState.update {
            it.copy(showDeleteConfirmDialog = record)
        }
    }

    /**
     * Confirms deletion of the selected history record.
     */
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

    /**
     * Dismisses the single record delete dialog.
     */
    fun dismissDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirmDialog = null) }
    }

    /**
     * Prompts confirmation dialog to delete all history records.
     */
    fun requestDeleteAllRecords() {
        _uiState.update { it.copy(showDeleteAllConfirmDialog = true) }
    }

    /**
     * Confirms deletion of all history records.
     */
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

    /**
     * Dismisses the delete-all confirmation dialog.
     */
    fun dismissDeleteAllConfirm() {
        _uiState.update { it.copy(showDeleteAllConfirmDialog = false) }
    }

    /**
     * Resets the entire calculator state and returns to MainInputScreen.
     * Note: Does NOT delete saved history records!
     */
    fun startNewCalculation() {
        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        val currentMonth = now.get(Calendar.MONTH) + 1

        _uiState.update {
            it.copy(
                dailyWageInput = "",
                advanceInput = "0",
                selectedMonth = currentMonth,
                selectedYear = currentYear,
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

    /**
     * Navigates back to Main Input screen.
     */
    fun navigateBackToInput() {
        _uiState.update {
            it.copy(
                currentScreen = AppScreen.MAIN_INPUT,
                saveConfirmationMessage = null
            )
        }
    }
}
