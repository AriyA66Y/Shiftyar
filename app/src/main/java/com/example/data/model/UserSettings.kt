package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val id: Int = 1,
    val mandatoryHours: Double = 160.0,
    val overtimeHourlyRate: Long = 120_000L,
    val userRole: String = "پرستار",
    val workplace: String = "بخش اورژانس"
)
