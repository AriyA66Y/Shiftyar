package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssignedShiftWithType
import com.example.data.model.ShiftType
import com.example.util.JalaliCalendar
import com.example.util.PersianFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShiftDetailDialog(
    year: Int,
    month: Int,
    day: Int,
    currentShiftWithType: AssignedShiftWithType?,
    shiftTypes: List<ShiftType>,
    onDismiss: () -> Unit,
    onSave: (
        shiftTypeId: Long,
        customHours: Double?,
        note: String,
        isCovering: Boolean,
        colleagueName: String,
        ward: String
    ) -> Unit,
    onDelete: () -> Unit
) {
    val jalaliDate = JalaliCalendar.JalaliDate(year, month, day)
    val dayOfWeekName = jalaliDate.dayOfWeekName()
    val monthName = jalaliDate.monthName()

    var selectedShiftTypeId by remember {
        mutableStateOf(currentShiftWithType?.shiftType?.id ?: shiftTypes.firstOrNull()?.id ?: 1L)
    }

    var isCustomHoursEnabled by remember {
        mutableStateOf(currentShiftWithType?.shift?.customHours != null)
    }

    var customHoursText by remember {
        mutableStateOf(
            currentShiftWithType?.shift?.customHours?.toString()
                ?: currentShiftWithType?.shiftType?.durationHours?.toString()
                ?: "6.5"
        )
    }

    var noteText by remember {
        mutableStateOf(currentShiftWithType?.shift?.note ?: "")
    }

    var isCovering by remember {
        mutableStateOf(currentShiftWithType?.shift?.isCoveringForColleague ?: false)
    }

    var colleagueNameText by remember {
        mutableStateOf(currentShiftWithType?.shift?.colleagueName ?: "")
    }

    var wardText by remember {
        mutableStateOf(currentShiftWithType?.shift?.ward ?: "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "کشیک $dayOfWeekName ${PersianFormatter.toPersianDigits(day)} $monthName ${PersianFormatter.toPersianDigits(year)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ثبت شیفت، ساعت کارکرد و یادداشت شیفت",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Shift Type Selection Chips
                Text(
                    text = "انتخاب نوع شیفت:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    shiftTypes.forEach { type ->
                        val isSelected = type.id == selectedShiftTypeId
                        val typeColor = try {
                            Color(android.graphics.Color.parseColor(type.colorHex))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) typeColor.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) typeColor else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedShiftTypeId = type.id
                                    if (!isCustomHoursEnabled) {
                                        customHoursText = type.durationHours.toString()
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(typeColor)
                                )
                                Text(
                                    text = type.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "انتخاب شده",
                                        tint = typeColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Custom hours toggle
                val selectedType = shiftTypes.find { it.id == selectedShiftTypeId }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ساعت کارکرد این روز:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "ساعت پیش‌فرض: ${PersianFormatter.formatHours(selectedType?.durationHours ?: 0.0)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isCustomHoursEnabled,
                        onCheckedChange = { isCustomHoursEnabled = it }
                    )
                }

                AnimatedVisibility(visible = isCustomHoursEnabled) {
                    OutlinedTextField(
                        value = customHoursText,
                        onValueChange = { customHoursText = it },
                        label = { Text("ساعت واقعی کارکرد (مثلاً: 8.5)") },
                        leadingIcon = {
                            Icon(Icons.Default.AccessTime, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Covering for colleague feature (Specifically requested by user!)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isCovering,
                        onCheckedChange = { isCovering = it }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "جایگزین / کاور همکار (امروز جای کسی اومدم)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "ثبت نام همکار برای اینکه یادت نره بعداً جبران کنه!",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                AnimatedVisibility(visible = isCovering) {
                    OutlinedTextField(
                        value = colleagueNameText,
                        onValueChange = { colleagueNameText = it },
                        label = { Text("نام همکار (مثلا: خانم دکتر رحیمی یا پرستار کاظمی)") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Note field (Requested: "جای یادداشتم داشته باشه خوبه")
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("یادداشت روز یا شیفت (اختیاری)") },
                    placeholder = { Text("مثلاً: تحویل شیفت به خانم کریمی، شیفت شلوغ اورژانس...") },
                    leadingIcon = {
                        Icon(Icons.Default.EditNote, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // Ward or Department
                OutlinedTextField(
                    value = wardText,
                    onValueChange = { wardText = it },
                    label = { Text("بخش / دپارتمان (اختیاری)") },
                    placeholder = { Text("مثلا: اورژانس، ICU، CCU، جراحی، اطفال...") },
                    leadingIcon = {
                        Icon(Icons.Default.LocalHospital, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val customHours = if (isCustomHoursEnabled) {
                        customHoursText.toDoubleOrNull()
                    } else null

                    onSave(
                        selectedShiftTypeId,
                        customHours,
                        noteText,
                        isCovering,
                        colleagueNameText,
                        wardText
                    )
                    onDismiss()
                }
            ) {
                Text("ذخیره شیفت")
            }
        },
        dismissButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (currentShiftWithType != null) {
                    TextButton(
                        onClick = {
                            onDelete()
                            onDismiss()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "حذف",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حذف شیفت")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("انصراف")
                }
            }
        }
    )
}
