package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssignedShiftWithType
import com.example.util.JalaliCalendar
import com.example.util.PersianFormatter

@Composable
fun CalendarMonthView(
    year: Int,
    month: Int,
    selectedDay: Int,
    shiftsByDay: Map<Int, AssignedShiftWithType>,
    onDayClick: (Int) -> Unit,
    onDayDoubleClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val daysInMonth = JalaliCalendar.getDaysInMonth(year, month)
    val firstDayOffset = JalaliCalendar.getFirstDayOfWeekForMonth(year, month)
    val today = JalaliCalendar.getToday()
    val isCurrentMonthToday = today.year == year && today.month == month

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            // Week day headers: ش ی د س چ پ ج
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                JalaliCalendar.WEEK_DAY_SHORT_NAMES.forEachIndexed { index, name ->
                    val isFriday = index == 6 // جمعه
                    val color = if (isFriday) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(
                        text = name,
                        fontWeight = if (isFriday) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        color = color,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Grid calculation: cells 0 until (firstDayOffset + daysInMonth)
            val totalCells = firstDayOffset + daysInMonth
            val totalRows = (totalCells + 6) / 7

            for (row in 0 until totalRows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (col in 0..6) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - firstDayOffset + 1

                        if (dayNumber in 1..daysInMonth) {
                            val isSelected = dayNumber == selectedDay
                            val isToday = isCurrentMonthToday && (dayNumber == today.day)
                            val isFriday = col == 6
                            val assignedShift = shiftsByDay[dayNumber]

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(2.dp)
                            ) {
                                CalendarDayCell(
                                    day = dayNumber,
                                    isFriday = isFriday,
                                    isToday = isToday,
                                    isSelected = isSelected,
                                    shift = assignedShift,
                                    onClick = { onDayClick(dayNumber) }
                                )
                            }
                        } else {
                            // Blank cell
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(2.dp)
                                    .aspectRatio(0.85f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    day: Int,
    isFriday: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    shift: AssignedShiftWithType?,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            else -> Color.Transparent
        },
        label = "cellBorder"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            isToday -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else -> Color.Transparent
        },
        label = "cellBg"
    )

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isFriday -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.82f)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else if (isToday) 1.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 3.dp, horizontal = 2.dp)
            .semantics { contentDescription = "روز $day" },
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Day Number + Indicators
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = PersianFormatter.toPersianDigits(day),
                    fontSize = 13.sp,
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )

                if (isToday) {
                    Box(
                        modifier = Modifier
                            .padding(start = 2.dp)
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Shift Badge if assigned
            if (shift != null) {
                val shiftColor = try {
                    Color(android.graphics.Color.parseColor(shift.shiftType.colorHex))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.secondary
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(shiftColor)
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = shift.shiftType.shortCode,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!shift.shiftType.isOff) {
                            Text(
                                text = PersianFormatter.formatHoursNumberOnly(shift.effectiveHours),
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 8.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Small badge for note or covering colleague
                if (shift.shift.note.isNotBlank() || shift.shift.isCoveringForColleague) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 1.dp)
                    ) {
                        if (shift.shift.isCoveringForColleague) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "جایگزین همکار",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                        if (shift.shift.note.isNotBlank()) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = "دارای یادداشت",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}
