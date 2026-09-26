package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.JalaliCalendar
import com.example.util.PersianFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("شیفت‌یار", appName)
    }

    @Test
    fun `jalali calendar calculation`() {
        // Test Jalali month days
        assertEquals(31, JalaliCalendar.getDaysInMonth(1405, 1)) // Farvardin
        assertEquals(31, JalaliCalendar.getDaysInMonth(1405, 6)) // Shahrivar
        assertEquals(30, JalaliCalendar.getDaysInMonth(1405, 7)) // Mehr
        assertEquals(30, JalaliCalendar.getDaysInMonth(1405, 11)) // Bahman

        // Persian digits conversion
        val persian = PersianFormatter.toPersianDigits("1405/07/25")
        assertEquals("۱۴۰۵/۰۷/۲۵", persian)

        // Tomans currency format
        val tomans = PersianFormatter.formatTomans(1200000L)
        assertTrue(tomans.contains("تومان"))
    }
}
