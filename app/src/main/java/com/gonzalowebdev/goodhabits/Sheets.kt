@file:OptIn(ExperimentalMaterial3Api::class)

package com.gonzalowebdev.goodhabits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LogSheet(ui: Ui, snap: Snapshot, req: LogReq, st: Settings) {
    val h = snap.habit(req.habitId) ?: return
    val today = D.today()
    val color = Palette.color(h.colorIdx)
    var day by remember(req) { mutableLongStateOf(req.day) }
    val e0 = snap.em(h.id)[day]
    var value by remember(req, day) { mutableStateOf(e0?.value ?: 0.0) }
    var slip by remember(req, day) { mutableStateOf(e0?.slip ?: false) }
    var note by remember(req, day) { mutableStateOf(e0?.note ?: "") }
    var text by remember(req, day) { mutableStateOf(D.fmt(e0?.value ?: 0.0)) }
    var showDate by remember { mutableStateOf(false) }

    GhSheet(onDismiss = { ui.log = null }) {
        Column(
            Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HabitTile(h.icon, color, 46.dp)
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    T(h.name, 18, gh.text, FontWeight.SemiBold, maxLines = 1)
                    T(if (day == today) "Hoy" else D.short(day), 13, gh.text2, mono = true)
                }
                if (h.mode == MODE_YN) {
                    if (!h.quit) {
                        val done = value >= 1.0
                        val shape = RoundedCornerShape(14.dp)
                        Box(
                            Modifier.size(46.dp).clip(shape)
                                .background(if (done) Brush.verticalGradient(listOf(color, color.copy(alpha = 0.82f))) else Brush.verticalGradient(listOf(gh.chip, gh.chip)), shape)
                                .border(1.dp, if (done) color else gh.line, shape)
                                .press { value = if (done) 0.0 else 1.0 },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Outlined.Check, null, tint = if (done) Color(0xFF1C1604) else gh.text3, modifier = Modifier.size(22.dp)) }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CircleBtn(Icons.Outlined.Remove, {
                            value = (value - 1).coerceAtLeast(0.0); text = D.fmt(value)
                        }, size = 38.dp)
                        NumberField(text, { text = it; value = it.toDoubleOrNull() ?: 0.0 }, Modifier.width(78.dp))
                        CircleBtn(Icons.Outlined.Add, {
                            value += 1; text = D.fmt(value)
                        }, size = 38.dp)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(24.dp).clip(CircleShape).border(1.dp, gh.text3, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.Close, null, tint = gh.text3, modifier = Modifier.size(14.dp)) }
                T("Marcado como desliz", 15, gh.text, modifier = Modifier.weight(1f).padding(start = 10.dp))
                GhSwitch(slip) { slip = it }
            }
            if (st.notes) {
                TextInput(
                    note, { note = it }, "¿Qué fue bien? ¿Qué se interpuso?", singleLine = false, minHeight = 110.dp,
                    borderColor = if (note.isNotBlank()) color.copy(alpha = 0.6f) else gh.line
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CircleBtn(Icons.Outlined.CalendarMonth, { showDate = true }, size = 56.dp)
                PrimaryButton("Guardar", {
                    ui.vm.saveEntry(h, day, value, slip, note)
                    ui.log = null
                }, Modifier.weight(1f))
            }
        }
    }

    if (showDate) {
        val ds = rememberDatePickerState(initialSelectedDateMillis = day * 86_400_000L)
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                T("Aceptar", 15, gh.primary, FontWeight.SemiBold, modifier = Modifier.press {
                    val ms = ds.selectedDateMillis
                    if (ms != null) day = (ms / 86_400_000L).coerceAtMost(today)
                    showDate = false
                }.padding(16.dp))
            },
            dismissButton = { T("Cancelar", 15, gh.text2, modifier = Modifier.press { showDate = false }.padding(16.dp)) },
            colors = androidx.compose.material3.DatePickerDefaults.colors(containerColor = gh.surface)
        ) { DatePicker(state = ds) }
    }
}

@Composable
fun NumberDialog(ui: Ui, snap: Snapshot, req: LogReq) {
    val h = snap.habit(req.habitId) ?: return
    val cur = snap.em(h.id)[req.day]?.value ?: 0.0
    var text by remember(req) { mutableStateOf(if (cur == 0.0) "" else D.fmt(cur)) }
    AlertDialog(
        onDismissRequest = { ui.number = null },
        containerColor = gh.surface,
        title = { T(h.name, 18, gh.text, FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                T(
                    (if (h.quit) "Total del día (límite ${D.fmt(h.target)})" else "Total del día (objetivo ${D.fmt(h.target)})") +
                        if (h.unit.isNotBlank()) " · ${h.unit}" else "",
                    13, gh.text2
                )
                NumberField(text, { text = it }, Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            T("Guardar", 15, gh.primary, FontWeight.SemiBold, modifier = Modifier.press {
                ui.vm.setValue(h, req.day, text.toDoubleOrNull() ?: 0.0)
                ui.number = null
            }.padding(12.dp))
        },
        dismissButton = { T("Cancelar", 15, gh.text2, modifier = Modifier.press { ui.number = null }.padding(12.dp)) }
    )
}

@Composable
fun ConfirmDialog(ui: Ui, c: Confirm) {
    AlertDialog(
        onDismissRequest = { ui.confirm = null },
        containerColor = gh.surface,
        title = { T(c.title, 18, gh.text, FontWeight.SemiBold) },
        text = { T(c.text, 14, gh.text2) },
        confirmButton = {
            T(c.action, 15, Color(0xFFF77070), FontWeight.SemiBold, modifier = Modifier.press { ui.confirm = null; c.onOk() }.padding(12.dp))
        },
        dismissButton = { T("Cancelar", 15, gh.text2, modifier = Modifier.press { ui.confirm = null }.padding(12.dp)) }
    )
}

@Composable
fun TimeDialog(minute: Int, onDismiss: () -> Unit, onDone: (Int) -> Unit) {
    val state = rememberTimePickerState(initialHour = minute / 60, initialMinute = minute % 60, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = gh.surface,
        text = { TimePicker(state = state) },
        confirmButton = {
            T("Aceptar", 15, gh.primary, FontWeight.SemiBold, modifier = Modifier.press { onDone(state.hour * 60 + state.minute) }.padding(12.dp))
        },
        dismissButton = { T("Cancelar", 15, gh.text2, modifier = Modifier.press { onDismiss() }.padding(12.dp)) }
    )
}
