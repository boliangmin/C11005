package com.example.money_log

import android.content.Context
import android.os.Bundle
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.money_log.ui.theme.Money_LogTheme
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round
import kotlin.math.roundToInt

private val Bg = Color(0xFFF7F7F7)
private val Ink = Color(0xFF2D3037)
private val IncomeAmountGreen = Color(0xFF57B881)
private val Muted = Color(0xFF777A81)
private val Danger = Color(0xFFC43D3D)
private val DeleteSwipeBackground = Color(0xFFE57373)
private val DeleteActionRed = Color(0xFFE53935)
private val NeutralIconBorder = Color(0xFFE5E5E5)
private val EditCategorySelectedGreen = Color(0xFFC8E6C9)
private const val GoldenRatio = 1.618f
private const val BookkeepingDialogWidthFraction = 0.8f
private val CalculatorKeyboardHeight = 340.dp

internal enum class ColorTheme(
    val storageKey: String,
    val title: String,
    val accent: Color,
    val softAccent: Color,
    val subtleAccent: Color,
    val accentDark: Color,
    val onAccent: Color = Ink,
) {
    CLASSIC(
        storageKey = "classic",
        title = "经典黄",
        accent = Color(0xFFF5CE3E),
        softAccent = Color(0xFFFFF4C8),
        subtleAccent = Color(0xFFFFF1B8),
        accentDark = Color(0xFF8A6A20),
    ),
    FRESH_GREEN(
        storageKey = "fresh_green",
        title = "清新绿",
        accent = Color(0xFF57B881),
        softAccent = Color(0xFFE4F4E9),
        subtleAccent = Color(0xFFEBF8F0),
        accentDark = Color(0xFF2F7653),
    ),
    SKY_BLUE(
        storageKey = "sky_blue",
        title = "天空蓝",
        accent = Color(0xFF5B9DF9),
        softAccent = Color(0xFFE7F0FF),
        subtleAccent = Color(0xFFEEF5FF),
        accentDark = Color(0xFF315F9F),
    ),
    SOFT_PURPLE(
        storageKey = "soft_purple",
        title = "柔和紫",
        accent = Color(0xFF9B7DE2),
        softAccent = Color(0xFFF0EAFB),
        subtleAccent = Color(0xFFF5F1FC),
        accentDark = Color(0xFF61469D),
    ),
    CORAL_ORANGE(
        storageKey = "coral_orange",
        title = "珊瑚橙",
        accent = Color(0xFFF28B70),
        softAccent = Color(0xFFFFECE7),
        subtleAccent = Color(0xFFFFF2EE),
        accentDark = Color(0xFF9D4F3D),
    ),
}

internal fun colorThemeFromStorage(storageKey: String?): ColorTheme =
    ColorTheme.entries.firstOrNull { it.storageKey == storageKey } ?: ColorTheme.CLASSIC

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

private val LocalColorTheme = staticCompositionLocalOf { ColorTheme.CLASSIC }

private enum class AppPage { DETAILS, ADD, TITLE_BAR_STYLE, CATEGORIES }
private enum class DetailsSection { BILLS, LEDGER, STATISTICS, MORE }
private const val UncategorizedName = "未分类"
private const val UncategorizedIcon = "⭕"

data class Entry(
    val id: String,
    val kind: String,
    val category: String,
    val amount: Double,
    val date: Long,
    val note: String = "",
)
data class LedgerCategory(val name: String, val symbol: String)
internal data class EntryMonthGroup(val year: Int, val month: Int, val entries: List<Entry>)
internal data class EntryDayGroup(val year: Int, val month: Int, val day: Int, val entries: List<Entry>)
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
            })
        }
        return a.toString()
    }

    private fun saveEntryList(key: String, value: List<Entry>) =
        p.edit().putString(key, entryListJson(value)).apply()

    fun entries() = readEntries("entries")
    fun saveEntries(value: List<Entry>) = saveEntryList("entries", value)
    fun colorTheme() = colorThemeFromStorage(p.getString("colorTheme", null))
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
    val colorTheme = remember { store.colorTheme() }
    var titleBarStyle by remember { mutableStateOf(store.titleBarStyle()) }
    val appTextSelectionColors = remember {
        TextSelectionColors(
            handleColor = Color.Transparent,
            backgroundColor = Ink.copy(alpha = 0.14f),
        )
    }
    Money_LogTheme(
        darkTheme = false,
        dynamicColor = false,
        primaryColor = colorTheme.accent,
        onPrimaryColor = colorTheme.onAccent,
        primaryContainerColor = colorTheme.softAccent,
        onPrimaryContainerColor = colorTheme.accentDark,
    ) {
        CompositionLocalProvider(
            LocalColorTheme provides colorTheme,
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
    var page by remember { mutableStateOf(AppPage.DETAILS) }
    var addReturnPage by remember { mutableStateOf(AppPage.DETAILS) }
    var addInitialDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var addButtonXFraction by remember { mutableFloatStateOf(1f) }
    var addButtonYFraction by remember { mutableFloatStateOf(1f) }
    var selectedSection by remember { mutableStateOf(DetailsSection.BILLS) }

    BackHandler(enabled = page != AppPage.DETAILS) {
        page = when (page) {
            AppPage.ADD -> addReturnPage
            AppPage.CATEGORIES -> AppPage.DETAILS
            AppPage.TITLE_BAR_STYLE -> AppPage.DETAILS
            AppPage.DETAILS -> AppPage.DETAILS
        }
    }

    Surface(Modifier.fillMaxSize(), color = Bg) {
        Box(Modifier.fillMaxSize()) {
            when (page) {
            AppPage.DETAILS, AppPage.ADD -> DetailsScreen(
                entries = entries,
                expenseCategories = expenses,
                incomeCategories = incomes,
                titleBarStyle = selectedTitleBarStyle,
                selectedSection = selectedSection,
                onSectionSelected = { selectedSection = it },
                onAdd = {
                    addReturnPage = AppPage.DETAILS
                    addInitialDate = initialDateForNewEntry(entries)
                    page = AppPage.ADD
                },
                onManageCategories = { page = AppPage.CATEGORIES },
                onTitleBarStyle = { page = AppPage.TITLE_BAR_STYLE },
                addButtonXFraction = addButtonXFraction,
                addButtonYFraction = addButtonYFraction,
                onAddButtonPositionChange = { xFraction, yFraction ->
                    addButtonXFraction = xFraction
                    addButtonYFraction = yFraction
                },
                onUpdateEntry = { updatedEntry ->
                    val updatedEntries = updateEntry(entries, updatedEntry)
                    entries.clear()
                    entries.addAll(updatedEntries)
                    store.saveEntries(entries)
                },
                onDeleteEntry = { entry ->
                    val updatedEntries = deleteEntry(entries, entry.id)
                    entries.clear()
                    entries.addAll(updatedEntries)
                    store.saveEntries(entries)
                },
            )
            AppPage.TITLE_BAR_STYLE -> TitleBarStyleScreen(
                selectedStyle = selectedTitleBarStyle,
                onStyleSelected = onTitleBarStyleSelected,
            )
            AppPage.CATEGORIES -> CategoryOverviewScreen(
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
            if (page == AppPage.ADD) {
                AddScreen(
                    expenseCategories = expenses,
                    incomeCategories = incomes,
                    initialDate = addInitialDate,
                    onDismiss = { page = addReturnPage },
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
    expenseCategories: List<LedgerCategory>,
    incomeCategories: List<LedgerCategory>,
    titleBarStyle: TitleBarStyle,
    selectedSection: DetailsSection,
    onSectionSelected: (DetailsSection) -> Unit,
    onAdd: () -> Unit,
    onManageCategories: () -> Unit,
    onTitleBarStyle: () -> Unit,
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

    LaunchedEffect(entryFilter, titleBarStyle, selectedSection) {
        if (selectedSection == DetailsSection.BILLS) {
            listState.scrollToItem(0)
        }
        autoHeaderOffsetPx = 0f
    }
    val fixedPrimaryDark = colorResource(R.color.theme_primary_dark)
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
                        selectedSection = selectedSection,
                        onSectionSelected = { section ->
                            onSectionSelected(section)
                            revealedEntryId = null
                        },
                        onBillLongClick = { showBillOptions = true },
                    )
                }
            }
            if (selectedSection == DetailsSection.MORE) {
                MoreContent(
                    modifier = Modifier.weight(1f),
                    onManageCategories = onManageCategories,
                    onTitleBarStyle = onTitleBarStyle,
                )
            } else if (selectedSection != DetailsSection.BILLS) {
                Spacer(Modifier.weight(1f))
            } else if (entries.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🧾", fontSize = 52.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("还没有账目", fontSize = 18.sp, color = Muted)
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
                    Modifier.weight(1f).padding(horizontal = 16.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 92.dp),
                ) {
                    dayGroups.forEach { dayGroup ->
                        item(key = "day-${dayGroup.year}-${dayGroup.month}-${dayGroup.day}") {
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

@Composable
private fun DetailsFeatureBar(
    selectedSection: DetailsSection,
    onSectionSelected: (DetailsSection) -> Unit,
    onBillLongClick: () -> Unit,
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
                    DetailsFeatureItem(
                        Modifier.weight(1f),
                        R.drawable.ic_bill_outline,
                        "账单",
                        selected = selectedSection == DetailsSection.BILLS,
                        onClick = { onSectionSelected(DetailsSection.BILLS) },
                        onLongClick = onBillLongClick,
                    )
                    DetailsFeatureItem(
                        Modifier.weight(1f),
                        R.drawable.ic_ledger_outline,
                        "账本",
                        selected = selectedSection == DetailsSection.LEDGER,
                        onClick = { onSectionSelected(DetailsSection.LEDGER) },
                    )
                    DetailsFeatureItem(
                        Modifier.weight(1f),
                        R.drawable.ic_statistics_outline,
                        "统计",
                        selected = selectedSection == DetailsSection.STATISTICS,
                        onClick = { onSectionSelected(DetailsSection.STATISTICS) },
                    )
                    DetailsFeatureItem(
                        Modifier.weight(1f),
                        R.drawable.ic_more_outline,
                        "更多",
                        selected = selectedSection == DetailsSection.MORE,
                        onClick = { onSectionSelected(DetailsSection.MORE) },
                    )
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
                DetailsFeatureItem(
                    Modifier.weight(1f),
                    R.drawable.ic_bill_outline,
                    "账单",
                    selected = selectedSection == DetailsSection.BILLS,
                    onClick = { onSectionSelected(DetailsSection.BILLS) },
                    onLongClick = onBillLongClick,
                )
                DetailsFeatureItem(
                    Modifier.weight(1f),
                    R.drawable.ic_ledger_outline,
                    "账本",
                    selected = selectedSection == DetailsSection.LEDGER,
                    onClick = { onSectionSelected(DetailsSection.LEDGER) },
                )
                DetailsFeatureItem(
                    Modifier.weight(1f),
                    R.drawable.ic_statistics_outline,
                    "统计",
                    selected = selectedSection == DetailsSection.STATISTICS,
                    onClick = { onSectionSelected(DetailsSection.STATISTICS) },
                )
                DetailsFeatureItem(
                    Modifier.weight(1f),
                    R.drawable.ic_more_outline,
                    "更多",
                    selected = selectedSection == DetailsSection.MORE,
                    onClick = { onSectionSelected(DetailsSection.MORE) },
                )
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
    val fixedPrimaryDark = colorResource(R.color.theme_primary_dark)
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
    val colorTheme = LocalColorTheme.current
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
                            if (mode == EntryFilterMode.MONTH) colorTheme.softAccent else Bg,
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
                                    colorTheme.accent.copy(alpha = 0.24f)
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
    val colorTheme = LocalColorTheme.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) colorTheme.softAccent else Bg, RoundedCornerShape(14.dp))
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
    val selectionColor = Color(0xFF315F9F)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) Color(0xFFEEF5FF) else Color.White, RoundedCornerShape(16.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(48.dp).background(Color.White, CircleShape),
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
            color = selectionColor,
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
    val fixedPrimaryDark = colorResource(R.color.theme_primary_dark)
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
    val colorTheme = LocalColorTheme.current
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
                iconBackgroundColor ?: colorTheme.softAccent,
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
    val colorTheme = LocalColorTheme.current
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
                        Text(entry.note, fontSize = 12.sp, color = colorTheme.accentDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Text(
                    "${if (income) "+" else "−"}¥${money(entry.amount)}",
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
    val fixedPrimaryDark = colorResource(R.color.theme_primary_dark)
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
    val focusManager = LocalFocusManager.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val calculatorFocusRequester = remember { FocusRequester() }
    val fixedPrimaryDark = colorResource(R.color.theme_primary_dark)
    val currentCategory = categories.firstOrNull { it.name == entry.category }
    var selected by remember(entry.id, entry.category) { mutableStateOf(currentCategory) }
    var amount by remember(entry.id) { mutableStateOf(entry.amount.toString()) }
    var date by remember(entry.id) { mutableLongStateOf(entry.date) }
    var note by remember(entry.id) { mutableStateOf(entry.note) }
    var invalid by remember(entry.id) { mutableStateOf(false) }
    var showDatePicker by remember(entry.id) { mutableStateOf(false) }
    var showCalculatorKeyboard by remember(entry.id) { mutableStateOf(false) }
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
        focusedPrefixColor = Ink,
        unfocusedPrefixColor = Ink,
    )
    val editSelectionColors = remember {
        TextSelectionColors(
            handleColor = Color.Transparent,
            backgroundColor = Ink.copy(alpha = 0.14f),
        )
    }
    val calculatedAmount = remember(amount) { evaluateAmountExpression(amount) }
    val changed = selected?.name != entry.category || calculatedAmount != entry.amount ||
        date != entry.date || note.trim() != entry.note
    val dismissInput = {
        focusManager.clearFocus(force = true)
        softwareKeyboardController?.hide()
        showCalculatorKeyboard = false
    }

    LaunchedEffect(showCalculatorKeyboard) {
        if (showCalculatorKeyboard) {
            focusManager.clearFocus(force = true)
            softwareKeyboardController?.hide()
            calculatorFocusRequester.requestFocus()
        }
    }

    BackHandler(enabled = showCalculatorKeyboard) { dismissInput() }

    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("修改账单") },
        text = {
            Column(
                Modifier
                    .focusRequester(calculatorFocusRequester)
                    .focusable()
                    .verticalScroll(rememberScrollState())
                    .clickable(
                        interactionSource = dismissInputInteraction,
                        indication = null,
                        onClick = dismissInput,
                    ),
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
                CompositionLocalProvider(LocalTextSelectionColors provides editSelectionColors) {
                    Box(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("金额") },
                        prefix = { Text("¥ ") },
                        isError = invalid,
                        supportingText = if (invalid) ({ Text("请输入结果大于 0 的有效算式") }) else null,
                        readOnly = true,
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = editFieldColors,
                    )
                        Box(
                            Modifier.matchParentSize().clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                dismissInput()
                                showCalculatorKeyboard = true
                            },
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Box(
                    Modifier.fillMaxWidth().clickable(
                        interactionSource = dateInteractionSource,
                        indication = null,
                    ) {
                        dismissInput()
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
                CompositionLocalProvider(LocalTextSelectionColors provides editSelectionColors) {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { if (it.length <= 100) note = it },
                        modifier = Modifier.fillMaxWidth().onFocusChanged {
                            if (it.isFocused) {
                                showCalculatorKeyboard = false
                            }
                        },
                        label = { Text("备注") },
                        maxLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        colors = editFieldColors,
                    )
                }
                if (showCalculatorKeyboard) {
                    AmountCalculatorKeyboardPopup(
                        expression = amount,
                        onExpressionChange = {
                            amount = it
                            invalid = false
                        },
                        onInvalidExpression = { invalid = true },
                        onDismiss = { showCalculatorKeyboard = false },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val value = evaluateAmountExpression(amount)
                    if (value == null || value <= 0) invalid = true
                    else if (isDateAfterToday(date)) {
                        Toast.makeText(context, "不能添加晚于今天的账单", Toast.LENGTH_SHORT).show()
                    }
                    else confirm(entry.copy(category = selected!!.name, amount = value, date = date, note = note.trim()))
                },
                enabled = selected != null && changed,
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
                "将永久删除${day(entry.date)}的${entry.category}账单，金额为¥${money(entry.amount)}。删除后无法恢复。",
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
private fun AddScreen(
    expenseCategories: List<LedgerCategory>,
    incomeCategories: List<LedgerCategory>,
    initialDate: Long,
    onDismiss: () -> Unit,
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
    val selectedCategory = selected
    val initialPagerPage = remember { (Int.MAX_VALUE / 2).let { it - it % 2 } }
    val pagerState = rememberPagerState(initialPage = initialPagerPage) { Int.MAX_VALUE }
    val pagerFlingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        snapAnimationSpec = tween(durationMillis = 650),
    )
    val colorTheme = LocalColorTheme.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
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
                                    if (pagerState.currentPage % 2 == pageIndex) colorTheme.accentDark
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
            onDismiss = {
                selected = null
                amount = ""
                date = initialDate
                note = ""
                invalid = false
            },
            onSave = { value ->
                onSave(Entry(UUID.randomUUID().toString(), kind, selectedCategory.name, value, date, note.trim()))
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
}

@Composable
private fun CategorySelectionSection(
    title: String,
    categories: List<LedgerCategory>,
    modifier: Modifier = Modifier,
    onCategorySelected: (LedgerCategory) -> Unit,
    onCategoryLongPressed: (LedgerCategory) -> Unit,
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
            items(categories.size) { index ->
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
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val calculatorFocusRequester = remember { FocusRequester() }
    val dateInteractionSource = remember { MutableInteractionSource() }
    val colorTheme = LocalColorTheme.current
    var showDatePicker by remember { mutableStateOf(false) }
    var showCalculatorKeyboard by remember { mutableStateOf(false) }
    val dismissInputInteraction = remember { MutableInteractionSource() }
    val dismissInputDragThreshold = with(density) { 24.dp.toPx() }
    val imeBottom = WindowInsets.ime.getBottom(density)
    val navigationBarBottom = WindowInsets.navigationBars.getBottom(density)
    val visibleImeHeight = (imeBottom - navigationBarBottom).coerceAtLeast(0)
    var matchedInputPanelHeightPx by remember {
        mutableIntStateOf(with(density) { CalculatorKeyboardHeight.roundToPx() })
    }
    val matchedInputPanelHeight = with(density) { matchedInputPanelHeightPx.toDp() }
    val isSystemKeyboardVisible = imeBottom > navigationBarBottom
    val entryInputFieldBorder = Color(0xFF7A7D83)
    val entryInputFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Ink,
        unfocusedTextColor = Ink,
        cursorColor = Ink,
        focusedBorderColor = entryInputFieldBorder,
        unfocusedBorderColor = entryInputFieldBorder,
        focusedLabelColor = Muted,
        unfocusedLabelColor = Muted,
        focusedPrefixColor = Ink,
        unfocusedPrefixColor = Ink,
    )
    val dismissInput = {
        focusManager.clearFocus(force = true)
        softwareKeyboardController?.hide()
        showCalculatorKeyboard = false
    }

    LaunchedEffect(visibleImeHeight) {
        if (visibleImeHeight > 0) {
            matchedInputPanelHeightPx = visibleImeHeight
        }
    }

    LaunchedEffect(showCalculatorKeyboard) {
        if (showCalculatorKeyboard) {
            focusManager.clearFocus(force = true)
            softwareKeyboardController?.hide()
            calculatorFocusRequester.requestFocus()
        }
    }

    BackHandler(enabled = showCalculatorKeyboard) { dismissInput() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .focusRequester(calculatorFocusRequester)
                .focusable()
                .clickable(
                    interactionSource = dismissInputInteraction,
                    indication = null,
                    onClick = dismissInput,
                )
                .pointerInput(dismissInputDragThreshold) {
                    var totalDragY = 0f
                    detectVerticalDragGestures(
                        onDragStart = { totalDragY = 0f },
                        onVerticalDrag = { _, dragAmount -> totalDragY += dragAmount },
                        onDragEnd = {
                            if (totalDragY <= -dismissInputDragThreshold ||
                                totalDragY >= dismissInputDragThreshold
                            ) {
                                dismissInput()
                            }
                        },
                    )
                },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        bottom = if (showCalculatorKeyboard || isSystemKeyboardVisible) {
                            matchedInputPanelHeight
                        }
                        else {
                            0.dp
                        },
                    ),
                contentAlignment = if (showCalculatorKeyboard || isSystemKeyboardVisible) {
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
                                value = amount,
                                onValueChange = {},
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("金额") },
                                prefix = { Text("¥ ") },
                                isError = invalid,
                                supportingText = if (invalid) ({ Text("请输入结果大于 0 的有效算式") }) else null,
                                readOnly = true,
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = entryInputFieldColors,
                            )
                            Box(
                                Modifier.matchParentSize().clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) {
                                    dismissInput()
                                    showCalculatorKeyboard = true
                                },
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Box(
                            Modifier.fillMaxWidth().clickable(
                                interactionSource = dateInteractionSource,
                                indication = null,
                            ) {
                                dismissInput()
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
                                if (it.isFocused) {
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
                            colors = ButtonDefaults.buttonColors(containerColor = colorTheme.accent, contentColor = colorTheme.onAccent),
                        ) { Text("完成记账", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }
            if (showCalculatorKeyboard) {
                AmountCalculatorKeyboard(
                    expression = amount,
                    onExpressionChange = onAmountChange,
                    onInvalidExpression = onInvalidAmount,
                    onDismiss = { showCalculatorKeyboard = false },
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
    val fixedPrimaryDark = colorResource(R.color.theme_primary_dark)
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
    val colorTheme = LocalColorTheme.current
    Card(
        modifier
            .aspectRatio(1f)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = click,
                onLongClick = longClick,
            ),
        colors = CardDefaults.cardColors(containerColor = if (active) colorTheme.accent else Color.White),
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
                color = if (active) colorTheme.onAccent else Ink,
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
    val fixedPrimaryDark = colorResource(R.color.theme_primary_dark)
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
                val result = evaluateAmountExpression(expression)
                if (result == null || result <= 0) {
                    onInvalidExpression()
                    return
                }
                String.format(Locale.US, "%.2f", result)
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
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = Color(0xFFF1F3F6),
        shadowElevation = 12.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(36.dp)
                            .clickable(dismissInteraction, indication = null, onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_expand_less),
                            contentDescription = "收起计算器",
                            modifier = Modifier.size(24.dp).rotate(180f),
                            tint = Muted,
                        )
                    }
                }
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        row.forEach { label ->
                            CalculatorKeyboardKey(
                                label = label,
                                modifier = Modifier.weight(1f).height(56.dp),
                                onClick = { applyKey(label) },
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().height(124.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(3f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf("1", "2", "3").forEach { label ->
                                CalculatorKeyboardKey(
                                    label = label,
                                    modifier = Modifier.weight(1f).height(56.dp),
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
                                modifier = Modifier.weight(1f).height(56.dp),
                                onClick = { applyKey(".") },
                            )
                            CalculatorKeyboardKey(
                                label = "0",
                                modifier = Modifier.weight(1f).height(56.dp),
                                onClick = { applyKey("0") },
                            )
                            CalculatorKeyboardKey(
                                label = "⌫",
                                modifier = Modifier.weight(1f).height(56.dp),
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
private fun AmountCalculatorKeyboardPopup(
    expression: String,
    onExpressionChange: (String) -> Unit,
    onInvalidExpression: () -> Unit,
    onDismiss: () -> Unit,
) {
    Popup(
        alignment = Alignment.BottomCenter,
        properties = PopupProperties(focusable = false, clippingEnabled = false),
    ) {
        AmountCalculatorKeyboard(
            expression = expression,
            onExpressionChange = onExpressionChange,
            onInvalidExpression = onInvalidExpression,
            onDismiss = onDismiss,
            modifier = Modifier.fillMaxWidth().height(CalculatorKeyboardHeight),
        )
    }
}

@Composable
private fun CalculatorKeyboardKey(
    label: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val fixedPrimaryDark = colorResource(R.color.theme_primary_dark)
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
    val primary = colorResource(R.color.theme_primary_dark)
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
