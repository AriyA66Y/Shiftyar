package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AssignedShift
import com.example.data.model.AssignedShiftWithType
import com.example.data.model.ShiftType
import com.example.data.model.UserSettings
import com.example.data.repository.ShiftRepository
import com.example.util.JalaliCalendar
import com.example.util.PersianFormatter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MonthlyCalculationSummary(
    val totalWorkedHours: Double = 0.0,
    val mandatoryHours: Double = 160.0,
    val overtimeHours: Double = 0.0,
    val deficitHours: Double = 0.0,
    val hourlyRate: Long = 120_000L,
    val overtimePay: Long = 0L,
    val shiftsCount: Int = 0,
    val coveringCount: Int = 0,
    val shiftTypeCounts: Map<String, Int> = emptyMap(),
    val progressFraction: Float = 0f
)

class ShiftViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ShiftRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = ShiftRepository(
            database.shiftTypeDao(),
            database.assignedShiftDao(),
            database.userSettingsDao()
        )
    }

    private val today = JalaliCalendar.getToday()

    private val _currentYear = MutableStateFlow(today.year)
    val currentYear: StateFlow<Int> = _currentYear.asStateFlow()

    private val _currentMonth = MutableStateFlow(today.month)
    val currentMonth: StateFlow<Int> = _currentMonth.asStateFlow()

    private val _selectedDay = MutableStateFlow(today.day)
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    // Shift types flow
    val shiftTypes: StateFlow<List<ShiftType>> = repository.allShiftTypes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User settings flow
    val userSettings: StateFlow<UserSettings> = repository.userSettings
        .combine(MutableStateFlow(Unit)) { settings, _ ->
            settings ?: UserSettings()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserSettings()
        )

    // Shifts for current year & month
    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyShifts: StateFlow<List<AssignedShiftWithType>> = combine(
        _currentYear,
        _currentMonth
    ) { year, month ->
        Pair(year, month)
    }.flatMapLatest { (year, month) ->
        repository.getShiftsForMonthWithTypes(year, month)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Shifts mapped by day of the month for instant calendar lookup
    val monthlyShiftsByDay: StateFlow<Map<Int, AssignedShiftWithType>> = monthlyShifts
        .combine(MutableStateFlow(Unit)) { shifts, _ ->
            shifts.associateBy { it.shift.day }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // All shifts that have notes or colleague covering
    val notesAndCoveringShifts: StateFlow<List<AssignedShiftWithType>> =
        repository.getNotesAndCoveringShiftsWithTypes()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search query for notes
    private val _notesSearchQuery = MutableStateFlow("")
    val notesSearchQuery: StateFlow<String> = _notesSearchQuery.asStateFlow()

    fun setNotesSearchQuery(query: String) {
        _notesSearchQuery.value = query
    }

    // Monthly calculation summary
    val monthlySummary: StateFlow<MonthlyCalculationSummary> = combine(
        monthlyShifts,
        userSettings
    ) { shifts, settings ->
        var totalHours = 0.0
        var coveringCount = 0
        val countsByType = mutableMapOf<String, Int>()

        for (item in shifts) {
            if (!item.shiftType.isOff) {
                totalHours += item.effectiveHours
            }
            if (item.shift.isCoveringForColleague || item.shift.colleagueName.isNotBlank()) {
                coveringCount++
            }
            val typeName = item.shiftType.name
            countsByType[typeName] = (countsByType[typeName] ?: 0) + 1
        }

        val mandatory = settings.mandatoryHours
        val overtime = if (totalHours > mandatory) totalHours - mandatory else 0.0
        val deficit = if (totalHours < mandatory) mandatory - totalHours else 0.0
        val rate = settings.overtimeHourlyRate
        val pay = (overtime * rate).toLong()
        val progress = if (mandatory > 0) (totalHours / mandatory).toFloat().coerceIn(0f, 2f) else 1f

        MonthlyCalculationSummary(
            totalWorkedHours = totalHours,
            mandatoryHours = mandatory,
            overtimeHours = overtime,
            deficitHours = deficit,
            hourlyRate = rate,
            overtimePay = pay,
            shiftsCount = shifts.filter { !it.shiftType.isOff }.size,
            coveringCount = coveringCount,
            shiftTypeCounts = countsByType,
            progressFraction = progress
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthlyCalculationSummary()
    )

    // Navigation methods
    fun selectDay(day: Int) {
        _selectedDay.value = day
    }

    fun nextMonth() {
        if (_currentMonth.value == 12) {
            _currentYear.value += 1
            _currentMonth.value = 1
        } else {
            _currentMonth.value += 1
        }
        val maxDays = JalaliCalendar.getDaysInMonth(_currentYear.value, _currentMonth.value)
        if (_selectedDay.value > maxDays) {
            _selectedDay.value = maxDays
        }
    }

    fun prevMonth() {
        if (_currentMonth.value == 1) {
            _currentYear.value -= 1
            _currentMonth.value = 12
        } else {
            _currentMonth.value -= 1
        }
        val maxDays = JalaliCalendar.getDaysInMonth(_currentYear.value, _currentMonth.value)
        if (_selectedDay.value > maxDays) {
            _selectedDay.value = maxDays
        }
    }

    fun goToToday() {
        val today = JalaliCalendar.getToday()
        _currentYear.value = today.year
        _currentMonth.value = today.month
        _selectedDay.value = today.day
    }

    // Quick shift assignment for selected day
    fun assignShiftToSelectedDay(shiftType: ShiftType) {
        viewModelScope.launch {
            repository.assignShift(
                year = _currentYear.value,
                month = _currentMonth.value,
                day = _selectedDay.value,
                shiftTypeId = shiftType.id
            )
        }
    }

    // Detailed shift save
    fun saveShiftDetail(
        day: Int,
        shiftTypeId: Long,
        customHours: Double?,
        note: String,
        isCovering: Boolean,
        colleagueName: String,
        ward: String
    ) {
        viewModelScope.launch {
            repository.assignShift(
                year = _currentYear.value,
                month = _currentMonth.value,
                day = day,
                shiftTypeId = shiftTypeId,
                customHours = customHours,
                note = note,
                isCoveringForColleague = isCovering,
                colleagueName = colleagueName,
                ward = ward
            )
        }
    }

    fun clearShiftForDay(day: Int) {
        viewModelScope.launch {
            repository.deleteShiftsForDay(_currentYear.value, _currentMonth.value, day)
        }
    }

    fun deleteShift(shift: AssignedShift) {
        viewModelScope.launch {
            repository.deleteShift(shift)
        }
    }

    // User settings update
    fun updateSettings(
        mandatoryHours: Double,
        overtimeHourlyRate: Long,
        userRole: String,
        workplace: String
    ) {
        viewModelScope.launch {
            val updated = userSettings.value.copy(
                mandatoryHours = mandatoryHours,
                overtimeHourlyRate = overtimeHourlyRate,
                userRole = userRole,
                workplace = workplace
            )
            repository.saveUserSettings(updated)
        }
    }

    // Shift Type management
    fun addShiftType(
        name: String,
        shortCode: String,
        colorHex: String,
        startTime: String,
        endTime: String,
        durationHours: Double,
        isOff: Boolean
    ) {
        viewModelScope.launch {
            val maxOrder = shiftTypes.value.maxOfOrNull { it.orderIndex } ?: 0
            val newType = ShiftType(
                name = name,
                shortCode = shortCode,
                colorHex = colorHex,
                startTime = startTime,
                endTime = endTime,
                durationHours = durationHours,
                isOff = isOff,
                orderIndex = maxOrder + 1
            )
            repository.insertShiftType(newType)
        }
    }

    fun updateShiftType(shiftType: ShiftType) {
        viewModelScope.launch {
            repository.updateShiftType(shiftType)
        }
    }

    fun deleteShiftType(shiftType: ShiftType) {
        viewModelScope.launch {
            repository.deleteShiftType(shiftType)
        }
    }

    // Formats a WhatsApp / Messenger schedule export text
    fun generateScheduleText(): String {
        val monthName = JalaliCalendar.MONTH_NAMES[_currentMonth.value - 1]
        val year = PersianFormatter.toPersianDigits(_currentYear.value)
        val summary = monthlySummary.value
        val shifts = monthlyShifts.value.sortedBy { it.shift.day }

        val sb = StringBuilder()
        sb.appendLine("📋 برنامه شیفت و کشیک های ماه $monthName $year")
        sb.appendLine("کاربر: ${userSettings.value.userRole} | ${userSettings.value.workplace}")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━")

        for (item in shifts) {
            val dayName = JalaliCalendar.WEEK_DAY_FULL_NAMES[JalaliCalendar.getDayOfWeek(
                JalaliCalendar.JalaliDate(_currentYear.value, _currentMonth.value, item.shift.day)
            )]
            val dayStr = PersianFormatter.toPersianDigits(item.shift.day)
            sb.append("$dayStr $dayName: ${item.shiftType.name} (${PersianFormatter.formatHours(item.effectiveHours)})")
            if (item.shift.isCoveringForColleague && item.shift.colleagueName.isNotBlank()) {
                sb.append(" [جایگزین ${item.shift.colleagueName}]")
            }
            if (item.shift.note.isNotBlank()) {
                sb.append(" 📝 ${item.shift.note}")
            }
            sb.appendLine()
        }

        sb.appendLine("━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("⏱ مجموع کارکرد: ${PersianFormatter.formatHours(summary.totalWorkedHours)}")
        sb.appendLine("📌 ساعت موظفی: ${PersianFormatter.formatHours(summary.mandatoryHours)}")
        if (summary.overtimeHours > 0) {
            sb.appendLine("⚡ اضافه‌کاری: ${PersianFormatter.formatHours(summary.overtimeHours)}")
            sb.appendLine("💰 تخمین اضافه کار: ${PersianFormatter.formatTomans(summary.overtimePay)}")
        } else if (summary.deficitHours > 0) {
            sb.appendLine("⚠️ کسری کار: ${PersianFormatter.formatHours(summary.deficitHours)}")
        }
        sb.appendLine("ایجاد شده توسط اپلیکیشن شیفت‌یار")
        return sb.toString()
    }
}
