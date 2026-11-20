package com.gonzalowebdev.goodhabits

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment as GAlignment
import androidx.glance.layout.Box as GBox
import androidx.glance.layout.Column as GColumn
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row as GRow
import androidx.glance.layout.Spacer as GSpacer
import androidx.glance.layout.fillMaxSize as gFillMaxSize
import androidx.glance.layout.fillMaxWidth as gFillMaxWidth
import androidx.glance.layout.height as gHeight
import androidx.glance.layout.padding as gPadding
import androidx.glance.layout.size as gSize
import androidx.glance.text.FontWeight as GFontWeight
import androidx.glance.text.Text as GText
import androidx.glance.text.TextStyle as GTextStyle
import androidx.glance.unit.ColorProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color as CColor

object WidgetPrefs {
    private fun sp(c: Context) = c.getSharedPreferences("gh_widgets", Context.MODE_PRIVATE)
    fun get(c: Context, wid: Int): Long = sp(c).getLong("w$wid", -1L)
    fun set(c: Context, wid: Int, habitId: Long) { sp(c).edit().putLong("w$wid", habitId).apply() }
}

private fun heatBitmap(
    w: Int, h: Int, habit: Habit, em: Map<Long, Entry>, weekStart: Int, dark: Boolean
): Bitmap {
    val bmp = Bitmap.createBitmap(w.coerceAtLeast(1), h.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
    val cv = Canvas(bmp)
    val today = D.today()
    val color = Palette.color(habit.colorIdx)
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    val cell = h / 7.4f
    val gap = cell * 0.24f
    val step = cell + gap
    val weeks = ((w + gap) / step).toInt().coerceAtLeast(1)
    val x0 = w - (weeks * step - gap)
    val ws = D.weekStartOf(today, weekStart)
    val emptyAlpha = if (dark) 0.12f else 0.16f
    for (col in 0 until weeks) {
        val first = ws - 7L * (weeks - 1 - col)
        for (row in 0..6) {
            val day = first + row
            if (day > today) continue
            val pr = habit.progress(em[day], day, today)
            val c = cellFill(color, pr, color.copy(alpha = emptyAlpha))
            p.color = c.toArgb()
            val l = x0 + col * step
            val t = row * step
            cv.drawRoundRect(RectF(l, t, l + cell, t + cell), cell * 0.28f, cell * 0.28f, p)
        }
    }
    return bmp
}

class HabitIdKey { companion object { val Key = ActionParameters.Key<Long>("habitId") } }

class WidgetTap : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val id = parameters[HabitIdKey.Key] ?: return
        val dao = GhDb.get(context).dao()
        val h = dao.habits().firstOrNull { it.id == id } ?: return
        val day = D.today()
        val cur = dao.entry(id, day) ?: Entry(id, day, 0.0, false, "")
        val n = when {
            h.quit && h.mode == MODE_YN -> cur.copy(slip = !cur.slip)
            h.mode == MODE_YN -> cur.copy(value = if (cur.value >= 1.0) 0.0 else 1.0)
            else -> cur.copy(value = cur.value + 1.0)
        }
        if (n.value == 0.0 && !n.slip && n.note.isBlank()) dao.deleteEntry(id, day) else dao.putEntry(n)
        HabitWidget().updateAll(context)
    }
}

class HabitWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val wid = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val habitId = WidgetPrefs.get(context, wid)
        val dao = GhDb.get(context).dao()
        val habit = dao.habits().firstOrNull { it.id == habitId }
        val em = if (habit != null) dao.entries().filter { it.habitId == habit.id }.associateBy { it.day } else emptyMap()
        val st = Prefs.get(context)
        val dark = when (st.widgetTheme) {
            1 -> false
            2 -> true
            else -> (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        }
        provideContent { WidgetContent(habit, em, dark, st.weekStart) }
    }
}

@androidx.compose.runtime.Composable
private fun WidgetContent(habit: Habit?, em: Map<Long, Entry>, dark: Boolean, weekStart: Int) {
    val ctx = LocalContext.current
    val size = LocalSize.current
    val d = ctx.resources.displayMetrics.density
    val bg = if (dark) CColor(0xFF1A191C) else CColor(0xFFFFFFFF)
    val fg = if (dark) CColor(0xFFF2F2F5) else CColor(0xFF16151A)
    GBox(
        modifier = GlanceModifier.gFillMaxSize().background(ColorProvider(bg)).cornerRadius(22.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        if (habit == null) {
            GText("Toca para abrir Good Habits", style = GTextStyle(color = ColorProvider(fg), fontSize = 13.sp))
        } else {
            val color = Palette.color(habit.colorIdx)
            val done = habit.isDone(em[D.today()], D.today(), D.today())
            val gridW = ((size.width.value - 28f) * d).toInt()
            val gridH = ((size.height.value - 28f - 44f) * d).toInt()
            GColumn(modifier = GlanceModifier.gFillMaxSize().gPadding(14.dp)) {
                GRow(modifier = GlanceModifier.gFillMaxWidth(), verticalAlignment = GAlignment.Vertical.CenterVertically) {
                    GText(
                        habit.name, modifier = GlanceModifier.defaultWeight(), maxLines = 1,
                        style = GTextStyle(color = ColorProvider(fg), fontSize = 16.sp, fontWeight = GFontWeight.Bold)
                    )
                    GBox(
                        modifier = GlanceModifier.gSize(36.dp)
                            .background(ColorProvider(if (done) color else (if (dark) CColor(0xFF2B2A31) else CColor(0xFFE3E2E9))))
                            .cornerRadius(11.dp)
                            .clickable(actionRunCallback<WidgetTap>(actionParametersOf(HabitIdKey.Key to habit.id))),
                        contentAlignment = GAlignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_check), contentDescription = null,
                            colorFilter = ColorFilter.tint(ColorProvider(if (done) CColor(0xFF1C1604) else CColor(0xFF8E8D96))),
                            modifier = GlanceModifier.gSize(20.dp)
                        )
                    }
                }
                GSpacer(modifier = GlanceModifier.gHeight(8.dp))
                if (gridW > 10 && gridH > 10) {
                    Image(
                        provider = ImageProvider(heatBitmap(gridW, gridH, habit, em, weekStart, dark)),
                        contentDescription = null, contentScale = ContentScale.Fit,
                        modifier = GlanceModifier.gFillMaxWidth()
                    )
                }
            }
        }
    }
}

class HabitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HabitWidget()
}

class WidgetConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prefs.init(this)
        val wid = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        setResult(RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, wid))
        if (wid == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }
        enableEdgeToEdge()
        val dao = GhDb.get(this).dao()
        setContent {
            val st by Prefs.flow.collectAsState()
            GhTheme(st.themeMode) {
                val habits by dao.habitsFlow().collectAsState(initial = emptyList())
                Column(Modifier.fillMaxSize().background(gh.bg).statusBarsPadding().padding(16.dp)) {
                    T("Elige un hábito", 22, gh.text, FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
                    val list = habits.filter { !it.archived }
                    if (list.isEmpty()) T("Primero crea un hábito dentro de la app.", 14, gh.text2)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                        items(list, key = { it.id }) { h ->
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(gh.surface)
                                    .border(1.dp, gh.line, RoundedCornerShape(16.dp))
                                    .clickable {
                                        WidgetPrefs.set(this@WidgetConfigActivity, wid, h.id)
                                        lifecycleScope.launch(Dispatchers.Main) {
                                            try {
                                                val gid = GlanceAppWidgetManager(this@WidgetConfigActivity).getGlanceIdBy(wid)
                                                HabitWidget().update(this@WidgetConfigActivity, gid)
                                            } catch (e: Exception) {}
                                            setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, wid))
                                            finish()
                                        }
                                    }.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HabitTile(h.icon, Palette.color(h.colorIdx), 40.dp)
                                T(h.name, 16, gh.text, FontWeight.Medium, modifier = Modifier.padding(start = 12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
