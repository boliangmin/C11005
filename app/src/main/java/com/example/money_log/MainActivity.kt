package com.example.money_log

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.money_log.ui.theme.Money_LogTheme
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

private val Yellow = Color(0xFFF5CE3E)
private val Bg = Color(0xFFF7F7F7)
private val Ink = Color(0xFF2D3037)
private val Muted = Color(0xFF777A81)
private val IncomeGreen = Color(0xFF2E8B57)
private val Danger = Color(0xFFC43D3D)

private enum class AppPage { DETAILS, ADD, MORE, CATEGORIES }
private const val UncategorizedName = "未分类"
private const val UncategorizedIcon = "⭕"

data class Entry(val id: String, val kind: String, val category: String, val amount: Double, val date: Long)
data class LedgerCategory(val name: String, val symbol: String)
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

internal fun ensureUncategorizedCategory(categories: List<LedgerCategory>): List<LedgerCategory> =
    if (categories.any { it.name == UncategorizedName }) categories
    else categories + LedgerCategory(UncategorizedName, UncategorizedIcon)

private class Store(context: Context) {
    private val p = context.getSharedPreferences("ledger", Context.MODE_PRIVATE)
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
    fun entries() = runCatching {
        val a = JSONArray(p.getString("entries", null)!!)
        List(a.length()) {
            val o = a.getJSONObject(it)
            Entry(o.getString("id"), o.optString("kind", "支出"), o.getString("category"), o.getDouble("amount"), o.getLong("date"))
        }
    }.getOrDefault(emptyList())
    fun saveEntries(value: List<Entry>) {
        val a = JSONArray()
        value.forEach { e ->
            a.put(JSONObject().apply {
                put("id", e.id); put("kind", e.kind); put("category", e.category); put("amount", e.amount); put("date", e.date)
            })
        }
        p.edit().putString("entries", a.toString()).apply()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { Money_LogTheme(darkTheme = false, dynamicColor = false) { CompositionLocalProvider(LocalRippleConfiguration provides null) { LedgerApp() } } }
    }
}

@Composable
private fun LedgerApp() {
    val context = LocalContext.current
    val store = remember { Store(context.applicationContext) }
    val expenses = remember { mutableStateListOf<LedgerCategory>().apply { addAll(store.categories("支出")) } }
    val incomes = remember { mutableStateListOf<LedgerCategory>().apply { addAll(store.categories("收入")) } }
    val entries = remember { mutableStateListOf<Entry>().apply { addAll(store.entries()) } }
    var page by remember { mutableStateOf(AppPage.DETAILS) }
    var addReturnPage by remember { mutableStateOf(AppPage.DETAILS) }
    var addInitialDate by remember { mutableLongStateOf(System.currentTimeMillis()) }

    BackHandler(enabled = page != AppPage.DETAILS) {
        page = when (page) {
            AppPage.ADD -> addReturnPage
            AppPage.CATEGORIES -> AppPage.MORE
            AppPage.MORE -> AppPage.DETAILS
            AppPage.DETAILS -> AppPage.DETAILS
        }
    }

    Surface(Modifier.fillMaxSize(), color = Bg) {
        when (page) {
            AppPage.DETAILS -> DetailsScreen(
                entries = entries,
                expenseCategories = expenses,
                incomeCategories = incomes,
                onAdd = {
                    addReturnPage = AppPage.DETAILS
                    addInitialDate = initialDateForNewEntry(entries)
                    page = AppPage.ADD
                },
                onMore = { page = AppPage.MORE },
                onEditEntryCategory = { entry, category ->
                    val updatedEntries = updateSingleEntryCategory(entries, entry.id, category.name)
                    entries.clear()
                    entries.addAll(updatedEntries)
                    store.saveEntries(entries)
                },
            )
            AppPage.ADD -> AddScreen(
                expenseCategories = expenses,
                incomeCategories = incomes,
                initialDate = addInitialDate,
                onNewCategory = { kind, category ->
                    val target = if (kind == "支出") expenses else incomes
                    target.add(category)
                    store.saveCategories(kind, target)
                },
                onSave = {
                    entries.add(it)
                    store.saveEntries(entries)
                    page = AppPage.DETAILS
                },
            )
            AppPage.MORE -> MoreScreen(
                onDetails = { page = AppPage.DETAILS },
                onAdd = {
                    addReturnPage = AppPage.MORE
                    addInitialDate = initialDateForNewEntry(entries)
                    page = AppPage.ADD
                },
                onViewCategories = { page = AppPage.CATEGORIES },
            )
            AppPage.CATEGORIES -> CategoryOverviewScreen(
                expenseCategories = expenses,
                incomeCategories = incomes,
                onBack = { page = AppPage.MORE },
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
    }
}

@Composable
private fun DetailsScreen(
    entries: List<Entry>,
    expenseCategories: List<LedgerCategory>,
    incomeCategories: List<LedgerCategory>,
    onAdd: () -> Unit,
    onMore: () -> Unit,
    onEditEntryCategory: (Entry, LedgerCategory) -> Unit,
) {
    var editingEntry by remember { mutableStateOf<Entry?>(null) }
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Box(
            Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp, vertical = 18.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text("明细账单", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)
        }
        if (entries.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🧾", fontSize = 52.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("还没有账目", fontSize = 18.sp, color = Muted)
                    Text("点击底部“记账”添加第一笔记录", color = Color(0xFFAAADB3))
                }
            }
        } else {
            LazyColumn(
                Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
            ) {
                items(entries.sortedByDescending { it.date }, key = { it.id }) { entry ->
                    val categories = if (entry.kind == "收入") incomeCategories else expenseCategories
                    val symbol = categories.firstOrNull { it.name == entry.category }?.symbol
                        ?: defaultIcon(entry.category)
                    EntryCard(entry, symbol) { editingEntry = entry }
                }
            }
        }
        BottomBar(
            selected = AppPage.DETAILS,
            onDetails = {},
            onAdd = onAdd,
            onMore = onMore,
        )
    }
    editingEntry?.let { entry ->
        val categories = if (entry.kind == "收入") incomeCategories else expenseCategories
        EntryCategoryDialog(
            entry = entry,
            categories = categories,
            dismiss = { editingEntry = null },
            confirm = { category ->
                onEditEntryCategory(entry, category)
                editingEntry = null
            },
        )
    }
}

@Composable
private fun BottomBar(
    selected: AppPage,
    onDetails: () -> Unit,
    onAdd: () -> Unit,
    onMore: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().height(92.dp).background(Color.White),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomItem(Modifier.weight(1f), "▤", "明细", selected = selected == AppPage.DETAILS, onClick = onDetails)
        BottomItem(Modifier.weight(1f), "+", "记账", center = true, onClick = onAdd)
        BottomItem(Modifier.weight(1f), "☰", "更多", selected = selected == AppPage.MORE, onClick = onMore)
    }
}

@Composable
private fun MoreScreen(onDetails: () -> Unit, onAdd: () -> Unit, onViewCategories: () -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Box(
            Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp, vertical = 18.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text("更多", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)
        }
        Column(Modifier.weight(1f).padding(16.dp)) {
            MoreMenuItem(
                symbol = "▦",
                title = "查看分类",
                subtitle = "查看已添加的支出与收入类型",
                onClick = onViewCategories,
            )
        }
        BottomBar(
            selected = AppPage.MORE,
            onDetails = onDetails,
            onAdd = onAdd,
            onMore = {},
        )
    }
}

@Composable
private fun MoreMenuItem(symbol: String, title: String, subtitle: String, onClick: () -> Unit) {
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
            Modifier.size(48.dp).background(Color(0xFFFFF4C8), CircleShape),
            contentAlignment = Alignment.Center,
        ) { Text(symbol, fontSize = 24.sp, color = Ink) }
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = Ink)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, fontSize = 13.sp, color = Muted)
        }
        Text("›", fontSize = 28.sp, color = Muted)
    }
}

@Composable
private fun CategoryOverviewScreen(
    expenseCategories: List<LedgerCategory>,
    incomeCategories: List<LedgerCategory>,
    onBack: () -> Unit,
    onEditCategory: (String, LedgerCategory, LedgerCategory) -> Unit,
    onDeleteCategory: (String, LedgerCategory) -> Unit,
) {
    var actionTarget by remember { mutableStateOf<CategoryEditTarget?>(null) }
    var editingTarget by remember { mutableStateOf<CategoryEditTarget?>(null) }
    var deleteRequest by remember { mutableStateOf<CategoryDeleteRequest?>(null) }
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onBack) { Text("返回", color = Ink, fontSize = 17.sp) }
            Text("查看分类", Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(64.dp))
        }
        Text(
            "长按分类可以修改或删除类型",
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            color = Muted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                CategoryOverviewSection(
                    title = "支出类型",
                    categories = expenseCategories,
                    iconBackground = Color(0xFFFFF4C8),
                    onLongClick = { actionTarget = CategoryEditTarget("支出", it) },
                )
            }
            item {
                CategoryOverviewSection(
                    title = "收入类型",
                    categories = incomeCategories,
                    iconBackground = Color(0xFFE4F4E9),
                    onLongClick = { actionTarget = CategoryEditTarget("收入", it) },
                )
            }
        }
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
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("${category.symbol}  ${category.name}") },
        text = {
            Column {
                CategoryActionItem(
                    symbol = "✎",
                    title = if (canDelete) "修改类型" else "修改图标",
                    subtitle = if (canDelete) "修改类型名称和图标" else "未分类名称固定，只能修改图标",
                    onClick = onEdit,
                )
                if (canDelete) {
                    Spacer(Modifier.height(10.dp))
                    CategoryActionItem(
                        symbol = "×",
                        title = "删除类型",
                        subtitle = "对应账目将归入未分类",
                        destructive = true,
                        onClick = onDelete,
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(dismiss) { Text("取消") } },
    )
}

@Composable
private fun CategoryActionItem(
    symbol: String,
    title: String,
    subtitle: String,
    destructive: Boolean = false,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val contentColor = if (destructive) Danger else Ink
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Bg, RoundedCornerShape(14.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(42.dp).background(if (destructive) Color(0xFFFFE8E8) else Color(0xFFFFF4C8), CircleShape),
            contentAlignment = Alignment.Center,
        ) { Text(symbol, fontSize = 22.sp, color = contentColor) }
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = contentColor)
            Text(subtitle, fontSize = 13.sp, color = Muted)
        }
        Text("›", fontSize = 26.sp, color = if (destructive) Danger else Muted)
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
                color = Ink,
            )
        },
        confirmButton = {
            TextButton(
                onClick = confirm,
                colors = ButtonDefaults.textButtonColors(contentColor = Danger),
            ) { Text(if (isFinalConfirmation) "确认删除" else "继续") }
        },
        dismissButton = { TextButton(dismiss) { Text("取消") } },
    )
}

@Composable
private fun CategoryOverviewSection(
    title: String,
    categories: List<LedgerCategory>,
    iconBackground: Color,
    onLongClick: (LedgerCategory) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
                Spacer(Modifier.width(8.dp))
                Text("${categories.size} 个", fontSize = 13.sp, color = Muted)
            }
            Spacer(Modifier.height(14.dp))
            categories.chunked(4).forEachIndexed { rowIndex, row ->
                if (rowIndex > 0) Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { category ->
                        CategoryOverviewItem(
                            category = category,
                            iconBackground = iconBackground,
                            modifier = Modifier.weight(1f),
                            onLongClick = { onLongClick(category) },
                        )
                    }
                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryOverviewItem(
    category: LedgerCategory,
    iconBackground: Color,
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
        Box(
            Modifier.size(46.dp).background(iconBackground, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Text(category.symbol, fontSize = 23.sp) }
        Spacer(Modifier.height(5.dp))
        Text(category.name, fontSize = 12.sp, color = Ink, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Composable
private fun BottomItem(
    modifier: Modifier,
    symbol: String,
    label: String,
    selected: Boolean = false,
    center: Boolean = false,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = modifier.fillMaxHeight().clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(54.dp).background(
                color = when { center -> Yellow; selected -> Color(0xFFFFF4C8); else -> Color.Transparent },
                shape = CircleShape,
            ),
            contentAlignment = Alignment.Center,
        ) { Text(symbol, fontSize = if (center) 32.sp else 24.sp, color = Ink) }
        Spacer(Modifier.height(2.dp))
        Text(label, color = if (selected) Ink else Muted, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EntryCard(entry: Entry, symbol: String, onLongClick: () -> Unit) {
    val income = entry.kind == "收入"
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {},
            onLongClick = onLongClick,
        ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(46.dp).background(if (income) Color(0xFFE4F4E9) else Color(0xFFFFF4C8), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Text(symbol, fontSize = 23.sp) }
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(entry.category, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                Text("${entry.kind} · ${day(entry.date)}", fontSize = 13.sp, color = Muted)
            }
            Text(
                "${if (income) "+" else "−"}¥${money(entry.amount)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (income) IncomeGreen else Ink,
            )
        }
    }
}

@Composable
private fun EntryCategoryDialog(
    entry: Entry,
    categories: List<LedgerCategory>,
    dismiss: () -> Unit,
    confirm: (LedgerCategory) -> Unit,
) {
    val currentCategory = categories.firstOrNull { it.name == entry.category }
    var selected by remember(entry.id, entry.category) { mutableStateOf(currentCategory) }
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("修改交易分类") },
        text = {
            Column {
                Text("该交易仍为${entry.kind}，只修改所属分类", fontSize = 13.sp, color = Muted)
                Spacer(Modifier.height(14.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
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
            }
        },
        confirmButton = {
            TextButton(
                onClick = { confirm(selected!!) },
                enabled = selected != null && selected!!.name != entry.category,
            ) { Text("保存") }
        },
        dismissButton = { TextButton(dismiss) { Text("取消") } },
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
            .background(if (active) Yellow else Bg, RoundedCornerShape(12.dp))
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
    onNewCategory: (String, LedgerCategory) -> Unit,
    onSave: (Entry) -> Unit,
) {
    val context = LocalContext.current
    var kind by remember { mutableStateOf("支出") }
    var selected by remember { mutableStateOf<LedgerCategory?>(null) }
    var amount by remember { mutableStateOf("") }
    var date by remember(initialDate) { mutableLongStateOf(initialDate) }
    var showDialog by remember { mutableStateOf(false) }
    var invalid by remember { mutableStateOf(false) }
    val dateInteractionSource = remember { MutableInteractionSource() }
    val categories = if (kind == "支出") expenseCategories else incomeCategories

    BackHandler(enabled = selected != null) {
        selected = null
        amount = ""
        invalid = false
    }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Text("${kind}分类", Modifier.padding(start = 20.dp, top = 20.dp, bottom = 10.dp), color = Muted)
        val rowCount = (categories.size + 4) / 4
        val visibleRows = rowCount.coerceAtMost(4)
        val gridHeight = (visibleRows * 96 + (visibleRows - 1) * 10).dp
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth().height(gridHeight),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(categories.size) { index ->
                val category = categories[index]
                CategoryCard(category.name, category.symbol, selected == category) { selected = category }
            }
            item { CategoryCard("添加", "+", false) { showDialog = true } }
        }
        if (selected == null) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("请选择一个${kind}类型", color = Muted)
            }
        } else {
            Column(Modifier.weight(1f).padding(20.dp)) {
                Text("${selected!!.symbol}  ${selected!!.name}", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(22.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        if (validAmountInput(it)) amount = it
                        invalid = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("金额") },
                    prefix = { Text("¥ ") },
                    isError = invalid,
                    supportingText = if (invalid) ({ Text("请输入大于 0 的金额") }) else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.height(14.dp))
                Box(Modifier.fillMaxWidth().clickable(interactionSource = dateInteractionSource, indication = null) { pickDate(context, date) { date = it } }) {
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
            }
            Button(
                onClick = {
                    val value = amount.toDoubleOrNull()
                    if (value == null || value <= 0) invalid = true
                    else if (isDateAfterToday(date)) {
                        Toast.makeText(context, "不能添加晚于今天的账单", Toast.LENGTH_SHORT).show()
                    }
                    else onSave(Entry(UUID.randomUUID().toString(), kind, selected!!.name, value, date))
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp).height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = Ink),
            ) { Text("完成记账", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        }
        TabRow(
            selectedTabIndex = if (kind == "支出") 0 else 1,
            modifier = Modifier.navigationBarsPadding(),
            containerColor = Color.White,
            contentColor = Ink,
        ) {
            listOf("支出", "收入").forEach { option ->
                Tab(
                    selected = kind == option,
                    onClick = { kind = option; selected = null; invalid = false },
                    text = { Text(option, fontSize = 18.sp, fontWeight = if (kind == option) FontWeight.Bold else FontWeight.Normal) },
                )
            }
        }
    }
    if (showDialog) {
        CategoryDialog(
            kind = kind,
            existing = categories.map { it.name },
            dismiss = { showDialog = false },
            confirm = { onNewCategory(kind, it); selected = it; showDialog = false },
        )
    }
}

@Composable
private fun CategoryCard(label: String, symbol: String, active: Boolean, click: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        Modifier.fillMaxWidth().height(96.dp).clickable(interactionSource = interactionSource, indication = null, onClick = click),
        colors = CardDefaults.cardColors(containerColor = if (active) Yellow else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(symbol, fontSize = 28.sp)
            Text(label, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
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
    var input by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    val availableIcons = if (kind == "支出") expenseIcons else incomeIcons
    val icons = remember(kind, initial?.symbol) {
        if (initial != null && initial.symbol !in availableIcons) listOf(initial.symbol) + availableIcons
        else availableIcons
    }
    var selectedIcon by remember(initial) { mutableStateOf(initial?.symbol) }
    val name = input.trim()
    val changed = initial == null || name != initial.name || selectedIcon != initial.symbol
    AlertDialog(
        onDismissRequest = dismiss,
        title = {
            Text(
                if (lockName) "修改未分类图标"
                else "${if (initial == null) "添加" else "修改"}${kind}类型"
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = input,
                    onValueChange = { if (!lockName && it.length <= 8) input = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (lockName) "类型名称（固定）" else "类型名称") },
                    readOnly = lockName,
                    singleLine = true,
                )
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
            ) { Text(if (initial == null) "添加" else "保存") }
        },
        dismissButton = { TextButton(dismiss) { Text("取消") } },
    )
}

@Composable
private fun IconChoice(symbol: String, active: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (active) Yellow else Bg, RoundedCornerShape(12.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(symbol, fontSize = 27.sp)
    }
}

private fun validAmountInput(value: String): Boolean {
    if (value.any { !it.isDigit() && it != '.' } || value.count { it == '.' } > 1) return false
    val parts = value.split('.')
    return parts[0].length <= 9 && (parts.size == 1 || parts[1].length <= 2)
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

private fun pickDate(
    context: Context,
    millis: Long,
    currentTime: Long = System.currentTimeMillis(),
    selected: (Long) -> Unit,
) {
    val initialDate = if (isDateAfterToday(millis, currentTime)) currentTime else millis
    val c = Calendar.getInstance().apply { timeInMillis = initialDate }
    val maximumDate = Calendar.getInstance().apply {
        timeInMillis = currentTime
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis
    DatePickerDialog(context, { _, y, m, d ->
        selected(Calendar.getInstance().apply { set(y, m, d, 12, 0, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis)
    }, c[Calendar.YEAR], c[Calendar.MONTH], c[Calendar.DAY_OF_MONTH]).apply {
        datePicker.maxDate = maximumDate
    }.show()
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
private fun money(value: Double) = String.format(Locale.getDefault(), "%.2f", value)
private fun day(value: Long) = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA).format(Date(value))
