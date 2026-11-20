@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.gonzalowebdev.goodhabits

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarViewMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VerticalAlignBottom
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun HomeScreen(ui: Ui, snap: Snapshot, st: Settings) {
    val scope = rememberCoroutineScope()
    val buzz = rememberBuzz()
    var catFilter by remember { mutableStateOf<Long?>(null) }
    var filterOpen by remember { mutableStateOf(false) }
    var completedOpen by remember { mutableStateOf(false) }
    var searching by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val today = D.today()

    val base = snap.habits.filter { !it.archived }
        .filter { catFilter == null || catFilter in snap.catsOf(it.id) }
        .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
    fun doneToday(h: Habit) = h.isDone(snap.em(h.id)[today], today, today)
    val list = when (st.completedMode) {
        1 -> base.sortedBy { doneToday(it) }
        2 -> base.filter { !doneToday(it) }
        else -> base
    }

    val pager = rememberPagerState(initialPage = st.viewMode.coerceIn(0, 2)) { 3 }
    LaunchedEffect(pager.settledPage) {
        if (pager.settledPage != st.viewMode) Prefs.update { it.copy(viewMode = pager.settledPage) }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleBtn(Icons.Outlined.Settings, { ui.push(Route.Cfg(SPage.Main)) })
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (st.showSearch) CircleBtn(Icons.Outlined.Search, { searching = !searching; if (!searching) query = "" })
                    if (st.showFilter) CircleBtn(Icons.Outlined.FilterList, { filterOpen = !filterOpen; if (!filterOpen) catFilter = null })
                    CircleBtn(Icons.Outlined.BarChart, { ui.push(Route.Stats) })
                    CircleBtn(Icons.Outlined.Add, { ui.editor = EditorReq(0) }, filled = true)
                }
            }
            if (searching) {
                TextInput(query, { query = it }, "Buscar hábito", Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            }
            if (filterOpen && snap.categories.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { FilterChip("Todas", catFilter == null) { catFilter = null } }
                    items(snap.categories, key = { it.id }) { c ->
                        FilterChip(c.name, catFilter == c.id) { catFilter = if (catFilter == c.id) null else c.id }
                    }
                }
            }
            if (st.showCompletedFilter) {
                CompletedBar(st.completedMode, completedOpen, { completedOpen = !completedOpen }) { m ->
                    Prefs.update { it.copy(completedMode = m) }
                    completedOpen = false
                }
            }
            HorizontalPager(
                state = pager, userScrollEnabled = st.swipeModes, modifier = Modifier.weight(1f).fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> CardsPage(ui, snap, st, list, buzz)
                    1 -> CompactPage(ui, snap, st, list, buzz)
                    else -> MonthlyPage(ui, snap, st, list)
                }
            }
        }
        if (st.showBottomBar) {
            Row(
                Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 14.dp)
                    .clip(CircleShape).background(gh.surface.copy(alpha = 0.96f))
                    .border(1.dp, gh.line, CircleShape).padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val icons = listOf(Icons.Outlined.GridView, Icons.Outlined.Checklist, Icons.Outlined.CalendarViewMonth)
                icons.forEachIndexed { i, ic ->
                    Box(
                        Modifier.size(width = 54.dp, height = 44.dp).clip(CircleShape)
                            .background(if (pager.currentPage == i) gh.chip else Color.Transparent)
                            .press { scope.launch { pager.animateScrollToPage(i) } },
                        contentAlignment = Alignment.Center
                    ) { Icon(ic, null, tint = if (pager.currentPage == i) gh.text else gh.text3, modifier = Modifier.size(22.dp)) }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(CircleShape).background(if (selected) gh.primary else gh.surface)
            .border(1.dp, gh.line, CircleShape).press(onClick).padding(horizontal = 14.dp, vertical = 8.dp)
    ) { T(text, 13, if (selected) gh.onPrimary else gh.text, FontWeight.Medium) }
}

@Composable
private fun CompletedBar(mode: Int, open: Boolean, onToggle: () -> Unit, onPick: (Int) -> Unit) {
    val icons: List<ImageVector> = listOf(Icons.Outlined.FormatListBulleted, Icons.Outlined.VerticalAlignBottom, Icons.Outlined.VisibilityOff)
    val labels = listOf("Mostrar en su lugar", "Mover al final", "Ocultar completados")
    Row(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            Modifier.clip(CircleShape).background(gh.surface).border(1.dp, gh.line, CircleShape).padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!open) {
                Box(Modifier.size(38.dp).clip(CircleShape).press(onToggle), contentAlignment = Alignment.Center) {
                    Icon(icons[mode], null, tint = gh.text2, modifier = Modifier.size(20.dp))
                }
            } else {
                icons.forEachIndexed { i, ic ->
                    Box(
                        Modifier.size(38.dp).clip(CircleShape).background(if (i == mode) gh.chip else Color.Transparent).press { onPick(i) },
                        contentAlignment = Alignment.Center
                    ) { Icon(ic, null, tint = if (i == mode) gh.primary else gh.text3, modifier = Modifier.size(20.dp)) }
                }
                Box(Modifier.padding(horizontal = 8.dp).width(1.dp).height(22.dp).background(gh.line))
                T(labels[mode], 13, gh.text, FontWeight.Medium, modifier = Modifier.padding(end = 12.dp).press(onToggle))
            }
        }
    }
}

@Composable
private fun EmptyState(ui: Ui) {
    Column(
        Modifier.fillMaxWidth().padding(top = 90.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Outlined.GridView, null, tint = gh.text3, modifier = Modifier.size(34.dp))
        T("Aún no hay hábitos", 18, gh.text, FontWeight.SemiBold)
        T("Crea tu primer hábito para empezar a llenar la cuadrícula.", 14, gh.text2, align = TextAlign.Center, modifier = Modifier.padding(horizontal = 40.dp))
        Spacer(Modifier.height(6.dp))
        Box(Modifier.padding(horizontal = 40.dp)) { PrimaryButton("Nuevo hábito", { ui.editor = EditorReq(0) }) }
    }
}

private fun catNames(snap: Snapshot, id: Long): String =
    snap.categories.filter { it.id in snap.catsOf(id) }.joinToString(", ") { it.name }

// ------------------------------------------------------------------ vista de tarjetas

@Composable
private fun CardsPage(ui: Ui, snap: Snapshot, st: Settings, list: List<Habit>, buzz: () -> Unit) {
    val today = D.today()
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()
    ) {
        if (list.isEmpty() && snap.loaded) item { EmptyState(ui) }
        items(list, key = { it.id }) { h ->
            val color = Palette.color(h.colorIdx)
            val em = snap.em(h.id)
            val shape = RoundedCornerShape(24.dp)
            Box(
                Modifier.fillMaxWidth().clip(shape)
                    .background(Brush.verticalGradient(listOf(color.copy(alpha = if (gh.dark) 0.14f else 0.18f), gh.surface)), shape)
                    .border(1.dp, gh.line, shape).press({ ui.push(Route.Detail(h.id)) })
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HabitTile(h.icon, color, 40.dp)
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            T(h.name, 17, gh.text, FontWeight.SemiBold, maxLines = 1)
                            if (st.showCategoryNames) {
                                val n = catNames(snap, h.id)
                                if (n.isNotBlank()) T(n, 11, gh.text3, maxLines = 1)
                            }
                            if (st.streakCards) {
                                Spacer(Modifier.height(4.dp))
                                Pill("${streakOf(h, em, today)}", color, Icons.Outlined.LocalFireDepartment)
                            }
                        }
                        CheckBtn(h, em[today], today, onTap = { buzz(); ui.quick(h, today) }, onLong = { buzz(); ui.log = LogReq(h.id, today) })
                    }
                    Spacer(Modifier.height(14.dp))
                    Heatmap(h, em, st.weekStart, 8.dp, 2.5.dp, st.monthLabels, st.dayLabels)
                }
            }
        }
    }
}

// ------------------------------------------------------------------ lista compacta

@Composable
private fun CompactPage(ui: Ui, snap: Snapshot, st: Settings, list: List<Habit>, buzz: () -> Unit) {
    val today = D.today()
    val n = st.compactDays.coerceIn(1, 7)
    val days = (today - (n - 1)..today).toList()
    val cs = if (n > 5) 32.dp else 38.dp
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.clip(CircleShape).background(gh.surface).border(1.dp, gh.line, CircleShape)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) { T("Últimos $n días", 13, gh.text, FontWeight.Medium) }
                Spacer(Modifier.weight(1f))
                T(D.rangeLabel(days.first(), days.last()), 11, gh.text3, mono = true)
            }
        }
        if (list.isEmpty() && snap.loaded) item { EmptyState(ui) }
        else item {
            GhCard(Modifier.padding(bottom = 8.dp)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        T("${list.size} HÁBITO${if (list.size == 1) "" else "S"}", 10, gh.text3, mono = true, modifier = Modifier.weight(1f), spacing = 0.6f)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            days.forEach { d ->
                                val isToday = d == today
                                Column(Modifier.width(cs), horizontalAlignment = Alignment.CenterHorizontally) {
                                    T(D.DAYS2[D.dow(d)], 11, gh.text3, mono = true)
                                    T(
                                        D.date(d).dayOfMonth.toString(), 11,
                                        if (isToday) gh.text else gh.text3, if (isToday) FontWeight.Bold else FontWeight.Normal, mono = true
                                    )
                                }
                            }
                        }
                    }
                    list.forEach { h ->
                        val color = Palette.color(h.colorIdx)
                        val em = snap.em(h.id)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row(
                                Modifier.weight(1f).press({ ui.push(Route.Detail(h.id)) }),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HabitTile(h.icon, color, 30.dp)
                                if (st.compactNames) {
                                    Column(Modifier.padding(start = 8.dp, end = 6.dp)) {
                                        T(h.name, 14, gh.text, FontWeight.Medium, maxLines = 1)
                                        if (st.showCategories) {
                                            val cn = catNames(snap, h.id)
                                            if (cn.isNotBlank()) T(cn, 10, gh.text3, maxLines = 1)
                                        }
                                        if (st.streakCompact) T("🔥 ${streakOf(h, em, today)}", 10, color, mono = true)
                                    }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                days.forEach { d ->
                                    val p = h.progress(em[d], d, today)
                                    Box(
                                        Modifier.size(cs).clip(RoundedCornerShape(11.dp))
                                            .background(cellFill(color, p, color.copy(alpha = 0.12f)))
                                            .press({ buzz(); ui.quick(h, d) }, { buzz(); ui.log = LogReq(h.id, d) })
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------ vista mensual

@Composable
private fun MonthlyPage(ui: Ui, snap: Snapshot, st: Settings, list: List<Habit>) {
    val today = D.today()
    var offset by remember { mutableIntStateOf(0) }
    var menu by remember { mutableStateOf(false) }
    val first = LocalDate.now().withDayOfMonth(1).plusMonths(offset.toLong())
    val fd = first.toEpochDay()
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            Row(
                Modifier.clip(CircleShape).background(gh.surface).border(1.dp, gh.line, CircleShape)
                    .press { menu = true }.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                T(D.monthYear(fd), 13, gh.text, FontWeight.Medium)
                Icon(Icons.Outlined.KeyboardArrowDown, null, tint = gh.text2, modifier = Modifier.size(16.dp).padding(start = 2.dp))
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }, containerColor = gh.surface) {
                for (i in 0 downTo -24) {
                    val m = LocalDate.now().withDayOfMonth(1).plusMonths(i.toLong())
                    DropdownMenuItem(
                        text = { T(D.monthYear(m.toEpochDay()), 14, gh.text) },
                        onClick = { offset = i; menu = false }
                    )
                }
            }
        }
        if (list.isEmpty() && snap.loaded) EmptyState(ui)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(list, key = { it.id }) { h ->
                val color = Palette.color(h.colorIdx)
                val em = snap.em(h.id)
                val done = h.isDone(em[today], today, today)
                val shape = RoundedCornerShape(18.dp)
                Column(
                    Modifier.clip(shape).background(gh.surface).border(1.dp, gh.line, shape)
                        .press({ ui.push(Route.Detail(h.id)) }).padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(26.dp).clip(CircleShape).background(if (done) color else Color.Transparent)
                                .border(1.dp, if (done) color else gh.line, CircleShape)
                                .press({ ui.quick(h, today) }),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (done) Ic.get(h.icon) else Icons.Outlined.Check, null,
                                tint = if (done) Color(0xFF1C1604) else gh.text3, modifier = Modifier.size(14.dp)
                            )
                        }
                        Column(Modifier.padding(start = 8.dp)) {
                            T(h.name, 13, gh.text, maxLines = 1)
                            if (st.showCategories) {
                                val cn = catNames(snap, h.id)
                                if (cn.isNotBlank()) T(cn, 10, gh.text3, maxLines = 1)
                            }
                            if (st.streakMonthly) T("🔥 ${streakOf(h, em, today)}", 10, color, mono = true)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    MonthDots(h, em, fd, st.weekStart)
                }
            }
        }
    }
}
