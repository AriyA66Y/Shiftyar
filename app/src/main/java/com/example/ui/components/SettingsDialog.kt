package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserSettings
import com.example.util.PersianFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    currentSettings: UserSettings,
    onDismiss: () -> Unit,
    onSave: (mandatoryHours: Double, hourlyRate: Long, userRole: String, workplace: String) -> Unit
) {
    var mandatoryHoursText by remember { mutableStateOf(currentSettings.mandatoryHours.toInt().toString()) }
    var hourlyRateText by remember { mutableStateOf(currentSettings.overtimeHourlyRate.toString()) }
    var userRoleText by remember { mutableStateOf(currentSettings.userRole) }
    var workplaceText by remember { mutableStateOf(currentSettings.workplace) }

    val presetHours = listOf(140, 150, 160, 175, 192)
    val presetRates = listOf(80000L, 100000L, 120000L, 150000L, 200000L)
    val presetRoles = listOf("پزشک عمومی", "متخصص / رزیدنت", "پرستار", "سرپرستار", "ماما", "بهیار", "اینترن")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "تنظیمات موظفی و نرخ اضافه کار",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = "تعیین ساعت موظفی ماهانه و فی هر ساعت اضافه‌کاری",
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
                // Mandatory Hours
                Text(
                    text = "ساعت موظفی ماه:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetHours.forEach { h ->
                        val isSelected = mandatoryHoursText == h.toString()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { mandatoryHoursText = h.toString() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${PersianFormatter.toPersianDigits(h)} ساعت",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = mandatoryHoursText,
                    onValueChange = { mandatoryHoursText = it },
                    label = { Text("ساعت موظفی (سفارشی)") },
                    leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Hourly Overtime Rate
                Text(
                    text = "فی هر ساعت اضافه کار (تومان):",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetRates.forEach { rate ->
                        val isSelected = hourlyRateText == rate.toString()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { hourlyRateText = rate.toString() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = PersianFormatter.formatTomans(rate),
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = hourlyRateText,
                    onValueChange = { hourlyRateText = it },
                    label = { Text("فی هر ساعت اضافه‌کاری (تومان)") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(2.dp))

                // User Role
                Text(
                    text = "سمت و عنوان شغلی:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetRoles.forEach { role ->
                        val isSelected = userRoleText == role
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.tertiaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { userRoleText = role }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = role,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = userRoleText,
                    onValueChange = { userRoleText = it },
                    label = { Text("عنوان شغلی") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Workplace / Department
                OutlinedTextField(
                    value = workplaceText,
                    onValueChange = { workplaceText = it },
                    label = { Text("بیمارستان / بخش پیش‌فرض") },
                    leadingIcon = { Icon(Icons.Default.LocalHospital, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val mandatory = mandatoryHoursText.toDoubleOrNull() ?: 160.0
                    val rate = hourlyRateText.toLongOrNull() ?: 120000L
                    onSave(mandatory, rate, userRoleText, workplaceText)
                    onDismiss()
                }
            ) {
                Text("ذخیره تنظیمات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}
