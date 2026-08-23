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
    fun evaluateAmountExpression_obeysOperatorPrecedence() {
        assertEquals(41.0, evaluateAmountExpression("12+8*4-6/2")!!, 0.0)
    }

    @Test
    fun evaluateAmountExpression_supportsDisplayMultiplicationAndDivisionSymbols() {
        assertEquals(9.0, evaluateAmountExpression("12÷2+3×1")!!, 0.0)
    }

    @Test
    fun evaluateAmountExpression_roundsTheFinalResultToTwoDecimals() {
        assertEquals(3.33, evaluateAmountExpression("10/3")!!, 0.0)
    }

    @Test
    fun evaluateAmountExpression_rejectsIncompleteInvalidAndDivisionByZeroExpressions() {
        assertNull(evaluateAmountExpression(""))
        assertNull(evaluateAmountExpression("12+"))
        assertNull(evaluateAmountExpression("4/0"))
        assertNull(evaluateAmountExpression("1..2+3"))
        assertNull(evaluateAmountExpression("1000000000"))
    }

    @Test
    fun appendAmountOperator_replacesAnExistingTrailingOperator() {
        assertEquals("12×", appendAmountOperator("12+", "×"))
        assertEquals("12+", appendAmountOperator("12", "+"))
        assertEquals("-", appendAmountOperator("", "-"))
        assertEquals("", appendAmountOperator("", "×"))
    }

    @Test
    fun appendAmountDecimal_addsOneDecimalPointToTheCurrentNumber() {
        assertEquals("0.", appendAmountDecimal(""))
        assertEquals("12.3", appendAmountDecimal("12.3"))
        assertEquals("12+0.", appendAmountDecimal("12+"))
        assertEquals("12+3.", appendAmountDecimal("12+3"))
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
    fun ensureUncategorizedCategory_addsItOncePreservesItsIconAndMovesItLast() {
        val added = ensureUncategorizedCategory(listOf(LedgerCategory("餐饮", "🍜")))
        assertEquals(2, added.size)
        assertEquals(LedgerCategory("未分类", "⭕"), added.last())

        val customized = listOf(LedgerCategory("未分类", "⭐"))
        assertEquals(customized, ensureUncategorizedCategory(customized))

        val reordered = ensureUncategorizedCategory(
            listOf(
                LedgerCategory("餐饮", "🍜"),
                LedgerCategory("未分类", "⭐"),
                LedgerCategory("交通", "🚇"),
            ),
        )
        assertEquals(
            listOf(
                LedgerCategory("餐饮", "🍜"),
                LedgerCategory("交通", "🚇"),
                LedgerCategory("未分类", "⭐"),
            ),
            reordered,
        )
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
    fun updateEntry_replacesAllEditableFieldsAndPreservesOtherEntries() {
        val target = Entry("1", "支出", "交通", 12.0, 1L, "原备注")
        val other = Entry("2", "收入", "工资", 20.0, 2L, "保留")
        val updated = target.copy(category = "餐饮", amount = 18.5, date = 3L, note = "新备注")

        val result = updateEntry(listOf(target, other), updated)

        assertEquals(listOf(updated, other), result)
    }

    @Test
    fun deleteEntry_removesOnlyTheMatchingEntry() {
        val target = Entry("1", "支出", "餐饮", 12.0, 1L, "删除")
        val other = Entry("2", "支出", "住房", 20.0, 2L, "保留")

        assertEquals(listOf(other), deleteEntry(listOf(target, other), target.id))
        assertEquals(listOf(target, other), deleteEntry(listOf(target, other), "missing"))
    }

    @Test
    fun deleteEntry_doesNotMutateTheSourceList() {
        val target = Entry("1", "支出", "餐饮", 12.0, 1L)
        val other = Entry("2", "收入", "工资", 20.0, 2L)
        val source = mutableListOf(target, other)

        val result = deleteEntry(source, target.id)

        assertEquals(listOf(target, other), source)
        assertEquals(listOf(other), result)
    }

    @Test
    fun deleteEntry_removesEveryRecordWithTheMatchingId() {
        val first = Entry("same", "支出", "餐饮", 12.0, 1L)
        val duplicate = Entry("same", "收入", "工资", 20.0, 2L)
        val other = Entry("other", "支出", "住房", 30.0, 3L)

        val result = deleteEntry(listOf(first, duplicate, other), "same")

        assertEquals(listOf(other), result)
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

    @Test
    fun isDateAfterToday_rejectsTomorrowButAllowsAnyTimeToday() {
        val currentTime = timestamp(2026, Calendar.AUGUST, 21)
        val laterToday = Calendar.getInstance().apply {
            timeInMillis = currentTime
            set(Calendar.HOUR_OF_DAY, 23)
        }.timeInMillis

        assertFalse(isDateAfterToday(timestamp(2026, Calendar.AUGUST, 20), currentTime))
        assertFalse(isDateAfterToday(laterToday, currentTime))
        assertTrue(isDateAfterToday(timestamp(2026, Calendar.AUGUST, 22), currentTime))
        assertTrue(isDateAfterToday(timestamp(2026, Calendar.SEPTEMBER, 1), currentTime))
    }

    @Test
    fun initialDateForNewEntry_futureMonthFallsBackToCurrentDate() {
        val currentTime = timestamp(2026, Calendar.AUGUST, 21)
        val futureEntry = Entry("1", "支出", "餐饮", 20.0, timestamp(2026, Calendar.SEPTEMBER, 8))

        assertEquals(currentTime, initialDateForNewEntry(listOf(futureEntry), currentTime))
    }

    @Test
    fun groupEntriesByMonth_sortsNewestFirstAndSeparatesTheSameMonthAcrossYears() {
        val july21 = Entry("1", "支出", "餐饮", 20.0, timestamp(2026, Calendar.JULY, 21))
        val august2 = Entry("2", "支出", "交通", 30.0, timestamp(2026, Calendar.AUGUST, 2))
        val july25 = Entry("3", "收入", "工资", 40.0, timestamp(2026, Calendar.JULY, 25))
        val previousAugust = Entry("4", "支出", "住房", 50.0, timestamp(2025, Calendar.AUGUST, 30))

        val groups = groupEntriesByMonth(listOf(july21, previousAugust, august2, july25))

        assertEquals(listOf(2026 to 8, 2026 to 7, 2025 to 8), groups.map { it.year to it.month })
        assertEquals(listOf("2"), groups[0].entries.map { it.id })
        assertEquals(listOf("3", "1"), groups[1].entries.map { it.id })
        assertEquals(listOf("4"), groups[2].entries.map { it.id })
    }

    @Test
    fun groupEntriesByDay_sortsNewestFirstAndCombinesEntriesFromTheSameDate() {
        val july21Expense = Entry("1", "支出", "餐饮", 20.0, timestamp(2026, Calendar.JULY, 21))
        val august1 = Entry("2", "支出", "交通", 30.0, timestamp(2026, Calendar.AUGUST, 1))
        val july21Income = Entry("3", "收入", "工资", 40.0, timestamp(2026, Calendar.JULY, 21))
        val previousJuly21 = Entry("4", "支出", "住房", 50.0, timestamp(2025, Calendar.JULY, 21))

        val groups = groupEntriesByDay(listOf(july21Expense, previousJuly21, august1, july21Income))

        assertEquals(
            listOf(Triple(2026, 8, 1), Triple(2026, 7, 21), Triple(2025, 7, 21)),
            groups.map { Triple(it.year, it.month, it.day) },
        )
        assertEquals(listOf("1", "3"), groups[1].entries.map { it.id })
    }

    @Test
    fun filterEntries_supportsAllAndSpecificMonthModes() {
        val july2026 = Entry("1", "支出", "餐饮", 20.0, timestamp(2026, Calendar.JULY, 21))
        val august2026 = Entry("2", "收入", "工资", 30.0, timestamp(2026, Calendar.AUGUST, 1))
        val july2025 = Entry("3", "支出", "住房", 40.0, timestamp(2025, Calendar.JULY, 21))
        val entries = listOf(july2026, august2026, july2025)
        val baseFilter = EntryFilter(
            mode = EntryFilterMode.ALL,
            year = 2026,
            month = 7,
        )

        assertEquals(entries, filterEntries(entries, baseFilter))
        assertEquals(listOf(july2026), filterEntries(entries, baseFilter.copy(mode = EntryFilterMode.MONTH)))
        assertEquals(
            listOf(july2025),
            filterEntries(entries, baseFilter.copy(mode = EntryFilterMode.MONTH, year = 2025)),
        )
    }

    @Test
    fun colorThemeFromStorage_restoresSavedThemeAndFallsBackToClassic() {
        assertEquals(ColorTheme.SKY_BLUE, colorThemeFromStorage("sky_blue"))
        assertEquals(ColorTheme.CLASSIC, colorThemeFromStorage(null))
        assertEquals(ColorTheme.CLASSIC, colorThemeFromStorage("unknown"))
    }

    @Test
    fun colorThemes_haveUniqueStableStorageKeys() {
        val storageKeys = ColorTheme.entries.map { it.storageKey }

        assertEquals(storageKeys.size, storageKeys.toSet().size)
        assertEquals(
            setOf("classic", "fresh_green", "sky_blue", "soft_purple", "coral_orange"),
            storageKeys.toSet(),
        )
    }

    @Test
    fun titleBarStyleFromStorage_restoresSavedStyleAndFallsBackToDesignOne() {
        assertEquals(TitleBarStyle.DESIGN_TWO, titleBarStyleFromStorage("design_two"))
        assertEquals(TitleBarStyle.DESIGN_ONE, titleBarStyleFromStorage(null))
        assertEquals(TitleBarStyle.DESIGN_ONE, titleBarStyleFromStorage("unknown"))
    }

    @Test
    fun titleBarStyles_haveUniqueStableStorageKeys() {
        val storageKeys = TitleBarStyle.entries.map { it.storageKey }

        assertEquals(storageKeys.size, storageKeys.toSet().size)
        assertEquals(setOf("design_one", "design_two"), storageKeys.toSet())
    }
}
