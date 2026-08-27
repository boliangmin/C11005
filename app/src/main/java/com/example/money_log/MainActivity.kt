package com.example.money_log

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.placeCursorAtEnd
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.example.money_log.ui.theme.AppOnPrimary
import com.example.money_log.ui.theme.AppOnPrimaryContainer
import com.example.money_log.ui.theme.AppPrimary
import com.example.money_log.ui.theme.AppPrimaryContainer
import com.example.money_log.ui.theme.Money_LogTheme
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.roundToInt

private val Bg = Color(0xFFF7F7F7)
private val Ink = Color(0xFF2D3037)
private val IncomeAmountGreen = Color(0xFF57B881)
private val NoteSlate = Color(0xFF64748B)
private val Muted = Color(0xFF777A81)
private val Danger = Color(0xFFC43D3D)
private val DeleteSwipeBackground = Color(0xFFE57373)
private val DeleteActionRed = Color(0xFFE53935)
private val NeutralIconBorder = Color(0xFFE5E5E5)
private val EditCategorySelectedGreen = Color(0xFFC8E6C9)
private val SelectionBlue = Color(0xFF315F9F)
private val SelectionBlueContainer = Color(0xFFEEF5FF)
private val AnnualDashboardInk = Color(0xFF263648)
// blmcc：年度看板备用配色（翡翠绿 → 深青色 → 宝蓝色 → 靛紫色）
// private val AnnualDashboardGradientColors = listOf(
//     Color(0xFF00796B),
//     Color(0xFF00838F),
//     Color(0xFF1565C0),
//     Color(0xFF512DA8),
// )
private val AnnualDashboardGradientColors = listOf(
    Color(0xFFB8BEC8),
    Color(0xFFAEBFCE),
    Color(0xFFA7D8F5),
)
private data class PieColorCandidate(val color: Color, val hue: Float)

private val MonthlyExpensePieColorCandidates = listOf(
    PieColorCandidate(Color(0xFFFF8A00), 30f),
    PieColorCandidate(Color(0xFF3F7DE0), 215f),
    PieColorCandidate(Color(0xFF8AC926), 90f),
    PieColorCandidate(Color(0xFF7C4DFF), 270f),
    PieColorCandidate(Color(0xFF22B573), 145f),
    PieColorCandidate(Color(0xFFE83E8C), 330f),
    PieColorCandidate(Color(0xFFF4C430), 50f),
    PieColorCandidate(Color(0xFF4B50C9), 240f),
    PieColorCandidate(Color(0xFF00A6A6), 175f),
    PieColorCandidate(Color(0xFFEF4444), 0f),
    PieColorCandidate(Color(0xFF00B8D9), 195f),
    PieColorCandidate(Color(0xFFFF5D8F), 345f),
)
private val MonthlyExpensePieFallbackColor = MonthlyExpensePieColorCandidates.first().color
private const val MonthlyExpensePieMaxSlices = 8
private val MonthlyExpensePieSize = 132.dp
private val MonthlyExpensePieLegendRowHeight = 14.dp
private val MonthlyExpensePieLegendHorizontalGap = 8.dp
private val MonthlyExpensePieLegendCategoryWidth = 64.dp
private val MonthlyExpensePieLegendPercentageWidth = 40.dp
private val MonthlyExpensePieLegendSwatchGap = 4.dp

private fun hueDistance(first: Float, second: Float): Float {
    val distance = abs(first - second)
    return minOf(distance, 360f - distance)
}

private fun highContrastPieColors(count: Int, startIndex: Int): List<Color> {
    if (count <= 0) return emptyList()
    val uniqueColorCount = minOf(count, MonthlyExpensePieColorCandidates.size)
    val normalizedStartIndex = Math.floorMod(startIndex, MonthlyExpensePieColorCandidates.size)
    val selected = mutableListOf(MonthlyExpensePieColorCandidates[normalizedStartIndex])
    val remaining = MonthlyExpensePieColorCandidates
        .filterIndexed { index, _ -> index != normalizedStartIndex }
        .toMutableList()
    while (selected.size < uniqueColorCount) {
        val previous = selected.last()
        val closesCircle = selected.size == uniqueColorCount - 1
        val next = remaining.maxByOrNull { candidate ->
            val previousDistance = hueDistance(previous.hue, candidate.hue)
            if (closesCircle) {
                minOf(previousDistance, hueDistance(candidate.hue, selected.first().hue))
            } else {
                previousDistance
            }
        } ?: break
        selected += next
        remaining -= next
    }
    return List(count) { index -> selected[index % selected.size].color }
}

private fun summarizeMonthlyExpensePieStatistics(
    statistics: List<CategoryExpenseStatistics>,
): List<CategoryExpenseStatistics> {
    if (statistics.size <= MonthlyExpensePieMaxSlices) return statistics
    val visibleStatistics = statistics.take(MonthlyExpensePieMaxSlices - 1)
    val remainingStatistics = statistics.drop(MonthlyExpensePieMaxSlices - 1)
    return visibleStatistics + CategoryExpenseStatistics(
        category = "其他${remainingStatistics.size}项",
        amount = remainingStatistics.sumOf { item -> item.amount },
    )
}

private val MonthlyExpenseCategoryBarColors = listOf(
    Color(0xFFD81B60),
    Color(0xFF00A896),
    Color(0xFFE76F51),
    Color(0xFF6A4C93),
    Color(0xFF43AA8B),
    Color(0xFFFF5D8F),
    Color(0xFFC77D00),
    Color(0xFF2A9D8F),
    Color(0xFFB5179E),
    Color(0xFFF9844A),
    Color(0xFF8D6E63),
    Color(0xFF90BE6D),
)
// private val MonthlyExpenseBarColor = Color(0xFF82A8C4)    //blmcc
// private val MonthlyIncomeBarColor = Color(0xFFB7BEC6)
private val MonthlyExpenseBarColor = Color(0xFFB7BEC6)
private val MonthlyIncomeBarColor = Color(0xFF82A8C4)
private const val GoldenRatio = 1.618f
private const val BookkeepingDialogWidthFraction = 0.8f
private val CalculatorKeyboardHeight = 340.dp
private val CalculatorDialogGap = 12.dp
// 340dp = (10dp arrow top gap + 8.5dp arrow) + 10dp arrow-to-keys gap
//     + 4 * 66.375dp key rows + 3 * 12dp key-row gaps + 10dp bottom padding.
private val CalculatorArrowAreaHeight = 18.5.dp
private val CalculatorArrowToKeysGap = 10.dp
private val CalculatorKeyHeight = 66.375.dp
private val CalculatorKeyVerticalGap = 12.dp
private val CalculatorDoubleKeyHeight = 144.75.dp
private val CalculatorKeyboardBottomPadding = 10.dp
private const val InputPanelTransitionDelayMillis = 220L
private val RejectCalculatorAmountInput = InputTransformation { revertAllChanges() }

private class InputWindowActions {
    var hideKeyboard: () -> Unit = {}
    var clearFocus: () -> Unit = {}
}

@Composable
private fun BindInputWindowActions(actions: InputWindowActions) {
    val focusManager = LocalFocusManager.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current
    val inputMethodManager = remember(view) {
        view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }
    SideEffect {
        actions.hideKeyboard = {
            softwareKeyboardController?.hide()
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
        actions.clearFocus = { focusManager.clearFocus(force = true) }
    }
    DisposableEffect(actions) {
        onDispose {
            actions.hideKeyboard = {}
            actions.clearFocus = {}
        }
    }
}

@Composable
private fun DisableDialogImeWindowPanning() {
    val view = LocalView.current
    val dialogWindow = remember(view) {
        (view.parent as? DialogWindowProvider)?.window
    }
    DisposableEffect(dialogWindow) {
        val previousSoftInputMode = dialogWindow?.attributes?.softInputMode
        dialogWindow?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        onDispose {
            previousSoftInputMode?.let { dialogWindow.setSoftInputMode(it) }
        }
    }
}

@Composable
private fun rememberCalculatorAmountFieldState(amount: String): TextFieldState {
    val fieldState = rememberTextFieldState(initialText = amount)
    LaunchedEffect(amount) {
        if (fieldState.text.toString() != amount) {
            fieldState.setTextAndPlaceCursorAtEnd(amount)
        }
    }
    LaunchedEffect(fieldState.text, fieldState.selection) {
        if (fieldState.selection != TextRange(fieldState.text.length)) {
            fieldState.edit { placeCursorAtEnd() }
        }
    }
    return fieldState
}

internal enum class TitleBarStyle(
    val storageKey: String,
    val title: String,
    val subtitle: String,
) {
    DESIGN_ONE(
        storageKey = "design_one",
        title = "经典",
        subtitle = "使用按钮手动控制菜单显隐",
    ),
    DESIGN_TWO(
        storageKey = "design_two",
        title = "沉浸",
        subtitle = "滑动查看账单时自动显隐菜单",
    ),
}

internal fun titleBarStyleFromStorage(storageKey: String?): TitleBarStyle =
    TitleBarStyle.entries.firstOrNull { it.storageKey == storageKey } ?: TitleBarStyle.DESIGN_ONE

private enum class AppPage {
    DETAILS,
    ADD_ENTRY,
    ADD_LEDGER,
    LEDGER_DETAILS,
    MONTHLY_STATISTICS,
    MONTHLY_STATISTICS_DETAIL,
    TITLE_BAR_STYLE,
    CATEGORIES,
}
private enum class DetailsSection { BILLS, LEDGER, STATISTICS, MORE }
private const val DetailsSectionTransitionDurationMillis = 300
private const val UncategorizedName = "未分类"
private const val UncategorizedIcon = "⭕"

data class Entry(
    val id: String,
    val kind: String,
    val category: String,
    val amount: Double,
    val date: Long,
    val note: String = "",
    val ledgerId: String? = null,
)
data class LedgerBook(
    val id: String,
    val symbol: String,
    val name: String,
    val date: Long,
    val note: String = "",
    val includeInBills: Boolean = true,
)
data class LedgerCategory(val name: String, val symbol: String)
internal data class EntryMonthGroup(val year: Int, val month: Int, val entries: List<Entry>)
internal data class EntryDayGroup(val year: Int, val month: Int, val day: Int, val entries: List<Entry>)
internal data class MonthlyBillStatistics(
    val year: Int,
    val month: Int,
    val expenseTotal: Double,
    val incomeTotal: Double,
) {
    val balance: Double get() = incomeTotal - expenseTotal
}
internal data class AnnualBillStatistics(
    val year: Int,
    val expenseTotal: Double,
    val incomeTotal: Double,
) {
    val balance: Double get() = incomeTotal - expenseTotal
}
internal data class CategoryExpenseStatistics(
    val category: String,
    val amount: Double,
)
internal enum class EntryFilterMode { ALL, MONTH }
internal data class EntryFilter(
    val mode: EntryFilterMode,
    val year: Int,
    val month: Int,
)
private data class CategoryEditTarget(val kind: String, val category: LedgerCategory)
private data class CategoryDeleteRequest(val target: CategoryEditTarget, val isFinalConfirmation: Boolean = false)

private val expenseIcons = listOf(
    "🍜", "🍚", "🍔", "☕", "🥤", "🍰", "🛒", "🛍️",
    "📦", "🚇", "🚌", "🚕", "✈️", "🚲", "🏠", "🏨",
    "💡", "💧", "🔥", "📱", "🌐", "💻", "🎮", "🎬",
    "🎵", "🏥", "💊", "🦷", "📚", "✏️", "👕", "👟",
    "💄", "✂️", "🐾", "🎁", "🏃", "🏋️", "🚗", "⛽",
    "🔧", "💳", "🧾", "❤️", "👶", "👨‍👩‍👧", "🌿", "📷",
    "🎨", "⭐", "🪙", "⭕",
)

private val incomeIcons = listOf(
    "💰", "💼", "🎁", "📈", "🏠", "🧧", "💵", "🪙",
    "🏦", "💳", "📊", "🧾", "⭐", "🤝", "🛍️", "⭕",
)

private val ledgerIcons = listOf(
    "ledger-icon-06", "ledger-icon-07", "ledger-icon-08", "ledger-icon-09",
    "ledger-icon-10", "ledger-icon-13", "ledger-icon-15", "ledger-icon-16",
    "ledger-icon-17", "ledger-icon-18", "ledger-icon-19", "ledger-icon-20",
    "ledger-icon-21",
)

private val ledgerIconResources = mapOf(
    "ledger-icon-01" to R.drawable.ledger_icon_01,
    "ledger-icon-02" to R.drawable.ledger_icon_02,
    "ledger-icon-03" to R.drawable.ledger_icon_03,
    "ledger-icon-04" to R.drawable.ledger_icon_04,
    "ledger-icon-05" to R.drawable.ledger_icon_05,
    "ledger-icon-06" to R.drawable.ledger_icon_06,
    "ledger-icon-07" to R.drawable.ledger_icon_07,
    "ledger-icon-08" to R.drawable.ledger_icon_08,
    "ledger-icon-09" to R.drawable.ledger_icon_09,
    "ledger-icon-10" to R.drawable.ledger_icon_10,
    "ledger-icon-11" to R.drawable.ledger_icon_11,
    "ledger-icon-12" to R.drawable.ledger_icon_12,
    "ledger-icon-13" to R.drawable.ledger_icon_13,
    "ledger-icon-14" to R.drawable.ledger_icon_14,
    "ledger-icon-15" to R.drawable.ledger_icon_15,
    "ledger-icon-16" to R.drawable.ledger_icon_16,
    "ledger-icon-17" to R.drawable.ledger_icon_17,
    "ledger-icon-18" to R.drawable.ledger_icon_18,
    "ledger-icon-19" to R.drawable.ledger_icon_19,
    "ledger-icon-20" to R.drawable.ledger_icon_20,
    "ledger-icon-21" to R.drawable.ledger_icon_21,
)

private val ledgerIconBadges = mapOf(
    "📒💰" to "💰",
    "📒🏠" to "🏠",
    "📒✈️" to "✈️",
    "📒🎓" to "🎓",
    "📒🐾" to "🐾",
    "📒🎁" to "🎁",
    "📒❤️" to "❤️",
)

private val legacyDefaultIcons = mapOf(
    "餐饮" to "🍜",
    "住房" to "🏠",
    "工资" to "💰",
    "副业" to "💼",
    UncategorizedName to UncategorizedIcon,
)

internal fun ensureUncategorizedCategory(categories: List<LedgerCategory>): List<LedgerCategory> {
    val uncategorized = categories.firstOrNull { it.name == UncategorizedName }
        ?: LedgerCategory(UncategorizedName, UncategorizedIcon)
    return categories.filterNot { it.name == UncategorizedName } + uncategorized
}

private class Store(context: Context) {
    private val p = context.getSharedPreferences("ledger", Context.MODE_PRIVATE)

    init {
        p.edit().remove("trashEntries").apply()
    }

    fun categories(kind: String): List<LedgerCategory> {
        val key = if (kind == "支出") "expenseCategories" else "incomeCategories"
        val defaults = if (kind == "支出") {
            listOf(LedgerCategory("餐饮", "🍜"), LedgerCategory("住房", "🏠"), LedgerCategory(UncategorizedName, UncategorizedIcon))
        } else {
            listOf(LedgerCategory("工资", "💰"), LedgerCategory("副业", "💼"), LedgerCategory(UncategorizedName, UncategorizedIcon))
        }
        val saved = p.getString(key, null)
            ?: if (kind == "支出") p.getString("categories", null) else null
            ?: return defaults
        val categories = runCatching {
            val a = JSONArray(saved)
            List(a.length()) { index ->
                when (val item = a.get(index)) {
                    is JSONObject -> {
                        val name = item.getString("name")
                        LedgerCategory(name, item.optString("symbol").ifBlank { defaultIcon(name) })
                    }
                    else -> {
                        val name = item.toString()
                        LedgerCategory(name, defaultIcon(name))
                    }
                }
            }
        }.getOrDefault(defaults)
        return ensureUncategorizedCategory(categories)
    }
    fun saveCategories(kind: String, value: List<LedgerCategory>) {
        val key = if (kind == "支出") "expenseCategories" else "incomeCategories"
        val categories = JSONArray()
        ensureUncategorizedCategory(value).forEach { category ->
            categories.put(JSONObject().apply {
                put("name", category.name)
                put("symbol", category.symbol)
            })
        }
        p.edit().putString(key, categories.toString()).apply()
    }
    private fun readEntries(key: String) = runCatching {
        val a = JSONArray(p.getString(key, null)!!)
        List(a.length()) {
            val o = a.getJSONObject(it)
            Entry(
                id = o.getString("id"),
                kind = o.optString("kind", "支出"),
                category = o.getString("category"),
                amount = o.getDouble("amount"),
                date = o.getLong("date"),
                note = o.optString("note", ""),
                ledgerId = if (o.isNull("ledgerId")) {
                    null
                } else {
                    o.optString("ledgerId").takeIf { value -> value.isNotBlank() }
                },
            )
        }
    }.getOrDefault(emptyList())

    private fun entryListJson(value: List<Entry>): String {
        val a = JSONArray()
        value.forEach { e ->
            a.put(JSONObject().apply {
                put("id", e.id)
                put("kind", e.kind)
                put("category", e.category)
                put("amount", e.amount)
                put("date", e.date)
                put("note", e.note)
                e.ledgerId?.let { put("ledgerId", it) }
            })
        }
        return a.toString()
    }

    private fun saveEntryList(key: String, value: List<Entry>) =
        p.edit().putString(key, entryListJson(value)).apply()

    fun entries() = readEntries("entries")
    fun saveEntries(value: List<Entry>) = saveEntryList("entries", value)
    fun ledgers(): List<LedgerBook> = runCatching {
        val values = JSONArray(p.getString("ledgers", null)!!)
        List(values.length()) { index ->
            val value = values.getJSONObject(index)
            LedgerBook(
                id = value.getString("id"),
                symbol = value.optString("symbol", UncategorizedIcon),
                name = value.getString("name"),
                date = value.getLong("date"),
                note = value.optString("note", ""),
                includeInBills = value.optBoolean("includeInBills", true),
            )
        }
    }.getOrDefault(emptyList())
    fun saveLedgers(value: List<LedgerBook>) {
        val values = JSONArray()
        value.forEach { ledger ->
            values.put(JSONObject().apply {
                put("id", ledger.id)
                put("symbol", ledger.symbol)
                put("name", ledger.name)
                put("date", ledger.date)
                put("note", ledger.note)
                put("includeInBills", ledger.includeInBills)
            })
        }
        p.edit().putString("ledgers", values.toString()).apply()
    }
    fun titleBarStyle() = titleBarStyleFromStorage(p.getString("titleBarStyle", null))
    fun saveTitleBarStyle(value: TitleBarStyle) = p.edit().putString("titleBarStyle", value.storageKey).apply()
}

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { LedgerRoot() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LedgerRoot() {
    val context = LocalContext.current
    val store = remember { Store(context.applicationContext) }
    var titleBarStyle by remember { mutableStateOf(store.titleBarStyle()) }
    val appTextSelectionColors = remember {
        TextSelectionColors(
            handleColor = Color.Transparent,
            backgroundColor = Ink.copy(alpha = 0.14f),
        )
    }
    Money_LogTheme {
        CompositionLocalProvider(
            LocalRippleConfiguration provides null,
            LocalTextSelectionColors provides appTextSelectionColors,
        ) {
            LedgerApp(
                store = store,
                selectedTitleBarStyle = titleBarStyle,
                onTitleBarStyleSelected = { selected ->
                    titleBarStyle = selected
                    store.saveTitleBarStyle(selected)
                },
            )
        }
    }
}

@Composable
private fun LedgerApp(
    store: Store,
    selectedTitleBarStyle: TitleBarStyle,
    onTitleBarStyleSelected: (TitleBarStyle) -> Unit,
) {
    val expenses = remember { mutableStateListOf<LedgerCategory>().apply { addAll(store.categories("支出")) } }
    val incomes = remember { mutableStateListOf<LedgerCategory>().apply { addAll(store.categories("收入")) } }
    val entries = remember { mutableStateListOf<Entry>().apply { addAll(store.entries()) } }
    val ledgers = remember { mutableStateListOf<LedgerBook>().apply { addAll(store.ledgers()) } }
    var page by remember { mutableStateOf(AppPage.DETAILS) }
    var addReturnPage by remember { mutableStateOf(AppPage.DETAILS) }
    var addEntryLedgerId by remember { mutableStateOf<String?>(null) }
    var addInitialDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var addLedgerInitialDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var addButtonXFraction by remember { mutableFloatStateOf(1f) }
    var addButtonYFraction by remember { mutableFloatStateOf(1f) }
    var selectedSection by remember { mutableStateOf(DetailsSection.BILLS) }
    var selectedLedgerId by remember { mutableStateOf<String?>(null) }
    var selectedMonthlyStatistics by remember { mutableStateOf<MonthlyBillStatistics?>(null) }
    var selectedStatisticsYear by remember {
        mutableIntStateOf(Calendar.getInstance()[Calendar.YEAR].coerceIn(2000, 2099))
    }
    val selectedLedger = ledgers.firstOrNull { it.id == selectedLedgerId }
    val mainBillEntries = entriesForMainBills(entries, ledgers)

    val updateStoredEntry: (Entry) -> Unit = { updatedEntry ->
        val updatedEntries = updateEntry(entries, updatedEntry)
        entries.clear()
        entries.addAll(updatedEntries)
        store.saveEntries(entries)
    }
    val deleteStoredEntry: (Entry) -> Unit = { entry ->
        val updatedEntries = deleteEntry(entries, entry.id)
        entries.clear()
        entries.addAll(updatedEntries)
        store.saveEntries(entries)
    }

    BackHandler(enabled = page != AppPage.DETAILS) {
        page = when (page) {
            AppPage.ADD_ENTRY -> addReturnPage
            AppPage.ADD_LEDGER -> AppPage.DETAILS
            AppPage.LEDGER_DETAILS -> AppPage.DETAILS
            AppPage.MONTHLY_STATISTICS -> AppPage.DETAILS
            AppPage.MONTHLY_STATISTICS_DETAIL -> AppPage.MONTHLY_STATISTICS
            AppPage.CATEGORIES -> AppPage.DETAILS
            AppPage.TITLE_BAR_STYLE -> AppPage.DETAILS
            AppPage.DETAILS -> AppPage.DETAILS
        }
    }

    Surface(Modifier.fillMaxSize(), color = Bg) {
        Box(Modifier.fillMaxSize()) {
            val showLedgerDetail = page == AppPage.LEDGER_DETAILS ||
                page == AppPage.ADD_ENTRY && addReturnPage == AppPage.LEDGER_DETAILS
            when {
                showLedgerDetail && selectedLedger != null -> LedgerDetailScreen(
                    ledger = selectedLedger,
                    entries = entries.filter { it.ledgerId == selectedLedger.id },
                    expenseCategories = expenses,
                    incomeCategories = incomes,
                    titleBarStyle = selectedTitleBarStyle,
                    onBack = { page = AppPage.DETAILS },
                    onAdd = {
                        addReturnPage = AppPage.LEDGER_DETAILS
                        addEntryLedgerId = selectedLedger.id
                        addInitialDate = initialDateForNewEntry(entries.filter { it.ledgerId == selectedLedger.id })
                        page = AppPage.ADD_ENTRY
                    },
                    addButtonXFraction = addButtonXFraction,
                    addButtonYFraction = addButtonYFraction,
                    onAddButtonPositionChange = { xFraction, yFraction ->
                        addButtonXFraction = xFraction
                        addButtonYFraction = yFraction
                    },
                    onUpdateEntry = updateStoredEntry,
                    onDeleteEntry = deleteStoredEntry,
                )
                page == AppPage.DETAILS || page == AppPage.ADD_ENTRY || page == AppPage.ADD_LEDGER -> DetailsScreen(
                    entries = mainBillEntries,
                    allEntries = entries,
                    ledgers = ledgers,
                    expenseCategories = expenses,
                    incomeCategories = incomes,
                    titleBarStyle = selectedTitleBarStyle,
                    selectedSection = selectedSection,
                    onSectionSelected = { selectedSection = it },
                    onAddEntry = {
                        addReturnPage = AppPage.DETAILS
                        addEntryLedgerId = null
                        addInitialDate = initialDateForNewEntry(mainBillEntries)
                        page = AppPage.ADD_ENTRY
                    },
                    onAddLedger = {
                        addLedgerInitialDate = System.currentTimeMillis()
                        page = AppPage.ADD_LEDGER
                    },
                    onOpenLedger = { ledger ->
                        selectedLedgerId = ledger.id
                        page = AppPage.LEDGER_DETAILS
                    },
                    onUpdateLedger = { updatedLedger ->
                        val ledgerIndex = ledgers.indexOfFirst { it.id == updatedLedger.id }
                        if (ledgerIndex >= 0) {
                            ledgers[ledgerIndex] = updatedLedger
                            store.saveLedgers(ledgers)
                        }
                    },
                    onManageCategories = { page = AppPage.CATEGORIES },
                    onTitleBarStyle = { page = AppPage.TITLE_BAR_STYLE },
                    onMonthlyStatistics = { page = AppPage.MONTHLY_STATISTICS },
                    addButtonXFraction = addButtonXFraction,
                    addButtonYFraction = addButtonYFraction,
                    onAddButtonPositionChange = { xFraction, yFraction ->
                        addButtonXFraction = xFraction
                        addButtonYFraction = yFraction
                    },
                    onUpdateEntry = updateStoredEntry,
                    onDeleteEntry = deleteStoredEntry,
                )
                page == AppPage.MONTHLY_STATISTICS -> MonthlyBillStatisticsScreen(
                    entries = mainBillEntries,
                    selectedYear = selectedStatisticsYear,
                    onYearSelected = { year -> selectedStatisticsYear = year },
                    onMonthSelected = { statistics ->
                        selectedMonthlyStatistics = statistics
                        page = AppPage.MONTHLY_STATISTICS_DETAIL
                    },
                )
                page == AppPage.MONTHLY_STATISTICS_DETAIL && selectedMonthlyStatistics != null ->
                    MonthlyBillStatisticsDetailScreen(
                        statistics = selectedMonthlyStatistics!!,
                        entries = mainBillEntries,
                        expenseCategories = expenses,
                    )
                page == AppPage.TITLE_BAR_STYLE -> TitleBarStyleScreen(
                    selectedStyle = selectedTitleBarStyle,
                    onStyleSelected = onTitleBarStyleSelected,
                )
                page == AppPage.CATEGORIES -> CategoryOverviewScreen(
                    expenseCategories = expenses,
                    incomeCategories = incomes,
                    onNewCategory = { kind, category ->
                        val target = if (kind == "支出") expenses else incomes
                        val updatedCategories = ensureUncategorizedCategory(target + category)
                        target.clear()
                        target.addAll(updatedCategories)
                        store.saveCategories(kind, updatedCategories)
                    },
                    onEditCategory = { kind, current, updated ->
                        val target = if (kind == "支出") expenses else incomes
                        val categoryIndex = target.indexOf(current)
                        if (categoryIndex >= 0) {
                            val safeUpdated = if (current.name == UncategorizedName) {
                                updated.copy(name = UncategorizedName)
                            } else updated
                            target[categoryIndex] = safeUpdated
                            store.saveCategories(kind, target)
                            val updatedEntries = updateEntriesCategory(entries, kind, current.name, safeUpdated.name)
                            entries.clear()
                            entries.addAll(updatedEntries)
                            store.saveEntries(entries)
                        }
                    },
                    onDeleteCategory = { kind, category ->
                        val target = if (kind == "支出") expenses else incomes
                        if (category.name != UncategorizedName && target.remove(category)) {
                            val updatedEntries = updateEntriesCategory(entries, kind, category.name, UncategorizedName)
                            entries.clear()
                            entries.addAll(updatedEntries)
                            store.saveCategories(kind, target)
                            store.saveEntries(entries)
                        }
                    },
                )
            }
            if (page == AppPage.ADD_ENTRY) {
                AddScreen(
                    expenseCategories = expenses,
                    incomeCategories = incomes,
                    initialDate = addInitialDate,
                    ledgerId = addEntryLedgerId,
                    onDismiss = { page = addReturnPage },
                    onNewCategory = { kind, category ->
                        val target = if (kind == "支出") expenses else incomes
                        val updatedCategories = ensureUncategorizedCategory(target + category)
                        target.clear()
                        target.addAll(updatedCategories)
                        store.saveCategories(kind, updatedCategories)
                    },
                    onEditCategoryIcon = { kind, current, updated ->
                        val target = if (kind == "支出") expenses else incomes
                        val categoryIndex = target.indexOf(current)
                        if (categoryIndex >= 0) {
                            target[categoryIndex] = updated.copy(name = current.name)
                            store.saveCategories(kind, target)
                        }
                    },
                    onSave = {
                        entries.add(it)
                        store.saveEntries(entries)
                        page = addReturnPage
                    },
                )
            }
            if (page == AppPage.ADD_LEDGER) {
                LedgerEditorDialog(
                    existingNames = ledgers.map { it.name },
                    initialDate = addLedgerInitialDate,
                    onDismiss = { page = AppPage.DETAILS },
                    onSave = { ledger ->
                        ledgers.add(ledger)
                        store.saveLedgers(ledgers)
                        selectedSection = DetailsSection.LEDGER
                        page = AppPage.DETAILS
                    },
                )
            }
        }
    }
}

@Composable
private fun DetailsScreen(
    entries: List<Entry>,
    allEntries: List<Entry>,
    ledgers: List<LedgerBook>,
    expenseCategories: List<LedgerCategory>,
    incomeCategories: List<LedgerCategory>,
    titleBarStyle: TitleBarStyle,
    selectedSection: DetailsSection,
    onSectionSelected: (DetailsSection) -> Unit,
    onAddEntry: () -> Unit,
    onAddLedger: () -> Unit,
    onOpenLedger: (LedgerBook) -> Unit,
    onUpdateLedger: (LedgerBook) -> Unit,
    onManageCategories: () -> Unit,
    onTitleBarStyle: () -> Unit,
    onMonthlyStatistics: () -> Unit,
    addButtonXFraction: Float,
    addButtonYFraction: Float,
    onAddButtonPositionChange: (Float, Float) -> Unit,
    onUpdateEntry: (Entry) -> Unit,
    onDeleteEntry: (Entry) -> Unit,
) {
    var actionEntry by remember { mutableStateOf<Entry?>(null) }
    var editingEntry by remember { mutableStateOf<Entry?>(null) }
    var deleteEntryRequest by remember { mutableStateOf<Entry?>(null) }
    var revealedEntryId by remember { mutableStateOf<String?>(null) }
    var showBillOptions by remember { mutableStateOf(false) }
    var showEntryFilter by remember { mutableStateOf(false) }
    val initialFilter = remember {
        val currentTime = System.currentTimeMillis()
        val latestDate = entries.maxOfOrNull { it.date } ?: currentTime
        val latestCalendar = Calendar.getInstance().apply { timeInMillis = latestDate }
        EntryFilter(
            mode = EntryFilterMode.ALL,
            year = latestCalendar[Calendar.YEAR],
            month = latestCalendar[Calendar.MONTH] + 1,
        )
    }
    var entryFilter by remember { mutableStateOf(initialFilter) }
    val visibleEntries = filterEntries(entries, entryFilter)
    val dayGroups = groupEntriesByDay(visibleEntries)
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val currentOnSectionSelected by rememberUpdatedState(onSectionSelected)
    val sectionPagerState = rememberPagerState(initialPage = selectedSection.ordinal) {
        DetailsSection.entries.size
    }
    val sectionPagerFlingBehavior = PagerDefaults.flingBehavior(
        state = sectionPagerState,
        snapAnimationSpec = tween(durationMillis = DetailsSectionTransitionDurationMillis),
    )
    val displayedSection = DetailsSection.entries[sectionPagerState.currentPage]
    val autoHeaderHeight = 85.dp
    val autoHeaderHeightPx = with(density) { autoHeaderHeight.toPx() }
    var autoHeaderOffsetPx by remember(titleBarStyle) { mutableFloatStateOf(0f) }
    val autoHeaderScrollConnection = remember(autoHeaderHeightPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val previousOffset = autoHeaderOffsetPx
                autoHeaderOffsetPx = (previousOffset + available.y).coerceIn(-autoHeaderHeightPx, 0f)
                return Offset(x = 0f, y = autoHeaderOffsetPx - previousOffset)
            }
        }
    }

    BackHandler(enabled = selectedSection != DetailsSection.BILLS) {
        onSectionSelected(DetailsSection.BILLS)
    }

    LaunchedEffect(selectedSection) {
        val targetPage = selectedSection.ordinal
        if (sectionPagerState.settledPage != targetPage) {
            sectionPagerState.animateScrollToPage(
                page = targetPage,
                animationSpec = tween(durationMillis = DetailsSectionTransitionDurationMillis),
            )
        }
    }

    LaunchedEffect(sectionPagerState.settledPage) {
        val settledSection = DetailsSection.entries[sectionPagerState.settledPage]
        if (settledSection != selectedSection) {
            currentOnSectionSelected(settledSection)
            revealedEntryId = null
        }
    }

    LaunchedEffect(entryFilter, titleBarStyle, selectedSection) {
        if (selectedSection == DetailsSection.BILLS) {
            listState.scrollToItem(0)
        }
        autoHeaderOffsetPx = 0f
    }
    val fixedPrimaryDark = colorResource(R.color.app_primary_dark)
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val addButtonSize = 52.dp
        val edgePadding = 20.dp
        val addButtonSizePx = with(density) { addButtonSize.toPx() }
        val edgePaddingPx = with(density) { edgePadding.toPx() }
        val minimumX = edgePaddingPx
        val maximumX = (constraints.maxWidth - addButtonSizePx - edgePaddingPx).coerceAtLeast(minimumX)
        val minimumY = WindowInsets.statusBars.getTop(density) + edgePaddingPx
        val maximumY = (
            constraints.maxHeight -
                WindowInsets.navigationBars.getBottom(density) -
                addButtonSizePx -
                edgePaddingPx
            ).coerceAtLeast(minimumY)
        val addButtonX = minimumX + (maximumX - minimumX) * addButtonXFraction.coerceIn(0f, 1f)
        val addButtonY = minimumY + (maximumY - minimumY) * addButtonYFraction.coerceIn(0f, 1f)
        val currentAddButtonX by rememberUpdatedState(addButtonX)
        val currentAddButtonY by rememberUpdatedState(addButtonY)

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .then(
                    if (titleBarStyle == TitleBarStyle.DESIGN_TWO) {
                        Modifier.nestedScroll(autoHeaderScrollConnection)
                    } else {
                        Modifier
                    },
                ),
        ) {
            when (titleBarStyle) {
                TitleBarStyle.DESIGN_ONE -> DetailsFeatureBar(
                    selectedSection = displayedSection,
                    onSectionSelected = { section ->
                        onSectionSelected(section)
                        revealedEntryId = null
                    },
                    onBillLongClick = { showBillOptions = true },
                )
                TitleBarStyle.DESIGN_TWO -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(with(density) { (autoHeaderHeightPx + autoHeaderOffsetPx).toDp() })
                        .clipToBounds(),
                ) {
                    DetailsAutoCollapsingFeatureBar(
                        modifier = Modifier.offset {
                            IntOffset(x = 0, y = autoHeaderOffsetPx.roundToInt())
                        },
                        selectedSection = displayedSection,
                        onSectionSelected = { section ->
                            onSectionSelected(section)
                            revealedEntryId = null
                        },
                        onBillLongClick = { showBillOptions = true },
                    )
                }
            }
            HorizontalPager(
                state = sectionPagerState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                beyondViewportPageCount = 1,
                flingBehavior = sectionPagerFlingBehavior,
            ) { pageIndex ->
                when (DetailsSection.entries[pageIndex]) {
                    DetailsSection.STATISTICS -> StatisticsContent(
                        modifier = Modifier.fillMaxSize(),
                        onMonthlyStatistics = onMonthlyStatistics,
                    )
                    DetailsSection.MORE -> MoreContent(
                        modifier = Modifier.fillMaxSize(),
                        onManageCategories = onManageCategories,
                        onTitleBarStyle = onTitleBarStyle,
                    )
                    DetailsSection.LEDGER -> LedgerListContent(
                        modifier = Modifier.fillMaxSize(),
                        ledgers = ledgers,
                        onOpenLedger = onOpenLedger,
                        onUpdateLedger = onUpdateLedger,
                    )
                    DetailsSection.BILLS -> when {
                        entries.isEmpty() -> Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🧾", fontSize = 52.sp)
                                Spacer(Modifier.height(8.dp))
                                Text("还没有账目", fontSize = 18.sp, color = Muted)
                                Text("点击右下角“＋”添加第一笔记录", color = Color(0xFFAAADB3))
                            }
                        }
                        visibleEntries.isEmpty() -> Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🗓️", fontSize = 48.sp)
                                Spacer(Modifier.height(8.dp))
                                Text("当前筛选暂无账单", fontSize = 18.sp, color = Muted)
                                Text("长按顶部“账单”调整筛选", color = Color(0xFFAAADB3))
                            }
                        }
                        else -> LazyColumn(
                            Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(top = 12.dp, bottom = 92.dp),
                        ) {
                            dayGroups.forEach { dayGroup ->
                                item(key = "day-${dayGroup.year}-${dayGroup.month}-${dayGroup.day}") {
                                    EntryDayHeader(dayGroup.month, dayGroup.day)
                                }
                                items(dayGroup.entries, key = { it.id }) { entry ->
                                    val categories = if (entry.kind == "收入") {
                                        incomeCategories
                                    } else {
                                        expenseCategories
                                    }
                                    val symbol = categories.firstOrNull { it.name == entry.category }?.symbol
                                        ?: defaultIcon(entry.category)
                                    EntryCard(
                                        entry = entry,
                                        symbol = symbol,
                                        revealed = revealedEntryId == entry.id,
                                        onRevealChange = { reveal ->
                                            revealedEntryId = if (reveal) entry.id else null
                                        },
                                        onLongClick = {
                                            revealedEntryId = null
                                            actionEntry = entry
                                        },
                                        onDeleteClick = { deleteEntryRequest = entry },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if (displayedSection == DetailsSection.BILLS || displayedSection == DetailsSection.LEDGER) {
            FloatingActionButton(
                onClick = if (displayedSection == DetailsSection.BILLS) onAddEntry else onAddLedger,
                modifier = Modifier
                    .offset { IntOffset(addButtonX.roundToInt(), addButtonY.roundToInt()) }
                    .size(addButtonSize)
                    .pointerInput(minimumX, maximumX, minimumY, maximumY) {
                        var dragX = currentAddButtonX
                        var dragY = currentAddButtonY
                        detectDragGestures(
                            onDragStart = {
                                dragX = currentAddButtonX
                                dragY = currentAddButtonY
                            },
                            onDrag = { _, dragAmount ->
                                dragX = (dragX + dragAmount.x).coerceIn(minimumX, maximumX)
                                dragY = (dragY + dragAmount.y).coerceIn(minimumY, maximumY)
                                val xFraction = if (maximumX == minimumX) 0f else {
                                    (dragX - minimumX) / (maximumX - minimumX)
                                }
                                val yFraction = if (maximumY == minimumY) 0f else {
                                    (dragY - minimumY) / (maximumY - minimumY)
                                }
                                onAddButtonPositionChange(xFraction, yFraction)
                            },
                        )
                    },
                shape = RoundedCornerShape(15.dp),
                containerColor = fixedPrimaryDark,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 5.dp,
                    pressedElevation = 7.dp,
                ),
            ) {
                Text("+", fontSize = 34.sp, fontWeight = FontWeight.Normal)
            }
        }
    }
    actionEntry?.let { entry ->
        EntryActionDialog(
            entry = entry,
            dismiss = { actionEntry = null },
            onEdit = {
                actionEntry = null
                editingEntry = entry
            },
        )
    }
    editingEntry?.let { entry ->
        val categories = if (entry.kind == "收入") incomeCategories else expenseCategories
        EntryEditDialog(
            entry = entry,
            categories = categories,
            dismiss = { editingEntry = null },
            confirm = { updatedEntry ->
                onUpdateEntry(updatedEntry)
                editingEntry = null
            },
        )
    }
    deleteEntryRequest?.let { entry ->
        DeleteEntryDialog(
            entry = entry,
            dismiss = {
                deleteEntryRequest = null
                revealedEntryId = null
            },
            confirm = {
                onDeleteEntry(entry)
                deleteEntryRequest = null
                revealedEntryId = null
            },
        )
    }
    if (showBillOptions) {
        BillOptionsDialog(
            entryFilter = entryFilter,
            dismiss = { showBillOptions = false },
            onFilterView = {
                showBillOptions = false
                showEntryFilter = true
            },
        )
    }
    if (showEntryFilter) {
        EntryFilterDialog(
            initialFilter = entryFilter,
            dismiss = { showEntryFilter = false },
            onApply = { updatedFilter ->
                entryFilter = updatedFilter
                showEntryFilter = false
                revealedEntryId = null
            },
        )
    }
}

@Composable
private fun LedgerListContent(
    ledgers: List<LedgerBook>,
    onOpenLedger: (LedgerBook) -> Unit,
    onUpdateLedger: (LedgerBook) -> Unit,
    modifier: Modifier = Modifier,
) {
    var actionLedger by remember { mutableStateOf<LedgerBook?>(null) }
    var editingLedger by remember { mutableStateOf<LedgerBook?>(null) }
    var mergingLedger by remember { mutableStateOf<LedgerBook?>(null) }
    val ledgerDayGroups = ledgers.sortedByDescending { it.date }.groupBy { ledger ->
        Calendar.getInstance().apply { timeInMillis = ledger.date }.let { calendar ->
            Triple(
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH] + 1,
                calendar[Calendar.DAY_OF_MONTH],
            )
        }
    }
    if (ledgers.isEmpty()) {
        Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📒", fontSize = 52.sp)
                Spacer(Modifier.height(8.dp))
                Text("还没有账本", fontSize = 18.sp, color = Muted)
                Text("点击右下角“＋”添加第一个账本", color = Color(0xFFAAADB3))
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ledgerDayGroups.forEach { (date, dayLedgers) ->
                item(key = "ledger-${date.first}-${date.second}-${date.third}") {
                    EntryDayHeader(date.second, date.third)
                }
                items(dayLedgers, key = { it.id }) { ledger ->
                    LedgerCard(
                        ledger = ledger,
                        onClick = { onOpenLedger(ledger) },
                        onLongClick = { actionLedger = ledger },
                    )
                }
            }
        }
    }
    actionLedger?.let { ledger ->
        LedgerActionDialog(
            ledger = ledger,
            dismiss = { actionLedger = null },
            onEdit = {
                actionLedger = null
                editingLedger = ledger
            },
            onMergeSelection = {
                actionLedger = null
                mergingLedger = ledger
            },
        )
    }
    editingLedger?.let { ledger ->
        LedgerEditorDialog(
            existingNames = ledgers.filterNot { it.id == ledger.id }.map { it.name },
            initialDate = ledger.date,
            initial = ledger,
            onDismiss = { editingLedger = null },
            onSave = { updatedLedger ->
                onUpdateLedger(updatedLedger)
                editingLedger = null
            },
        )
    }
    mergingLedger?.let { ledger ->
        LedgerMergeDialog(
            ledger = ledger,
            dismiss = { mergingLedger = null },
            confirm = { updatedLedger ->
                onUpdateLedger(updatedLedger)
                mergingLedger = null
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LedgerCard(
    ledger: LedgerBook,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
            onLongClick = onLongClick,
        ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(46.dp).background(Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                LedgerSymbol(
                    symbol = ledger.symbol,
                    containerSize = 40.dp,
                    baseFontSize = 28.sp,
                    badgeSize = 18.dp,
                    badgeFontSize = 14.sp,
                )
            }
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(
                    ledger.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (ledger.note.isNotBlank()) {
                    Text(
                        ledger.note,
                        fontSize = 12.sp,
                        color = NoteSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Text("›", fontSize = 28.sp, color = Muted)
        }
    }
}

@Composable
private fun LedgerActionDialog(
    ledger: LedgerBook,
    dismiss: () -> Unit,
    onEdit: () -> Unit,
    onMergeSelection: () -> Unit,
) {
    val fixedPrimaryDark = colorResource(R.color.app_primary_dark)
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(ledger.name) },
        text = {
            Column {
                CategoryActionItem(
                    symbol = "",
                    title = "修改账本",
                    subtitle = "修改图标、名称、日期和备注",
                    iconResource = R.drawable.ic_edit_ledger_color,
                    iconBackgroundColor = Color.Transparent,
                    preserveIconColors = true,
                    expandIconToBackground = true,
                    onClick = onEdit,
                )
                Spacer(Modifier.height(10.dp))
                CategoryActionItem(
                    symbol = "",
                    title = "账单合入选择",
                    subtitle = if (ledger.includeInBills) "当前：合入总账单" else "当前：仅在本账本显示",
                    iconResource = R.drawable.ic_merge_bills_color,
                    iconBackgroundColor = Color.Transparent,
                    preserveIconColors = true,
                    expandIconToBackground = true,
                    onClick = onMergeSelection,
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = dismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = fixedPrimaryDark),
            ) { Text("取消") }
        },
    )
}

@Composable
private fun LedgerMergeDialog(
    ledger: LedgerBook,
    dismiss: () -> Unit,
    confirm: (LedgerBook) -> Unit,
) {
    val fixedPrimaryDark = colorResource(R.color.app_primary_dark)
    var includeInBills by remember(ledger.id, ledger.includeInBills) {
        mutableStateOf(ledger.includeInBills)
    }
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("账单合入选择") },
        text = {
            Column {
                LedgerMergeOption(
                    iconResource = R.drawable.ic_merge_bills_color,
                    title = "合入总账单",
                    subtitle = "账本内账单会同时显示在总账单中",
                    selected = includeInBills,
                    onClick = { includeInBills = true },
                )
                Spacer(Modifier.height(8.dp))
                LedgerMergeOption(
                    iconResource = R.drawable.ic_separate_bills_color,
                    title = "仅在本账本显示",
                    subtitle = "账本内账单不会显示在总账单中",
                    selected = !includeInBills,
                    onClick = { includeInBills = false },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { confirm(ledger.copy(includeInBills = includeInBills)) },
                enabled = includeInBills != ledger.includeInBills,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = fixedPrimaryDark,
                    disabledContentColor = Muted.copy(alpha = 0.38f),
                ),
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(
                onClick = dismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = fixedPrimaryDark),
            ) { Text("取消") }
        },
    )
}

@Composable
private fun LedgerMergeOption(
    iconResource: Int,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) SelectionBlueContainer else Color.White, RoundedCornerShape(14.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(42.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconResource),
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = Color.Unspecified,
            )
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Ink)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 12.sp, color = Muted)
        }
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Text(
                    text = "✓",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = SelectionBlue,
                )
            }
        }
    }
}

@Composable
private fun LedgerDetailScreen(
    ledger: LedgerBook,
    entries: List<Entry>,
    expenseCategories: List<LedgerCategory>,
    incomeCategories: List<LedgerCategory>,
    titleBarStyle: TitleBarStyle,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    addButtonXFraction: Float,
    addButtonYFraction: Float,
    onAddButtonPositionChange: (Float, Float) -> Unit,
    onUpdateEntry: (Entry) -> Unit,
    onDeleteEntry: (Entry) -> Unit,
) {
    var actionEntry by remember { mutableStateOf<Entry?>(null) }
    var editingEntry by remember { mutableStateOf<Entry?>(null) }
    var deleteEntryRequest by remember { mutableStateOf<Entry?>(null) }
    var revealedEntryId by remember { mutableStateOf<String?>(null) }
    var showBillOptions by remember { mutableStateOf(false) }
    var showEntryFilter by remember { mutableStateOf(false) }
    var selectedSection by remember(ledger.id) { mutableStateOf(DetailsSection.BILLS) }
    val ledgerSections = remember { listOf(DetailsSection.BILLS, DetailsSection.STATISTICS) }
    val initialFilter = remember(ledger.id) {
        val currentTime = System.currentTimeMillis()
        val latestDate = entries.maxOfOrNull { it.date } ?: currentTime
        val latestCalendar = Calendar.getInstance().apply { timeInMillis = latestDate }
        EntryFilter(
            mode = EntryFilterMode.ALL,
            year = latestCalendar[Calendar.YEAR],
            month = latestCalendar[Calendar.MONTH] + 1,
        )
    }
    var entryFilter by remember(ledger.id) { mutableStateOf(initialFilter) }
    val visibleEntries = filterEntries(entries, entryFilter)
    val dayGroups = groupEntriesByDay(visibleEntries)
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val autoHeaderHeight = 85.dp
    val autoHeaderHeightPx = with(density) { autoHeaderHeight.toPx() }
    var autoHeaderOffsetPx by remember(titleBarStyle, ledger.id) { mutableFloatStateOf(0f) }
    val autoHeaderScrollConnection = remember(autoHeaderHeightPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val previousOffset = autoHeaderOffsetPx
                autoHeaderOffsetPx = (previousOffset + available.y).coerceIn(-autoHeaderHeightPx, 0f)
                return Offset(x = 0f, y = autoHeaderOffsetPx - previousOffset)
            }
        }
    }
    val fixedPrimaryDark = colorResource(R.color.app_primary_dark)

    BackHandler {
        if (selectedSection == DetailsSection.BILLS) {
            onBack()
        } else {
            selectedSection = DetailsSection.BILLS
        }
    }

    LaunchedEffect(entryFilter, titleBarStyle, selectedSection, ledger.id) {
        if (selectedSection == DetailsSection.BILLS) {
            listState.scrollToItem(0)
        }
        autoHeaderOffsetPx = 0f
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val addButtonSize = 52.dp
        val edgePadding = 20.dp
        val addButtonSizePx = with(density) { addButtonSize.toPx() }
        val edgePaddingPx = with(density) { edgePadding.toPx() }
        val minimumX = edgePaddingPx
        val maximumX = (constraints.maxWidth - addButtonSizePx - edgePaddingPx).coerceAtLeast(minimumX)
        val minimumY = WindowInsets.statusBars.getTop(density) + edgePaddingPx
        val maximumY = (
            constraints.maxHeight -
                WindowInsets.navigationBars.getBottom(density) -
                addButtonSizePx -
                edgePaddingPx
            ).coerceAtLeast(minimumY)
        val addButtonX = minimumX + (maximumX - minimumX) * addButtonXFraction.coerceIn(0f, 1f)
        val addButtonY = minimumY + (maximumY - minimumY) * addButtonYFraction.coerceIn(0f, 1f)
        val currentAddButtonX by rememberUpdatedState(addButtonX)
        val currentAddButtonY by rememberUpdatedState(addButtonY)

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .then(
                    if (titleBarStyle == TitleBarStyle.DESIGN_TWO) {
                        Modifier.nestedScroll(autoHeaderScrollConnection)
                    } else {
                        Modifier
                    },
                ),
        ) {
            when (titleBarStyle) {
                TitleBarStyle.DESIGN_ONE -> DetailsFeatureBar(
                    selectedSection = selectedSection,
                    onSectionSelected = { section ->
                        selectedSection = section
                        revealedEntryId = null
                    },
                    onBillLongClick = { showBillOptions = true },
                    sections = ledgerSections,
                )
                TitleBarStyle.DESIGN_TWO -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(with(density) { (autoHeaderHeightPx + autoHeaderOffsetPx).toDp() })
                        .clipToBounds(),
                ) {
                    DetailsAutoCollapsingFeatureBar(
                        modifier = Modifier.offset {
                            IntOffset(x = 0, y = autoHeaderOffsetPx.roundToInt())
                        },
                        selectedSection = selectedSection,
                        onSectionSelected = { section ->
                            selectedSection = section
                            revealedEntryId = null
                        },
                        onBillLongClick = { showBillOptions = true },
                        sections = ledgerSections,
                    )
                }
            }
            if (selectedSection == DetailsSection.STATISTICS) {
                Spacer(Modifier.weight(1f))
            } else if (entries.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🧾", fontSize = 52.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("账本中还没有账单", fontSize = 18.sp, color = Muted)
                        Text("点击右下角“＋”添加第一笔记录", color = Color(0xFFAAADB3))
                    }
                }
            } else if (visibleEntries.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🗓️", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("当前筛选暂无账单", fontSize = 18.sp, color = Muted)
                        Text("长按顶部“账单”调整筛选", color = Color(0xFFAAADB3))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 92.dp),
                ) {
                    dayGroups.forEach { dayGroup ->
                        item(key = "ledger-day-${dayGroup.year}-${dayGroup.month}-${dayGroup.day}") {
                            EntryDayHeader(dayGroup.month, dayGroup.day)
                        }
                        items(dayGroup.entries, key = { it.id }) { entry ->
                            val categories = if (entry.kind == "收入") incomeCategories else expenseCategories
                            val symbol = categories.firstOrNull { it.name == entry.category }?.symbol
                                ?: defaultIcon(entry.category)
                            EntryCard(
                                entry = entry,
                                symbol = symbol,
                                revealed = revealedEntryId == entry.id,
                                onRevealChange = { reveal -> revealedEntryId = if (reveal) entry.id else null },
                                onLongClick = {
                                    revealedEntryId = null
                                    actionEntry = entry
                                },
                                onDeleteClick = { deleteEntryRequest = entry },
                            )
                        }
                    }
                }
            }
        }
        if (selectedSection == DetailsSection.BILLS) {
            FloatingActionButton(
                onClick = onAdd,
                modifier = Modifier
                .offset { IntOffset(addButtonX.roundToInt(), addButtonY.roundToInt()) }
                .size(addButtonSize)
                .pointerInput(minimumX, maximumX, minimumY, maximumY) {
                    var dragX = currentAddButtonX
                    var dragY = currentAddButtonY
                    detectDragGestures(
                        onDragStart = {
                            dragX = currentAddButtonX
                            dragY = currentAddButtonY
                        },
                        onDrag = { _, dragAmount ->
                            dragX = (dragX + dragAmount.x).coerceIn(minimumX, maximumX)
                            dragY = (dragY + dragAmount.y).coerceIn(minimumY, maximumY)
                            val xFraction = if (maximumX == minimumX) 0f else {
                                (dragX - minimumX) / (maximumX - minimumX)
                            }
                            val yFraction = if (maximumY == minimumY) 0f else {
                                (dragY - minimumY) / (maximumY - minimumY)
                            }
                            onAddButtonPositionChange(xFraction, yFraction)
                        },
                    )
                },
                shape = RoundedCornerShape(15.dp),
                containerColor = fixedPrimaryDark,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 5.dp,
                    pressedElevation = 7.dp,
                ),
            ) {
                Text("+", fontSize = 34.sp, fontWeight = FontWeight.Normal)
            }
        }
    }

    actionEntry?.let { entry ->
        EntryActionDialog(
            entry = entry,
            dismiss = { actionEntry = null },
            onEdit = {
                actionEntry = null
                editingEntry = entry
            },
        )
    }
    editingEntry?.let { entry ->
        val categories = if (entry.kind == "收入") incomeCategories else expenseCategories
        EntryEditDialog(
            entry = entry,
            categories = categories,
            dismiss = { editingEntry = null },
            confirm = { updatedEntry ->
                onUpdateEntry(updatedEntry)
                editingEntry = null
            },
        )
    }
    deleteEntryRequest?.let { entry ->
        DeleteEntryDialog(
            entry = entry,
            dismiss = {
                deleteEntryRequest = null
                revealedEntryId = null
            },
            confirm = {
                onDeleteEntry(entry)
                deleteEntryRequest = null
                revealedEntryId = null
            },
        )
    }
    if (showBillOptions) {
        BillOptionsDialog(
            entryFilter = entryFilter,
            dismiss = { showBillOptions = false },
            onFilterView = {
                showBillOptions = false
                showEntryFilter = true
            },
        )
    }
    if (showEntryFilter) {
        EntryFilterDialog(
            initialFilter = entryFilter,
            dismiss = { showEntryFilter = false },
            onApply = { updatedFilter ->
                entryFilter = updatedFilter
                showEntryFilter = false
                revealedEntryId = null
            },
        )
    }
}

@Composable
private fun LedgerEditorDialog(
    existingNames: List<String>,
    initialDate: Long,
    initial: LedgerBook? = null,
    onDismiss: () -> Unit,
    onSave: (LedgerBook) -> Unit,
) {
    val density = LocalDensity.current
    val inputWindowActions = remember { InputWindowActions() }
    val editorScrollState = rememberScrollState()
    val iconGridState = rememberLazyGridState()
    var selectedIcon by remember(initial?.id) {
        mutableStateOf(
            initial?.symbol,
        )
    }
    var name by remember(initial?.id) { mutableStateOf(initial?.name.orEmpty()) }
    var date by remember(initial?.id, initialDate) { mutableLongStateOf(initial?.date ?: initialDate) }
    var note by remember(initial?.id) { mutableStateOf(initial?.note.orEmpty()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var nameHasFocus by remember { mutableStateOf(false) }
    var noteHasFocus by remember { mutableStateOf(false) }
    var retainInputPanelPlacement by remember { mutableStateOf(false) }
    val dateInteractionSource = remember { MutableInteractionSource() }
    val dismissInputInteraction = remember { MutableInteractionSource() }
    val fixedPrimaryDark = colorResource(R.color.app_primary_dark)
    val imeBottom = WindowInsets.ime.getBottom(density)
    val navigationBarBottom = WindowInsets.navigationBars.getBottom(density)
    val isSystemKeyboardVisible = imeBottom > navigationBarBottom
    val inputPanelPlacementActive = isSystemKeyboardVisible || retainInputPanelPlacement
    val matchedInputPanelHeight = CalculatorKeyboardHeight
    val fieldBorder = Color(0xFF7A7D83)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Ink,
        unfocusedTextColor = Ink,
        cursorColor = Ink,
        focusedBorderColor = fieldBorder,
        unfocusedBorderColor = fieldBorder,
        focusedLabelColor = Muted,
        unfocusedLabelColor = Muted,
    )
    val normalizedName = name.trim()
    val changed = initial == null || selectedIcon != initial.symbol || normalizedName != initial.name ||
        date != initial.date || note.trim() != initial.note
    val canSave = selectedIcon != null && normalizedName.isNotEmpty() &&
        normalizedName !in existingNames && changed
    val collapseInputPanel = {
        inputWindowActions.hideKeyboard()
        inputWindowActions.clearFocus()
    }

    LaunchedEffect(initial?.id) {
        val selectedIndex = initial?.symbol?.let(ledgerIcons::indexOf) ?: -1
        if (selectedIndex >= 0) {
            iconGridState.scrollToItem(selectedIndex)
        }
    }

    LaunchedEffect(isSystemKeyboardVisible) {
        if (isSystemKeyboardVisible) {
            retainInputPanelPlacement = true
        } else {
            delay(InputPanelTransitionDelayMillis)
            retainInputPanelPlacement = false
        }
    }

    LaunchedEffect(
        nameHasFocus,
        noteHasFocus,
        inputPanelPlacementActive,
        editorScrollState.maxValue,
    ) {
        if ((nameHasFocus || noteHasFocus) && inputPanelPlacementActive) {
            withFrameNanos { }
            editorScrollState.scrollTo(editorScrollState.maxValue)
            withFrameNanos { }
            editorScrollState.scrollTo(editorScrollState.maxValue)
        }
    }

    Dialog(
        onDismissRequest = {
            if (nameHasFocus || noteHasFocus || inputPanelPlacementActive) {
                collapseInputPanel()
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BindInputWindowActions(inputWindowActions)
        DisableDialogImeWindowPanning()
        Box(
            Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = dismissInputInteraction,
                    indication = null,
                    onClick = collapseInputPanel,
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = 12.dp,
                        bottom = if (inputPanelPlacementActive) {
                            matchedInputPanelHeight + CalculatorDialogGap
                        } else {
                            12.dp
                        },
                    ),
                contentAlignment = if (inputPanelPlacementActive) {
                    Alignment.BottomCenter
                } else {
                    Alignment.Center
                },
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(BookkeepingDialogWidthFraction),
                    shape = RoundedCornerShape(28.dp),
                    color = Bg,
                    shadowElevation = 10.dp,
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Text(
                            if (initial == null) "新增账本" else "修改账本",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = Ink,
                        )
                        Spacer(Modifier.height(18.dp))
                        Column(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .verticalScroll(editorScrollState),
                        ) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                modifier = Modifier.fillMaxWidth().heightIn(max = 210.dp),
                                state = iconGridState,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(ledgerIcons.size) { index ->
                                    val symbol = ledgerIcons[index]
                                    LedgerIconChoice(symbol, selectedIcon == symbol) {
                                        selectedIcon = symbol
                                    }
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            OutlinedTextField(
                                value = name,
                                onValueChange = { if (it.length <= 20) name = it },
                                modifier = Modifier.fillMaxWidth().onFocusChanged {
                                    nameHasFocus = it.isFocused
                                },
                                label = { Text("账本名称") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = fieldColors,
                            )
                            Spacer(Modifier.height(14.dp))
                            Box(
                                Modifier.fillMaxWidth().clickable(
                                    interactionSource = dateInteractionSource,
                                    indication = null,
                                ) {
                                    collapseInputPanel()
                                    showDatePicker = true
                                },
                            ) {
                                OutlinedTextField(
                                    value = day(date),
                                    onValueChange = {},
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = false,
                                    label = { Text("日期") },
                                    trailingIcon = { Text("选择") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = Ink,
                                        disabledBorderColor = Color(0xFFB9BBC0),
                                        disabledLabelColor = Muted,
                                        disabledTrailingIconColor = Ink,
                                        disabledContainerColor = Color.Transparent,
                                    ),
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            OutlinedTextField(
                                value = note,
                                onValueChange = { if (it.length <= 100) note = it },
                                modifier = Modifier.fillMaxWidth().onFocusChanged {
                                    noteHasFocus = it.isFocused
                                },
                                label = { Text("备注信息") },
                                maxLines = 3,
                                shape = RoundedCornerShape(14.dp),
                                colors = fieldColors,
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(
                                onClick = onDismiss,
                                colors = ButtonDefaults.textButtonColors(contentColor = fixedPrimaryDark),
                            ) { Text("取消") }
                            TextButton(
                                onClick = {
                                    onSave(
                                        if (initial == null) {
                                            LedgerBook(
                                                id = UUID.randomUUID().toString(),
                                                symbol = selectedIcon!!,
                                                name = normalizedName,
                                                date = date,
                                                note = note.trim(),
                                            )
                                        } else {
                                            initial.copy(
                                                symbol = selectedIcon!!,
                                                name = normalizedName,
                                                date = date,
                                                note = note.trim(),
                                            )
                                        },
                                    )
                                },
                                enabled = canSave,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = fixedPrimaryDark,
                                    disabledContentColor = Muted.copy(alpha = 0.38f),
                                ),
                            ) { Text(if (initial == null) "添加" else "保存") }
                        }
                    }
                }
            }
        }
    }
    if (showDatePicker) {
        MoneyLogDatePickerDialog(
            initialMillis = date,
            onDismiss = { showDatePicker = false },
            onDateSelected = {
                date = it
                showDatePicker = false
            },
        )
    }
}

@Composable
private fun EntryDayHeader(month: Int, day: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("${month}月${day}日", color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.width(10.dp))
        HorizontalDivider(Modifier.weight(1f), color = Color(0xFFE8E9EB))
    }
}

private fun detailsSectionIcon(section: DetailsSection) = when (section) {
    DetailsSection.BILLS -> R.drawable.ic_bill_outline
    DetailsSection.LEDGER -> R.drawable.ic_ledger_outline
    DetailsSection.STATISTICS -> R.drawable.ic_statistics_outline
    DetailsSection.MORE -> R.drawable.ic_more_outline
}

private fun detailsSectionLabel(section: DetailsSection) = when (section) {
    DetailsSection.BILLS -> "账单"
    DetailsSection.LEDGER -> "账本"
    DetailsSection.STATISTICS -> "统计"
    DetailsSection.MORE -> "更多"
}

@Composable
private fun DetailsFeatureBar(
    selectedSection: DetailsSection,
    onSectionSelected: (DetailsSection) -> Unit,
    onBillLongClick: () -> Unit,
    sections: List<DetailsSection> = DetailsSection.entries,
) {
    var expanded by remember { mutableStateOf(true) }
    val dragThreshold = with(LocalDensity.current) { 24.dp.toPx() }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 0f else 180f,
        animationSpec = tween(durationMillis = 200),
        label = "detailsFeatureArrowRotation",
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, end = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            androidx.compose.animation.AnimatedVisibility(
                modifier = Modifier.fillMaxWidth(),
                visible = expanded,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 200),
                    expandFrom = Alignment.Top,
                ) + fadeIn(animationSpec = tween(durationMillis = 160)),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 200),
                    shrinkTowards = Alignment.Top,
                ) + fadeOut(animationSpec = tween(durationMillis = 140)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    sections.forEach { section ->
                        DetailsFeatureItem(
                            Modifier.weight(1f),
                            detailsSectionIcon(section),
                            detailsSectionLabel(section),
                            selected = selectedSection == section,
                            onClick = { onSectionSelected(section) },
                            onLongClick = if (section == DetailsSection.BILLS) onBillLongClick else null,
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(width = 48.dp, height = 24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { expanded = !expanded }
                    .pointerInput(expanded, dragThreshold) {
                        var totalDragY = 0f
                        detectVerticalDragGestures(
                            onDragStart = { totalDragY = 0f },
                            onVerticalDrag = { _, dragAmount -> totalDragY += dragAmount },
                            onDragEnd = {
                                when {
                                    totalDragY <= -dragThreshold -> expanded = false
                                    totalDragY >= dragThreshold -> expanded = true
                                }
                            },
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_expand_less),
                    contentDescription = if (expanded) "收起标题栏" else "展开标题栏",
                    modifier = Modifier.size(24.dp).rotate(arrowRotation),
                    tint = Color(0xFFBDBDBD),
                )
            }
        }
    }
}

@Composable
private fun DetailsAutoCollapsingFeatureBar(
    modifier: Modifier = Modifier,
    selectedSection: DetailsSection,
    onSectionSelected: (DetailsSection) -> Unit,
    onBillLongClick: () -> Unit,
    sections: List<DetailsSection> = DetailsSection.entries,
) {
    Box(modifier = modifier.fillMaxWidth().height(85.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, end = 16.dp)
                .height(77.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                sections.forEach { section ->
                    DetailsFeatureItem(
                        Modifier.weight(1f),
                        detailsSectionIcon(section),
                        detailsSectionLabel(section),
                        selected = selectedSection == section,
                        onClick = { onSectionSelected(section) },
                        onLongClick = if (section == DetailsSection.BILLS) onBillLongClick else null,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DetailsFeatureItem(
    modifier: Modifier,
    iconResource: Int,
    label: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val fixedPrimaryDark = colorResource(R.color.app_primary_dark)
    val interactionSource = remember { MutableInteractionSource() }
    val gestureModifier = when {
        onLongClick != null -> Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = { onClick?.invoke() },
            onLongClick = onLongClick,
        )
        onClick != null -> Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
        )
        else -> Modifier
    }
    Column(
        modifier = modifier.then(gestureModifier).padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(38.dp).background(
                color = if (selected) fixedPrimaryDark else Bg,
                shape = CircleShape,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconResource),
                contentDescription = label,
                modifier = Modifier.size(21.dp),
                tint = if (selected) Color.White else Ink,
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            label,
            color = if (selected) Ink else Muted,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            lineHeight = 12.sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun BillOptionsDialog(
    entryFilter: EntryFilter,
    dismiss: () -> Unit,
    onFilterView: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("账单选项") },
        text = {
            CategoryActionItem(
                symbol = "",
                title = "筛选查看",
                subtitle = "当前：${entryFilterSummary(entryFilter)}",
                iconResource = R.drawable.ic_calendar_month_outline,
                onClick = onFilterView,
            )
        },
        confirmButton = {},
        dismissButton = { TextButton(dismiss) { Text("取消") } },
    )
}

@Composable
private fun EntryFilterDialog(
    initialFilter: EntryFilter,
    dismiss: () -> Unit,
    onApply: (EntryFilter) -> Unit,
) {
    val currentCalendar = remember { Calendar.getInstance() }
    val currentYear = currentCalendar[Calendar.YEAR]
    val currentMonth = currentCalendar[Calendar.MONTH] + 1
    var mode by remember(initialFilter) { mutableStateOf(initialFilter.mode) }
    var year by remember(initialFilter) { mutableIntStateOf(initialFilter.year) }
    var month by remember(initialFilter) { mutableIntStateOf(initialFilter.month) }

    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("筛选查看") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                FilterModeOption(
                    title = "查看所有",
                    subtitle = "显示全部账单信息",
                    selected = mode == EntryFilterMode.ALL,
                    onClick = { mode = EntryFilterMode.ALL },
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (mode == EntryFilterMode.MONTH) AppPrimaryContainer else Bg,
                            RoundedCornerShape(14.dp),
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilterNumberPicker(
                        modifier = Modifier.weight(1f),
                        label = "年份",
                        value = "${year}年",
                        previousEnabled = year > 1970,
                        nextEnabled = year < currentYear,
                        onSelect = { mode = EntryFilterMode.MONTH },
                        onPrevious = {
                            mode = EntryFilterMode.MONTH
                            year -= 1
                        },
                        onNext = {
                            mode = EntryFilterMode.MONTH
                            year += 1
                            if (year == currentYear && month > currentMonth) month = currentMonth
                        },
                    )
                    Box(
                        Modifier
                            .width(1.dp)
                            .height(72.dp)
                            .background(
                                if (mode == EntryFilterMode.MONTH) {
                                    AppPrimary.copy(alpha = 0.24f)
                                } else {
                                    Color(0xFFE1E2E5)
                                },
                            ),
                    )
                    FilterNumberPicker(
                        modifier = Modifier.weight(1f),
                        label = "月份",
                        value = "${month}月",
                        previousEnabled = month > 1,
                        nextEnabled = month < 12 && (year < currentYear || month < currentMonth),
                        onSelect = { mode = EntryFilterMode.MONTH },
                        onPrevious = {
                            mode = EntryFilterMode.MONTH
                            month -= 1
                        },
                        onNext = {
                            mode = EntryFilterMode.MONTH
                            month += 1
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onApply(
                        EntryFilter(
                            mode = mode,
                            year = year,
                            month = month,
                        ),
                    )
                },
            ) { Text("应用") }
        },
        dismissButton = { TextButton(dismiss) { Text("取消") } },
    )
}

@Composable
private fun FilterModeOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) AppPrimaryContainer else Bg, RoundedCornerShape(14.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Ink)
            Text(subtitle, fontSize = 12.sp, color = Muted)
        }
    }
}

@Composable
private fun FilterNumberPicker(
    modifier: Modifier,
    label: String,
    value: String,
    previousEnabled: Boolean,
    nextEnabled: Boolean,
    onSelect: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .clickable(interactionSource = interactionSource, indication = null, onClick = onSelect)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, fontSize = 12.sp, color = Muted)
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Ink,
            maxLines = 1,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = onPrevious,
                enabled = previousEnabled,
                contentPadding = PaddingValues(0.dp),
            ) { Text("‹", fontSize = 22.sp) }
            TextButton(
                onClick = onNext,
                enabled = nextEnabled,
                contentPadding = PaddingValues(0.dp),
            ) { Text("›", fontSize = 22.sp) }
        }
    }
}

@Composable
private fun StatisticsContent(
    modifier: Modifier = Modifier,
    onMonthlyStatistics: () -> Unit,
) {
    Column(modifier.fillMaxWidth().padding(16.dp)) {
        MoreMenuItem(
            title = "月度账单统计",
            iconResource = R.drawable.ic_monthly_statistics_color,
            onClick = onMonthlyStatistics,
        )
    }
}

@Composable
private fun MonthlyBillStatisticsScreen(
    entries: List<Entry>,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    onMonthSelected: (MonthlyBillStatistics) -> Unit,
) {
    var showYearPicker by remember { mutableStateOf(false) }
    val annualStatistics = remember(entries, selectedYear) {
        calculateAnnualBillStatistics(entries = entries, year = selectedYear)
    }
    val monthlyStatistics = remember(entries, selectedYear) {
        calculateMonthlyBillStatistics(entries).filter { statistics -> statistics.year == selectedYear }
    }
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "annual-$selectedYear") {
                AnnualBillStatisticsCard(
                    statistics = annualStatistics,
                    onYearClick = { showYearPicker = true },
                )
            }
            if (monthlyStatistics.isEmpty()) {
                item(key = "empty-$selectedYear") {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("📊", fontSize = 52.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("${selectedYear}年暂无可统计账单", fontSize = 18.sp, color = Muted)
                        Text("添加账单后将在这里按月汇总", color = Color(0xFFAAADB3))
                    }
                }
            } else {
                items(
                    items = monthlyStatistics,
                    key = { statistics -> "${statistics.year}-${statistics.month}" },
                ) { statistics ->
                    MonthlyBillStatisticsMonthSection(
                        statistics = statistics,
                        onClick = { onMonthSelected(statistics) },
                    )
                }
            }
        }
    }
    if (showYearPicker) {
        StatisticsYearPickerDialog(
            selectedYear = selectedYear,
            onDismiss = { showYearPicker = false },
            onYearSelected = { year ->
                onYearSelected(year)
                showYearPicker = false
            },
        )
    }
}

@Composable
private fun AnnualBillStatisticsCard(
    statistics: AnnualBillStatistics,
    onYearClick: () -> Unit,
) {
    val balanceText = if (statistics.balance < 0) {
        "−${money(kotlin.math.abs(statistics.balance))}"
    } else {
        money(statistics.balance)
    }
    val dashboardGradient = remember {
        Brush.horizontalGradient(colors = AnnualDashboardGradientColors)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(dashboardGradient)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Row(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onYearClick,
                    )
                    .padding(end = 9.dp, top = 3.dp, bottom = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${statistics.year}年",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = AnnualDashboardInk,
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_keyboard_arrow_down),
                    contentDescription = "选择年份",
                    modifier = Modifier.size(width = 13.dp, height = 7.dp),
                    tint = AnnualDashboardInk.copy(alpha = 0.72f),
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 9.dp),
                color = AnnualDashboardInk.copy(alpha = 0.38f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AnnualBillStatisticsValue(
                    label = "总支出",
                    value = money(statistics.expenseTotal),
                    modifier = Modifier.weight(1f),
                )
                Box(
                    Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(AnnualDashboardInk.copy(alpha = 0.10f)),
                )
                AnnualBillStatisticsValue(
                    label = "总收入",
                    value = money(statistics.incomeTotal),
                    modifier = Modifier.weight(1f),
                )
                Box(
                    Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(AnnualDashboardInk.copy(alpha = 0.10f)),
                )
                AnnualBillStatisticsValue(
                    label = "总结余",
                    value = balanceText,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AnnualBillStatisticsValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 5.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = AnnualDashboardInk.copy(alpha = 0.72f),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = AnnualDashboardInk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StatisticsYearPickerDialog(
    selectedYear: Int,
    onDismiss: () -> Unit,
    onYearSelected: (Int) -> Unit,
) {
    val years = remember { (2000..2099).toList() }
    val initialIndex = remember(selectedYear) {
        (selectedYear - years.first() - 2).coerceIn(0, years.lastIndex)
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 12.dp,
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = "选择年份",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                )
                Spacer(Modifier.height(14.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(years, key = { year -> year }) { year ->
                        val selected = year == selectedYear
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) SelectionBlueContainer else Color.Transparent)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onYearSelected(year) },
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${year}年",
                                modifier = Modifier.weight(1f),
                                fontSize = 17.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selected) SelectionBlue else Ink,
                            )
                            if (selected) {
                                Text(
                                    text = "✓",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SelectionBlue,
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = SelectionBlue)
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyBillStatisticsMonthSection(
    statistics: MonthlyBillStatistics,
    onClick: () -> Unit,
) {
    val balanceColor = when {
        statistics.balance > 0 -> IncomeAmountGreen
        statistics.balance < 0 -> Danger
        else -> Ink
    }
    val balanceText = if (statistics.balance < 0) {
        "−${money(kotlin.math.abs(statistics.balance))}"
    } else {
        money(statistics.balance)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 14.dp, top = 10.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${statistics.month}月",
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Muted.copy(alpha = 0.72f),
                )
                Text(
                    text = "›",
                    fontSize = 22.sp,
                    color = Muted.copy(alpha = 0.72f),
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color(0xFFE8E9EB),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 12.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MonthlyBillStatisticsValue(
                    label = "支出",
                    value = money(statistics.expenseTotal),
                    valueColor = Ink,
                    modifier = Modifier.weight(1f),
                )
                MonthlyBillStatisticsValue(
                    label = "收入",
                    value = money(statistics.incomeTotal),
                    valueColor = Ink,
                    modifier = Modifier.weight(1f),
                )
                MonthlyBillStatisticsValue(
                    label = "结余",
                    value = balanceText,
                    valueColor = balanceColor,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MonthlyIncomeExpenseBarsCard(statistics: MonthlyBillStatistics) {
    val maximumAmount = maxOf(statistics.expenseTotal, statistics.incomeTotal)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "${statistics.year}年${statistics.month}月",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Ink,
            )
            Spacer(Modifier.height(16.dp))
            MonthlyIncomeExpenseBar(
                label = "支出",
                amount = statistics.expenseTotal,
                widthFraction = if (maximumAmount > 0.0) {
                    (statistics.expenseTotal / maximumAmount).toFloat()
                } else {
                    0f
                },
                color = MonthlyExpenseBarColor,
            )
            Spacer(Modifier.height(14.dp))
            MonthlyIncomeExpenseBar(
                label = "收入",
                amount = statistics.incomeTotal,
                widthFraction = if (maximumAmount > 0.0) {
                    (statistics.incomeTotal / maximumAmount).toFloat()
                } else {
                    0f
                },
                color = MonthlyIncomeBarColor,
            )
        }
    }
}

@Composable
private fun MonthlyIncomeExpenseBar(
    label: String,
    amount: Double,
    widthFraction: Float,
    color: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 13.sp, color = Muted)
        Spacer(Modifier.width(8.dp))
        Text(
            text = money(amount),
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ink,
            textAlign = TextAlign.End,
        )
    }
    Spacer(Modifier.height(7.dp))
    Box(
        Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFE9EDF3)),
    ) {
        Box(
            Modifier
                .fillMaxWidth(widthFraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(color),
        )
    }
}

@Composable
private fun MonthlyBillStatisticsDetailScreen(
    statistics: MonthlyBillStatistics,
    entries: List<Entry>,
    expenseCategories: List<LedgerCategory>,
) {
    val categoryStatistics = remember(entries, statistics.year, statistics.month) {
        calculateMonthlyCategoryExpenseStatistics(
            entries = entries,
            year = statistics.year,
            month = statistics.month,
        )
    }
    val categorySymbols = remember(expenseCategories) {
        expenseCategories.associate { category -> category.name to category.symbol }
    }
    val pieStatistics = remember(categoryStatistics) {
        summarizeMonthlyExpensePieStatistics(categoryStatistics)
    }
    val pieColorStartIndex = remember(pieStatistics) {
        kotlin.random.Random.nextInt(MonthlyExpensePieColorCandidates.size)
    }
    val pieCategoryColors = remember(pieStatistics, pieColorStartIndex) {
        val colors = highContrastPieColors(pieStatistics.size, pieColorStartIndex)
        pieStatistics.mapIndexed { index, item ->
            item.category to colors[index]
        }.toMap()
    }
    val barCategoryColors = remember(categoryStatistics) {
        categoryStatistics.mapIndexed { index, item ->
            item.category to MonthlyExpenseCategoryBarColors[
                index % MonthlyExpenseCategoryBarColors.size
            ]
        }.toMap()
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            MonthlyIncomeExpenseBarsCard(statistics = statistics)
        }
        item {
            MonthlyExpensePieChart(
                statistics = pieStatistics,
                categoryColors = pieCategoryColors,
            )
        }
        item {
            MonthlyCategoryExpenseChart(
                statistics = categoryStatistics,
                categorySymbols = categorySymbols,
                categoryColors = barCategoryColors,
            )
        }
    }
}

@Composable
private fun MonthlyExpensePieChart(
    statistics: List<CategoryExpenseStatistics>,
    categoryColors: Map<String, Color>,
) {
    val totalAmount = statistics.sumOf { item -> item.amount }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "支出占比",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Ink,
            )
            Spacer(Modifier.height(16.dp))
            if (statistics.isEmpty() || totalAmount <= 0.0) {
                Text(
                    text = "当月暂无支出记录",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp),
                    color = Muted,
                    textAlign = TextAlign.Center,
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    MonthlyExpensePieCanvas(
                        statistics = statistics,
                        totalAmount = totalAmount,
                        categoryColors = categoryColors,
                        modifier = Modifier.size(MonthlyExpensePieSize),
                    )
                    Spacer(Modifier.width(MonthlyExpensePieLegendHorizontalGap))
                    MonthlyExpensePieLegend(
                        statistics = statistics,
                        totalAmount = totalAmount,
                        categoryColors = categoryColors,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyExpensePieCanvas(
    statistics: List<CategoryExpenseStatistics>,
    totalAmount: Double,
    categoryColors: Map<String, Color>,
    modifier: Modifier,
) {
    Canvas(modifier) {
        var consumedAngle = 0f
        statistics.forEachIndexed { index, item ->
            val sweepAngle = if (index == statistics.lastIndex) {
                360f - consumedAngle
            } else {
                (item.amount / totalAmount * 360.0).toFloat()
            }
            drawArc(
                color = categoryColors[item.category] ?: MonthlyExpensePieFallbackColor,
                startAngle = -90f + consumedAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
            )
            consumedAngle += sweepAngle
        }
    }
}

@Composable
private fun MonthlyExpensePieLegend(
    statistics: List<CategoryExpenseStatistics>,
    totalAmount: Double,
    categoryColors: Map<String, Color>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.height(MonthlyExpensePieSize),
        verticalArrangement = if (statistics.size <= 1) {
            Arrangement.Center
        } else {
            Arrangement.SpaceBetween
        },
    ) {
        statistics.forEach { item ->
            val percentage = item.amount / totalAmount * 100.0
            Row(
                modifier = Modifier.fillMaxWidth().height(MonthlyExpensePieLegendRowHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.width(MonthlyExpensePieLegendCategoryWidth),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(width = 12.dp, height = 9.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                categoryColors[item.category] ?: MonthlyExpensePieFallbackColor,
                            ),
                    )
                    Spacer(Modifier.width(MonthlyExpensePieLegendSwatchGap))
                    Text(
                        text = item.category,
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 11.sp,
                        lineHeight = 12.sp,
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = if (percentage >= 99.95) {
                        "100%"
                    } else {
                        String.format(Locale.US, "%.1f%%", percentage)
                    },
                    modifier = Modifier.width(MonthlyExpensePieLegendPercentageWidth),
                    fontSize = 11.sp,
                    lineHeight = 12.sp,
                    color = Muted,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
                Text(
                    text = money(item.amount),
                    modifier = Modifier.weight(1f),
                    fontSize = 11.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun MonthlyCategoryExpenseChart(
    statistics: List<CategoryExpenseStatistics>,
    categorySymbols: Map<String, String>,
    categoryColors: Map<String, Color>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "分类支出",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Ink,
            )
            Spacer(Modifier.height(16.dp))
            if (statistics.isEmpty()) {
                Text(
                    text = "当月暂无支出记录",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp),
                    color = Muted,
                    textAlign = TextAlign.Center,
                )
            } else {
                val maximumAmount = statistics.maxOf { item -> item.amount }
                statistics.forEachIndexed { index, item ->
                    val itemColor = categoryColors[item.category]
                        ?: MonthlyExpenseCategoryBarColors.first()
                    val widthFraction = if (maximumAmount > 0.0) {
                        (item.amount / maximumAmount).toFloat().coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = categorySymbols[item.category] ?: defaultIcon(item.category),
                            fontSize = 22.sp,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = item.category,
                            modifier = Modifier.weight(1f),
                            fontSize = 15.sp,
                            color = Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = money(item.amount),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Ink,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE9EDF3)),
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(widthFraction)
                                .fillMaxHeight()
                                .background(itemColor),
                        )
                    }
                    if (index != statistics.lastIndex) {
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyBillStatisticsValue(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 13.sp, color = Muted)
        Spacer(Modifier.height(5.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MoreContent(
    modifier: Modifier = Modifier,
    onManageCategories: () -> Unit,
    onTitleBarStyle: () -> Unit,
) {
    Column(modifier.fillMaxWidth().padding(16.dp)) {
        MoreMenuItem(
            title = "类型管理",
            iconResource = R.drawable.ic_category_manage_color,
            onClick = onManageCategories,
        )
        Spacer(Modifier.height(12.dp))
        MoreMenuItem(
            title = "标题栏样式",
            iconResource = R.drawable.ic_title_bar_style_color,
            onClick = onTitleBarStyle,
        )
    }
}

@Composable
private fun TitleBarStyleScreen(
    selectedStyle: TitleBarStyle,
    onStyleSelected: (TitleBarStyle) -> Unit,
) {
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(TitleBarStyle.entries, key = { it.storageKey }) { style ->
                TitleBarStyleOption(
                    style = style,
                    selected = style == selectedStyle,
                    onClick = { onStyleSelected(style) },
                )
            }
        }
    }
}

@Composable
private fun TitleBarStyleOption(
    style: TitleBarStyle,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) SelectionBlueContainer else Color.White, RoundedCornerShape(16.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(
                    if (style == TitleBarStyle.DESIGN_ONE) {
                        R.drawable.ic_title_style_classic_color
                    } else {
                        R.drawable.ic_title_style_immersive_color
                    },
                ),
                contentDescription = style.title,
                modifier = Modifier.size(34.dp),
                tint = Color.Unspecified,
            )
        }
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(style.title, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = Ink)
            Spacer(Modifier.height(3.dp))
            Text(style.subtitle, fontSize = 13.sp, color = Muted)
        }
        Text(
            text = if (selected) "✓" else "",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = SelectionBlue,
        )
    }
}

@Composable
private fun MoreMenuItem(
    title: String,
    iconResource: Int,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(48.dp).background(Color.White, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconResource),
                contentDescription = null,
                modifier = Modifier.size(34.dp),
                tint = Color.Unspecified,
            )
        }
        Text(
            title,
            modifier = Modifier.weight(1f).padding(horizontal = 14.dp),
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = Ink,
        )
        Text("›", fontSize = 28.sp, color = Muted)
    }
}

@Composable
private fun CategoryOverviewScreen(
    expenseCategories: List<LedgerCategory>,
    incomeCategories: List<LedgerCategory>,
    onNewCategory: (String, LedgerCategory) -> Unit,
    onEditCategory: (String, LedgerCategory, LedgerCategory) -> Unit,
    onDeleteCategory: (String, LedgerCategory) -> Unit,
) {
    var actionTarget by remember { mutableStateOf<CategoryEditTarget?>(null) }
    var editingTarget by remember { mutableStateOf<CategoryEditTarget?>(null) }
    var deleteRequest by remember { mutableStateOf<CategoryDeleteRequest?>(null) }
    var addingKind by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                CategoryOverviewSection(
                    title = "支出类型",
                    categories = expenseCategories,
                    onLongClick = { actionTarget = CategoryEditTarget("支出", it) },
                    onAdd = { addingKind = "支出" },
                )
            }
            item {
                CategoryOverviewSection(
                    title = "收入类型",
                    categories = incomeCategories,
                    onLongClick = { actionTarget = CategoryEditTarget("收入", it) },
                    onAdd = { addingKind = "收入" },
                )
            }
        }
    }
    addingKind?.let { kind ->
        val categories = if (kind == "支出") expenseCategories else incomeCategories
        CategoryDialog(
            kind = kind,
            existing = categories.map { it.name },
            dismiss = { addingKind = null },
            confirm = { category ->
                onNewCategory(kind, category)
                addingKind = null
            },
        )
    }
    actionTarget?.let { target ->
        CategoryActionDialog(
            category = target.category,
            canDelete = target.category.name != UncategorizedName,
            dismiss = { actionTarget = null },
            onEdit = {
                actionTarget = null
                editingTarget = target
            },
            onDelete = {
                actionTarget = null
                deleteRequest = CategoryDeleteRequest(target)
            },
        )
    }
    editingTarget?.let { target ->
        val categories = if (target.kind == "支出") expenseCategories else incomeCategories
        CategoryDialog(
            kind = target.kind,
            existing = categories.filterNot { it == target.category }.map { it.name },
            initial = target.category,
            lockName = target.category.name == UncategorizedName,
            dismiss = { editingTarget = null },
            confirm = { updated ->
                onEditCategory(target.kind, target.category, updated)
                editingTarget = null
            },
        )
    }
    deleteRequest?.let { request ->
        DeleteCategoryDialog(
            category = request.target.category,
            isFinalConfirmation = request.isFinalConfirmation,
            dismiss = { deleteRequest = null },
            confirm = {
                if (request.isFinalConfirmation) {
                    onDeleteCategory(request.target.kind, request.target.category)
                    deleteRequest = null
                } else {
                    deleteRequest = request.copy(isFinalConfirmation = true)
                }
            },
        )
    }
}

@Composable
private fun CategoryActionDialog(
    category: LedgerCategory,
    canDelete: Boolean,
    dismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val fixedPrimaryDark = colorResource(R.color.app_primary_dark)
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("${category.symbol}  ${category.name}") },
        text = {
            Column {
                CategoryActionItem(
                    symbol = "",
                    title = if (canDelete) "修改类型" else "修改图标",
                    subtitle = if (canDelete) "修改类型名称和图标" else "未分类名称固定，只能修改图标",
                    iconResource = R.drawable.ic_edit_bill_color,
                    iconBackgroundColor = Color.Transparent,
                    preserveIconColors = true,
                    expandIconToBackground = true,
                    onClick = onEdit,
                )
                if (canDelete) {
                    Spacer(Modifier.height(10.dp))
                    CategoryActionItem(
                        symbol = "",
                        title = "删除类型",
                        subtitle = "对应账目将归入未分类",
                        iconResource = R.drawable.ic_delete_category_color,
                        iconBackgroundColor = Color.Transparent,
                        preserveIconColors = true,
                        expandIconToBackground = true,
                        onClick = onDelete,
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = dismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = fixedPrimaryDark),
            ) { Text("取消") }
        },
    )
}

@Composable
private fun CategoryActionItem(
    symbol: String,
    title: String,
    subtitle: String,
    iconResource: Int? = null,
    iconBackgroundColor: Color? = null,
    preserveIconColors: Boolean = false,
    expandIconToBackground: Boolean = false,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val contentColor = Ink
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Bg, RoundedCornerShape(14.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(42.dp).background(
                iconBackgroundColor ?: AppPrimaryContainer,
                CircleShape,
            ),
            contentAlignment = Alignment.Center,
        ) {
            if (iconResource == null) {
                Text(symbol, fontSize = 22.sp, color = contentColor)
            } else {
                Icon(
                    painter = painterResource(iconResource),
                    contentDescription = null,
                    modifier = Modifier.size(if (expandIconToBackground) 42.dp else 24.dp),
                    tint = if (preserveIconColors) Color.Unspecified else contentColor,
                )
            }
        }
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = contentColor)
            Text(subtitle, fontSize = 13.sp, color = Muted)
        }
        Text("›", fontSize = 26.sp, color = Muted)
    }
}

@Composable
private fun DeleteCategoryDialog(
    category: LedgerCategory,
    isFinalConfirmation: Boolean,
    dismiss: () -> Unit,
    confirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(if (isFinalConfirmation) "再次确认删除" else "删除类型？") },
        text = {
            Text(
                if (isFinalConfirmation) {
                    "此操作无法撤销。确定删除“${category.name}”，并将对应账目全部归入“未分类”吗？"
                } else {
                    "即将删除“${category.name}”类型。该类型下的历史账目会全部归入“未分类”。"
                },
                color = Muted,
            )
        },
        confirmButton = {
            TextButton(
                onClick = confirm,
                colors = ButtonDefaults.textButtonColors(contentColor = DeleteActionRed),
            ) { Text(if (isFinalConfirmation) "确认删除" else "继续") }
        },
        dismissButton = {
            TextButton(
                onClick = dismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Muted),
            ) { Text("取消") }
        },
    )
}

@Composable
private fun CategoryOverviewSection(
    title: String,
    categories: List<LedgerCategory>,
    onLongClick: (LedgerCategory) -> Unit,
    onAdd: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
            Spacer(Modifier.height(14.dp))
            val categoryItems = categories.map<LedgerCategory, LedgerCategory?> { it } + null
            categoryItems.chunked(4).forEachIndexed { rowIndex, row ->
                if (rowIndex > 0) Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { category ->
                        if (category == null) {
                            CategoryOverviewAddItem(
                                modifier = Modifier.weight(1f),
                                onClick = onAdd,
                            )
                        } else {
                            CategoryOverviewItem(
                                category = category,
                                modifier = Modifier.weight(1f),
                                onLongClick = { onLongClick(category) },
                            )
                        }
                    }
                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun CategoryOverviewAddItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = modifier.height(82.dp).clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("+", fontSize = 28.sp, color = Ink)
        Spacer(Modifier.height(5.dp))
        Text("添加", fontSize = 12.sp, color = Ink)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryOverviewItem(
    category: LedgerCategory,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = modifier.height(82.dp).combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {},
            onLongClick = onLongClick,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(category.symbol, fontSize = 28.sp)
        Spacer(Modifier.height(5.dp))
        Text(category.name, fontSize = 12.sp, color = Ink, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EntryCard(
    entry: Entry,
    symbol: String,
    revealed: Boolean,
    onRevealChange: (Boolean) -> Unit,
    onLongClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val income = entry.kind == "收入"
    val interactionSource = remember { MutableInteractionSource() }
    val maximumReveal = with(LocalDensity.current) { 72.dp.toPx() }
    var horizontalOffset by remember(entry.id) { mutableFloatStateOf(0f) }

    LaunchedEffect(revealed, maximumReveal) {
        horizontalOffset = if (revealed) -maximumReveal else 0f
    }

    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))) {
        Box(
            modifier = Modifier.matchParentSize().background(DeleteSwipeBackground),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Box(
                modifier = Modifier.fillMaxHeight().width(72.dp).clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDeleteClick,
                ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete_bin_outline),
                    contentDescription = "删除账单",
                    modifier = Modifier.size(27.dp),
                    tint = Color.White,
                )
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(horizontalOffset.roundToInt(), 0) }
                .pointerInput(entry.id, maximumReveal) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            horizontalOffset = (horizontalOffset + dragAmount).coerceIn(-maximumReveal, 0f)
                        },
                        onDragEnd = {
                            val shouldReveal = horizontalOffset <= -maximumReveal / 2f
                            horizontalOffset = if (shouldReveal) -maximumReveal else 0f
                            onRevealChange(shouldReveal)
                        },
                        onDragCancel = {
                            horizontalOffset = if (revealed) -maximumReveal else 0f
                        },
                    )
                }
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { if (revealed) onRevealChange(false) },
                    onLongClick = onLongClick,
                ),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(46.dp)
                        .background(Color.White, CircleShape),
                        // blmcc：暂时去除明细图标的 0.2.dp 边线，保留原实现便于恢复。
                        // .border(0.2.dp, NeutralIconBorder, CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Text(symbol, fontSize = 28.sp) }
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text(entry.category, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                    if (entry.note.isNotBlank()) {
                        Text(entry.note, fontSize = 12.sp, color = NoteSlate, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Text(
                    "${if (income) "+" else "−"}${money(entry.amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (income) IncomeAmountGreen else Ink,
                )
            }
        }
    }
}

@Composable
private fun EntryActionDialog(
    entry: Entry,
    dismiss: () -> Unit,
    onEdit: () -> Unit,
) {
    val fixedPrimaryDark = colorResource(R.color.app_primary_dark)
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(entry.category) },
        text = {
            CategoryActionItem(
                symbol = "",
                title = "修改账单",
                subtitle = "修改分类、金额、日期和备注",
                iconResource = R.drawable.ic_edit_bill_color,
                iconBackgroundColor = Color.Transparent,
                preserveIconColors = true,
                expandIconToBackground = true,
                onClick = onEdit,
            )
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = dismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = fixedPrimaryDark),
            ) { Text("取消") }
        },
    )
}

@Composable
private fun EntryEditDialog(
    entry: Entry,
    categories: List<LedgerCategory>,
    dismiss: () -> Unit,
    confirm: (Entry) -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val inputWindowActions = remember { InputWindowActions() }
    val amountFocusRequester = remember { FocusRequester() }
    val editScrollState = rememberScrollState()
    val fixedPrimaryDark = colorResource(R.color.app_primary_dark)
    val currentCategory = categories.firstOrNull { it.name == entry.category }
    var selected by remember(entry.id, entry.category) { mutableStateOf(currentCategory) }
    var amount by remember(entry.id) { mutableStateOf(entry.amount.toString()) }
    var date by remember(entry.id) { mutableLongStateOf(entry.date) }
    var note by remember(entry.id) { mutableStateOf(entry.note) }
    var invalid by remember(entry.id) { mutableStateOf(false) }
    var showDatePicker by remember(entry.id) { mutableStateOf(false) }
    var showCalculatorKeyboard by remember(entry.id) { mutableStateOf(false) }
    var amountHasFocus by remember(entry.id) { mutableStateOf(false) }
    var calculatorSurfaceVisible by remember(entry.id) { mutableStateOf(false) }
    var noteInputRequested by remember(entry.id) { mutableStateOf(false) }
    var noteKeyboardWasVisible by remember(entry.id) { mutableStateOf(false) }
    var noteHasFocus by remember(entry.id) { mutableStateOf(false) }
    val imeBottom = WindowInsets.ime.getBottom(density)
    val navigationBarBottom = WindowInsets.navigationBars.getBottom(density)
    val visibleImeHeight = (imeBottom - navigationBarBottom).coerceAtLeast(0)
    var retainInputPanelPlacement by remember(entry.id) { mutableStateOf(false) }
    // 修改账单位于独立 Dialog 窗口，外层 Activity 的 IME Insets 会包含额外的窗口
    // 调整高度。金额和备注必须共用同一个 340dp 面板基准，才能保持完全一致的
    // 属性页位置、最大滚动位置与输入面板间隔。
    val matchedInputPanelHeight = CalculatorKeyboardHeight
    val isSystemKeyboardVisible = imeBottom > navigationBarBottom
    val inputPanelPlacementActive =
        calculatorSurfaceVisible || isSystemKeyboardVisible || retainInputPanelPlacement
    val dismissInputInteraction = remember { MutableInteractionSource() }
    val dateInteractionSource = remember { MutableInteractionSource() }
    val editFieldBorder = Color(0xFF7A7D83)
    val editFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Ink,
        unfocusedTextColor = Ink,
        cursorColor = Ink,
        focusedBorderColor = editFieldBorder,
        unfocusedBorderColor = editFieldBorder,
        focusedLabelColor = Muted,
        unfocusedLabelColor = Muted,
    )
    val amountFieldState = rememberCalculatorAmountFieldState(amount)
    val calculatedAmount = remember(amount) { evaluateAmountExpression(amount) }
    val changed = selected?.name != entry.category || calculatedAmount != entry.amount ||
        date != entry.date || note.trim() != entry.note
    val collapseInputPanel = {
        inputWindowActions.hideKeyboard()
        noteInputRequested = false
        noteKeyboardWasVisible = false
        showCalculatorKeyboard = false
        calculatorSurfaceVisible = false
    }
    val clearInputFocus = {
        collapseInputPanel()
        inputWindowActions.clearFocus()
    }

    LaunchedEffect(showCalculatorKeyboard) {
        if (showCalculatorKeyboard) {
            inputWindowActions.hideKeyboard()
            amountFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(amountHasFocus, showCalculatorKeyboard) {
        if (showCalculatorKeyboard && !amountHasFocus) {
            withFrameNanos { }
            if (showCalculatorKeyboard && !amountHasFocus && !noteHasFocus && !showDatePicker) {
                collapseInputPanel()
                amountFocusRequester.requestFocus()
            }
        }
    }

    LaunchedEffect(noteHasFocus, noteInputRequested, isSystemKeyboardVisible) {
        when {
            !noteHasFocus -> {
                noteInputRequested = false
                noteKeyboardWasVisible = false
            }
            isSystemKeyboardVisible -> {
                noteInputRequested = true
                noteKeyboardWasVisible = true
            }
            noteInputRequested && noteKeyboardWasVisible -> collapseInputPanel()
        }
    }

    LaunchedEffect(showCalculatorKeyboard, noteInputRequested, visibleImeHeight) {
        when {
            showCalculatorKeyboard -> calculatorSurfaceVisible = true
            noteInputRequested && visibleImeHeight > 0 -> {
                delay(InputPanelTransitionDelayMillis)
                calculatorSurfaceVisible = false
            }
            noteInputRequested -> Unit
            else -> calculatorSurfaceVisible = false
        }
    }

    LaunchedEffect(calculatorSurfaceVisible, isSystemKeyboardVisible) {
        if (calculatorSurfaceVisible || isSystemKeyboardVisible) {
            retainInputPanelPlacement = true
        } else {
            delay(InputPanelTransitionDelayMillis)
            retainInputPanelPlacement = false
        }
    }

    LaunchedEffect(
        showCalculatorKeyboard,
        noteHasFocus,
        inputPanelPlacementActive,
        matchedInputPanelHeight,
    ) {
        if ((showCalculatorKeyboard || noteHasFocus) && inputPanelPlacementActive) {
            withFrameNanos { }
            editScrollState.scrollTo(editScrollState.maxValue)
            withFrameNanos { }
            editScrollState.scrollTo(editScrollState.maxValue)
        }
    }

    Dialog(
        onDismissRequest = {
            if (
                showCalculatorKeyboard || noteInputRequested ||
                calculatorSurfaceVisible || isSystemKeyboardVisible
            ) {
                collapseInputPanel()
            } else {
                dismiss()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BindInputWindowActions(inputWindowActions)
        // 系统键盘默认会对 Dialog 执行 adjustPan，把整个修改账单页额外推到屏幕顶端。
        // 禁止窗口级平移，只保留与金额计算器共用的 340dp 面板占位和内部滚动。
        DisableDialogImeWindowPanning()
        Box(
            Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = dismissInputInteraction,
                    indication = null,
                    onClick = collapseInputPanel,
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = 12.dp,
                        bottom = if (inputPanelPlacementActive) {
                            matchedInputPanelHeight + CalculatorDialogGap
                        } else {
                            12.dp
                        },
                    ),
                contentAlignment = if (inputPanelPlacementActive) {
                    Alignment.BottomCenter
                } else {
                    Alignment.Center
                },
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(BookkeepingDialogWidthFraction),
                    shape = RoundedCornerShape(28.dp),
                    color = Bg,
                    shadowElevation = 10.dp,
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Text(
                            "修改账单",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = Ink,
                        )
                        Spacer(Modifier.height(18.dp))
                        Column(
                            Modifier
                                .weight(1f, fill = false)
                                .verticalScroll(editScrollState),
                        ) {
                            Text("分类", fontWeight = FontWeight.Medium, color = Ink)
                            Spacer(Modifier.height(8.dp))
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                modifier = Modifier.fillMaxWidth().heightIn(max = 210.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(categories.size) { index ->
                                    val category = categories[index]
                                    EntryCategoryChoice(
                                        category = category,
                                        active = selected == category,
                                        onClick = { selected = category },
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            Box(Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    state = amountFieldState,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(amountFocusRequester)
                                        .onFocusChanged {
                                            val lostAmountFocus = amountHasFocus && !it.isFocused
                                            amountHasFocus = it.isFocused
                                            if (it.isFocused) {
                                                noteInputRequested = false
                                                inputWindowActions.hideKeyboard()
                                                calculatorSurfaceVisible = true
                                                showCalculatorKeyboard = true
                                            } else if (lostAmountFocus) {
                                                formatEvaluatedAmount(amount)?.let { result ->
                                                    amount = result
                                                    invalid = false
                                                }
                                            }
                                        },
                                    label = { Text("金额") },
                                    isError = invalid,
                                    supportingText = if (invalid) ({ Text("请输入结果大于 0 的有效算式") }) else null,
                                    inputTransformation = RejectCalculatorAmountInput,
                                    keyboardOptions = KeyboardOptions(showKeyboardOnFocus = false),
                                    lineLimits = TextFieldLineLimits.SingleLine,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = editFieldColors,
                                )
                                Box(
                                    Modifier
                                        .matchParentSize()
                                        .pointerInput(Unit) {
                                            detectTapGestures {
                                                noteInputRequested = false
                                                inputWindowActions.hideKeyboard()
                                                calculatorSurfaceVisible = true
                                                showCalculatorKeyboard = true
                                                amountFocusRequester.requestFocus()
                                            }
                                        },
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            Box(
                                Modifier.fillMaxWidth().clickable(
                                    interactionSource = dateInteractionSource,
                                    indication = null,
                                ) {
                                    clearInputFocus()
                                    showDatePicker = true
                                },
                            ) {
                                OutlinedTextField(
                                    value = day(date),
                                    onValueChange = {},
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = false,
                                    label = { Text("日期") },
                                    trailingIcon = { Text("选择") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = Ink,
                                        disabledBorderColor = Color(0xFFB9BBC0),
                                        disabledLabelColor = Muted,
                                        disabledTrailingIconColor = Ink,
                                        disabledContainerColor = Color.Transparent,
                                    ),
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            OutlinedTextField(
                                value = note,
                                onValueChange = { if (it.length <= 100) note = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged {
                                        noteHasFocus = it.isFocused
                                        if (it.isFocused) {
                                            noteInputRequested = true
                                            showCalculatorKeyboard = false
                                        }
                                    },
                                label = { Text("备注") },
                                maxLines = 3,
                                shape = RoundedCornerShape(14.dp),
                                colors = editFieldColors,
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(
                                onClick = dismiss,
                                colors = ButtonDefaults.textButtonColors(contentColor = fixedPrimaryDark),
                            ) { Text("取消") }
                            TextButton(
                                onClick = {
                                    val value = evaluateAmountExpression(amount)
                                    if (value == null || value <= 0) invalid = true
                                    else if (isDateAfterToday(date)) {
                                        Toast.makeText(context, "不能添加晚于今天的账单", Toast.LENGTH_SHORT).show()
                                    }
                                    else confirm(
                                        entry.copy(
                                            category = selected!!.name,
                                            amount = value,
                                            date = date,
                                            note = note.trim(),
                                        ),
                                    )
                                },
                                enabled = selected != null && changed,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = fixedPrimaryDark,
                                    disabledContentColor = Muted.copy(alpha = 0.38f),
                                ),
                            ) { Text("保存") }
                        }
                    }
                }
            }
            if (calculatorSurfaceVisible) {
                AmountCalculatorKeyboard(
                    expression = amount,
                    onExpressionChange = {
                        amount = it
                        invalid = false
                    },
                    onInvalidExpression = { invalid = true },
                    onDismiss = collapseInputPanel,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(matchedInputPanelHeight),
                )
            }
        }
    }
    if (showDatePicker) {
        MoneyLogDatePickerDialog(
            initialMillis = date,
            onDismiss = { showDatePicker = false },
            onDateSelected = {
                date = it
                showDatePicker = false
            },
        )
    }
}

@Composable
private fun DeleteEntryDialog(entry: Entry, dismiss: () -> Unit, confirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("确认删除账单？") },
        text = {
            Text(
                "将永久删除${day(entry.date)}的${entry.category}账单，金额为${money(entry.amount)}。删除后无法恢复。",
                color = Muted,
            )
        },
        confirmButton = {
            TextButton(
                onClick = confirm,
                colors = ButtonDefaults.textButtonColors(contentColor = DeleteActionRed),
            ) { Text("删除") }
        },
        dismissButton = {
            TextButton(
                onClick = dismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Muted),
            ) { Text("取消") }
        },
    )
}

@Composable
private fun EntryCategoryChoice(
    category: LedgerCategory,
    active: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .background(if (active) EditCategorySelectedGreen else Bg, RoundedCornerShape(12.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(category.symbol, fontSize = 25.sp)
        Spacer(Modifier.height(3.dp))
        Text(category.name, fontSize = 11.sp, color = Ink, maxLines = 1)
    }
}

@Composable
private fun LedgerIconChoice(symbol: String, active: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(if (active) EditCategorySelectedGreen else Bg, RoundedCornerShape(12.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        LedgerSymbol(
            symbol = symbol,
            containerSize = 48.dp,
            baseFontSize = 32.sp,
            badgeSize = 21.dp,
            badgeFontSize = 16.sp,
        )
    }
}

@Composable
private fun LedgerSymbol(
    symbol: String,
    containerSize: Dp,
    baseFontSize: TextUnit,
    badgeSize: Dp,
    badgeFontSize: TextUnit,
) {
    val resourceId = ledgerIconResources[symbol]
    if (resourceId != null) {
        Image(
            painter = painterResource(resourceId),
            contentDescription = null,
            modifier = Modifier.size(containerSize),
            contentScale = ContentScale.Fit,
        )
        return
    }
    val badge = ledgerIconBadges[symbol]
    if (badge == null) {
        Text(symbol, fontSize = baseFontSize)
        return
    }
    Box(
        modifier = Modifier.size(containerSize),
        contentAlignment = Alignment.Center,
    ) {
        Text("📒", fontSize = baseFontSize)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(badgeSize)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(badge, fontSize = badgeFontSize)
        }
    }
}

@Composable
private fun AddScreen(
    expenseCategories: List<LedgerCategory>,
    incomeCategories: List<LedgerCategory>,
    initialDate: Long,
    ledgerId: String?,
    onDismiss: () -> Unit,
    onNewCategory: (String, LedgerCategory) -> Unit,
    onEditCategoryIcon: (String, LedgerCategory, LedgerCategory) -> Unit,
    onSave: (Entry) -> Unit,
) {
    var kind by remember { mutableStateOf("支出") }
    var selected by remember { mutableStateOf<LedgerCategory?>(null) }
    var amount by remember { mutableStateOf("") }
    var date by remember(initialDate) { mutableLongStateOf(initialDate) }
    var note by remember { mutableStateOf("") }
    var invalid by remember { mutableStateOf(false) }
    var categoryActionTarget by remember { mutableStateOf<CategoryEditTarget?>(null) }
    var editingIconTarget by remember { mutableStateOf<CategoryEditTarget?>(null) }
    var addingKind by remember { mutableStateOf<String?>(null) }
    var inputPanelHeight by remember { mutableStateOf(0.dp) }
    val selectedCategory = selected
    val initialPagerPage = remember { (Int.MAX_VALUE / 2).let { it - it % 2 } }
    val pagerState = rememberPagerState(initialPage = initialPagerPage) { Int.MAX_VALUE }
    val pagerFlingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        snapAnimationSpec = tween(durationMillis = 650),
    )
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = inputPanelHeight),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(BookkeepingDialogWidthFraction)
                    .aspectRatio(1f / GoldenRatio),
                shape = RoundedCornerShape(24.dp),
                color = Bg,
                shadowElevation = 12.dp,
            ) {
                Column(Modifier.fillMaxSize()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        flingBehavior = pagerFlingBehavior,
                    ) { page ->
                        val pageKind = if (page % 2 == 0) "支出" else "收入"
                        val pageCategories = if (pageKind == "支出") expenseCategories else incomeCategories
                        CategorySelectionSection(
                            title = "${pageKind}分类",
                            categories = pageCategories,
                            modifier = Modifier.fillMaxSize(),
                            onCategorySelected = {
                                kind = pageKind
                                amount = ""
                                date = initialDate
                                note = ""
                                invalid = false
                                selected = it
                            },
                            onCategoryLongPressed = { categoryActionTarget = CategoryEditTarget(pageKind, it) },
                            onAddCategory = { addingKind = pageKind },
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().height(28.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(2) { pageIndex ->
                            Box(
                                Modifier
                                    .size(7.dp)
                                    .background(
                                        if (pagerState.currentPage % 2 == pageIndex) AppOnPrimaryContainer
                                        else Color(0xFFD0D1D3),
                                        CircleShape,
                                    ),
                            )
                            if (pageIndex == 0) Spacer(Modifier.width(7.dp))
                        }
                    }
                }
            }
        }
    }
    if (selectedCategory != null) {
        EntryInputDialog(
            kind = kind,
            category = selectedCategory,
            amount = amount,
            date = date,
            note = note,
            invalid = invalid,
            onAmountChange = {
                if (validAmountExpressionInput(it)) amount = it
                invalid = false
            },
            onDateSelected = { date = it },
            onNoteChange = { if (it.length <= 100) note = it },
            onInvalidAmount = { invalid = true },
            onInputPanelHeightChanged = { inputPanelHeight = it },
            onDismiss = {
                inputPanelHeight = 0.dp
                selected = null
                amount = ""
                date = initialDate
                note = ""
                invalid = false
            },
            onSave = { value ->
                onSave(
                    Entry(
                        id = UUID.randomUUID().toString(),
                        kind = kind,
                        category = selectedCategory.name,
                        amount = value,
                        date = date,
                        note = note.trim(),
                        ledgerId = ledgerId,
                    ),
                )
            },
        )
    }
    categoryActionTarget?.let { target ->
        CategoryIconActionDialog(
            category = target.category,
            dismiss = { categoryActionTarget = null },
            onEditIcon = {
                categoryActionTarget = null
                editingIconTarget = target
            },
        )
    }
    editingIconTarget?.let { target ->
        val targetCategories = if (target.kind == "支出") expenseCategories else incomeCategories
        CategoryDialog(
            kind = target.kind,
            existing = targetCategories.filterNot { it == target.category }.map { it.name },
            initial = target.category,
            lockName = true,
            dismiss = { editingIconTarget = null },
            confirm = { updated ->
                onEditCategoryIcon(target.kind, target.category, updated)
                editingIconTarget = null
            },
        )
    }
    addingKind?.let { targetKind ->
        val targetCategories = if (targetKind == "支出") expenseCategories else incomeCategories
        CategoryDialog(
            kind = targetKind,
            existing = targetCategories.map { it.name },
            dismiss = { addingKind = null },
            confirm = { category ->
                onNewCategory(targetKind, category)
                addingKind = null
            },
        )
    }
}

@Composable
private fun CategorySelectionSection(
    title: String,
    categories: List<LedgerCategory>,
    modifier: Modifier = Modifier,
    onCategorySelected: (LedgerCategory) -> Unit,
    onCategoryLongPressed: (LedgerCategory) -> Unit,
    onAddCategory: () -> Unit,
) {
    Column(modifier.fillMaxWidth()) {
        Text(
            title,
            Modifier.padding(start = 20.dp, top = 10.dp, bottom = 8.dp),
            color = Muted,
            fontSize = 14.sp,
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            items(categories.size + 1) { index ->
                if (index == categories.size) {
                    CategoryCard(
                        label = "添加",
                        symbol = "+",
                        active = false,
                        click = onAddCategory,
                    )
                } else {
                    val category = categories[index]
                    CategoryCard(
                        label = category.name,
                        symbol = category.symbol,
                        active = false,
                        click = { onCategorySelected(category) },
                        longClick = { onCategoryLongPressed(category) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EntryInputDialog(
    kind: String,
    category: LedgerCategory,
    amount: String,
    date: Long,
    note: String,
    invalid: Boolean,
    onAmountChange: (String) -> Unit,
    onDateSelected: (Long) -> Unit,
    onNoteChange: (String) -> Unit,
    onInvalidAmount: () -> Unit,
    onInputPanelHeightChanged: (Dp) -> Unit,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val inputWindowActions = remember { InputWindowActions() }
    val amountFocusRequester = remember { FocusRequester() }
    val dateInteractionSource = remember { MutableInteractionSource() }
    val fixedPrimaryDark = colorResource(R.color.app_primary_dark)
    var showDatePicker by remember { mutableStateOf(false) }
    var showCalculatorKeyboard by remember { mutableStateOf(false) }
    var amountHasFocus by remember { mutableStateOf(false) }
    var calculatorSurfaceVisible by remember { mutableStateOf(false) }
    var noteInputRequested by remember { mutableStateOf(false) }
    var noteKeyboardWasVisible by remember { mutableStateOf(false) }
    var noteHasFocus by remember { mutableStateOf(false) }
    val dismissInputInteraction = remember { MutableInteractionSource() }
    val imeBottom = WindowInsets.ime.getBottom(density)
    val navigationBarBottom = WindowInsets.navigationBars.getBottom(density)
    val visibleImeHeight = (imeBottom - navigationBarBottom).coerceAtLeast(0)
    var matchedInputPanelHeightPx by remember {
        mutableIntStateOf(with(density) { CalculatorKeyboardHeight.roundToPx() })
    }
    var retainInputPanelPlacement by remember { mutableStateOf(false) }
    val matchedInputPanelHeight = with(density) { matchedInputPanelHeightPx.toDp() }
    val isSystemKeyboardVisible = imeBottom > navigationBarBottom
    val inputPanelPlacementActive =
        calculatorSurfaceVisible || isSystemKeyboardVisible || retainInputPanelPlacement
    val amountFieldState = rememberCalculatorAmountFieldState(amount)
    val entryInputFieldBorder = Color(0xFF7A7D83)
    val entryInputFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Ink,
        unfocusedTextColor = Ink,
        cursorColor = Ink,
        focusedBorderColor = entryInputFieldBorder,
        unfocusedBorderColor = entryInputFieldBorder,
        focusedLabelColor = Muted,
        unfocusedLabelColor = Muted,
    )
    val collapseInputPanel = {
        inputWindowActions.hideKeyboard()
        noteInputRequested = false
        noteKeyboardWasVisible = false
        showCalculatorKeyboard = false
        calculatorSurfaceVisible = false
    }
    val clearInputFocus = {
        collapseInputPanel()
        inputWindowActions.clearFocus()
    }

    LaunchedEffect(visibleImeHeight) {
        if (visibleImeHeight > 0) {
            // 系统键盘展开期间会连续上报中间高度；只采用稳定后的最终高度，
            // 避免后方分类卡片跟随每个动画帧反复移动。
            delay(InputPanelTransitionDelayMillis)
            matchedInputPanelHeightPx = visibleImeHeight
        }
    }

    LaunchedEffect(showCalculatorKeyboard) {
        if (showCalculatorKeyboard) {
            inputWindowActions.hideKeyboard()
            amountFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(amountHasFocus, showCalculatorKeyboard) {
        if (showCalculatorKeyboard && !amountHasFocus) {
            withFrameNanos { }
            if (showCalculatorKeyboard && !amountHasFocus && !noteHasFocus && !showDatePicker) {
                collapseInputPanel()
                amountFocusRequester.requestFocus()
            }
        }
    }

    LaunchedEffect(noteHasFocus, noteInputRequested, isSystemKeyboardVisible) {
        when {
            !noteHasFocus -> {
                noteInputRequested = false
                noteKeyboardWasVisible = false
            }
            isSystemKeyboardVisible -> {
                noteInputRequested = true
                noteKeyboardWasVisible = true
            }
            noteInputRequested && noteKeyboardWasVisible -> collapseInputPanel()
        }
    }

    LaunchedEffect(showCalculatorKeyboard, noteInputRequested, visibleImeHeight) {
        when {
            showCalculatorKeyboard -> calculatorSurfaceVisible = true
            noteInputRequested && visibleImeHeight > 0 -> {
                // 金额切换到备注时，让计算器保留在系统键盘下方，
                // 只在键盘高度稳定后移除，避免底部面板先消失再弹出。
                delay(InputPanelTransitionDelayMillis)
                calculatorSurfaceVisible = false
            }
            noteInputRequested -> Unit
            else -> calculatorSurfaceVisible = false
        }
    }

    LaunchedEffect(calculatorSurfaceVisible, isSystemKeyboardVisible, matchedInputPanelHeight) {
        if (calculatorSurfaceVisible || isSystemKeyboardVisible) {
            onInputPanelHeightChanged(matchedInputPanelHeight)
        } else {
            // 金额与备注输入互相切换时，另一个面板的可见状态会稍晚建立。
            // 暂时保留上一面板高度；若新面板出现，本次 effect 会被取消。
            delay(InputPanelTransitionDelayMillis)
            onInputPanelHeightChanged(0.dp)
        }
    }

    LaunchedEffect(calculatorSurfaceVisible, isSystemKeyboardVisible) {
        if (calculatorSurfaceVisible || isSystemKeyboardVisible) {
            retainInputPanelPlacement = true
        } else {
            // 计算器关闭到系统键盘 Insets 建立之间存在短暂空档。
            // 在交接结束前保持记账弹窗位置，防止先居中再被键盘顶回。
            delay(InputPanelTransitionDelayMillis)
            retainInputPanelPlacement = false
        }
    }

    Dialog(
        onDismissRequest = {
            if (
                showCalculatorKeyboard || noteInputRequested ||
                calculatorSurfaceVisible || isSystemKeyboardVisible
            ) {
                collapseInputPanel()
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BindInputWindowActions(inputWindowActions)
        Box(
            Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = dismissInputInteraction,
                    indication = null,
                    onClick = collapseInputPanel,
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        bottom = if (inputPanelPlacementActive) {
                            // 计算器与系统键盘必须共用完全相同的弹窗位置。
                            // 不能再按输入类型增减间距，否则金额与备注切换时弹窗会跳动。
                            matchedInputPanelHeight + CalculatorDialogGap
                        }
                        else {
                            0.dp
                        },
                    ),
                contentAlignment = if (inputPanelPlacementActive) {
                    Alignment.BottomCenter
                }
                else {
                    Alignment.Center
                },
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(BookkeepingDialogWidthFraction),
                    shape = RoundedCornerShape(28.dp),
                    color = Bg,
                    shadowElevation = 10.dp,
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Text("${category.symbol}  ${category.name}", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(22.dp))
                        Box(Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                state = amountFieldState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(amountFocusRequester)
                                    .onFocusChanged {
                                        val lostAmountFocus = amountHasFocus && !it.isFocused
                                        amountHasFocus = it.isFocused
                                        if (it.isFocused) {
                                            noteInputRequested = false
                                            inputWindowActions.hideKeyboard()
                                            calculatorSurfaceVisible = true
                                            showCalculatorKeyboard = true
                                        } else if (lostAmountFocus) {
                                            formatEvaluatedAmount(amount)?.let(onAmountChange)
                                        }
                                    },
                                label = { Text("金额") },
                                isError = invalid,
                                supportingText = if (invalid) ({ Text("请输入结果大于 0 的有效算式") }) else null,
                                inputTransformation = RejectCalculatorAmountInput,
                                keyboardOptions = KeyboardOptions(showKeyboardOnFocus = false),
                                lineLimits = TextFieldLineLimits.SingleLine,
                                shape = RoundedCornerShape(14.dp),
                                colors = entryInputFieldColors,
                            )
                            Box(
                                Modifier
                                    .matchParentSize()
                                    .pointerInput(Unit) {
                                        detectTapGestures {
                                            noteInputRequested = false
                                            inputWindowActions.hideKeyboard()
                                            calculatorSurfaceVisible = true
                                            showCalculatorKeyboard = true
                                            amountFocusRequester.requestFocus()
                                        }
                                    },
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Box(
                            Modifier.fillMaxWidth().clickable(
                                interactionSource = dateInteractionSource,
                                indication = null,
                            ) {
                                clearInputFocus()
                                showDatePicker = true
                            },
                        ) {
                            OutlinedTextField(
                                value = day(date),
                                onValueChange = {},
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false,
                                label = { Text("日期") },
                                trailingIcon = { Text("选择") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = Ink,
                                    disabledBorderColor = Color(0xFFB9BBC0),
                                    disabledLabelColor = Muted,
                                    disabledTrailingIconColor = Ink,
                                    disabledContainerColor = Color.Transparent,
                                ),
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        OutlinedTextField(
                            value = note,
                            onValueChange = onNoteChange,
                            modifier = Modifier.fillMaxWidth().onFocusChanged {
                                noteHasFocus = it.isFocused
                                if (it.isFocused) {
                                    noteInputRequested = true
                                    showCalculatorKeyboard = false
                                }
                            },
                            label = { Text("备注") },
                            maxLines = 3,
                            shape = RoundedCornerShape(14.dp),
                            colors = entryInputFieldColors,
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = {
                                val value = evaluateAmountExpression(amount)
                                if (value == null || value <= 0) onInvalidAmount()
                                else if (isDateAfterToday(date)) {
                                    Toast.makeText(context, "不能添加晚于今天的账单", Toast.LENGTH_SHORT).show()
                                }
                                else onSave(value)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = fixedPrimaryDark,
                                contentColor = Color.White,
                            ),
                        ) { Text("完成记账", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }
            if (calculatorSurfaceVisible) {
                AmountCalculatorKeyboard(
                    expression = amount,
                    onExpressionChange = onAmountChange,
                    onInvalidExpression = onInvalidAmount,
                    onDismiss = collapseInputPanel,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(matchedInputPanelHeight),
                )
            }
        }
    }
    if (showDatePicker) {
        MoneyLogDatePickerDialog(
            initialMillis = date,
            onDismiss = { showDatePicker = false },
            onDateSelected = {
                onDateSelected(it)
                showDatePicker = false
            },
        )
    }
}

@Composable
private fun CategoryIconActionDialog(
    category: LedgerCategory,
    dismiss: () -> Unit,
    onEditIcon: () -> Unit,
) {
    val fixedPrimaryDark = colorResource(R.color.app_primary_dark)
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("${category.symbol}  ${category.name}") },
        text = {
            CategoryActionItem(
                symbol = "",
                title = "修改图标",
                subtitle = "为该类型选择新的图标",
                iconResource = R.drawable.ic_edit_bill_color,
                iconBackgroundColor = Color.Transparent,
                preserveIconColors = true,
                expandIconToBackground = true,
                onClick = onEditIcon,
            )
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = dismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = fixedPrimaryDark),
            ) { Text("取消") }
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryCard(
    label: String,
    symbol: String,
    active: Boolean,
    modifier: Modifier = Modifier.fillMaxWidth(),
    click: () -> Unit,
    longClick: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        modifier
            .aspectRatio(1f)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = click,
                onLongClick = longClick,
            ),
        colors = CardDefaults.cardColors(containerColor = if (active) AppPrimary else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(15.dp),
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(symbol, fontSize = 37.sp)
            Spacer(Modifier.height(1.dp))
            Text(
                label,
                color = if (active) AppOnPrimary else Ink,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun CategoryDialog(
    kind: String,
    existing: List<String>,
    initial: LedgerCategory? = null,
    lockName: Boolean = false,
    dismiss: () -> Unit,
    confirm: (LedgerCategory) -> Unit,
) {
    val fixedPrimaryDark = colorResource(R.color.app_primary_dark)
    var input by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    val availableIcons = if (kind == "支出") expenseIcons else incomeIcons
    val icons = remember(kind, initial?.symbol) {
        if (initial != null && initial.symbol !in availableIcons) listOf(initial.symbol) + availableIcons
        else availableIcons
    }
    var selectedIcon by remember(initial) { mutableStateOf(initial?.symbol) }
    val categoryFieldBorder = Color(0xFF7A7D83)
    val categoryFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Ink,
        unfocusedTextColor = Ink,
        cursorColor = Ink,
        focusedBorderColor = categoryFieldBorder,
        unfocusedBorderColor = categoryFieldBorder,
        focusedLabelColor = Muted,
        unfocusedLabelColor = Muted,
    )
    val categorySelectionColors = remember {
        TextSelectionColors(
            handleColor = Color.Transparent,
            backgroundColor = Ink.copy(alpha = 0.14f),
        )
    }
    val name = input.trim()
    val changed = initial == null || name != initial.name || selectedIcon != initial.symbol
    AlertDialog(
        onDismissRequest = dismiss,
        title = {
            Text(
                if (lockName) "修改${kind}类型图标"
                else "${if (initial == null) "添加" else "修改"}${kind}类型"
            )
        },
        text = {
            Column {
                CompositionLocalProvider(LocalTextSelectionColors provides categorySelectionColors) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { if (!lockName && it.length <= 8) input = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(if (lockName) "类型名称（固定）" else "类型名称") },
                        readOnly = lockName,
                        singleLine = true,
                        colors = categoryFieldColors,
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text("选择图标", fontWeight = FontWeight.Medium, color = Ink)
                Text("图标可用于任意类型", fontSize = 12.sp, color = Muted)
                Spacer(Modifier.height(10.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 254.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(icons.size) { index ->
                        val symbol = icons[index]
                        IconChoice(symbol, selectedIcon == symbol) { selectedIcon = symbol }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { confirm(LedgerCategory(name, selectedIcon!!)) },
                enabled = name.isNotEmpty() && name !in existing && selectedIcon != null && changed,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = fixedPrimaryDark,
                    disabledContentColor = Muted.copy(alpha = 0.38f),
                ),
            ) { Text(if (initial == null) "添加" else "保存") }
        },
        dismissButton = {
            TextButton(
                onClick = dismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = fixedPrimaryDark),
            ) { Text("取消") }
        },
    )
}

@Composable
private fun IconChoice(symbol: String, active: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (active) EditCategorySelectedGreen else Bg, RoundedCornerShape(12.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(symbol, fontSize = 27.sp, color = Ink)
    }
}

@Composable
private fun AmountCalculatorKeyboard(
    expression: String,
    onExpressionChange: (String) -> Unit,
    onInvalidExpression: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissInteraction = remember { MutableInteractionSource() }
    val dismissDragThreshold = with(LocalDensity.current) { 24.dp.toPx() }

    fun applyKey(label: String) {
        val nextExpression = when (label) {
            "C" -> ""
            "⌫" -> expression.dropLast(1)
            "+" -> appendAmountOperator(expression, "+")
            "−" -> appendAmountOperator(expression, "-")
            "×" -> appendAmountOperator(expression, "×")
            "÷" -> appendAmountOperator(expression, "÷")
            "." -> appendAmountDecimal(expression)
            "=" -> {
                val result = formatEvaluatedAmount(expression)
                if (result == null) {
                    onInvalidExpression()
                    return
                }
                result
            }
            else -> expression + label
        }
        if (validAmountExpressionInput(nextExpression)) onExpressionChange(nextExpression)
    }

    val rows = listOf(
        listOf("7", "8", "9", "−"),
        listOf("4", "5", "6", "+"),
    )
    Surface(
        modifier = modifier
            .clickable(
                interactionSource = dismissInteraction,
                indication = null,
                onClick = onDismiss,
            )
            .pointerInput(dismissDragThreshold) {
                var totalDragY = 0f
                detectVerticalDragGestures(
                    onDragStart = { totalDragY = 0f },
                    onVerticalDrag = { _, dragAmount -> totalDragY += dragAmount },
                    onDragEnd = {
                        if (totalDragY <= -dismissDragThreshold || totalDragY >= dismissDragThreshold) {
                            onDismiss()
                        }
                    },
                )
            },
        shape = RoundedCornerShape(0.dp),
        color = Color(0xFFF1F3F6),
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    bottom = CalculatorKeyboardBottomPadding,
                ),
        ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CalculatorArrowAreaHeight)
                        .padding(end = 16.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(width = 32.dp, height = CalculatorArrowAreaHeight)
                            .clickable(dismissInteraction, indication = null, onClick = onDismiss),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_keyboard_arrow_down),
                            contentDescription = "收起计算器",
                            modifier = Modifier
                                .offset(x = 1.dp)
                                .size(width = 16.5.dp, height = 8.5.dp),
                            tint = Color(0xFF63666D),
                        )
                    }
                }
                Spacer(Modifier.height(CalculatorArrowToKeysGap))
                rows.forEachIndexed { rowIndex, row ->
                    if (rowIndex > 0) Spacer(Modifier.height(CalculatorKeyVerticalGap))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        row.forEach { label ->
                            CalculatorKeyboardKey(
                                label = label,
                                modifier = Modifier.weight(1f).height(CalculatorKeyHeight),
                                onClick = { applyKey(label) },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(CalculatorKeyVerticalGap))
                Row(
                    modifier = Modifier.fillMaxWidth().height(CalculatorDoubleKeyHeight),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(3f),
                        verticalArrangement = Arrangement.spacedBy(CalculatorKeyVerticalGap),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf("1", "2", "3").forEach { label ->
                                CalculatorKeyboardKey(
                                    label = label,
                                    modifier = Modifier.weight(1f).height(CalculatorKeyHeight),
                                    onClick = { applyKey(label) },
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            CalculatorKeyboardKey(
                                label = ".",
                                modifier = Modifier.weight(1f).height(CalculatorKeyHeight),
                                onClick = { applyKey(".") },
                            )
                            CalculatorKeyboardKey(
                                label = "0",
                                modifier = Modifier.weight(1f).height(CalculatorKeyHeight),
                                onClick = { applyKey("0") },
                            )
                            CalculatorKeyboardKey(
                                label = "⌫",
                                modifier = Modifier.weight(1f).height(CalculatorKeyHeight),
                                onClick = { applyKey("⌫") },
                            )
                        }
                    }
                    CalculatorKeyboardKey(
                        label = "=",
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { applyKey("=") },
                    )
                }
        }
    }
}

@Composable
private fun CalculatorKeyboardKey(
    label: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val fixedPrimaryDark = colorResource(R.color.app_primary_dark)
    val operator = label in setOf("+", "−", "×", "÷")
    val action = label == "C" || label == "⌫"
    val background = when {
        label == "=" -> fixedPrimaryDark
        operator -> Color(0xFFE5EBF2)
        action -> Color(0xFFE7E9ED)
        else -> Color.White
    }
    val foreground = if (label == "=") Color.White else if (operator) fixedPrimaryDark else Ink
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .border(1.dp, NeutralIconBorder, RoundedCornerShape(10.dp))
            .clickable(interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = foreground, fontSize = 20.sp, fontWeight = FontWeight.Medium)
    }
}

internal fun appendAmountDecimal(expression: String): String {
    val operators = setOf('+', '-', '*', '/', '×', '÷')
    val tokenStart = expression.indexOfLast { it in operators } + 1
    val currentToken = expression.substring(tokenStart)
    if ('.' in currentToken) return expression
    return if (currentToken.isEmpty()) expression + "0." else expression + "."
}

internal fun appendAmountOperator(expression: String, operator: String): String {
    if (expression.isEmpty()) return if (operator == "-") "-" else expression
    if (expression.last() == '.') return expression
    val operators = setOf('+', '-', '*', '/', '×', '÷')
    return if (expression.last() in operators) expression.dropLast(1) + operator else expression + operator
}

internal fun validAmountExpressionInput(value: String): Boolean {
    if (value.length > 80) return false
    val operators = setOf('+', '-', '*', '/', '×', '÷')
    if (value.any { !it.isDigit() && it != '.' && it !in operators }) return false

    fun validNumberToken(token: String): Boolean {
        if (token.count { it == '.' } > 1) return false
        val parts = token.split('.')
        return parts[0].length <= 9 && (parts.size == 1 || parts[1].length <= 2)
    }

    var start = 0
    value.forEachIndexed { index, character ->
        if (character in operators) {
            if (index > start && !validNumberToken(value.substring(start, index))) return false
            start = index + 1
        }
    }
    return start >= value.length || validNumberToken(value.substring(start))
}

internal fun evaluateAmountExpression(expression: String): Double? {
    if (!validAmountExpressionInput(expression)) return null
    val input = expression.replace('×', '*').replace('÷', '/')
    if (input.isEmpty()) return null
    var index = 0

    fun parseNumber(): Double? {
        var sign = 1.0
        if (index < input.length && (input[index] == '+' || input[index] == '-')) {
            if (input[index] == '-') sign = -1.0
            index++
        }
        val start = index
        var hasDigit = false
        while (index < input.length && (input[index].isDigit() || input[index] == '.')) {
            if (input[index].isDigit()) hasDigit = true
            index++
        }
        if (!hasDigit) return null
        return input.substring(start, index).toDoubleOrNull()?.times(sign)
    }

    fun parseProduct(): Double? {
        var value = parseNumber() ?: return null
        while (index < input.length && (input[index] == '*' || input[index] == '/')) {
            val operator = input[index++]
            val right = parseNumber() ?: return null
            if (operator == '/' && right == 0.0) return null
            value = if (operator == '*') value * right else value / right
            if (!value.isFinite()) return null
        }
        return value
    }

    var value = parseProduct() ?: return null
    while (index < input.length) {
        val operator = input[index]
        if (operator != '+' && operator != '-') return null
        index++
        val right = parseProduct() ?: return null
        value = if (operator == '+') value + right else value - right
        if (!value.isFinite()) return null
    }
    if (kotlin.math.abs(value) > 999_999_999.99) return null
    return round(value * 100.0) / 100.0
}

private fun formatEvaluatedAmount(expression: String): String? {
    val result = evaluateAmountExpression(expression) ?: return null
    if (result <= 0) return null
    return String.format(Locale.US, "%.2f", result)
}

internal fun isDateAfterToday(date: Long, currentTime: Long = System.currentTimeMillis()): Boolean {
    fun startOfDay(millis: Long) = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    return startOfDay(date) > startOfDay(currentTime)
}

internal fun initialDateForNewEntry(entries: List<Entry>, currentTime: Long = System.currentTimeMillis()): Long {
    val previousEntryDate = entries.lastOrNull()?.date ?: return currentTime
    val previousMonthFirst = Calendar.getInstance().apply {
        timeInMillis = previousEntryDate
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    return if (isDateAfterToday(previousMonthFirst, currentTime)) currentTime else previousMonthFirst
}

@Composable
private fun MoneyLogDatePickerDialog(
    initialMillis: Long,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit,
) {
    val primary = colorResource(R.color.app_primary_dark)
    val currentTime = remember { System.currentTimeMillis() }
    val safeInitialDate = remember(initialMillis, currentTime) {
        if (isDateAfterToday(initialMillis, currentTime)) currentTime else initialMillis
    }
    val initialCalendar = remember(safeInitialDate) {
        Calendar.getInstance().apply { timeInMillis = safeInitialDate }
    }
    val todayCalendar = remember(currentTime) {
        Calendar.getInstance().apply { timeInMillis = currentTime }
    }
    var selectedDate by remember(safeInitialDate) { mutableLongStateOf(safeInitialDate) }
    var displayedYear by remember(safeInitialDate) { mutableIntStateOf(initialCalendar[Calendar.YEAR]) }
    var displayedMonth by remember(safeInitialDate) { mutableIntStateOf(initialCalendar[Calendar.MONTH]) }
    val selectedCalendar = remember(selectedDate) {
        Calendar.getInstance().apply { timeInMillis = selectedDate }
    }
    val weekLabels = remember { listOf("日", "一", "二", "三", "四", "五", "六") }
    val firstDayOffset = remember(displayedYear, displayedMonth) {
        Calendar.getInstance().apply {
            clear()
            set(displayedYear, displayedMonth, 1, 12, 0, 0)
        }[Calendar.DAY_OF_WEEK] - Calendar.SUNDAY
    }
    val daysInMonth = remember(displayedYear, displayedMonth) {
        Calendar.getInstance().apply {
            clear()
            set(displayedYear, displayedMonth, 1, 12, 0, 0)
        }.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    val canShowNextMonth = displayedYear < todayCalendar[Calendar.YEAR] ||
        displayedYear == todayCalendar[Calendar.YEAR] && displayedMonth < todayCalendar[Calendar.MONTH]

    fun shiftDisplayedMonth(offset: Int) {
        val shifted = Calendar.getInstance().apply {
            clear()
            set(displayedYear, displayedMonth, 1, 12, 0, 0)
            add(Calendar.MONTH, offset)
        }
        displayedYear = shifted[Calendar.YEAR]
        displayedMonth = shifted[Calendar.MONTH]
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFF9F9F9),
            shape = RoundedCornerShape(4.dp),
            shadowElevation = 12.dp,
        ) {
            Column {
                Column(
                    Modifier.fillMaxWidth().background(primary).padding(horizontal = 28.dp, vertical = 24.dp),
                ) {
                    Text(
                        text = "${selectedCalendar[Calendar.YEAR]}年",
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${selectedCalendar[Calendar.MONTH] + 1}月${selectedCalendar[Calendar.DAY_OF_MONTH]}日周${weekLabels[selectedCalendar[Calendar.DAY_OF_WEEK] - Calendar.SUNDAY]}",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Column(Modifier.padding(horizontal = 22.dp, vertical = 14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TextButton(onClick = { shiftDisplayedMonth(-1) }) {
                            Text("‹", color = Ink, fontSize = 34.sp, lineHeight = 34.sp)
                        }
                        Text(
                            text = "${displayedYear}年${displayedMonth + 1}月",
                            color = Ink,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        TextButton(
                            onClick = { shiftDisplayedMonth(1) },
                            enabled = canShowNextMonth,
                        ) {
                            Text(
                                "›",
                                color = if (canShowNextMonth) Ink else Muted.copy(alpha = 0.28f),
                                fontSize = 34.sp,
                                lineHeight = 34.sp,
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        weekLabels.forEach { label ->
                            Box(Modifier.weight(1f).height(38.dp), contentAlignment = Alignment.Center) {
                                Text(label, color = Muted, fontSize = 13.sp)
                            }
                        }
                    }
                    repeat(6) { row ->
                        Row(Modifier.fillMaxWidth()) {
                            repeat(7) { column ->
                                val day = row * 7 + column - firstDayOffset + 1
                                if (day in 1..daysInMonth) {
                                    val dayMillis = Calendar.getInstance().apply {
                                        clear()
                                        set(displayedYear, displayedMonth, day, 12, 0, 0)
                                    }.timeInMillis
                                    val enabled = !isDateAfterToday(dayMillis, currentTime)
                                    val isSelected = selectedCalendar[Calendar.YEAR] == displayedYear &&
                                        selectedCalendar[Calendar.MONTH] == displayedMonth &&
                                        selectedCalendar[Calendar.DAY_OF_MONTH] == day
                                    val selectedTextSize by animateFloatAsState(
                                        targetValue = if (isSelected) 20f else 15f,
                                        animationSpec = tween(160),
                                        label = "selectedDateTextSize",
                                    )
                                    Box(
                                        modifier = Modifier.weight(1f).height(46.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) primary else Color.Transparent)
                                                .clickable(enabled = enabled) { selectedDate = dayMillis },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = day.toString(),
                                                color = when {
                                                    isSelected -> Color.White
                                                    enabled -> Ink
                                                    else -> Muted.copy(alpha = 0.32f)
                                                },
                                                fontSize = selectedTextSize.sp,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            )
                                        }
                                    }
                                }
                                else {
                                    Spacer(Modifier.weight(1f).height(46.dp))
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = onDismiss) { Text("取消", color = primary) }
                        Spacer(Modifier.width(10.dp))
                        TextButton(onClick = { onDateSelected(selectedDate) }) { Text("确定", color = primary) }
                    }
                }
            }
        }
    }
}
private fun defaultIcon(value: String) = legacyDefaultIcons[value] ?: "◉"
internal fun entriesForMainBills(entries: List<Entry>, ledgers: List<LedgerBook>): List<Entry> {
    val ledgerVisibility = ledgers.associate { it.id to it.includeInBills }
    return entries.filter { entry ->
        entry.ledgerId == null || ledgerVisibility[entry.ledgerId] != false
    }
}
internal fun updateEntriesCategory(
    entries: List<Entry>,
    kind: String,
    currentName: String,
    updatedName: String,
) = entries.map { entry ->
    if (entry.kind == kind && entry.category == currentName) entry.copy(category = updatedName) else entry
}
internal fun updateSingleEntryCategory(
    entries: List<Entry>,
    entryId: String,
    updatedCategory: String,
) = entries.map { entry ->
    if (entry.id == entryId) entry.copy(category = updatedCategory) else entry
}
internal fun updateEntry(entries: List<Entry>, updatedEntry: Entry) = entries.map { entry ->
    if (entry.id == updatedEntry.id) updatedEntry else entry
}
internal fun deleteEntry(entries: List<Entry>, entryId: String) = entries.filterNot { it.id == entryId }
internal fun groupEntriesByMonth(entries: List<Entry>): List<EntryMonthGroup> =
    entries.sortedByDescending { it.date }
        .groupBy { entry ->
            Calendar.getInstance().apply { timeInMillis = entry.date }.let { calendar ->
                calendar[Calendar.YEAR] to calendar[Calendar.MONTH] + 1
            }
        }
        .map { (yearMonth, monthEntries) ->
            EntryMonthGroup(year = yearMonth.first, month = yearMonth.second, entries = monthEntries)
        }

internal fun calculateMonthlyBillStatistics(entries: List<Entry>): List<MonthlyBillStatistics> =
    groupEntriesByMonth(entries).map { monthGroup ->
        val expenseTotal = monthGroup.entries
            .filter { it.kind == "支出" }
            .sumOf { it.amount }
        val incomeTotal = monthGroup.entries
            .filter { it.kind == "收入" }
            .sumOf { it.amount }
        MonthlyBillStatistics(
            year = monthGroup.year,
            month = monthGroup.month,
            expenseTotal = expenseTotal,
            incomeTotal = incomeTotal,
        )
    }

internal fun calculateAnnualBillStatistics(entries: List<Entry>, year: Int): AnnualBillStatistics {
    val annualEntries = entries.filter { entry ->
        Calendar.getInstance().apply { timeInMillis = entry.date }[Calendar.YEAR] == year
    }
    return AnnualBillStatistics(
        year = year,
        expenseTotal = annualEntries.filter { entry -> entry.kind == "支出" }.sumOf { entry -> entry.amount },
        incomeTotal = annualEntries.filter { entry -> entry.kind == "收入" }.sumOf { entry -> entry.amount },
    )
}

internal fun calculateMonthlyCategoryExpenseStatistics(
    entries: List<Entry>,
    year: Int,
    month: Int,
): List<CategoryExpenseStatistics> = entries
    .filter { entry ->
        if (entry.kind != "支出") return@filter false
        Calendar.getInstance().apply { timeInMillis = entry.date }.let { calendar ->
            calendar[Calendar.YEAR] == year && calendar[Calendar.MONTH] + 1 == month
        }
    }
    .groupBy { entry -> entry.category }
    .map { (category, categoryEntries) ->
        CategoryExpenseStatistics(
            category = category,
            amount = categoryEntries.sumOf { entry -> entry.amount },
        )
    }
    .sortedWith(
        compareByDescending<CategoryExpenseStatistics> { item -> item.amount }
            .thenBy { item -> item.category },
    )

internal fun groupEntriesByDay(entries: List<Entry>): List<EntryDayGroup> =
    entries.sortedByDescending { it.date }
        .groupBy { entry ->
            Calendar.getInstance().apply { timeInMillis = entry.date }.let { calendar ->
                Triple(
                    calendar[Calendar.YEAR],
                    calendar[Calendar.MONTH] + 1,
                    calendar[Calendar.DAY_OF_MONTH],
                )
            }
        }
        .map { (date, dayEntries) ->
            EntryDayGroup(year = date.first, month = date.second, day = date.third, entries = dayEntries)
        }

internal fun filterEntries(entries: List<Entry>, filter: EntryFilter): List<Entry> = when (filter.mode) {
    EntryFilterMode.ALL -> entries
    EntryFilterMode.MONTH -> entries.filter { entry ->
        Calendar.getInstance().apply { timeInMillis = entry.date }.let { calendar ->
            calendar[Calendar.YEAR] == filter.year && calendar[Calendar.MONTH] + 1 == filter.month
        }
    }
}

private fun entryFilterSummary(filter: EntryFilter) = when (filter.mode) {
    EntryFilterMode.ALL -> "全部账单"
    EntryFilterMode.MONTH -> "${filter.year}年${filter.month}月"
}

private fun money(value: Double) = String.format(Locale.getDefault(), "%.2f", value)
private fun day(value: Long) = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA).format(Date(value))
