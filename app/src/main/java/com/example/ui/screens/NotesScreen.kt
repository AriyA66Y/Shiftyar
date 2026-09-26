package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssignedShiftWithType
import com.example.ui.ShiftViewModel
import com.example.ui.components.ShiftDetailDialog
import com.example.util.JalaliCalendar
import com.example.util.PersianFormatter

enum class NotesFilter {
    ALL,
    COVERING_ONLY,
    THIS_MONTH
}

@Composable
fun NotesScreen(
    viewModel: ShiftViewModel,
    modifier: Modifier = Modifier
) {
    val notesShifts by viewModel.notesAndCoveringShifts.collectAsState()
    val currentYear by viewModel.currentYear.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val shiftTypes by viewModel.shiftTypes.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(NotesFilter.ALL) }
    var editingShift by remember { mutableStateOf<AssignedShiftWithType?>(null) }

    val filteredList = notesShifts.filter { item ->
        val shift = item.shift
        val matchesQuery = searchQuery.isBlank() ||
                shift.note.contains(searchQuery, ignoreCase = true) ||
                shift.colleagueName.contains(searchQuery, ignoreCase = true) ||
                shift.ward.contains(searchQuery, ignoreCase = true) ||
                item.shiftType.name.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            NotesFilter.ALL -> true
            NotesFilter.COVERING_ONLY -> shift.isCoveringForColleague || shift.colleagueName.isNotBlank()
            NotesFilter.THIS_MONTH -> shift.year == currentYear && shift.month == currentMonth
        }

        matchesQuery && matchesFilter
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("جستجو در یادداشت‌ها و همکاران...") },
                placeholder = { Text("مثلاً: نام همکار، اورژانس، تحویل شیفت") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "پاک کردن")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        // Filter Chips Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == NotesFilter.ALL,
                    onClick = { selectedFilter = NotesFilter.ALL },
                    label = { Text("همه (${PersianFormatter.toPersianDigits(notesShifts.size)})") }
                )

                FilterChip(
                    selected = selectedFilter == NotesFilter.COVERING_ONLY,
                    onClick = { selectedFilter = NotesFilter.COVERING_ONLY },
                    label = { Text("🤝 جایگزین همکار") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                FilterChip(
                    selected = selectedFilter == NotesFilter.THIS_MONTH,
                    onClick = { selectedFilter = NotesFilter.THIS_MONTH },
                    label = { Text("📅 این ماه") }
                )
            }
        }

        // List of Notes or Empty State
        if (filteredList.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "یادداشتی یافت نشد",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "هنگام ثبت شیفت در تقویم می‌توانید یادداشت بنویسید (مثلاً: امروز جای فلان همکار اومدم)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredList, key = { it.shift.id }) { item ->
                NoteCardItem(
                    item = item,
                    onEdit = { editingShift = item },
                    onDelete = { viewModel.deleteShift(item.shift) }
                )
            }
        }
    }

    editingShift?.let { item ->
        ShiftDetailDialog(
            year = item.shift.year,
            month = item.shift.month,
            day = item.shift.day,
            currentShiftWithType = item,
            shiftTypes = shiftTypes,
            onDismiss = { editingShift = null },
            onSave = { typeId, customHours, note, isCovering, colleagueName, ward ->
                viewModel.saveShiftDetail(
                    day = item.shift.day,
                    shiftTypeId = typeId,
                    customHours = customHours,
                    note = note,
                    isCovering = isCovering,
                    colleagueName = colleagueName,
                    ward = ward
                )
                editingShift = null
            },
            onDelete = {
                viewModel.deleteShift(item.shift)
                editingShift = null
            }
        )
    }
}

@Composable
private fun NoteCardItem(
    item: AssignedShiftWithType,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val shift = item.shift
    val type = item.shiftType
    val jalaliDate = JalaliCalendar.JalaliDate(shift.year, shift.month, shift.day)
    val dayOfWeekName = jalaliDate.dayOfWeekName()
    val monthName = jalaliDate.monthName()

    val shiftColor = try {
        Color(android.graphics.Color.parseColor(type.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Date & Shift Badge & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(shiftColor)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${type.name} • ${PersianFormatter.formatHours(item.effectiveHours)}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "$dayOfWeekName ${PersianFormatter.toPersianDigits(shift.day)} $monthName ${PersianFormatter.toPersianDigits(shift.year)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "ویرایش",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Covering Colleague Highlight
            if (shift.isCoveringForColleague || shift.colleagueName.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "جایگزین همکار: ${shift.colleagueName.ifBlank { "همکار" }} (پوشش شیفت)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // Note Text
            if (shift.note.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.EditNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = shift.note,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Ward / Department Tag
            if (shift.ward.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "بخش: ${shift.ward}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
