package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AssignedShift
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignedShiftDao {

    @Query("SELECT * FROM assigned_shifts WHERE year = :year AND month = :month ORDER BY day ASC, id ASC")
    fun getShiftsForMonth(year: Int, month: Int): Flow<List<AssignedShift>>

    @Query("SELECT * FROM assigned_shifts WHERE year = :year AND month = :month AND day = :day")
    fun getShiftsForDay(year: Int, month: Int, day: Int): Flow<List<AssignedShift>>

    @Query("SELECT * FROM assigned_shifts ORDER BY year DESC, month DESC, day DESC")
    fun getAllShifts(): Flow<List<AssignedShift>>

    @Query("SELECT * FROM assigned_shifts WHERE note != '' OR isCoveringForColleague = 1 ORDER BY year DESC, month DESC, day DESC")
    fun getShiftsWithNotesOrCovering(): Flow<List<AssignedShift>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: AssignedShift): Long

    @Update
    suspend fun updateShift(shift: AssignedShift)

    @Delete
    suspend fun deleteShift(shift: AssignedShift)

    @Query("DELETE FROM assigned_shifts WHERE id = :id")
    suspend fun deleteShiftById(id: Long)

    @Query("DELETE FROM assigned_shifts WHERE year = :year AND month = :month AND day = :day")
    suspend fun deleteShiftsForDay(year: Int, month: Int, day: Int)
}
