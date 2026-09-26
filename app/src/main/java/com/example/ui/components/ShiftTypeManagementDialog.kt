package com.example.ui.components

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
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.ShiftType
import com.example.ui.theme.ShiftColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShiftTypeManagementDialog(
    initialShiftType: ShiftType? = null,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        shortCode: String,
        colorHex: String,
        startTime: String,
        endTime: String,
        durationHours: Double,
        isOff: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf(initialShiftType?.name ?: "") }
    var shortCode by remember { mutableStateOf(initialShiftType?.shortCode ?: "") }
    var colorHex by remember { mutableStateOf(initialShiftType?.colorHex ?: ShiftColors[0]) }
    var startTime by remember { mutableStateOf(initialShiftType?.startTime ?: "08:00") }
    var endTime by remember { mutableStateOf(initialShiftType?.endTime ?: "14:30") }
    var durationHoursText by remember {
        mutableStateOf(initialShiftType?.durationHours?.toString() ?: "6.5")
    }
    var isOff by remember { mutableStateOf(initialShiftType?.isOff ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialShiftType == null) "تعریف نوع شیفت جدید" else "ویرایش مشخصات شیفت",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("عنوان شیفت (مثلاً: کشیک لانگ اورژانس)") },
                    leadingIcon = { Icon(Icons.Default.Label, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = shortCode,
                        onValueChange = { shortCode = it },
                        label = { Text("کد کوتاه تقویم (مثلاً: لانگ)") },
                        leadingIcon = { Icon(Icons.Default.ShortText, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = durationHoursText,
                        onValueChange = { durationHoursText = it },
                        label = { Text("مدت به ساعت") },
                        leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("ساعت شروع (مثلا 07:30)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("ساعت پایان (مثلا 14:00)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Is Off / Leave
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "مرخصی / آف استراحت",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "ساعت کارکرد این شیفت صفر محسوب می‌شود",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isOff,
                        onCheckedChange = {
                            isOff = it
                            if (it) durationHoursText = "0"
                        }
                    )
                }

                // Color picker
                Text(
                    text = "انتخاب رنگ تم شیفت:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShiftColors.forEach { hex ->
                        val isSelected = hex.equals(colorHex, ignoreCase = true)
                        val color = try {
                            Color(android.graphics.Color.parseColor(hex))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                                    shape = CircleShape
                                )
                                .clickable { colorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "انتخاب شده",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val duration = durationHoursText.toDoubleOrNull() ?: 6.5
                    onSave(
                        name.ifBlank { "شیفت جدید" },
                        shortCode.ifBlank { "شیفت" },
                        colorHex,
                        startTime,
                        endTime,
                        duration,
                        isOff
                    )
                    onDismiss()
                }
            ) {
                Text("ذخیره شیفت")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}
