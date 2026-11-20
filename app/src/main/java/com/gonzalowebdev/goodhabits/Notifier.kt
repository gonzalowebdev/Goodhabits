package com.gonzalowebdev.goodhabits

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

object Notifier {
    const val CH = "habits"
    const val DAILY_ID = 1_000_000

    fun ensureChannel(c: Context) {
        val nm = c.getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CH) == null) {
            nm.createNotificationChannel(NotificationChannel(CH, "Recordatorios de hábitos", NotificationManager.IMPORTANCE_DEFAULT))
        }
    }

    fun nextTrigger(daysMask: Int, minute: Int): Long {
        val now = ZonedDateTime.now()
        for (i in 0..8) {
            val date = now.toLocalDate().plusDays(i.toLong())
            val dow = date.dayOfWeek.value - 1
            if (((daysMask shr dow) and 1) == 0) continue
            val t = date.atTime(minute / 60, minute % 60).atZone(now.zone)
            if (t.isAfter(now)) return t.toInstant().toEpochMilli()
        }
        return now.plusDays(1).toInstant().toEpochMilli()
    }

    private fun pending(c: Context, id: Int): PendingIntent {
        val i = Intent(c, ReminderReceiver::class.java).putExtra("id", id)
        return PendingIntent.getBroadcast(c, id, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun schedule(c: Context, id: Int, at: Long) {
        val am = c.getSystemService(AlarmManager::class.java)
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending(c, id))
    }

    private fun cancel(c: Context, id: Int) {
        c.getSystemService(AlarmManager::class.java).cancel(pending(c, id))
    }

    suspend fun rescheduleAll(c: Context) {
        val dao = GhDb.get(c).dao()
        val store = c.getSharedPreferences("gh_alarms", Context.MODE_PRIVATE)
        (store.getStringSet("ids", emptySet()) ?: emptySet()).forEach { cancel(c, it.toInt()) }
        val ids = HashSet<String>()
        val active = dao.habits().filter { !it.archived }.map { it.id }.toSet()
        dao.reminders().filter { it.habitId in active }.forEach { r ->
            schedule(c, r.id.toInt(), nextTrigger(r.daysMask, r.minute))
            ids.add(r.id.toString())
        }
        val s = Prefs.get(c)
        if (s.dailyReminder) {
            schedule(c, DAILY_ID, nextTrigger(127, s.dailyMinute))
            ids.add(DAILY_ID.toString())
        }
        store.edit().putStringSet("ids", ids).apply()
    }

    fun notify(c: Context, id: Int, title: String, text: String) {
        val nm = c.getSystemService(NotificationManager::class.java)
        if (!nm.areNotificationsEnabled()) return
        ensureChannel(c)
        val open = PendingIntent.getActivity(
            c, 0, Intent(c, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = Notification.Builder(c, CH)
            .setSmallIcon(R.drawable.ic_notif)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        nm.notify(id, n)
    }

    suspend fun handle(c: Context, id: Int) {
        val dao = GhDb.get(c).dao()
        val today = D.today()
        val habits = dao.habits().filter { !it.archived }
        val entries = dao.entries().groupBy { it.habitId }.mapValues { (_, v) -> v.associateBy { it.day } }
        if (id == DAILY_ID) {
            val pend = habits.filter { h ->
                h.scheduled(today) && !h.quit && !h.isDone(entries[h.id]?.get(today), today, today)
            }
            if (pend.isNotEmpty()) {
                notify(c, DAILY_ID, "Good Habits", "Te quedan ${pend.size} hábito(s) por completar hoy.")
            }
            val s = Prefs.get(c)
            if (s.dailyReminder) schedule(c, DAILY_ID, nextTrigger(127, s.dailyMinute))
            return
        }
        val r = dao.reminders().firstOrNull { it.id == id.toLong() } ?: return
        val h = habits.firstOrNull { it.id == r.habitId } ?: return
        val done = h.isDone(entries[h.id]?.get(today), today, today)
        if (!done && h.scheduled(today)) {
            notify(c, id, h.name, if (h.quit) "Mantente firme hoy." else "Es hora de completar tu hábito.")
        }
        schedule(c, id, nextTrigger(r.daysMask, r.minute))
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", -1)
        if (id < 0) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try { Notifier.handle(context.applicationContext, id) } finally { pending.finish() }
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try { Notifier.rescheduleAll(context.applicationContext) } finally { pending.finish() }
        }
    }
}
