@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.gonzalowebdev.goodhabits

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EditorSheet(ui: Ui, snap: Snapshot, req: EditorReq) {
    val existing = remember(req) { snap.habit(req.habitId) }
    var d by remember(req) { mutableStateOf(existing ?: newHabit()) }
    var cats by remember(req) { mutableStateOf(snap.catsOf(req.habitId)) }
    var rems by remember(req) { mutableStateOf(snap.remindersOf(req.habitId)) }
    var targetText by remember(req) { mutableStateOf(D.fmt(d.target)) }
    var page by remember { mutableStateOf(0) }
    val h = LocalConfiguration.current.screenHeightDp.dp * 0.92f

    GhSheet(onDismiss = { ui.editor = null }) {
        Box(Modifier.fillMaxWidth().height(h).navigationBarsPadding().imePadding()) {
            when (page) {
                1 -> RemindersPage(
                    rems, d.id, { rems = it }, { page = 0 }, { ui.askNotif() }
                )
                2 -> RepeatPage(d.daysMask, { d = d.copy(daysMask = it) }, { page = 0 })
                3 -> CategoriesPage(
                    snap, cats, { id -> cats = if (id in cats) cats - id else cats + id },
                    { page = 0 }, { name -> ui.vm.addCategory(name) { id -> cats = cats + id } },
                    { id -> ui.vm.deleteCategory(id); cats = cats - id }
                )
                4 -> IconsPage(d.icon, Palette.color(d.colorIdx), { d = d.copy(icon = it); page = 0 }, { page = 0 })
                else -> EditorMain(
                    d, { d = it }, targetText, { targetText = it }, cats, rems, snap,
                    onClose = { ui.editor = null }, onPage = { page = it },
                    onSave = {
                        val tgt = targetText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 1.0
                        val fixed = d.copy(
                            name = d.name.trim(), description = d.description.trim(),
                            target = if (d.mode == MODE_YN) 1.0 else tgt
                        )
                        ui.vm.saveHabit(fixed, cats, rems)
                        ui.editor = null
                    }
                )
            }
        }
    }
}

@Composable
private fun PageHeader(title: String, sub: String? = null, onBack: () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) { CircleBtn(Icons.Outlined.ChevronLeft, onBack, size = 40.dp) }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            T(title, 18, gh.text, FontWeight.SemiBold)
            if (sub != null) T(sub, 11, gh.text3, mono = true)
        }
    }
}

@Composable
private fun PickRow(value: String, onClick: () -> Unit, trailing: @Composable () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(gh.surface2)
            .border(1.dp, gh.line, RoundedCornerShape(14.dp)).press(onClick).padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        T(value, 15, gh.text, modifier = Modifier.weight(1f), maxLines = 1)
        trailing()
        Icon(Icons.Outlined.ChevronRight, null, tint = gh.text3, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun Hint(text: String) {
    T(text, 12, gh.text3, align = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 6.dp, end = 6.dp))
}

@Composable
private fun EditorMain(
    d: Habit, setD: (Habit) -> Unit, targetText: String, setTarget: (String) -> Unit,
    cats: Set<Long>, rems: List<Reminder>, snap: Snapshot,
    onClose: () -> Unit, onPage: (Int) -> Unit, onSave: () -> Unit
) {
    val color = Palette.color(d.colorIdx)
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) { CircleBtn(Icons.Outlined.Close, onClose, size = 40.dp) }
            T(if (d.id == 0L) "Nuevo hábito" else "Editar hábito", 20, gh.text, FontWeight.Bold)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            // ---- ícono
            Box(Modifier.fillMaxWidth().height(138.dp).press { onPage(4) }, contentAlignment = Alignment.Center) {
                val spots = listOf(
                    -140 to -40, -100 to 10, -60 to -50, -130 to 45, -85 to 50, -30 to 30,
                    140 to -40, 100 to 10, 60 to -50, 130 to 45, 85 to 50, 30 to 30
                )
                spots.forEachIndexed { i, (x, y) ->
                    Icon(
                        Ic.list[(i * 5 + 3) % Ic.list.size].second, null, tint = color,
                        modifier = Modifier.offset(x.dp, (y - 8).dp).size(20.dp).alpha(0.16f)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            Modifier.size(76.dp).clip(CircleShape).background(color.copy(alpha = 0.2f))
                                .border(1.dp, color.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Icon(Ic.get(d.icon), null, tint = color, modifier = Modifier.size(38.dp)) }
                        Box(
                            Modifier.size(24.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center
                        ) { Icon(Icons.Outlined.Edit, null, tint = Color(0xFF1C1604), modifier = Modifier.size(13.dp)) }
                    }
                    T("Cambiar icono", 13, color, FontWeight.Medium, modifier = Modifier.padding(top = 8.dp))
                }
            }
            Label("Nombre")
            TextInput(d.name, { setD(d.copy(name = it)) }, "Nombre del hábito")
            Label("Descripción")
            TextInput(d.description, { setD(d.copy(description = it)) }, "Opcional")
            Label("Color")
            for (r in 0..2) {
                Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (c in 0..6) {
                        val idx = r * 7 + c
                        val col = Palette.color(idx)
                        val sel = idx == d.colorIdx
                        Box(
                            Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(14.dp))
                                .then(if (sel) Modifier.border(2.dp, col, RoundedCornerShape(14.dp)) else Modifier)
                                .press { setD(d.copy(colorIdx = idx)) }
                                .padding(if (sel) 4.dp else 0.dp)
                        ) { Box(Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)).background(col)) }
                    }
                }
            }
            Label("Tipo de hábito")
            Seg(listOf("Crear", "Dejar"), if (d.quit) 1 else 0, { setD(d.copy(quit = it == 1)) })
            Hint(
                if (d.quit) "Completado por defecto: marca solo los días en que tuviste un desliz. Tu racha cuenta los días consecutivos sin deslices. Los días pasados conservan su historial."
                else "Marca cada día en que lo completes."
            )
            Label("Repetición")
            PickRow(repeatLabel(d.daysMask), { onPage(2) }) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.padding(end = 8.dp)) {
                    for (i in 0..6) {
                        Box(
                            Modifier.size(6.dp).clip(CircleShape)
                                .background(if (((d.daysMask shr i) and 1) == 1) color else gh.text3.copy(alpha = 0.4f))
                        )
                    }
                }
            }
            Label("Registro")
            Seg(listOf("Sí / No", "Contador", "Medida"), d.mode, { m -> setD(d.copy(mode = m)) })
            Hint(
                when (d.mode) {
                    MODE_YN -> if (d.quit) "Un día cuenta como limpio salvo que registres un desliz" else "Marca el día: hecho o no hecho."
                    MODE_COUNT -> if (d.quit) "Cuenta los deslices y mantente por debajo del límite diario" else "Cuenta tus logros y alcanza el objetivo diario."
                    else -> if (d.quit) "Registra cantidades y mantente por debajo del límite diario" else "Registra cantidades y alcanza el objetivo diario."
                }
            )
            if (d.mode == MODE_MEASURE) {
                Label("Unidad")
                TextInput(d.unit, { setD(d.copy(unit = it)) }, "km, páginas, minutos...")
            }
            if (d.mode != MODE_YN) {
                Label(if (d.quit) "Mantente por debajo" else "Objetivo")
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(targetText, setTarget, Modifier.weight(1f))
                    T("/ Día", 13, gh.text3, mono = true)
                    CircleBtn(Icons.Outlined.Remove, { setTarget(D.fmt(((targetText.toDoubleOrNull() ?: 1.0) - 1).coerceAtLeast(1.0))) }, size = 44.dp)
                    CircleBtn(Icons.Outlined.Add, { setTarget(D.fmt((targetText.toDoubleOrNull() ?: 0.0) + 1)) }, size = 44.dp)
                }
                Hint(if (d.quit) "El día sigue limpio mientras te mantengas por debajo de esta cantidad." else "El día se completa al alcanzar esta cantidad.")
                Label("Al tocar")
                Seg(listOf("Añadir 1", "Introducir total"), if (d.tapTotal) 1 else 0, { setD(d.copy(tapTotal = it == 1)) })
                Hint(if (d.tapTotal) "Abre el editor para introducir el total del día" else "Suma 1 con cada toque")
            }
            Label("Recordatorio")
            PickRow(
                when {
                    rems.isEmpty() -> "Sin recordatorios"
                    rems.size == 1 -> (if (rems[0].daysMask == 127) "Diaria" else repeatLabel(rems[0].daysMask)) + " · " + D.hhmm(rems[0].minute)
                    else -> "${rems.size} recordatorios"
                }, { onPage(1) }
            )
            Label("Categorías")
            PickRow(
                if (cats.isEmpty()) "Ninguna" else snap.categories.filter { it.id in cats }.joinToString(", ") { it.name },
                { onPage(3) }
            )
            Spacer(Modifier.height(20.dp))
        }
        Box(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            PrimaryButton("Guardar", onSave, enabled = d.name.isNotBlank())
        }
    }
}

// ------------------------------------------------------------------ recordatorios

@Composable
private fun RemindersPage(
    rems: List<Reminder>, habitId: Long, setRems: (List<Reminder>) -> Unit, onBack: () -> Unit, askNotif: () -> Unit
) {
    var timeFor by remember { mutableStateOf(-1) }
    Column(Modifier.fillMaxSize()) {
        PageHeader("Recordatorio", "${rems.size} / 3", onBack)
        if (rems.isEmpty()) {
            Column(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = 28.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Outlined.Alarm, null, tint = gh.text, modifier = Modifier.size(30.dp))
                T("Sin recordatorios", 20, gh.text, FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
                T(
                    "Añade un recordatorio a tu hábito para notificaciones diarias.", 14, gh.text2, align = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
                )
                PrimaryButton("Añadir recordatorio", {
                    askNotif(); setRems(rems + Reminder(0, habitId, 127, 600))
                })
            }
        } else {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
                T(
                    "Los recordatorios se repiten a estas horas y se silencian cuando has completado el hábito del día.",
                    13, gh.text2, modifier = Modifier.padding(vertical = 10.dp)
                )
                rems.forEachIndexed { i, r ->
                    GhCard(Modifier.padding(bottom = 12.dp)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            T("Recordatorio #${i + 1}", 14, gh.text, FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                for (k in 0..6) {
                                    val on = ((r.daysMask shr k) and 1) == 1
                                    Box(
                                        Modifier.weight(1f).height(38.dp).clip(RoundedCornerShape(10.dp))
                                            .background(if (on) gh.primary.copy(alpha = 0.85f) else gh.surface)
                                            .press {
                                                val nm = r.daysMask xor (1 shl k)
                                                if (nm != 0) setRems(rems.toMutableList().also { it[i] = r.copy(daysMask = nm) })
                                            },
                                        contentAlignment = Alignment.Center
                                    ) { T(D.DAYS_CAP[k], 11, if (on) Color.White else gh.text2, FontWeight.Medium) }
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(14.dp)).background(gh.surface)
                                        .border(1.dp, gh.line, RoundedCornerShape(14.dp)).press { timeFor = i }.padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Outlined.Schedule, null, tint = gh.text2, modifier = Modifier.size(18.dp))
                                    T(D.hhmm(r.minute), 16, gh.text, FontWeight.Medium, mono = true, modifier = Modifier.padding(start = 8.dp))
                                }
                                CircleBtn(Icons.Outlined.Delete, { setRems(rems.toMutableList().also { it.removeAt(i) }) }, size = 48.dp)
                            }
                        }
                    }
                }
                if (rems.size < 3) {
                    PrimaryButton("+ Añadir recordatorio", { setRems(rems + Reminder(0, habitId, 127, 600)) })
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
    if (timeFor >= 0 && timeFor < rems.size) {
        TimeDialog(rems[timeFor].minute, { timeFor = -1 }) { m ->
            val i = timeFor
            setRems(rems.toMutableList().also { it[i] = it[i].copy(minute = m) })
            timeFor = -1
        }
    }
}

// ------------------------------------------------------------------ repetición

@Composable
private fun RepeatPage(mask: Int, setMask: (Int) -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        PageHeader("Repetición", null, onBack)
        Column(Modifier.padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
            Label("Preajustes")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Todos los días" to 127, "Entre semana" to 31, "Fines de semana" to 96).forEach { (t, m) ->
                    Box(
                        Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp))
                            .background(if (mask == m) gh.chip else gh.surface2).border(1.dp, gh.line, RoundedCornerShape(12.dp))
                            .press { setMask(m) },
                        contentAlignment = Alignment.Center
                    ) { T(t, 12, if (mask == m) gh.text else gh.text2, FontWeight.Medium, align = TextAlign.Center) }
                }
            }
            Label("Días de la semana")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (k in 0..6) {
                    val on = ((mask shr k) and 1) == 1
                    Box(
                        Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(12.dp))
                            .background(if (on) gh.primary.copy(alpha = 0.85f) else gh.surface2)
                            .border(1.dp, gh.line, RoundedCornerShape(12.dp))
                            .press { val nm = mask xor (1 shl k); if (nm != 0) setMask(nm) },
                        contentAlignment = Alignment.Center
                    ) { T(D.DAYS_CAP[k], 12, if (on) Color.White else gh.text2, FontWeight.Medium) }
                }
            }
            Hint("Los días sin marcar no cuentan en la racha ni en las estadísticas.")
        }
    }
}

// ------------------------------------------------------------------ categorías

@Composable
private fun CategoriesPage(
    snap: Snapshot, selected: Set<Long>, onToggle: (Long) -> Unit, onBack: () -> Unit,
    onAdd: (String) -> Unit, onDelete: (Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        PageHeader("Categorías", null, onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Label("Nueva categoría")
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) { TextInput(name, { name = it }, "Nombre de la categoría") }
                CircleBtn(Icons.Outlined.Add, { if (name.isNotBlank()) { onAdd(name); name = "" } }, size = 52.dp, filled = true)
            }
            Label("Tus categorías")
            if (snap.categories.isEmpty()) {
                T("Aún no creaste categorías.", 14, gh.text2, modifier = Modifier.padding(8.dp))
            } else {
                GhCard {
                    snap.categories.forEach { c ->
                        val on = c.id in selected
                        Row(
                            Modifier.fillMaxWidth().press { onToggle(c.id) }.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(24.dp).clip(CircleShape).background(if (on) gh.primary else Color.Transparent)
                                    .border(1.5.dp, if (on) gh.primary else gh.text3, CircleShape),
                                contentAlignment = Alignment.Center
                            ) { if (on) Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                            T(c.name, 15, gh.text, modifier = Modifier.weight(1f).padding(start = 12.dp))
                            Icon(
                                Icons.Outlined.Delete, null, tint = gh.text3,
                                modifier = Modifier.size(36.dp).clip(CircleShape).press { onDelete(c.id) }.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------ íconos

@Composable
private fun IconsPage(selected: String, color: Color, onPick: (String) -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        PageHeader("Elegir icono", null, onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Ic.list.chunked(5).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { (key, vec) ->
                        val sel = key == selected
                        Box(
                            Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(16.dp))
                                .background(if (sel) color.copy(alpha = 0.2f) else gh.surface2)
                                .border(1.dp, if (sel) color else gh.line, RoundedCornerShape(16.dp))
                                .press { onPick(key) },
                            contentAlignment = Alignment.Center
                        ) { Icon(vec, null, tint = if (sel) color else gh.text2, modifier = Modifier.size(26.dp)) }
                    }
                    repeat(5 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}
