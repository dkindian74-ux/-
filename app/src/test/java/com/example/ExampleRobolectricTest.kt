package com.example

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.WageDatabase
import com.example.data.repository.WageRepository
import com.example.model.AppScreen
import com.example.model.AttendanceStatus
import com.example.util.CalendarUtils
import com.example.util.CurrencyUtils
import com.example.util.ShareUtils
import com.example.util.WageImageGenerator
import java.io.File
import com.example.viewmodel.WageCalculatorViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MeriMazdooriRobolectricTest {

    private lateinit var app: Application
    private lateinit var inMemoryDb: WageDatabase
    private lateinit var repository: WageRepository

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        inMemoryDb = Room.inMemoryDatabaseBuilder(app, WageDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = WageRepository(inMemoryDb.wageRecordDao())
    }

    @After
    fun tearDown() {
        inMemoryDb.close()
    }

    private fun createViewModel(customRepo: WageRepository = repository): WageCalculatorViewModel {
        return WageCalculatorViewModel(app, customRepo)
    }

    @Test
    fun testAppNameString() {
        val appName = app.getString(R.string.app_name)
        assertEquals("मेरी मजदूरी", appName)
    }

    @Test
    fun testViewModelFactoryCreation() {
        val factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(app)
        val vm = factory.create(WageCalculatorViewModel::class.java)
        assertNotNull(vm)
    }

    @Test
    fun testMainActivityLaunch() {
        org.robolectric.Robolectric.buildActivity(MainActivity::class.java).use { controller ->
            val activity = controller.setup().get()
            assertNotNull(activity)
            assertFalse(activity.isFinishing)
        }
    }

    /**
     * Test Complete User Flow with 3-state attendance:
     * 1. Enter daily wage ₹500
     * 2. October 2026 (31 days)
     * 3. Tap date 4: Present -> Absent
     * 4. Tap date 18: Present -> Absent
     * 5. 29 present, 2 absent -> ₹14,500
     */
    @Test
    fun testSection11_CompleteUserFlow() {
        val viewModel = createViewModel()
        assertEquals(AppScreen.MAIN_INPUT, viewModel.uiState.value.currentScreen)

        viewModel.onWageInputChange("500")
        viewModel.onMonthSelected(10) // October
        viewModel.onYearSelected(2026)

        val proceedSuccess = viewModel.proceedToCalendar()
        assertTrue(proceedSuccess)

        assertEquals(AppScreen.ATTENDANCE_CALENDAR, viewModel.uiState.value.currentScreen)
        assertEquals(31, viewModel.uiState.value.totalCalendarDays)
        assertEquals(31, viewModel.uiState.value.presentDaysCount)
        assertEquals(0, viewModel.uiState.value.halfDaysCount)
        assertEquals(0, viewModel.uiState.value.absentDaysCount)

        // 1 tap on day 4: PRESENT -> ABSENT
        viewModel.toggleAttendance(4)
        assertEquals(AttendanceStatus.ABSENT, viewModel.uiState.value.attendanceMap[4])

        // 1 tap on day 18: PRESENT -> ABSENT
        viewModel.toggleAttendance(18)
        assertEquals(AttendanceStatus.ABSENT, viewModel.uiState.value.attendanceMap[18])

        assertEquals(31, viewModel.uiState.value.totalCalendarDays)
        assertEquals(29, viewModel.uiState.value.presentDaysCount)
        assertEquals(0, viewModel.uiState.value.halfDaysCount)
        assertEquals(2, viewModel.uiState.value.absentDaysCount)

        viewModel.calculateMonthlyWage()

        val result = viewModel.uiState.value.calculationResult
        assertNotNull(result)
        assertEquals(29, result?.presentDays)
        assertEquals(0, result?.halfDays)
        assertEquals(2, result?.absentDays)
        assertEquals(500.0, result?.dailyWage ?: 0.0, 0.001)
        assertEquals(14500.0, result?.grossWages ?: 0.0, 0.001)
        assertEquals(14500.0, result?.remainingPayable ?: 0.0, 0.001)
        assertEquals("₹14,500", result?.formattedRemainingPayable)
    }

    /**
     * Test Half-Day Attendance cycling and wage calculation:
     * Interaction cycle: Present -> Absent -> Half-Day -> Present
     * 30-day month (April 2026), Daily wage ₹200
     * 28 Present, 2 Half-Days, 0 Absent
     * Paid wage days = 28 + (2 × 0.5) = 29.0 days
     * Gross wage = 29 × ₹200 = ₹5,800
     */
    @Test
    fun testHalfDayAttendanceAndCalculation() {
        val viewModel = createViewModel()
        viewModel.onWageInputChange("200")
        viewModel.onMonthSelected(4) // April (30 days)
        viewModel.onYearSelected(2026)
        viewModel.proceedToCalendar()

        assertEquals(30, viewModel.uiState.value.totalCalendarDays)
        assertEquals(30, viewModel.uiState.value.presentDaysCount)

        // Day 1: 1st tap -> ABSENT, 2nd tap -> HALF_DAY
        viewModel.toggleAttendance(1)
        assertEquals(AttendanceStatus.ABSENT, viewModel.uiState.value.attendanceMap[1])
        viewModel.toggleAttendance(1)
        assertEquals(AttendanceStatus.HALF_DAY, viewModel.uiState.value.attendanceMap[1])

        // Day 2: 1st tap -> ABSENT, 2nd tap -> HALF_DAY
        viewModel.toggleAttendance(2)
        assertEquals(AttendanceStatus.ABSENT, viewModel.uiState.value.attendanceMap[2])
        viewModel.toggleAttendance(2)
        assertEquals(AttendanceStatus.HALF_DAY, viewModel.uiState.value.attendanceMap[2])

        // Verify counts
        assertEquals(28, viewModel.uiState.value.presentDaysCount)
        assertEquals(2, viewModel.uiState.value.halfDaysCount)
        assertEquals(0, viewModel.uiState.value.absentDaysCount)
        assertEquals(29.0, viewModel.uiState.value.paidWageDays, 0.001)

        viewModel.calculateMonthlyWage()
        val result = viewModel.uiState.value.calculationResult
        assertNotNull(result)
        assertEquals(28, result?.presentDays)
        assertEquals(2, result?.halfDays)
        assertEquals(0, result?.absentDays)
        assertEquals(29.0, result?.paidWageDays ?: 0.0, 0.001)
        assertEquals(5800.0, result?.grossWages ?: 0.0, 0.001)
        assertEquals("₹5,800", result?.formattedGrossWages)
    }

    /**
     * Test Advance Deduction:
     * Gross: ₹5,800, Advance: ₹1,000 -> Remaining Payable: ₹4,800
     * Advance: ₹7,000 -> Remaining Payable: -₹1,200 and isAdvanceExcess = true
     */
    @Test
    fun testAdvanceDeductionAndExcessWarning() {
        val viewModel = createViewModel()
        viewModel.onWageInputChange("200")
        viewModel.onMonthSelected(4)
        viewModel.onYearSelected(2026)
        viewModel.proceedToCalendar()

        // 28 present, 2 half-days = 29 days = ₹5,800
        viewModel.toggleAttendance(1)
        viewModel.toggleAttendance(1) // half-day
        viewModel.toggleAttendance(2)
        viewModel.toggleAttendance(2) // half-day

        // Set Advance = ₹1,000
        viewModel.onAdvanceInputChange("1000")
        viewModel.calculateMonthlyWage()

        var result = viewModel.uiState.value.calculationResult
        assertNotNull(result)
        assertEquals(5800.0, result?.grossWages ?: 0.0, 0.001)
        assertEquals(1000.0, result?.advanceAmount ?: 0.0, 0.001)
        assertEquals(4800.0, result?.remainingPayable ?: 0.0, 0.001)
        assertFalse(result?.isAdvanceExcess ?: true)
        assertEquals("₹4,800", result?.formattedRemainingPayable)

        // Set Advance = ₹7,000 (Excess)
        viewModel.onAdvanceInputChange("7000")
        viewModel.calculateMonthlyWage()

        result = viewModel.uiState.value.calculationResult
        assertNotNull(result)
        assertEquals(5800.0, result?.grossWages ?: 0.0, 0.001)
        assertEquals(7000.0, result?.advanceAmount ?: 0.0, 0.001)
        assertEquals(-1200.0, result?.remainingPayable ?: 0.0, 0.001)
        assertTrue(result?.isAdvanceExcess ?: false)
    }

    /**
     * Test Month Isolation & Persistence:
     * June 2026 saved with 5 absent days and ₹500 advance.
     * July 2026 saved with 2 half-days and ₹1,000 advance.
     * Switching back restores exact attendance for that month.
     */
    @Test
    fun testMonthDataIsolationAndPersistence() = runBlocking {
        val viewModel = createViewModel()

        // June 2026
        viewModel.onWageInputChange("400")
        viewModel.onMonthSelected(6) // June (30 days)
        viewModel.onYearSelected(2026)
        viewModel.proceedToCalendar()
        viewModel.onAdvanceInputChange("500")
        // Mark 5 days absent
        for (day in 1..5) {
            viewModel.toggleAttendance(day) // Present -> Absent
        }
        viewModel.calculateMonthlyWage()
        viewModel.saveCalculationDirectly()

        // Verify June saved
        val juneRecord = repository.getRecordByMonthAndYear(6, 2026)
        assertNotNull(juneRecord)
        assertEquals(25, juneRecord?.presentDays)
        assertEquals(5, juneRecord?.absentDays)
        assertEquals(500.0, juneRecord?.advanceAmount ?: 0.0, 0.001)

        // July 2026
        viewModel.startNewCalculation()
        viewModel.onWageInputChange("400")
        viewModel.onMonthSelected(7) // July (31 days)
        viewModel.onYearSelected(2026)
        viewModel.proceedToCalendar()
        viewModel.onAdvanceInputChange("1000")
        // Mark 2 half-days
        viewModel.toggleAttendance(10)
        viewModel.toggleAttendance(10) // Present -> Absent -> Half-Day
        viewModel.toggleAttendance(11)
        viewModel.toggleAttendance(11) // Present -> Absent -> Half-Day
        viewModel.calculateMonthlyWage()
        viewModel.saveCalculationDirectly()

        // Verify July saved
        val julyRecord = repository.getRecordByMonthAndYear(7, 2026)
        assertNotNull(julyRecord)
        assertEquals(29, julyRecord?.presentDays)
        assertEquals(2, julyRecord?.halfDays)
        assertEquals(1000.0, julyRecord?.advanceAmount ?: 0.0, 0.001)

        // Now reopen June from History
        viewModel.openRecordFromHistory(juneRecord!!)
        assertEquals(6, viewModel.uiState.value.selectedMonth)
        assertEquals(25, viewModel.uiState.value.presentDaysCount)
        assertEquals(5, viewModel.uiState.value.absentDaysCount)
        assertEquals("500", viewModel.uiState.value.advanceInput)
    }

    /**
     * Test WhatsApp Share Message formatting:
     * Verifies all required Hindi fields are populated.
     */
    @Test
    fun testWhatsAppShareMessage() {
        val viewModel = createViewModel()
        viewModel.onWageInputChange("300")
        viewModel.onMonthSelected(10)
        viewModel.onYearSelected(2026)
        viewModel.proceedToCalendar()
        viewModel.onAdvanceInputChange("500")

        viewModel.toggleAttendance(1) // Absent
        viewModel.toggleAttendance(2)
        viewModel.toggleAttendance(2) // Half-day

        viewModel.calculateMonthlyWage()
        val result = viewModel.uiState.value.calculationResult
        assertNotNull(result)

        val shareMessage = ShareUtils.buildShareMessage(result!!)
        assertTrue(shareMessage.contains("मेरी मजदूरी का हिसाब"))
        assertTrue(shareMessage.contains("महीना: अक्टूबर 2026"))
        assertTrue(shareMessage.contains("दैनिक मजदूरी: ₹300"))
        assertTrue(shareMessage.contains("कुल कैलेंडर दिन: 31"))
        assertTrue(shareMessage.contains("उपस्थित दिन: 29"))
        assertTrue(shareMessage.contains("आधी मजदूरी वाले दिन: 1"))
        assertTrue(shareMessage.contains("अनुपस्थित दिन: 1"))
        assertTrue(shareMessage.contains("कुल मजदूरी:"))
        assertTrue(shareMessage.contains("लिया गया एडवांस: ₹500"))
        assertTrue(shareMessage.contains("बाकी लेने योग्य राशि:"))
    }

    /**
     * Test Validation of daily wage input.
     */
    @Test
    fun testValidation() {
        val viewModel = createViewModel()

        viewModel.onWageInputChange("")
        assertFalse(viewModel.proceedToCalendar())
        assertEquals("कृपया दैनिक मजदूरी दर्ज करें।", viewModel.uiState.value.wageInputError)

        viewModel.onWageInputChange("0")
        assertFalse(viewModel.proceedToCalendar())
        assertEquals("कृपया सही मजदूरी दर्ज करें।", viewModel.uiState.value.wageInputError)

        viewModel.onWageInputChange("450")
        assertTrue(viewModel.proceedToCalendar())
        assertNull(viewModel.uiState.value.wageInputError)
    }

    /**
     * Test Indian currency formatting
     */
    @Test
    fun testCurrencyFormatting() {
        assertEquals("₹500", CurrencyUtils.formatIndianRupees(500.0))
        assertEquals("₹6,844", CurrencyUtils.formatIndianRupees(6844.0))
        assertEquals("₹14,000", CurrencyUtils.formatIndianRupees(14000.0))
        assertEquals("₹14,500", CurrencyUtils.formatIndianRupees(14500.0))
        assertEquals("₹1,00,000", CurrencyUtils.formatIndianRupees(100000.0))
        assertEquals("₹250.50", CurrencyUtils.formatIndianRupees(250.50))
    }

    /**
     * Test Wage Image generation:
     * Generates a high-res 1080x1440 Bitmap with complete details and verifies
     * caching and FileProvider URI creation.
     */
    @Test
    fun testWageImageGenerationAndCache() {
        val viewModel = createViewModel()
        viewModel.onWageInputChange("500")
        viewModel.onMonthSelected(4) // April 2026
        viewModel.onYearSelected(2026)
        viewModel.proceedToCalendar()
        viewModel.onAdvanceInputChange("2000")

        // 22 present, 2 half-days, 6 absent
        for (d in 23..28) {
            viewModel.toggleAttendance(d) // Absent
        }
        viewModel.toggleAttendance(29)
        viewModel.toggleAttendance(29) // Half-Day
        viewModel.toggleAttendance(30)
        viewModel.toggleAttendance(30) // Half-Day

        viewModel.calculateMonthlyWage()
        val result = viewModel.uiState.value.calculationResult
        assertNotNull(result)

        // 1. Generate Bitmap
        val summary = WageImageGenerator.fromCalculationResult(result!!)
        val bitmap = WageImageGenerator.createWageReportBitmap(summary)
        assertNotNull(bitmap)
        assertEquals(1080, bitmap.width)
        assertEquals(1440, bitmap.height)
        assertFalse(bitmap.isRecycled)

        // 2. Save to Cache and get FileProvider URI
        val uri = WageImageGenerator.saveBitmapToCache(app, bitmap, "test_april_2026")
        assertNotNull(uri)
        assertTrue(uri.toString().startsWith("content://"))
        assertTrue(uri.toString().contains("shared_images"))

        val cachedFile = File(app.cacheDir, "shared_images/hisab_test_april_2026.png")
        assertTrue("Cache file should exist", cachedFile.exists())
        assertTrue("Cache file should not be empty", cachedFile.length() > 0)

        // 3. Test execution of shareCalculationAsImage without exception
        ShareUtils.shareCalculationAsImage(app, result)
    }
}
