package com.gonzalowebdev.goodhabits

import java.time.LocalDate

object D {
    val DAYS3 = listOf("lun", "mar", "mié", "jue", "vie", "sáb", "dom")
    val DAYS2 = listOf("Lu", "Ma", "Mi", "Ju", "Vi", "Sá", "Do")
    val DAYS_CAP = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    val MONTHS = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio", "julio",
        "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )
    val MONTHS3 = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")

    fun today(): Long = LocalDate.now().toEpochDay()
    fun date(day: Long): LocalDate = LocalDate.ofEpochDay(day)
    /** Lunes = 0 ... Domingo = 6 */
    fun dow(day: Long): Int = Math.floorMod(day + 3, 7L).toInt()
    fun monthName(m: Int) = MONTHS[m - 1]
    fun monthYear(day: Long): String { val d = date(day); return "${MONTHS[d.monthValue - 1]} de ${d.year}" }
    fun short(day: Long): String { val d = date(day); return "${DAYS3[dow(day)]}, ${d.dayOfMonth} ${MONTHS3[d.monthValue - 1].lowercase()} ${d.year}" }
    fun rangeLabel(a: Long, b: Long): String {
        val x = date(a); val y = date(b)
        return "${x.dayOfMonth} ${MONTHS3[x.monthValue - 1].uppercase()} — ${y.dayOfMonth} ${MONTHS3[y.monthValue - 1].uppercase()}"
    }
    fun weekStartOf(day: Long, weekStart: Int): Long = day - Math.floorMod(dow(day) - weekStart, 7)
    fun fmt(v: Double): String = if (v == Math.floor(v) && Math.abs(v) < 1e9) v.toLong().toString() else String.format(java.util.Locale.US, "%.1f", v)
    fun hhmm(minute: Int) = String.format(java.util.Locale.US, "%02d:%02d", minute / 60, minute % 60)
}

fun Habit.scheduled(day: Long): Boolean = ((daysMask shr D.dow(day)) and 1) == 1

fun Habit.progress(e: Entry?, day: Long, today: Long): Float {
    if (day > today) return 0f
    if (quit) {
        if (day < createdDay) return 0f
        val slipped = e != null && (e.slip || (mode != MODE_YN && e.value >= target))
        return if (slipped) 0f else 1f
    }
    if (e != null && e.slip) return 0f
    val v = e?.value ?: 0.0
    if (mode == MODE_YN) return if (v >= 1.0) 1f else 0f
    if (target <= 0.0) return if (v > 0.0) 1f else 0f
    return (v / target).toFloat().coerceIn(0f, 1f)
}

fun Habit.isDone(e: Entry?, day: Long, today: Long): Boolean = progress(e, day, today) >= 1f

fun Habit.startDay(em: Map<Long, Entry>): Long {
    val first = em.keys.minOrNull()
    return if (quit) createdDay else if (first != null) minOf(createdDay, first) else createdDay
}

fun streakOf(h: Habit, em: Map<Long, Entry>, today: Long): Int {
    var d = today
    var n = 0
    val limit = if (h.quit) h.createdDay else today - 3650
    while (d >= limit) {
        if (h.scheduled(d)) {
            if (h.isDone(em[d], d, today)) n++
            else if (!(d == today && !h.quit)) break
        }
        d--
    }
    return n
}

fun longestOf(h: Habit, em: Map<Long, Entry>, today: Long): Int {
    val start = h.startDay(em)
    var best = 0
    var cur = 0
    var d = start
    while (d <= today) {
        if (h.scheduled(d)) {
            if (h.isDone(em[d], d, today)) { cur++; if (cur > best) best = cur }
            else if (!(d == today && !h.quit)) cur = 0
        }
        d++
    }
    return best
}

fun totalDone(h: Habit, em: Map<Long, Entry>, today: Long): Int {
    var n = 0
    var d = h.startDay(em)
    while (d <= today) {
        if (h.scheduled(d) && h.isDone(em[d], d, today)) n++
        d++
    }
    return n
}

/** Porcentaje (0..1) de días programados cumplidos en los últimos [n] días. */
fun rateOf(h: Habit, em: Map<Long, Entry>, today: Long, n: Int): Float {
    val from = maxOf(today - n + 1, h.startDay(em))
    var sched = 0
    var done = 0
    var d = from
    while (d <= today) {
        if (h.scheduled(d)) { sched++; if (h.isDone(em[d], d, today)) done++ }
        d++
    }
    return if (sched == 0) 0f else done.toFloat() / sched
}

fun doneByWeekday(h: Habit, em: Map<Long, Entry>, today: Long): IntArray {
    val out = IntArray(7)
    var d = h.startDay(em)
    while (d <= today) {
        if (h.scheduled(d) && h.isDone(em[d], d, today)) out[D.dow(d)]++
        d++
    }
    return out
}

/** Cumplidos por mes en los últimos 12 meses (el último es el mes actual). */
fun doneByMonth(h: Habit, em: Map<Long, Entry>, today: Long): List<Pair<String, Int>> {
    val now = D.date(today)
    val res = ArrayList<Pair<String, Int>>()
    for (i in 11 downTo 0) {
        val first = now.withDayOfMonth(1).minusMonths(i.toLong())
        val last = first.plusMonths(1).minusDays(1)
        var n = 0
        var d = first.toEpochDay()
        val end = minOf(last.toEpochDay(), today)
        while (d <= end) {
            if (h.scheduled(d) && h.isDone(em[d], d, today)) n++
            d++
        }
        res.add(D.MONTHS3[first.monthValue - 1] to n)
    }
    return res
}
