@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.gonzalowebdev.goodhabits

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(ui: Ui, snap: Snapshot, st: Settings, page: SPage) {
    when (page) {
        SPage.Main -> SMain(ui, snap, st)
        SPage.General -> SGeneral(ui, st)
        SPage.Overview -> SOverview(ui, st)
        SPage.Daily -> SDaily(ui, st)
        SPage.Theme -> STheme(ui, st)
        SPage.Archived -> SArchived(ui, snap)
        SPage.Backup -> SBackup(ui, st)
        SPage.Reorder -> SReorder(ui, snap)
    }
}

@Composable
private fun SScaffold(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                CircleBtn(Icons.Outlined.ChevronLeft, onBack, size = 40.dp)
            }
            T(title, 18, gh.text, FontWeight.SemiBold)
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            content = content
        )
    }
}

@Composable
private fun SRow(icon: ImageVector, tint: Color, title: String, value: String? = null, onClick: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().then(if (onClick != null) Modifier.press(onClick) else Modifier).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(tint.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(19.dp))
        }
        T(title, 15, gh.text, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
        if (value != null) T(value, 13, gh.text2, modifier = Modifier.padding(end = 6.dp))
        if (onClick != null) Icon(Icons.Outlined.ChevronRight, null, tint = gh.text3, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SToggle(title: String, checked: Boolean, desc: String? = null, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            T(title, 15, gh.text)
            if (desc != null) T(desc, 12, gh.text3)
        }
        GhSwitch(checked, onChange)
    }
}

@Composable
private fun SMain(ui: Ui, snap: Snapshot, st: Settings) {
    SScaffold("Ajustes", { ui.pop() }) {
        GhCard(Modifier.padding(top = 4.dp)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                val pal = Palette.habit
                Canvas(Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF100F12))) {
                    val step = size.width / 6.4f
                    val c = step * 0.78f
                    val x0 = (size.width - (5 * step - (step - c))) / 2f
                    for (r in 0..4) for (k in 0..4) {
                        val on = (r * 3 + k * 2) % 4 != 0
                        drawRoundRect(
                            if (on) pal[(r * 2 + k * 3) % 10] else Color(0xFF2B2A31),
                            Offset(x0 + k * step, x0 + r * step), Size(c, c), CornerRadius(c * 0.25f)
                        )
                    }
                }
                Column(Modifier.padding(start = 14.dp)) {
                    Row {
                        T("Good ", 18, gh.text, FontWeight.Bold)
                        T("Habits", 18, gh.primary, FontWeight.Bold)
                    }
                    T("Todas las funciones desbloqueadas.", 12, gh.text2)
                }
            }
        }
        Label("App")
        GhCard {
            SRow(Icons.Outlined.Settings, Color(0xFFF271B5), "General") { ui.push(Route.Cfg(SPage.General)) }
            SRow(Icons.Outlined.GridView, Color(0xFF2BD2BE), "Vista general") { ui.push(Route.Cfg(SPage.Overview)) }
            SRow(Icons.Outlined.Notifications, Color(0xFF20D1ED), "Recordatorios de control diario") { ui.push(Route.Cfg(SPage.Daily)) }
            SRow(Icons.Outlined.Palette, Color(0xFFFBBD23), "Tema") { ui.push(Route.Cfg(SPage.Theme)) }
            SRow(Icons.Outlined.Archive, Color(0xFF49DC80), "Hábitos archivados", snap.habits.count { it.archived }.toString()) { ui.push(Route.Cfg(SPage.Archived)) }
            SRow(
                Icons.Outlined.CloudUpload, Color(0xFF37BCF7), "Copia y restauración",
                if (st.lastBackup == 0L) "No" else SimpleDateFormat("dd/MM/yy", Locale("es")).format(Date(st.lastBackup))
            ) { ui.push(Route.Cfg(SPage.Backup)) }
            SRow(Icons.Outlined.FormatListBulleted, Color(0xFFF77070), "Reordenar hábitos") { ui.push(Route.Cfg(SPage.Reorder)) }
        }
        Label("Acerca de la app")
        GhCard { SRow(Icons.Outlined.Info, Color(0xFF93A0B7), "Versión", "1.0") }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SGeneral(ui: Ui, st: Settings) {
    SScaffold("General", { ui.pop() }) {
        Label("Inicio de la semana")
        Seg(listOf("Lunes", "Sábado", "Domingo"), when (st.weekStart) { 5 -> 1; 6 -> 2; else -> 0 }, { i ->
            Prefs.update { it.copy(weekStart = when (i) { 1 -> 5; 2 -> 6; else -> 0 }) }
        })
        Spacer(Modifier.height(12.dp))
        GhCard {
            SToggle("Resaltar el día actual", st.highlightToday) { v -> Prefs.update { it.copy(highlightToday = v) } }
            SToggle("Respuesta háptica", st.haptic) { v -> Prefs.update { it.copy(haptic = v) } }
            SToggle("Notas", st.notes) { v -> Prefs.update { it.copy(notes = v) } }
        }
        Label("Widgets de pantalla de inicio")
        T(
            "Mantén pulsada la pantalla de inicio de tu teléfono, elige Widgets y busca Good Habits. Al añadirlo podrás elegir el hábito que muestra.",
            13, gh.text2, modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun SOverview(ui: Ui, st: Settings) {
    SScaffold("Vista general", { ui.pop() }) {
        Label("Modos de vista del panel")
        GhCard {
            SToggle("Mostrar barra inferior", st.showBottomBar) { v -> Prefs.update { it.copy(showBottomBar = v) } }
            SToggle("Deslizar entre modos de vista", st.swipeModes) { v -> Prefs.update { it.copy(swipeModes = v) } }
            SToggle("Mostrar filtro", st.showFilter) { v -> Prefs.update { it.copy(showFilter = v) } }
            SToggle("Mostrar nombres de categorías", st.showCategoryNames) { v -> Prefs.update { it.copy(showCategoryNames = v) } }
            SToggle("Mostrar filtro de hábitos completados", st.showCompletedFilter) { v -> Prefs.update { it.copy(showCompletedFilter = v) } }
            SToggle("Mostrar botón de búsqueda", st.showSearch) { v -> Prefs.update { it.copy(showSearch = v) } }
        }
        Label("Hábitos completados")
        Seg(listOf("En su lugar", "Al final", "Ocultar"), st.completedMode, { m -> Prefs.update { it.copy(completedMode = m) } })
        Label("Mostrar contador de rachas")
        GhCard {
            SToggle("Vista de tarjetas", st.streakCards) { v -> Prefs.update { it.copy(streakCards = v) } }
            SToggle("Vista compacta", st.streakCompact) { v -> Prefs.update { it.copy(streakCompact = v) } }
            SToggle("Vista mensual", st.streakMonthly) { v -> Prefs.update { it.copy(streakMonthly = v) } }
        }
        Label("Personalización del panel")
        GhCard {
            SToggle("Mostrar etiquetas de mes", st.monthLabels) { v -> Prefs.update { it.copy(monthLabels = v) } }
            SToggle("Mostrar etiquetas de día", st.dayLabels) { v -> Prefs.update { it.copy(dayLabels = v) } }
            SToggle("Mostrar categorías (compacta y mensual)", st.showCategories) { v -> Prefs.update { it.copy(showCategories = v) } }
        }
        Label("Lista compacta")
        T("Días mostrados", 14, gh.text, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (n in 1..7) {
                Box(
                    Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(12.dp))
                        .background(if (st.compactDays == n) gh.primary else gh.surface2)
                        .border(1.dp, gh.line, RoundedCornerShape(12.dp))
                        .press { Prefs.update { it.copy(compactDays = n) } },
                    contentAlignment = Alignment.Center
                ) { T(n.toString(), 14, if (st.compactDays == n) Color.White else gh.text2, FontWeight.Medium, mono = true) }
            }
        }
        Spacer(Modifier.height(14.dp))
        T("Nombres de los hábitos", 14, gh.text, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        Seg(listOf("Oculto", "Visible"), if (st.compactNames) 1 else 0, { i -> Prefs.update { it.copy(compactNames = i == 1) } })
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SDaily(ui: Ui, st: Settings) {
    var timeDialog by remember { mutableStateOf(false) }
    SScaffold("Control diario", { ui.pop() }) {
        T(
            "Recibe un aviso al final del día si te quedan hábitos por completar.",
            13, gh.text2, modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        GhCard {
            SToggle("Aviso diario", st.dailyReminder) { v ->
                Prefs.update { it.copy(dailyReminder = v) }
                if (v) ui.askNotif()
                ui.vm.refresh()
            }
            if (st.dailyReminder) SRow(Icons.Outlined.Notifications, Color(0xFF20D1ED), "Hora", D.hhmm(st.dailyMinute)) { timeDialog = true }
        }
    }
    if (timeDialog) {
        TimeDialog(st.dailyMinute, { timeDialog = false }) { m ->
            Prefs.update { it.copy(dailyMinute = m) }
            ui.vm.refresh()
            timeDialog = false
        }
    }
}

@Composable
private fun RadioRow(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().press(onClick).padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        T(text, 15, gh.text, modifier = Modifier.weight(1f))
        if (selected) {
            Box(Modifier.size(22.dp).clip(CircleShape).background(gh.primary), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun STheme(ui: Ui, st: Settings) {
    SScaffold("Tema", { ui.pop() }) {
        Label("Modo")
        GhCard {
            listOf("Sistema", "Claro", "Oscuro").forEachIndexed { i, t -> RadioRow(t, st.themeMode == i) { Prefs.update { it.copy(themeMode = i) } } }
        }
        Label("Tema del widget")
        GhCard {
            listOf("Sistema", "Claro", "Oscuro").forEachIndexed { i, t ->
                RadioRow(t, st.widgetTheme == i) { Prefs.update { it.copy(widgetTheme = i) }; ui.vm.refresh() }
            }
        }
    }
}

@Composable
private fun SArchived(ui: Ui, snap: Snapshot) {
    val list = snap.habits.filter { it.archived }
    SScaffold("Hábitos archivados", { ui.pop() }) {
        if (list.isEmpty()) {
            Column(Modifier.fillMaxWidth().padding(top = 120.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.Archive, null, tint = gh.text2, modifier = Modifier.size(28.dp))
                T("Tu archivo está vacío", 18, gh.text, FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
                T("Los hábitos archivados aparecerán aquí.", 14, gh.text2, align = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
            }
        } else {
            GhCard(Modifier.padding(top = 6.dp)) {
                list.forEach { h ->
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        HabitTile(h.icon, Palette.color(h.colorIdx), 36.dp)
                        T(h.name, 15, gh.text, modifier = Modifier.weight(1f).padding(horizontal = 12.dp), maxLines = 1)
                        CircleBtn(Icons.Outlined.Unarchive, { ui.vm.archive(h.id, false) }, size = 38.dp)
                        Spacer(Modifier.size(8.dp))
                        CircleBtn(Icons.Outlined.Delete, {
                            ui.confirm = Confirm("Eliminar hábito", "Se borrará «${h.name}» con todo su historial. No se puede deshacer.", "Eliminar") { ui.vm.delete(h.id) }
                        }, size = 38.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SBackup(ui: Ui, st: Settings) {
    SScaffold("Copia y restauración", { ui.pop() }) {
        T(
            "Guarda todos tus hábitos, registros, notas y recordatorios en un archivo JSON, y restáuralos cuando quieras.",
            13, gh.text2, modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        GhCard {
            Column(Modifier.padding(16.dp)) {
                T("Última copia", 12, gh.text3)
                T(
                    if (st.lastBackup == 0L) "Aún no hiciste ninguna copia" else SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es")).format(Date(st.lastBackup)),
                    15, gh.text, mono = true, modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        PrimaryButton("Exportar copia", { ui.doExport() })
        Spacer(Modifier.height(10.dp))
        Box(
            Modifier.fillMaxWidth().height(56.dp).clip(CircleShape).background(gh.surface2).border(1.dp, gh.line, CircleShape)
                .press {
                    ui.confirm = Confirm("Restaurar copia", "Se reemplazarán todos los datos actuales por los del archivo elegido.", "Elegir archivo") { ui.doImport() }
                },
            contentAlignment = Alignment.Center
        ) { T("Importar copia", 16, gh.text, FontWeight.SemiBold) }
    }
}

@Composable
private fun SReorder(ui: Ui, snap: Snapshot) {
    val list = snap.habits.filter { !it.archived }
    SScaffold("Reordenar hábitos", { ui.pop() }) {
        if (list.isEmpty()) T("No hay hábitos para ordenar.", 14, gh.text2, modifier = Modifier.padding(16.dp))
        else GhCard(Modifier.padding(top = 6.dp)) {
            list.forEachIndexed { i, h ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    HabitTile(h.icon, Palette.color(h.colorIdx), 36.dp)
                    T(h.name, 15, gh.text, modifier = Modifier.weight(1f).padding(horizontal = 12.dp), maxLines = 1)
                    CircleBtn(Icons.Outlined.ArrowUpward, { if (i > 0) ui.vm.move(h.id, -1) }, size = 38.dp)
                    Spacer(Modifier.size(8.dp))
                    CircleBtn(Icons.Outlined.ArrowDownward, { if (i < list.lastIndex) ui.vm.move(h.id, 1) }, size = 38.dp)
                }
            }
        }
    }
}
