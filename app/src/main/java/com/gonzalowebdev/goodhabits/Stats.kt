@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.gonzalowebdev.goodhabits

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatsScreen(ui: Ui, snap: Snapshot) {
    val habits = snap.habits.filter { !it.archived }
    var sel by remember { mutableStateOf(0L) } // 0 = resumen
    val today = D.today()
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            CircleBtn(Icons.Outlined.ChevronLeft, { ui.pop() }, size = 40.dp)
            T("Gráficos y estadísticas", 18, gh.text, FontWeight.SemiBold, modifier = Modifier.padding(start = 14.dp))
        }
        if (habits.isEmpty()) {
            T("Crea un hábito para ver tus estadísticas.", 14, gh.text2, align = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 80.dp))
            return@Column
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Chip("Resumen", sel == 0L, gh.primary) { sel = 0L } }
            items(habits, key = { it.id }) { h -> Chip(h.name, sel == h.id, Palette.color(h.colorIdx)) { sel = h.id } }
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val h = habits.firstOrNull { it.id == sel }
            if (h == null) Summary(habits, snap, today) else Detail(h, snap.em(h.id), today)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Chip(text: String, selected: Boolean, color: Color, onClick: () -> Unit) {
    Box(
        Modifier.clip(CircleShape).background(if (selected) color.copy(alpha = 0.22f) else gh.surface)
            .border(1.dp, if (selected) color else gh.line, CircleShape).press(onClick).padding(horizontal = 14.dp, vertical = 8.dp)
    ) { T(text, 13, if (selected) color else gh.text2, FontWeight.Medium, maxLines = 1) }
}

@Composable
private fun Tile(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(gh.surface2).border(1.dp, gh.line, RoundedCornerShape(18.dp)).padding(14.dp)
    ) {
        T(title, 11, gh.text3, spacing = 0.4f)
        T(value, 24, color, FontWeight.Bold, mono = true, modifier = Modifier.padding(top = 4.dp))
    }
}

private fun pct(f: Float) = "${Math.round(f * 100)}%"

@Composable
private fun Summary(habits: List<Habit>, snap: Snapshot, today: Long) {
    val rates = habits.map { rateOf(it, snap.em(it.id), today, 30) }
    val avg = if (rates.isEmpty()) 0f else rates.sum() / rates.size
    val best = habits.maxOfOrNull { longestOf(it, snap.em(it.id), today) } ?: 0
    val total = habits.sumOf { totalDone(it, snap.em(it.id), today) }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Tile("CUMPLIMIENTO 30 D", pct(avg), gh.primary, Modifier.weight(1f))
        Tile("COMPLETADOS", total.toString(), gh.primary, Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Tile("MEJOR RACHA", best.toString(), gh.primary, Modifier.weight(1f))
        Tile("HÁBITOS ACTIVOS", habits.size.toString(), gh.primary, Modifier.weight(1f))
    }
    GhCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            T("Cumplimiento por hábito (30 días)", 15, gh.text, FontWeight.SemiBold)
            habits.forEachIndexed { i, h ->
                val c = Palette.color(h.colorIdx)
                Column {
                    Row {
                        T(h.name, 13, gh.text, modifier = Modifier.weight(1f), maxLines = 1)
                        T(pct(rates[i]), 13, c, mono = true)
                    }
                    Box(Modifier.padding(top = 6.dp).fillMaxWidth().height(8.dp).clip(CircleShape).background(c.copy(alpha = 0.14f))) {
                        Box(Modifier.fillMaxWidth(rates[i].coerceIn(0f, 1f)).height(8.dp).clip(CircleShape).background(c))
                    }
                }
            }
        }
    }
}

@Composable
private fun Detail(h: Habit, em: Map<Long, Entry>, today: Long) {
    val c = Palette.color(h.colorIdx)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Tile("RACHA ACTUAL", streakOf(h, em, today).toString(), c, Modifier.weight(1f))
        Tile("MEJOR RACHA", longestOf(h, em, today).toString(), c, Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Tile(if (h.quit) "DÍAS LIMPIOS" else "COMPLETADOS", totalDone(h, em, today).toString(), c, Modifier.weight(1f))
        Tile("CUMPLIMIENTO 30 D", pct(rateOf(h, em, today, 30)), c, Modifier.weight(1f))
    }
    GhCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            T("Tasa de cumplimiento", 15, gh.text, FontWeight.SemiBold)
            listOf(7 to "Últimos 7 días", 30 to "Últimos 30 días", 90 to "Últimos 90 días", 365 to "Último año").forEach { (n, label) ->
                val r = rateOf(h, em, today, n)
                Column {
                    Row {
                        T(label, 13, gh.text2, modifier = Modifier.weight(1f))
                        T(pct(r), 13, c, mono = true)
                    }
                    Box(Modifier.padding(top = 6.dp).fillMaxWidth().height(8.dp).clip(CircleShape).background(c.copy(alpha = 0.14f))) {
                        Box(Modifier.fillMaxWidth(r.coerceIn(0f, 1f)).height(8.dp).clip(CircleShape).background(c))
                    }
                }
            }
        }
    }
    GhCard {
        Column(Modifier.padding(16.dp)) {
            T("Por día de la semana", 15, gh.text, FontWeight.SemiBold)
            val byDow = doneByWeekday(h, em, today)
            Bars(D.DAYS2.mapIndexed { i, s -> s to byDow[i] }, c)
        }
    }
    GhCard {
        Column(Modifier.padding(16.dp)) {
            T("Últimos 12 meses", 15, gh.text, FontWeight.SemiBold)
            Bars(doneByMonth(h, em, today), c)
        }
    }
}

@Composable
private fun Bars(data: List<Pair<String, Int>>, color: Color) {
    val tm = rememberTextMeasurer()
    val lab = TextStyle(fontSize = 9.sp, color = gh.text3, fontFamily = Mono)
    val num = TextStyle(fontSize = 9.sp, color = gh.text2, fontFamily = Mono)
    val max = (data.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)
    Canvas(Modifier.fillMaxWidth().padding(top = 12.dp).height(150.dp)) {
        val n = data.size
        val slot = size.width / n
        val bw = slot * 0.56f
        val top = 14.dp.toPx()
        val bottom = size.height - 16.dp.toPx()
        data.forEachIndexed { i, (l, v) ->
            val x = i * slot + (slot - bw) / 2f
            val bh = (bottom - top) * (v.toFloat() / max)
            drawRoundRect(color.copy(alpha = 0.14f), Offset(x, top), Size(bw, bottom - top), CornerRadius(bw * 0.3f))
            if (v > 0) drawRoundRect(color, Offset(x, bottom - bh), Size(bw, bh), CornerRadius(bw * 0.3f))
            drawText(tm, l, Offset(i * slot + (slot - 18.dp.toPx()) / 2f, bottom + 3.dp.toPx()), lab)
            if (v > 0) drawText(tm, v.toString(), Offset(i * slot + slot / 2f - 3.dp.toPx() * v.toString().length, (bottom - bh - 12.dp.toPx()).coerceAtLeast(0f)), num)
        }
    }
}
