package com.example.money_log

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
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
}
