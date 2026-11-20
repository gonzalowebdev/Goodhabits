package com.gonzalowebdev.goodhabits

import android.app.Application
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class Snapshot(
    val loaded: Boolean = false,
    val habits: List<Habit> = emptyList(),
    val entries: Map<Long, Map<Long, Entry>> = emptyMap(),
    val categories: List<Category> = emptyList(),
    val links: List<HabitCategory> = emptyList(),
    val reminders: List<Reminder> = emptyList()
) {
    fun em(id: Long): Map<Long, Entry> = entries[id] ?: emptyMap()
    fun habit(id: Long): Habit? = habits.firstOrNull { it.id == id }
    fun catsOf(id: Long): Set<Long> = links.filter { it.habitId == id }.map { it.categoryId }.toSet()
    fun remindersOf(id: Long): List<Reminder> = reminders.filter { it.habitId == id }
}

class AppVm(private val app: Application) : AndroidViewModel(app) {
    private val dao = GhDb.get(app).dao()

    val data: StateFlow<Snapshot> = combine(
        dao.habitsFlow(), dao.entriesFlow(), dao.categoriesFlow(), dao.linksFlow(), dao.remindersFlow()
    ) { h, e, c, l, r ->
        Snapshot(true, h, e.groupBy { it.habitId }.mapValues { (_, v) -> v.associateBy { it.day } }, c, l, r)
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.Eagerly, Snapshot())

    private fun io(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) { block() }
    }

    private suspend fun afterChange() {
        try { Notifier.rescheduleAll(app) } catch (e: Exception) {}
        try { HabitWidget().updateAll(app) } catch (e: Exception) {}
    }

    // ---- registro de entradas ----
    private suspend fun upsert(h: Habit, day: Long, f: (Entry) -> Entry) {
        val cur = dao.entry(h.id, day) ?: Entry(h.id, day, 0.0, false, "")
        val n = f(cur)
        if (n.value == 0.0 && !n.slip && n.note.isBlank()) dao.deleteEntry(h.id, day) else dao.putEntry(n)
        afterChange()
    }

    /** Acción rápida al tocar el botón o una celda. */
    fun tap(h: Habit, day: Long) = io {
        upsert(h, day) { e ->
            when {
                h.quit && h.mode == MODE_YN -> e.copy(slip = !e.slip)
                h.mode == MODE_YN -> e.copy(value = if (e.value >= 1.0) 0.0 else 1.0)
                else -> e.copy(value = e.value + 1.0)
            }
        }
    }

    fun setValue(h: Habit, day: Long, v: Double) = io { upsert(h, day) { it.copy(value = v.coerceAtLeast(0.0)) } }

    fun saveEntry(h: Habit, day: Long, value: Double, slip: Boolean, note: String) = io {
        upsert(h, day) { it.copy(value = value, slip = slip, note = note.trim()) }
    }

    fun deleteEntry(h: Habit, day: Long) = io { dao.deleteEntry(h.id, day); afterChange() }

    // ---- hábitos ----
    fun saveHabit(d: Habit, cats: Set<Long>, rems: List<Reminder>, onSaved: (Long) -> Unit = {}) = io {
        val max = dao.habits().maxOfOrNull { it.sortOrder } ?: -1
        val id = if (d.id == 0L) dao.putHabit(d.copy(sortOrder = max + 1, createdDay = D.today())) else { dao.putHabit(d); d.id }
        dao.deleteLinksOfHabit(id)
        cats.forEach { dao.putLink(HabitCategory(id, it)) }
        dao.deleteRemindersOf(id)
        rems.forEach { dao.putReminder(it.copy(id = 0, habitId = id)) }
        afterChange()
        onSaved(id)
    }

    fun archive(id: Long, a: Boolean) = io { dao.setArchived(id, a); afterChange() }

    fun delete(id: Long) = io {
        dao.deleteEntriesOf(id); dao.deleteLinksOfHabit(id); dao.deleteRemindersOf(id); dao.deleteHabit(id)
        afterChange()
    }

    fun move(id: Long, delta: Int) = io {
        val list = dao.habits().toMutableList()
        val i = list.indexOfFirst { it.id == id }
        if (i < 0) return@io
        var j = i + delta
        while (j in list.indices && list[j].archived) j += delta
        if (j !in list.indices) return@io
        val t = list[i]; list[i] = list[j]; list[j] = t
        list.forEachIndexed { idx, h -> dao.setOrder(h.id, idx) }
        afterChange()
    }

    fun refresh() = io { afterChange() }

    // ---- categorías ----
    fun addCategory(name: String, onAdded: (Long) -> Unit) = io {
        val max = dao.categories().maxOfOrNull { it.sortOrder } ?: -1
        val id = dao.putCategory(Category(0, name.trim(), max + 1))
        onAdded(id)
    }

    fun deleteCategory(id: Long) = io { dao.deleteLinksOfCategory(id); dao.deleteCategory(id) }

    // ---- copia y restauración ----
    suspend fun exportJson(): String = Backup.export(dao)

    suspend fun importJson(text: String) {
        Backup.import(dao, text)
        afterChange()
    }
}
