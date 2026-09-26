package com.example.data.repository

import com.example.data.dao.AssignedShiftDao
import com.example.data.dao.ShiftTypeDao
import com.example.data.dao.UserSettingsDao
import com.example.data.model.AssignedShift
import com.example.data.model.AssignedShiftWithType
import com.example.data.model.ShiftType
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ShiftRepository(
    private val shiftTypeDao: ShiftTypeDao,
    private val assignedShiftDao: AssignedShiftDao,
    private val userSettingsDao: UserSettingsDao
) {

    val allShiftTypes: Flow<List<ShiftType>> = shiftTypeDao.getAllShiftTypes()

    val userSettings: Flow<UserSettings?> = userSettingsDao.getUserSettings()

    fun getShiftsForMonthWithTypes(year: Int, month: Int): Flow<List<AssignedShiftWithType>> {
        return combine(
            assignedShiftDao.getShiftsForMonth(year, month),
            shiftTypeDao.getAllShiftTypes()
        ) { shifts, types ->
            val typeMap = types.associateBy { it.id }
            shifts.mapNotNull { shift ->
                typeMap[shift.shiftTypeId]?.let { type ->
                    AssignedShiftWithType(shift = shift, shiftType = type)
                }
            }
        }
    }

    fun getNotesAndCoveringShiftsWithTypes(): Flow<List<AssignedShiftWithType>> {
        return combine(
            assignedShiftDao.getShiftsWithNotesOrCovering(),
            shiftTypeDao.getAllShiftTypes()
        ) { shifts, types ->
            val typeMap = types.associateBy { it.id }
            shifts.mapNotNull { shift ->
                typeMap[shift.shiftTypeId]?.let { type ->
                    AssignedShiftWithType(shift = shift, shiftType = type)
                }
            }
        }
    }

    suspend fun assignShift(
        year: Int,
        month: Int,
        day: Int,
        shiftTypeId: Long,
        customHours: Double? = null,
        note: String = "",
        isCoveringForColleague: Boolean = false,
        colleagueName: String = "",
        ward: String = ""
    ) {
        // First delete any existing shift for this day so we don't duplicate (or can add multiple if desired, but 1 primary shift per day is standard for shift rosters)
        assignedShiftDao.deleteShiftsForDay(year, month, day)
        val shift = AssignedShift(
            year = year,
            month = month,
            day = day,
            shiftTypeId = shiftTypeId,
            customHours = customHours,
            note = note.trim(),
            isCoveringForColleague = isCoveringForColleague,
            colleagueName = colleagueName.trim(),
            ward = ward.trim()
        )
        assignedShiftDao.insertShift(shift)
    }

    suspend fun updateShift(shift: AssignedShift) {
        assignedShiftDao.updateShift(shift)
    }

    suspend fun deleteShift(shift: AssignedShift) {
        assignedShiftDao.deleteShift(shift)
    }

    suspend fun deleteShiftsForDay(year: Int, month: Int, day: Int) {
        assignedShiftDao.deleteShiftsForDay(year, month, day)
    }

    suspend fun insertShiftType(shiftType: ShiftType): Long {
        return shiftTypeDao.insertShiftType(shiftType)
    }

    suspend fun updateShiftType(shiftType: ShiftType) {
        shiftTypeDao.updateShiftType(shiftType)
    }

    suspend fun deleteShiftType(shiftType: ShiftType) {
        shiftTypeDao.deleteShiftType(shiftType)
    }

    suspend fun saveUserSettings(settings: UserSettings) {
        userSettingsDao.saveUserSettings(settings)
    }
}
