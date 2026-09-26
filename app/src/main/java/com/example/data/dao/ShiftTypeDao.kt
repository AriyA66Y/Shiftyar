package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ShiftType
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftTypeDao {

    @Query("SELECT * FROM shift_types ORDER BY orderIndex ASC, id ASC")
    fun getAllShiftTypes(): Flow<List<ShiftType>>

    @Query("SELECT * FROM shift_types WHERE id = :id LIMIT 1")
    suspend fun getShiftTypeById(id: Long): ShiftType?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShiftType(shiftType: ShiftType): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shiftTypes: List<ShiftType>)

    @Update
    suspend fun updateShiftType(shiftType: ShiftType)

    @Delete
    suspend fun deleteShiftType(shiftType: ShiftType)

    @Query("DELETE FROM shift_types WHERE id = :id")
    suspend fun deleteShiftTypeById(id: Long)
}
