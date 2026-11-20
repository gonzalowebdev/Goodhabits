package com.gonzalowebdev.goodhabits

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

sealed interface Route {
    data object Home : Route
    data class Detail(val id: Long) : Route
    data class Notes(val id: Long) : Route
    data object Stats : Route
    data class Cfg(val page: SPage) : Route
}

enum class SPage { Main, General, Overview, Daily, Theme, Archived, Backup, Reorder }

data class LogReq(val habitId: Long, val day: Long)
data class EditorReq(val habitId: Long)
data class Confirm(val title: String, val text: String, val action: String, val onOk: () -> Unit)

class Ui(val vm: AppVm) {
    val stack = mutableStateListOf<Route>(Route.Home)
    var editor by mutableStateOf<EditorReq?>(null)
    var log by mutableStateOf<LogReq?>(null)
    var number by mutableStateOf<LogReq?>(null)
    var confirm by mutableStateOf<Confirm?>(null)
    var doExport: () -> Unit = {}
    var doImport: () -> Unit = {}
    var askNotif: () -> Unit = {}

    fun push(r: Route) { stack.add(r) }
    fun pop() { if (stack.size > 1) stack.removeAt(stack.lastIndex) }

    /** Toque rápido sobre un botón o una celda. */
    fun quick(h: Habit, day: Long) {
        if (h.mode == MODE_MEASURE || (h.mode == MODE_COUNT && h.tapTotal)) number = LogReq(h.id, day)
        else vm.tap(h, day)
    }
}

@Composable
fun rememberBuzz(): () -> Unit {
    val hf = LocalHapticFeedback.current
    val st by Prefs.flow.collectAsState()
    return { if (st.haptic) hf.performHapticFeedback(HapticFeedbackType.LongPress) }
}

fun repeatLabel(mask: Int): String = when (mask) {
    127 -> "Todos los días"
    31 -> "Entre semana"
    96 -> "Fines de semana"
    else -> (0..6).filter { ((mask shr it) and 1) == 1 }.joinToString(", ") { D.DAYS_CAP[it] }
}
