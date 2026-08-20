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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.money_log.ui.theme.Money_LogTheme
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

private val Yellow = Color(0xFFF5CE3E)
private val Bg = Color(0xFFF7F7F7)
private val Ink = Color(0xFF2D3037)
private val Muted = Color(0xFF777A81)
private val IncomeGreen = Color(0xFF2E8B57)
private val Danger = Color(0xFFC43D3D)
private val NoteAccent = Color(0xFF8A6A20)

private enum class AppPage { DETAILS, ADD, MORE, CATEGORIES, TRASH }
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
internal data class EntryTransferResult(val activeEntries: List<Entry>, val trashEntries: List<Entry>)
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
    fun trashEntries() = readEntries("trashEntries")
    fun saveTrashEntries(value: List<Entry>) = saveEntryList("trashEntries", value)
    fun saveEntryCollections(activeEntries: List<Entry>, trashEntries: List<Entry>) {
        p.edit()
            .putString("entries", entryListJson(activeEntries))
            .putString("trashEntries", entryListJson(trashEntries))
            .apply()
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
    val trashEntries = remember { mutableStateListOf<Entry>().apply { addAll(store.trashEntries()) } }
    var page by remember { mutableStateOf(AppPage.DETAILS) }
    var addReturnPage by remember { mutableStateOf(AppPage.DETAILS) }
    var addInitialDate by remember { mutableLongStateOf(System.currentTimeMillis()) }

    BackHandler(enabled = page != AppPage.DETAILS) {
        page = when (page) {
            AppPage.ADD -> addReturnPage
            AppPage.CATEGORIES -> AppPage.MORE
            AppPage.TRASH -> AppPage.MORE
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
                onUpdateEntry = { updatedEntry ->
                    val updatedEntries = updateEntry(entries, updatedEntry)
                    entries.clear()
                    entries.addAll(updatedEntries)
                    store.saveEntries(entries)
                },
                onDeleteEntry = { entry ->
                    val result = moveEntryToTrash(entries, trashEntries, entry.id)
                    entries.clear()
                    entries.addAll(result.activeEntries)
                    trashEntries.clear()
                    trashEntries.addAll(result.trashEntries)
                    store.saveEntryCollections(entries, trashEntries)
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
            AppPage.MORE -> MoreScreen(
                onDetails = { page = AppPage.DETAILS },
                onAdd = {
                    addReturnPage = AppPage.MORE
                    addInitialDate = initialDateForNewEntry(entries)
                    page = AppPage.ADD
                },
                onViewCategories = { page = AppPage.CATEGORIES },
                onTrash = { page = AppPage.TRASH },
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
                        val updatedTrashEntries = updateEntriesCategory(trashEntries, kind, current.name, safeUpdated.name)
                        entries.clear()
                        entries.addAll(updatedEntries)
                        trashEntries.clear()
                        trashEntries.addAll(updatedTrashEntries)
                        store.saveEntries(entries)
                        store.saveTrashEntries(trashEntries)
                    }
                },
                onDeleteCategory = { kind, category ->
                    val target = if (kind == "支出") expenses else incomes
                    if (category.name != UncategorizedName && target.remove(category)) {
                        val updatedEntries = updateEntriesCategory(entries, kind, category.name, UncategorizedName)
                        val updatedTrashEntries = updateEntriesCategory(trashEntries, kind, category.name, UncategorizedName)
                        entries.clear()
                        entries.addAll(updatedEntries)
                        trashEntries.clear()
                        trashEntries.addAll(updatedTrashEntries)
                        store.saveCategories(kind, target)
                        store.saveEntries(entries)
                        store.saveTrashEntries(trashEntries)
                    }
                },
            )
            AppPage.TRASH -> RecycleBinScreen(
                entries = trashEntries,
                expenseCategories = expenses,
                incomeCategories = incomes,
                onBack = { page = AppPage.MORE },
                onRestore = { entry ->
                    val result = restoreEntryFromTrash(entries, trashEntries, entry.id)
                    entries.clear()
                    entries.addAll(result.activeEntries)
                    trashEntries.clear()
                    trashEntries.addAll(result.trashEntries)
                    store.saveEntryCollections(entries, trashEntries)
                },
                onDeletePermanently = { entry ->
                    val updatedTrash = deleteEntry(trashEntries, entry.id)
                    trashEntries.clear()
                    trashEntries.addAll(updatedTrash)
                    store.saveTrashEntries(trashEntries)
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
    onUpdateEntry: (Entry) -> Unit,
    onDeleteEntry: (Entry) -> Unit,
) {
    var actionEntry by remember { mutableStateOf<Entry?>(null) }
    var editingEntry by remember { mutableStateOf<Entry?>(null) }
    var deleteEntryRequest by remember { mutableStateOf<Entry?>(null) }
    var revealedEntryId by remember { mutableStateOf<String?>(null) }
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
        BottomBar(
            selected = AppPage.DETAILS,
            onDetails = {},
            onAdd = onAdd,
            onMore = onMore,
        )
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
private fun MoreScreen(
    onDetails: () -> Unit,
    onAdd: () -> Unit,
    onViewCategories: () -> Unit,
    onTrash: () -> Unit,
) {
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
            Spacer(Modifier.height(12.dp))
            MoreMenuItem(
                symbol = "♲",
                title = "回收站",
                subtitle = "恢复或彻底删除已删除的账单",
                onClick = onTrash,
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
private fun RecycleBinScreen(
    entries: List<Entry>,
    expenseCategories: List<LedgerCategory>,
    incomeCategories: List<LedgerCategory>,
    onBack: () -> Unit,
    onRestore: (Entry) -> Unit,
    onDeletePermanently: (Entry) -> Unit,
) {
    var permanentDeleteRequest by remember { mutableStateOf<Entry?>(null) }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onBack) { Text("返回", color = Ink, fontSize = 17.sp) }
            Text("回收站", Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(64.dp))
        }
        if (entries.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("♲", fontSize = 52.sp, color = Muted)
                    Spacer(Modifier.height(8.dp))
                    Text("回收站为空", fontSize = 18.sp, color = Muted)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(entries.asReversed(), key = { it.id }) { entry ->
                    val categories = if (entry.kind == "收入") incomeCategories else expenseCategories
                    val symbol = categories.firstOrNull { it.name == entry.category }?.symbol
                        ?: defaultIcon(entry.category)
                    RecycleBinEntryCard(
                        entry = entry,
                        symbol = symbol,
                        onRestore = { onRestore(entry) },
                        onDeletePermanently = { permanentDeleteRequest = entry },
                    )
                }
            }
        }
    }
    permanentDeleteRequest?.let { entry ->
        PermanentlyDeleteEntryDialog(
            entry = entry,
            dismiss = { permanentDeleteRequest = null },
            confirm = {
                onDeletePermanently(entry)
                permanentDeleteRequest = null
            },
        )
    }
}

@Composable
private fun RecycleBinEntryCard(
    entry: Entry,
    symbol: String,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit,
) {
    val income = entry.kind == "收入"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(46.dp).background(if (income) Color(0xFFE4F4E9) else Color(0xFFFFF4C8), CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Text(symbol, fontSize = 23.sp) }
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text(entry.category, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                    Text("${entry.kind} · ${day(entry.date)}", fontSize = 13.sp, color = Muted)
                    if (entry.note.isNotBlank()) {
                        Text(entry.note, fontSize = 12.sp, color = NoteAccent, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Text(
                    "${if (income) "+" else "−"}¥${money(entry.amount)}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (income) IncomeGreen else Ink,
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onDeletePermanently) { Text("彻底删除", color = Danger) }
                Spacer(Modifier.width(8.dp))
                TextButton(
                    onClick = onRestore,
                    colors = ButtonDefaults.textButtonColors(contentColor = Ink),
                ) { Text("恢复") }
            }
        }
    }
}

@Composable
private fun PermanentlyDeleteEntryDialog(entry: Entry, dismiss: () -> Unit, confirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("彻底删除账单？") },
        text = {
            Text(
                "将永久删除${day(entry.date)}的${entry.category}账单，删除后无法恢复。",
                color = Muted,
            )
        },
        confirmButton = {
            TextButton(
                onClick = confirm,
                colors = ButtonDefaults.textButtonColors(contentColor = Danger),
            ) { Text("彻底删除") }
        },
        dismissButton = { TextButton(dismiss) { Text("取消") } },
    )
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
            modifier = Modifier.matchParentSize().background(Color(0xFFFFFAFA)),
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
                Box(
                    Modifier.size(42.dp).background(Color(0xFFFFE8E8), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_remove_circle_outline),
                        contentDescription = "删除账单",
                        modifier = Modifier.size(25.dp),
                        tint = Danger,
                    )
                }
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
                    Modifier.size(46.dp).background(if (income) Color(0xFFE4F4E9) else Color(0xFFFFF4C8), CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Text(symbol, fontSize = 23.sp) }
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text(entry.category, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                    Text("${entry.kind} · ${day(entry.date)}", fontSize = 13.sp, color = Muted)
                    if (entry.note.isNotBlank()) {
                        Text(entry.note, fontSize = 12.sp, color = NoteAccent, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
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
}

@Composable
private fun EntryActionDialog(
    entry: Entry,
    dismiss: () -> Unit,
    onEdit: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("${entry.category}账单") },
        text = {
            CategoryActionItem(
                symbol = "✎",
                title = "修改账单",
                subtitle = "修改分类、金额、日期和备注",
                onClick = onEdit,
            )
        },
        confirmButton = {},
        dismissButton = { TextButton(dismiss) { Text("取消") } },
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
    val currentCategory = categories.firstOrNull { it.name == entry.category }
    var selected by remember(entry.id, entry.category) { mutableStateOf(currentCategory) }
    var amount by remember(entry.id) { mutableStateOf(entry.amount.toString()) }
    var date by remember(entry.id) { mutableLongStateOf(entry.date) }
    var note by remember(entry.id) { mutableStateOf(entry.note) }
    var invalid by remember(entry.id) { mutableStateOf(false) }
    val dateInteractionSource = remember { MutableInteractionSource() }
    val changed = selected?.name != entry.category || amount.toDoubleOrNull() != entry.amount ||
        date != entry.date || note.trim() != entry.note

    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("修改账单") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("账单类型固定为${entry.kind}", fontSize = 13.sp, color = Muted)
                Spacer(Modifier.height(14.dp))
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
                Box(
                    Modifier.fillMaxWidth().clickable(
                        interactionSource = dateInteractionSource,
                        indication = null,
                    ) { pickDate(context, date) { date = it } },
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
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("备注") },
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val value = amount.toDoubleOrNull()
                    if (value == null || value <= 0) invalid = true
                    else if (isDateAfterToday(date)) {
                        Toast.makeText(context, "不能添加晚于今天的账单", Toast.LENGTH_SHORT).show()
                    }
                    else confirm(entry.copy(category = selected!!.name, amount = value, date = date, note = note.trim()))
                },
                enabled = selected != null && changed,
            ) { Text("保存") }
        },
        dismissButton = { TextButton(dismiss) { Text("取消") } },
    )
}

@Composable
private fun DeleteEntryDialog(entry: Entry, dismiss: () -> Unit, confirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("确认删除账单？") },
        text = {
            Text(
                "将删除${day(entry.date)}的${entry.category}账单，金额为¥${money(entry.amount)}。删除后可在回收站恢复。",
                color = Muted,
            )
        },
        confirmButton = {
            TextButton(
                onClick = confirm,
                colors = ButtonDefaults.textButtonColors(contentColor = Danger),
            ) { Text("删除") }
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
    onEditCategoryIcon: (String, LedgerCategory, LedgerCategory) -> Unit,
    onSave: (Entry) -> Unit,
) {
    var kind by remember { mutableStateOf("支出") }
    var selected by remember { mutableStateOf<LedgerCategory?>(null) }
    var amount by remember { mutableStateOf("") }
    var date by remember(initialDate) { mutableLongStateOf(initialDate) }
    var note by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var invalid by remember { mutableStateOf(false) }
    var categoryActionTarget by remember { mutableStateOf<CategoryEditTarget?>(null) }
    var editingIconTarget by remember { mutableStateOf<CategoryEditTarget?>(null) }
    val categories = if (kind == "支出") expenseCategories else incomeCategories
    val selectedCategory = selected

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        CategorySelectionSection(
            title = "支出分类",
            categories = expenseCategories,
            modifier = Modifier.weight(1f),
            onCategorySelected = {
                kind = "支出"
                amount = ""
                date = initialDate
                note = ""
                invalid = false
                selected = it
            },
            onCategoryLongPressed = { categoryActionTarget = CategoryEditTarget("支出", it) },
            onAdd = {
                kind = "支出"
                showDialog = true
            },
        )
        HorizontalDivider(color = Color(0xFFD6D6D8))
        CategorySelectionSection(
            title = "收入分类",
            categories = incomeCategories,
            modifier = Modifier.weight(1f),
            onCategorySelected = {
                kind = "收入"
                amount = ""
                date = initialDate
                note = ""
                invalid = false
                selected = it
            },
            onCategoryLongPressed = { categoryActionTarget = CategoryEditTarget("收入", it) },
            onAdd = {
                kind = "收入"
                showDialog = true
            },
        )
    }
    if (showDialog) {
        CategoryDialog(
            kind = kind,
            existing = categories.map { it.name },
            dismiss = { showDialog = false },
            confirm = {
                onNewCategory(kind, it)
                amount = ""
                date = initialDate
                note = ""
                invalid = false
                selected = it
                showDialog = false
            },
        )
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
                if (validAmountInput(it)) amount = it
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
    onAdd: () -> Unit,
) {
    Column(modifier.fillMaxWidth()) {
        Text(title, Modifier.padding(start = 20.dp, top = 16.dp, bottom = 10.dp), color = Muted)
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
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
            item { CategoryCard("添加", "+", false, onAdd) }
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
    val dateInteractionSource = remember { MutableInteractionSource() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = Bg,
            shadowElevation = 10.dp,
        ) {
            Column(Modifier.padding(24.dp)) {
                Text("${category.symbol}  ${category.name}", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text(kind, color = Muted, fontSize = 13.sp)
                Spacer(Modifier.height(22.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChange,
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
                Box(
                    Modifier.fillMaxWidth().clickable(
                        interactionSource = dateInteractionSource,
                        indication = null,
                    ) { pickDate(context, date, selected = onDateSelected) },
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
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("备注") },
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        val value = amount.toDoubleOrNull()
                        if (value == null || value <= 0) onInvalidAmount()
                        else if (isDateAfterToday(date)) {
                            Toast.makeText(context, "不能添加晚于今天的账单", Toast.LENGTH_SHORT).show()
                        }
                        else onSave(value)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = Ink),
                ) { Text("完成记账", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun CategoryIconActionDialog(
    category: LedgerCategory,
    dismiss: () -> Unit,
    onEditIcon: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text("${category.symbol}  ${category.name}") },
        text = {
            CategoryActionItem(
                symbol = "✎",
                title = "修改图标",
                subtitle = "为该类型选择新的图标",
                onClick = onEditIcon,
            )
        },
        confirmButton = {},
        dismissButton = { TextButton(dismiss) { Text("取消") } },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryCard(
    label: String,
    symbol: String,
    active: Boolean,
    click: () -> Unit,
    longClick: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        Modifier.fillMaxWidth().height(96.dp).combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = click,
            onLongClick = longClick,
        ),
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
                if (lockName) "修改${kind}类型图标"
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
internal fun updateEntry(entries: List<Entry>, updatedEntry: Entry) = entries.map { entry ->
    if (entry.id == updatedEntry.id) updatedEntry else entry
}
internal fun deleteEntry(entries: List<Entry>, entryId: String) = entries.filterNot { it.id == entryId }
internal fun moveEntryToTrash(
    activeEntries: List<Entry>,
    trashEntries: List<Entry>,
    entryId: String,
): EntryTransferResult {
    val entry = activeEntries.firstOrNull { it.id == entryId }
        ?: return EntryTransferResult(activeEntries, trashEntries)
    val updatedTrash = if (trashEntries.any { it.id == entry.id }) trashEntries else trashEntries + entry
    return EntryTransferResult(deleteEntry(activeEntries, entryId), updatedTrash)
}
internal fun restoreEntryFromTrash(
    activeEntries: List<Entry>,
    trashEntries: List<Entry>,
    entryId: String,
): EntryTransferResult {
    val entry = trashEntries.firstOrNull { it.id == entryId }
        ?: return EntryTransferResult(activeEntries, trashEntries)
    val updatedActive = if (activeEntries.any { it.id == entry.id }) activeEntries else activeEntries + entry
    return EntryTransferResult(updatedActive, deleteEntry(trashEntries, entryId))
}
private fun money(value: Double) = String.format(Locale.getDefault(), "%.2f", value)
private fun day(value: Long) = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA).format(Date(value))
