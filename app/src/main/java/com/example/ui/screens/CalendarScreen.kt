package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ShiftViewModel
import com.example.ui.components.CalendarMonthView
import com.example.ui.components.QuickShiftBar
import com.example.ui.components.ShiftDetailDialog
import com.example.util.JalaliCalendar
import com.example.util.PersianFormatter

@Composable
fun CalendarScreen(
    viewModel: ShiftViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentYear by viewModel.currentYear.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val shiftsByDay by viewModel.monthlyShiftsByDay.collectAsState()
    val shiftTypes by viewModel.shiftTypes.collectAsState()
    val summary by viewModel.monthlySummary.collectAsState()

    var showDetailDialog by remember { mutableStateOf(false) }

    val monthName = JalaliCalendar.MONTH_NAMES.getOrElse(currentMonth - 1) { "" }
    val currentShiftWithType = shiftsByDay[selectedDay]

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Month Navigation Header
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.prevMonth() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ماه قبل",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "$monthName ${PersianFormatter.toPersianDigits(currentYear)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        AssistChip(
                            onClick = { viewModel.goToToday() },
                            label = {
                                Text(
                                    text = "امروز",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = null
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            val text = viewModel.generateScheduleText()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, text)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "ارسال برنامه شیفت"))
                        }) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "اشتراک برنامه",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(onClick = { viewModel.nextMonth() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "ماه بعد",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Quick Month KPI Summary Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickKpiChip(
                    title = "کارکرد",
                    value = PersianFormatter.formatHours(summary.totalWorkedHours),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                QuickKpiChip(
                    title = "موظفی",
                    value = PersianFormatter.formatHours(summary.mandatoryHours),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                val hasOvertime = summary.overtimeHours > 0
                QuickKpiChip(
                    title = if (hasOvertime) "اضافه‌کاری" else "کسری",
                    value = if (hasOvertime) PersianFormatter.formatHours(summary.overtimeHours) else PersianFormatter.formatHours(summary.deficitHours),
                    valueColor = if (hasOvertime) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                    containerColor = if (hasOvertime) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    modifier = Modifier.weight(1f)
                )

                QuickKpiChip(
                    title = "مبلغ اضافه کار",
                    value = PersianFormatter.formatTomans(summary.overtimePay),
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    valueColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1.3f)
                )
            }
        }

        // Full Solar Hijri Calendar Grid
        item {
            CalendarMonthView(
                year = currentYear,
                month = currentMonth,
                selectedDay = selectedDay,
                shiftsByDay = shiftsByDay,
                onDayClick = { day ->
                    viewModel.selectDay(day)
                },
                onDayDoubleClick = { day ->
                    viewModel.selectDay(day)
                    showDetailDialog = true
                }
            )
        }

        // Quick Shift Bar for selected day
        item {
            QuickShiftBar(
                year = currentYear,
                month = currentMonth,
                selectedDay = selectedDay,
                currentShiftWithType = currentShiftWithType,
                shiftTypes = shiftTypes,
                onQuickAssignShift = { type ->
                    viewModel.assignShiftToSelectedDay(type)
                },
                onOpenDetailDialog = {
                    showDetailDialog = true
                },
                onClearShift = {
                    viewModel.clearShiftForDay(selectedDay)
                }
            )
        }
    }

    if (showDetailDialog) {
        ShiftDetailDialog(
            year = currentYear,
            month = currentMonth,
            day = selectedDay,
            currentShiftWithType = currentShiftWithType,
            shiftTypes = shiftTypes,
            onDismiss = { showDetailDialog = false },
            onSave = { typeId, customHours, note, isCovering, colleagueName, ward ->
                viewModel.saveShiftDetail(
                    day = selectedDay,
                    shiftTypeId = typeId,
                    customHours = customHours,
                    note = note,
                    isCovering = isCovering,
                    colleagueName = colleagueName,
                    ward = ward
                )
            },
            onDelete = {
                viewModel.clearShiftForDay(selectedDay)
            }
        )
    }
}

@Composable
private fun QuickKpiChip(
    title: String,
    value: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1
            )
        }
    }
}
