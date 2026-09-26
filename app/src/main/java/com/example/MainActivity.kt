package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ShiftViewModel
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.ShiftTypesScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.PersianFormatter

enum class AppTab(val title: String, val icon: ImageVector) {
    CALENDAR("تقویم کشیک", Icons.Default.CalendarMonth),
    CALCULATOR("اضافه کار و مالی", Icons.Default.Calculate),
    NOTES("یادداشت‌ها", Icons.AutoMirrored.Filled.Notes),
    SHIFT_TYPES("انواع شیفت", Icons.Default.Schedule)
}

class MainActivity : ComponentActivity() {

    private val viewModel: ShiftViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var currentTab by remember { mutableStateOf(AppTab.CALENDAR) }
                var showSettingsDialog by remember { mutableStateOf(false) }

                val userSettings by viewModel.userSettings.collectAsState()
                val notesShifts by viewModel.notesAndCoveringShifts.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                    Text(
                                        text = "شیفت‌یار",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 19.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${userSettings.userRole} • ${userSettings.workplace}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = { showSettingsDialog = true }) {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = "تنظیمات",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 3.dp
                        ) {
                            AppTab.entries.forEach { tab ->
                                val selected = currentTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentTab = tab },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    icon = {
                                        if (tab == AppTab.NOTES && notesShifts.isNotEmpty()) {
                                            BadgedBox(
                                                badge = {
                                                    Badge {
                                                        Text(PersianFormatter.toPersianDigits(notesShifts.size))
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = tab.icon,
                                                    contentDescription = tab.title,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = tab.icon,
                                                contentDescription = tab.title,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "tabTransition"
                        ) { tab ->
                            when (tab) {
                                AppTab.CALENDAR -> CalendarScreen(viewModel = viewModel)
                                AppTab.CALCULATOR -> CalculatorScreen(viewModel = viewModel)
                                AppTab.NOTES -> NotesScreen(viewModel = viewModel)
                                AppTab.SHIFT_TYPES -> ShiftTypesScreen(viewModel = viewModel)
                            }
                        }
                    }
                }

                if (showSettingsDialog) {
                    SettingsDialog(
                        currentSettings = userSettings,
                        onDismiss = { showSettingsDialog = false },
                        onSave = { mandatory, rate, role, workplace ->
                            viewModel.updateSettings(mandatory, rate, role, workplace)
                        }
                    )
                }
            }
        }
    }
}
