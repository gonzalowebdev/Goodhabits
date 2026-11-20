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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
fun DetailScreen(ui: Ui, snap: Snapshot, st: Settings, id: Long) {
    val h = snap.habit(id)
    if (h == null) {
        if (snap.loaded) LaunchedEffect(Unit) { ui.pop() }
        return
    }
    val em = snap.em(id)
    val color = Palette.color(h.colorIdx)
    val today = D.today()
    val buzz = rememberBuzz()
    var monthOffset by remember { mutableIntStateOf(0) }
    var monthMenu by remember { mutableStateOf(false) }
    var gear by remember { mutableStateOf(false) }
    val notes = em.values.filter { it.note.isNotBlank() }.sortedByDescending { it.day }

    Column(
        Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())
            .navigationBarsPadding().padding(horizontal = 16.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            CircleBtn(Icons.Outlined.ChevronLeft, { ui.pop() }, size = 40.dp)
            Spacer(Modifier.weight(1f))
            CircleBtn(Icons.Outlined.Edit, { ui.editor = EditorReq(h.id) }, size = 40.dp)
            Spacer(Modifier.size(8.dp))
            CircleBtn(Icons.Outlined.Settings, { gear = true }, size = 40.dp)
        }
        Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            HabitTile(h.icon, color, 56.dp)
            Column(Modifier.padding(start = 14.dp)) {
                T(h.name, 26, gh.text, FontWeight.Bold, maxLines = 2)
                T(h.description.ifBlank { "Sin descripción" }, 14, gh.text2, maxLines = 2)
            }
        }
        Spacer(Modifier.height(8.dp))
        val shape = RoundedCornerShape(22.dp)
        Box(
            Modifier.fillMaxWidth().clip(shape)
                .background(Brush.verticalGradient(listOf(color.copy(alpha = if (gh.dark) 0.12f else 0.16f), gh.surface)), shape)
                .border(1.dp, gh.line, shape).padding(14.dp)
        ) { Heatmap(h, em, st.weekStart, 9.dp, 2.5.dp, true, true) }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val s = streakOf(h, em, today)
            Pill("$s ${if (s == 1) "día" else "días"}", color, Icons.Outlined.LocalFireDepartment)
            Pill(repeatLabel(h.daysMask), color, Icons.Outlined.Repeat)
        }
        Spacer(Modifier.height(16.dp))

        // ---- calendario
        val first = LocalDate.now().withDayOfMonth(1).plusMonths(monthOffset.toLong())
        val fd = first.toEpochDay()
        val gridStart = fd - Math.floorMod(D.dow(fd) - st.weekStart, 7)
        val monthIdx = first.monthValue
        GhCard {
            Column(Modifier.padding(14.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    for (i in 0..6) {
                        T(
                            D.DAYS3[(st.weekStart + i) % 7], 12, gh.text3, mono = true,
                            modifier = Modifier.weight(1f), align = TextAlign.Center
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                for (r in 0..5) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        for (c in 0..6) {
                            val d = gridStart + r * 7 + c
                            val inMonth = D.date(d).monthValue == monthIdx
                            val p = if (inMonth) h.progress(em[d], d, today) else 0f
                            val isToday = d == today
                            val hasNote = em[d]?.note?.isNotBlank() == true
                            val cs = RoundedCornerShape(13.dp)
                            Box(Modifier.weight(1f).height(46.dp).padding(horizontal = 2.dp), contentAlignment = Alignment.Center) {
                                Box(
                                    Modifier.fillMaxSize().clip(cs)
                                        .background(if (p > 0f) color.copy(alpha = 0.10f + 0.13f * p) else Color.Transparent, cs)
                                        .then(
                                            if (isToday) Modifier.border(1.4.dp, color, cs)
                                            else if (p >= 1f) Modifier.border(1.dp, color.copy(alpha = 0.22f), cs) else Modifier
                                        )
                                        .press(
                                            { if (inMonth && d <= today) { buzz(); ui.quick(h, d) } },
                                            { if (d <= today) { buzz(); ui.log = LogReq(h.id, d) } }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        T(
                                            D.date(d).dayOfMonth.toString(), 14,
                                            when {
                                                !inMonth -> gh.text3.copy(alpha = 0.55f)
                                                d > today -> gh.text3
                                                else -> gh.text
                                            },
                                            if (isToday && st.highlightToday) FontWeight.Bold else FontWeight.Normal, mono = true
                                        )
                                        if (hasNote && inMonth) Box(Modifier.padding(top = 2.dp).size(4.dp).clip(CircleShape).background(color))
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box {
                        Row(
                            Modifier.clip(CircleShape).background(gh.surface).border(1.dp, gh.line, CircleShape)
                                .press { monthMenu = true }.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            T("${D.MONTHS3[first.monthValue - 1].lowercase()} ${first.year}", 12, gh.text, mono = true)
                            Icon(Icons.Outlined.KeyboardArrowDown, null, tint = gh.text2, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(expanded = monthMenu, onDismissRequest = { monthMenu = false }, containerColor = gh.surface) {
                            for (i in 0 downTo -36) {
                                val m = LocalDate.now().withDayOfMonth(1).plusMonths(i.toLong())
                                DropdownMenuItem(
                                    text = { T(D.monthYear(m.toEpochDay()), 14, gh.text) },
                                    onClick = { monthOffset = i; monthMenu = false }
                                )
                            }
                        }
                    }
                    Box(
                        Modifier.clip(CircleShape).background(gh.surface).border(1.dp, gh.line, CircleShape)
                            .press { monthOffset = 0 }.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) { T("Hoy", 12, gh.text, mono = true) }
                    Spacer(Modifier.weight(1f))
                    CircleBtn(Icons.Outlined.ChevronLeft, { monthOffset -= 1 }, size = 34.dp)
                    CircleBtn(Icons.Outlined.ChevronRight, { if (monthOffset < 0) monthOffset += 1 }, size = 34.dp)
                }
                T(
                    "Mantén pulsado un día para añadir una nota", 12, gh.text3,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp), align = TextAlign.Center
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        // ---- notas
        if (st.notes) {
            GhCard {
                if (notes.isEmpty()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        HabitTile("write", color, 40.dp)
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            T("Aún no hay notas", 15, gh.text, FontWeight.Medium)
                            T("¿Qué fue bien? ¿Qué se interpuso?", 12, gh.text2)
                        }
                        CircleBtn(Icons.Outlined.Add, { ui.log = LogReq(h.id, today) }, size = 38.dp)
                    }
                } else {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            T("Notas", 16, gh.text, FontWeight.SemiBold)
                            Spacer(Modifier.weight(1f))
                            Row(
                                Modifier.clip(CircleShape).background(gh.surface).border(1.dp, gh.line, CircleShape)
                                    .press { ui.push(Route.Notes(h.id)) }.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                T("Todas las notas", 12, gh.text2)
                                Icon(Icons.Outlined.ChevronRight, null, tint = gh.text2, modifier = Modifier.size(14.dp))
                            }
                        }
                        NoteCard(h, notes.first(), today) { ui.log = LogReq(h.id, it) }
                    }
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }

    if (gear) {
        GhSheet(onDismiss = { gear = false }) {
            Column(Modifier.navigationBarsPadding().padding(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                T(h.name, 18, gh.text, FontWeight.SemiBold)
                GhCard {
                    SheetRow(Icons.Outlined.Archive, "Archivar hábito", gh.text) {
                        gear = false; ui.vm.archive(h.id, true); ui.pop()
                    }
                    SheetRow(Icons.Outlined.Delete, "Eliminar hábito", Color(0xFFF77070)) {
                        gear = false
                        ui.confirm = Confirm("Eliminar hábito", "Se borrará «${h.name}» con todo su historial. No se puede deshacer.", "Eliminar") {
                            ui.vm.delete(h.id); ui.pop()
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SheetRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, tint: Color, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().press(onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        T(text, 15, tint, modifier = Modifier.padding(start = 14.dp))
    }
}

@Composable
fun NoteCard(h: Habit, e: Entry, today: Long, onEdit: (Long) -> Unit) {
    val color = Palette.color(h.colorIdx)
    val shape = RoundedCornerShape(16.dp)
    Column(
        Modifier.fillMaxWidth().clip(shape).background(gh.surface).border(1.4.dp, color.copy(alpha = 0.55f), shape).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(color))
            T(
                D.short(e.day) + if (e.day == today) " · Hoy" else "", 12, gh.text2, mono = true,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
            Box(
                Modifier.size(30.dp).clip(CircleShape).background(gh.chip).press { onEdit(e.day) },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Outlined.Edit, null, tint = color, modifier = Modifier.size(15.dp)) }
        }
        T(e.note, 15, gh.text)
    }
}

@Composable
fun NotesScreen(ui: Ui, snap: Snapshot, id: Long) {
    val h = snap.habit(id)
    if (h == null) {
        if (snap.loaded) LaunchedEffect(Unit) { ui.pop() }
        return
    }
    val today = D.today()
    val notes = snap.em(id).values.filter { it.note.isNotBlank() }.sortedByDescending { it.day }
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            CircleBtn(Icons.Outlined.ChevronLeft, { ui.pop() }, size = 40.dp)
            HabitTile(h.icon, Palette.color(h.colorIdx), 36.dp, Modifier.padding(start = 12.dp))
            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                T("Notas", 18, gh.text, FontWeight.SemiBold)
                T(h.name, 12, gh.text2, maxLines = 1)
            }
            CircleBtn(Icons.Outlined.Add, { ui.log = LogReq(h.id, today) }, size = 40.dp)
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize().navigationBarsPadding()
        ) {
            if (notes.isEmpty()) item {
                T("Todavía no escribiste notas para este hábito.", 14, gh.text2, align = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 60.dp))
            }
            items(notes, key = { it.day }) { e -> NoteCard(h, e, today) { ui.log = LogReq(h.id, it) } }
        }
    }
}
