package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shift_types")
data class ShiftType(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val shortCode: String,
    val colorHex: String,
    val startTime: String,
    val endTime: String,
    val durationHours: Double,
    val isOff: Boolean = false,
    val orderIndex: Int = 0
)
