package com.example.money_log

import org.junit.Test

import org.junit.Assert.*
import java.util.Calendar

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    private fun timestamp(year: Int, month: Int, day: Int): Long = Calendar.getInstance().apply {
        set(year, month, day, 12, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun updateEntriesCategory_onlyUpdatesMatchingKindAndCategory() {
        val expense = Entry("1", "支出", "交通", 12.0, 1L)
        val sameNameIncome = Entry("2", "收入", "交通", 20.0, 2L)
        val otherExpense = Entry("3", "支出", "餐饮", 30.0, 3L)

        val result = updateEntriesCategory(
            entries = listOf(expense, sameNameIncome, otherExpense),
            kind = "支出",
            currentName = "交通",
            updatedName = "通勤",
        )

        assertEquals("通勤", result[0].category)
        assertEquals("交通", result[1].category)
        assertEquals("餐饮", result[2].category)
    }

    @Test
    fun ensureUncategorizedCategory_addsItOnceAndPreservesItsIcon() {
        val added = ensureUncategorizedCategory(listOf(LedgerCategory("餐饮", "🍜")))
        assertEquals(2, added.size)
        assertEquals(LedgerCategory("未分类", "⭕"), added.last())

        val customized = listOf(LedgerCategory("未分类", "⭐"))
        assertEquals(customized, ensureUncategorizedCategory(customized))
    }

    @Test
    fun deletingCategory_movesOnlyMatchingEntriesToUncategorized() {
        val expense = Entry("1", "支出", "交通", 12.0, 1L)
        val sameNameIncome = Entry("2", "收入", "交通", 20.0, 2L)

        val result = updateEntriesCategory(
            entries = listOf(expense, sameNameIncome),
            kind = "支出",
            currentName = "交通",
            updatedName = "未分类",
        )

        assertEquals("未分类", result[0].category)
        assertEquals("交通", result[1].category)
    }

    @Test
    fun updateSingleEntryCategory_changesOnlyCategoryOfTargetEntry() {
        val target = Entry("1", "支出", "交通", 12.0, 1L)
        val other = Entry("2", "支出", "交通", 20.0, 2L)

        val result = updateSingleEntryCategory(
            entries = listOf(target, other),
            entryId = "1",
            updatedCategory = "餐饮",
        )

        assertEquals(target.copy(category = "餐饮"), result[0])
        assertEquals(other, result[1])
        assertEquals("支出", result[0].kind)
        assertEquals(12.0, result[0].amount, 0.0)
        assertEquals(1L, result[0].date)
    }

    @Test
    fun initialDateForNewEntry_usesFirstDayOfMostRecentlySavedEntryMonth() {
        val augustEntry = Entry("1", "支出", "餐饮", 20.0, timestamp(2026, Calendar.AUGUST, 15))
        val mostRecentlySavedJulyEntry = Entry("2", "支出", "交通", 30.0, timestamp(2026, Calendar.JULY, 8))

        val result = initialDateForNewEntry(
            entries = listOf(augustEntry, mostRecentlySavedJulyEntry),
            currentTime = timestamp(2026, Calendar.AUGUST, 21),
        )
        val date = Calendar.getInstance().apply { timeInMillis = result }

        assertEquals(2026, date[Calendar.YEAR])
        assertEquals(Calendar.JULY, date[Calendar.MONTH])
        assertEquals(1, date[Calendar.DAY_OF_MONTH])
        assertEquals(12, date[Calendar.HOUR_OF_DAY])
    }

    @Test
    fun initialDateForNewEntry_withoutEntriesUsesCurrentDate() {
        val currentTime = timestamp(2026, Calendar.AUGUST, 21)

        assertEquals(currentTime, initialDateForNewEntry(emptyList(), currentTime))
    }
}
