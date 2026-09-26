package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "assigned_shifts",
    foreignKeys = [
        ForeignKey(
            entity = ShiftType::class,
            parentColumns = ["id"],
            childColumns = ["shiftTypeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["year", "month", "day"]),
        Index(value = ["shiftTypeId"])
    ]
)
data class AssignedShift(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val year: Int,
    val month: Int,
    val day: Int,
    val shiftTypeId: Long,
    val customHours: Double? = null,
    val note: String = "",
    val isCoveringForColleague: Boolean = false,
    val colleagueName: String = "",
    val ward: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class AssignedShiftWithType(
    val shift: AssignedShift,
    val shiftType: ShiftType
) {
    val effectiveHours: Double
        get() = shift.customHours ?: shiftType.durationHours
}
