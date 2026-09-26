package com.example.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * High-accuracy Jalali (Solar Hijri / تقویم شمسی) calendar implementation.
 * Supports bidirectional Gregorian <-> Jalali conversions, leap year detection,
 * month day counts, and Persian week day ordering (starting from Saturday / شنبه).
 */
object JalaliCalendar {

    data class JalaliDate(val year: Int, val month: Int, val day: Int) {
        fun formatShamsi(): String = "$year/${month.toString().padStart(2, '0')}/${day.toString().padStart(2, '0')}"
        fun monthName(): String = MONTH_NAMES.getOrElse(month - 1) { "" }
        fun dayOfWeekName(): String = WEEK_DAY_FULL_NAMES.getOrElse(getDayOfWeek(this)) { "" }
    }

    data class GregorianDate(val year: Int, val month: Int, val day: Int)

    val MONTH_NAMES = listOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    // Weeks in Iran start on Saturday (شنبه)
    val WEEK_DAY_SHORT_NAMES = listOf(
        "ش", "ی", "د", "س", "چ", "پ", "ج"
    )

    val WEEK_DAY_FULL_NAMES = listOf(
        "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه"
    )

    fun getToday(): JalaliDate {
        val now = LocalDate.now()
        return gregorianToJalali(now.year, now.monthValue, now.dayOfMonth)
    }

    /**
     * Converts a Gregorian date to Jalali (Solar Hijri) date.
     */
    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val gdm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) (gy + 1) else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + gdm[gm - 1]
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += ((days - 1) / 365)
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + (days / 31)
            jd = 1 + (days % 31)
        } else {
            jm = 7 + ((days - 186) / 30)
            jd = 1 + ((days - 186) % 30)
        }
        return JalaliDate(jy, jm, jd)
    }

    /**
     * Converts a Jalali date to Gregorian date.
     */
    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): GregorianDate {
        val jy2 = jy + 1595
        var days = -355668 + (365 * jy2) + ((jy2 / 33) * 8) + (((jy2 % 33) + 3) / 4) + jd +
                (if (jm < 7) ((jm - 1) * 31) else (((jm - 7) * 30) + 186))
        var gy = 400 * (days / 146097)
        days %= 146097
        if (days > 36524) {
            gy += 100 * (--days / 36524)
            days %= 36524
            if (days >= 365) days++
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            gy += ((days - 1) / 365)
            days = (days - 1) % 365
        }
        var gd = days + 1
        val isLeap = (gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)
        val sala = intArrayOf(0, 31, if (isLeap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        while (gm < 13 && gd > sala[gm]) {
            gd -= sala[gm]
            gm++
        }
        return GregorianDate(gy, gm, gd)
    }

    /**
     * Returns true if the Jalali year is a leap year (366 days).
     */
    fun isLeapYear(jy: Int): Boolean {
        val gStart = jalaliToGregorian(jy, 1, 1)
        val gNext = jalaliToGregorian(jy + 1, 1, 1)
        val start = LocalDate.of(gStart.year, gStart.month, gStart.day)
        val next = LocalDate.of(gNext.year, gNext.month, gNext.day)
        return ChronoUnit.DAYS.between(start, next) == 366L
    }

    /**
     * Number of days in the specified Jalali month (1..12).
     */
    fun getDaysInMonth(jy: Int, jm: Int): Int {
        return when {
            jm in 1..6 -> 31
            jm in 7..11 -> 30
            jm == 12 -> if (isLeapYear(jy)) 30 else 29
            else -> 30
        }
    }

    /**
     * Gets the day of week for a Jalali date.
     * Returns:
     * 0: شنبه (Saturday)
     * 1: یکشنبه (Sunday)
     * 2: دوشنبه (Monday)
     * 3: سه‌شنبه (Tuesday)
     * 4: چهارشنبه (Wednesday)
     * 5: پنج‌شنبه (Thursday)
     * 6: جمعه (Friday - holiday)
     */
    fun getDayOfWeek(date: JalaliDate): Int {
        val g = jalaliToGregorian(date.year, date.month, date.day)
        val localDate = LocalDate.of(g.year, g.month, g.day)
        // LocalDate.dayOfWeek: Monday=1, ..., Saturday=6, Sunday=7
        return (localDate.dayOfWeek.value % 7 + 1) % 7
    }

    /**
     * Returns the day of the week index (0..6) for the 1st of the specified Jalali month.
     */
    fun getFirstDayOfWeekForMonth(jy: Int, jm: Int): Int {
        return getDayOfWeek(JalaliDate(jy, jm, 1))
    }
}
