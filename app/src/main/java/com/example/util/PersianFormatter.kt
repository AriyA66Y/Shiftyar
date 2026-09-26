package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object PersianFormatter {

    private val PERSIAN_DIGITS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun toPersianDigits(input: Any?): String {
        if (input == null) return ""
        val str = input.toString()
        val sb = java.lang.StringBuilder()
        for (ch in str) {
            if (ch in '0'..'9') {
                sb.append(PERSIAN_DIGITS[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatTomans(amount: Long): String {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
        }
        val formatter = DecimalFormat("#,###", symbols)
        val formatted = formatter.format(amount)
        return "${toPersianDigits(formatted)} تومان"
    }

    fun formatHours(hours: Double): String {
        val formatted = if (hours % 1.0 == 0.0) {
            hours.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", hours)
        }
        return "${toPersianDigits(formatted)} ساعت"
    }

    fun formatHoursNumberOnly(hours: Double): String {
        val formatted = if (hours % 1.0 == 0.0) {
            hours.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", hours)
        }
        return toPersianDigits(formatted)
    }
}
