package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AssignedShiftDao
import com.example.data.dao.ShiftTypeDao
import com.example.data.dao.UserSettingsDao
import com.example.data.model.AssignedShift
import com.example.data.model.ShiftType
import com.example.data.model.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ShiftType::class, AssignedShift::class, UserSettings::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun shiftTypeDao(): ShiftTypeDao
    abstract fun assignedShiftDao(): AssignedShiftDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shiftyar_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.shiftTypeDao(), database.userSettingsDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(shiftTypeDao: ShiftTypeDao, userSettingsDao: UserSettingsDao) {
            val defaultShifts = listOf(
                ShiftType(
                    id = 1,
                    name = "شیفت صبح (M)",
                    shortCode = "صبح",
                    colorHex = "#0288D1",
                    startTime = "07:30",
                    endTime = "14:00",
                    durationHours = 6.5,
                    isOff = false,
                    orderIndex = 1
                ),
                ShiftType(
                    id = 2,
                    name = "شیفت عصر (E)",
                    shortCode = "عصر",
                    colorHex = "#F57C00",
                    startTime = "13:30",
                    endTime = "20:00",
                    durationHours = 6.5,
                    isOff = false,
                    orderIndex = 2
                ),
                ShiftType(
                    id = 3,
                    name = "شیفت شب (N)",
                    shortCode = "شب",
                    colorHex = "#512DA8",
                    startTime = "19:30",
                    endTime = "08:00",
                    durationHours = 12.5,
                    isOff = false,
                    orderIndex = 3
                ),
                ShiftType(
                    id = 4,
                    name = "شیفت لانگ (L)",
                    shortCode = "لانگ",
                    colorHex = "#C2185B",
                    startTime = "07:30",
                    endTime = "20:00",
                    durationHours = 12.5,
                    isOff = false,
                    orderIndex = 4
                ),
                ShiftType(
                    id = 5,
                    name = "کشیک ۲۴ ساعته (24H)",
                    shortCode = "۲۴س",
                    colorHex = "#D32F2F",
                    startTime = "08:00",
                    endTime = "08:00",
                    durationHours = 24.0,
                    isOff = false,
                    orderIndex = 5
                ),
                ShiftType(
                    id = 6,
                    name = "کشیک آنکال (On-Call)",
                    shortCode = "آنکال",
                    colorHex = "#00897B",
                    startTime = "14:00",
                    endTime = "08:00",
                    durationHours = 6.0,
                    isOff = false,
                    orderIndex = 6
                ),
                ShiftType(
                    id = 7,
                    name = "آف / استراحت (Off)",
                    shortCode = "آف",
                    colorHex = "#78909C",
                    startTime = "00:00",
                    endTime = "00:00",
                    durationHours = 0.0,
                    isOff = true,
                    orderIndex = 7
                )
            )
            shiftTypeDao.insertAll(defaultShifts)

            userSettingsDao.saveUserSettings(
                UserSettings(
                    id = 1,
                    mandatoryHours = 160.0,
                    overtimeHourlyRate = 120_000L,
                    userRole = "پرستار",
                    workplace = "بخش اورژانس"
                )
            )
        }
    }
}
