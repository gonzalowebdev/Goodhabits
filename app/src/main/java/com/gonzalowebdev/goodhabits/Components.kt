@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.gonzalowebdev.goodhabits

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Bathtub
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Hiking
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.NoDrinks
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Piano
import androidx.compose.material.icons.outlined.Pool
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.SmokeFree
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.drawscope.DrawScope

// ------------------------------------------------------------------ íconos de hábitos

object Ic {
    val list: List<Pair<String, ImageVector>> = listOf(
        "pulse" to Icons.Outlined.MonitorHeart, "fitness" to Icons.Outlined.FitnessCenter,
        "run" to Icons.Outlined.DirectionsRun, "walk" to Icons.Outlined.DirectionsWalk,
        "bike" to Icons.Outlined.DirectionsBike, "swim" to Icons.Outlined.Pool,
        "hike" to Icons.Outlined.Hiking, "soccer" to Icons.Outlined.SportsSoccer,
        "yoga" to Icons.Outlined.SelfImprovement, "book" to Icons.Outlined.MenuBook,
        "write" to Icons.Outlined.Edit, "brush" to Icons.Outlined.Brush,
        "palette" to Icons.Outlined.Palette, "music" to Icons.Outlined.MusicNote,
        "piano" to Icons.Outlined.Piano, "headphones" to Icons.Outlined.Headphones,
        "game" to Icons.Outlined.SportsEsports, "movie" to Icons.Outlined.Movie,
        "code" to Icons.Outlined.Code, "laptop" to Icons.Outlined.Laptop,
        "phone" to Icons.Outlined.PhoneAndroid, "camera" to Icons.Outlined.CameraAlt,
        "language" to Icons.Outlined.Language, "translate" to Icons.Outlined.Translate,
        "school" to Icons.Outlined.School, "work" to Icons.Outlined.Work,
        "home" to Icons.Outlined.Home, "clean" to Icons.Outlined.CleaningServices,
        "cart" to Icons.Outlined.ShoppingCart, "money" to Icons.Outlined.AttachMoney,
        "savings" to Icons.Outlined.Savings, "water" to Icons.Outlined.WaterDrop,
        "drink" to Icons.Outlined.LocalDrink, "coffee" to Icons.Outlined.LocalCafe,
        "food" to Icons.Outlined.Restaurant, "fastfood" to Icons.Outlined.Fastfood,
        "pills" to Icons.Outlined.Medication, "sleep" to Icons.Outlined.Bedtime,
        "sun" to Icons.Outlined.WbSunny, "alarm" to Icons.Outlined.Alarm,
        "timer" to Icons.Outlined.Timer, "check" to Icons.Outlined.Checklist,
        "flag" to Icons.Outlined.Flag, "star" to Icons.Outlined.Star,
        "heart" to Icons.Outlined.Favorite, "bolt" to Icons.Outlined.Bolt,
        "trophy" to Icons.Outlined.EmojiEvents, "mind" to Icons.Outlined.Psychology,
        "mood" to Icons.Outlined.Mood, "pets" to Icons.Outlined.Pets,
        "spa" to Icons.Outlined.Spa, "plant" to Icons.Outlined.LocalFlorist,
        "tree" to Icons.Outlined.Park, "forest" to Icons.Outlined.Forest,
        "eco" to Icons.Outlined.Eco, "car" to Icons.Outlined.DirectionsCar,
        "flight" to Icons.Outlined.Flight, "cake" to Icons.Outlined.Cake,
        "bath" to Icons.Outlined.Bathtub, "nosmoke" to Icons.Outlined.SmokeFree,
        "nodrink" to Icons.Outlined.NoDrinks, "call" to Icons.Outlined.Call,
        "mail" to Icons.Outlined.Mail, "group" to Icons.Outlined.Group,
        "cloud" to Icons.Outlined.Cloud
    )
    private val map = list.toMap()
    fun get(key: String): ImageVector = map[key] ?: Icons.Outlined.MonitorHeart
}

// ------------------------------------------------------------------ utilidades

val Mono = FontFamily.Monospace

fun Modifier.press(onClick: () -> Unit): Modifier = this.combinedClickable(onClick = onClick)

fun Modifier.press(onClick: () -> Unit, onLong: (() -> Unit)?): Modifier =
    this.combinedClickable(onLongClick = onLong, onClick = onClick)

@Composable
fun T(
    text: String,
    size: Int = 15,
    color: Color = gh.text,
    weight: FontWeight = FontWeight.Normal,
    mono: Boolean = false,
    modifier: Modifier = Modifier,
    align: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    spacing: Float = 0f
) {
    Text(
        text = text, color = color, fontSize = size.sp, fontWeight = weight,
        fontFamily = if (mono) Mono else FontFamily.Default, modifier = modifier,
        textAlign = align, maxLines = maxLines, overflow = TextOverflow.Ellipsis,
        letterSpacing = spacing.sp, lineHeight = (size * 1.35f).sp
    )
}

@Composable
fun Label(text: String, modifier: Modifier = Modifier) {
    T(text.uppercase(), 11, gh.text3, FontWeight.Medium, mono = false, modifier = modifier.padding(start = 4.dp, bottom = 8.dp, top = 18.dp), spacing = 0.8f)
}

@Composable
fun CircleBtn(
    icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier,
    size: Dp = 44.dp, filled: Boolean = false
) {
    Box(
        modifier = modifier.size(size).clip(CircleShape)
            .background(if (filled) gh.primary else gh.surface)
            .border(1.dp, if (filled) Color.Transparent else gh.line, CircleShape)
            .press(onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = if (filled) gh.onPrimary else gh.text, modifier = Modifier.size(size * 0.46f))
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Box(
        modifier = modifier.fillMaxWidth().height(56.dp).clip(CircleShape)
            .background(if (enabled) gh.primary else gh.chip)
            .press { if (enabled) onClick() },
        contentAlignment = Alignment.Center
    ) { T(text, 16, if (enabled) gh.onPrimary else gh.text3, FontWeight.SemiBold) }
}

@Composable
fun Seg(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(gh.surface2)
            .border(1.dp, gh.line, RoundedCornerShape(16.dp)).padding(4.dp)
    ) {
        options.forEachIndexed { i, o ->
            Box(
                modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(if (i == selected) gh.chip else Color.Transparent)
                    .press { onSelect(i) },
                contentAlignment = Alignment.Center
            ) { T(o, 14, if (i == selected) gh.text else gh.text2, FontWeight.Medium) }
        }
    }
}

@Composable
fun GhSwitch(checked: Boolean, onChange: (Boolean) -> Unit) {
    Switch(
        checked = checked, onCheckedChange = onChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White, checkedTrackColor = gh.primary, checkedBorderColor = gh.primary,
            uncheckedThumbColor = gh.text3, uncheckedTrackColor = gh.bg, uncheckedBorderColor = gh.primary.copy(alpha = 0.7f)
        )
    )
}

@Composable
fun GhCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(gh.surface2)
            .border(1.dp, gh.line, RoundedCornerShape(20.dp)),
        content = content
    )
}

@Composable
fun HabitTile(icon: String, color: Color, size: Dp = 40.dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(size).clip(RoundedCornerShape(size * 0.3f)).background(color.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) { Icon(Ic.get(icon), null, tint = color, modifier = Modifier.size(size * 0.55f)) }
}

@Composable
fun Pill(text: String, color: Color, icon: ImageVector? = null, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clip(RoundedCornerShape(50)).background(color.copy(alpha = 0.13f))
            .border(1.dp, color.copy(alpha = 0.28f), RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (icon != null) Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
        T(text, 12, color, FontWeight.Medium, mono = true)
    }
}

@Composable
fun GhSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss, sheetState = state, containerColor = gh.bg, contentColor = gh.text,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(Modifier.padding(top = 10.dp, bottom = 6.dp).size(width = 36.dp, height = 4.dp).clip(CircleShape).background(gh.chip))
        },
        content = content
    )
}

@Composable
fun NumberField(value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier, hint: String = "0") {
    Box(
        modifier = modifier.height(52.dp).clip(RoundedCornerShape(14.dp)).background(gh.surface2)
            .border(1.dp, gh.line, RoundedCornerShape(14.dp)).padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) T(hint, 16, gh.text3, mono = true)
        BasicTextField(
            value = value, onValueChange = { s -> onChange(s.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.')) },
            singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = TextStyle(color = gh.text, fontSize = 16.sp, fontFamily = Mono),
            cursorBrush = SolidColor(gh.primary), modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun TextInput(
    value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier,
    singleLine: Boolean = true, minHeight: Dp = 52.dp, borderColor: Color = gh.line
) {
    Box(
        modifier = modifier.fillMaxWidth().height(if (singleLine) minHeight else minHeight).clip(RoundedCornerShape(14.dp))
            .background(gh.surface2).border(1.dp, borderColor, RoundedCornerShape(14.dp)).padding(horizontal = 14.dp, vertical = if (singleLine) 0.dp else 12.dp),
        contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
    ) {
        if (value.isEmpty()) T(hint, 15, gh.text3)
        BasicTextField(
            value = value, onValueChange = onChange, singleLine = singleLine,
            textStyle = TextStyle(color = gh.text, fontSize = 15.sp), cursorBrush = SolidColor(gh.primary),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ------------------------------------------------------------------ botón de marcar

@Composable
fun CheckBtn(
    h: Habit, e: Entry?, day: Long, onTap: () -> Unit, onLong: () -> Unit,
    modifier: Modifier = Modifier, size: Dp = 46.dp
) {
    val today = D.today()
    val color = Palette.color(h.colorIdx)
    val p = h.progress(e, day, today)
    val done = p >= 1f
    val shape = RoundedCornerShape(14.dp)
    val brush: Brush = if (done) Brush.verticalGradient(listOf(color, color.copy(alpha = 0.82f))) else SolidColor(gh.chip.copy(alpha = 0.7f))
    val fg = if (done) Color(0xFF1C1604) else gh.text3
    Box(
        modifier = modifier.size(size).clip(shape).background(brush, shape)
            .border(1.dp, if (done) color else gh.line, shape).press(onTap, onLong),
        contentAlignment = Alignment.Center
    ) {
        if (h.mode == MODE_YN) {
            val slipped = h.quit && !done
            Icon(if (slipped) Icons.Outlined.Close else Icons.Outlined.Check, null, tint = fg, modifier = Modifier.size(22.dp))
        } else {
            val v = e?.value ?: 0.0
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                T(D.fmt(v), 14, fg, FontWeight.Bold, mono = true)
                T(if (h.quit) "lím ${D.fmt(h.target)}" else "/${D.fmt(h.target)}", 8, fg.copy(alpha = 0.7f), mono = true)
            }
        }
    }
}

// ------------------------------------------------------------------ cuadrícula de contribuciones

fun cellFill(color: Color, p: Float, empty: Color): Color =
    if (p >= 1f) color else if (p > 0f) color.copy(alpha = 0.35f + 0.5f * p) else empty

@Composable
fun Heatmap(
    h: Habit, em: Map<Long, Entry>, weekStart: Int, cell: Dp, gap: Dp,
    months: Boolean, days: Boolean, modifier: Modifier = Modifier
) {
    val today = D.today()
    val color = Palette.color(h.colorIdx)
    val empty = color.copy(alpha = if (gh.dark) 0.10f else 0.14f)
    val tm = rememberTextMeasurer()
    val label = TextStyle(fontSize = 9.sp, color = gh.text3, fontFamily = Mono)
    val topH = if (months) 16.dp else 0.dp
    val total = topH + cell * 7 + gap * 6
    Canvas(modifier = modifier.fillMaxWidth().height(total)) {
        val c = cell.toPx()
        val g = gap.toPx()
        val step = c + g
        val leftW = if (days) 30.dp.toPx() else 0f
        val top = topH.toPx()
        val weeks = (((size.width - leftW) + g) / step).toInt().coerceAtLeast(1)
        val x0 = size.width - (weeks * step - g)
        val ws = D.weekStartOf(today, weekStart)
        var lastLabelCol = -10
        for (col in 0 until weeks) {
            val first = ws - 7L * (weeks - 1 - col)
            if (months) {
                val dm = D.date(first).dayOfMonth
                if (dm <= 7 && col - lastLabelCol >= 3) {
                    val m = D.date(first).monthValue
                    drawText(tm, D.MONTHS3[m - 1], Offset(x0 + col * step, 0f), label)
                    lastLabelCol = col
                }
            }
            for (row in 0..6) {
                val day = first + row
                if (day > today) continue
                val p = h.progress(em[day], day, today)
                drawRoundRect(
                    cellFill(color, p, empty), Offset(x0 + col * step, top + row * step),
                    Size(c, c), CornerRadius(c * 0.28f)
                )
            }
        }
        if (days) {
            for (row in intArrayOf(1, 3, 5)) {
                drawText(tm, D.DAYS_CAP[(weekStart + row) % 7], Offset(0f, top + row * step - 1.dp.toPx()), label)
            }
        }
    }
}

/** Puntos de un mes (semanas en filas) para la vista mensual. */
@Composable
fun MonthDots(h: Habit, em: Map<Long, Entry>, firstDay: Long, weekStart: Int, modifier: Modifier = Modifier) {
    val today = D.today()
    val color = Palette.color(h.colorIdx)
    val empty = color.copy(alpha = if (gh.dark) 0.10f else 0.14f)
    val date = D.date(firstDay)
    val len = date.lengthOfMonth()
    val offset = Math.floorMod(D.dow(firstDay) - weekStart, 7)
    val rows = (offset + len + 6) / 7
    Canvas(modifier = modifier.fillMaxWidth().height(((rows * 15) + 2).dp)) {
        val step = 15.dp.toPx()
        val r = 4.dp.toPx()
        val gridW = step * 7
        val x0 = ((size.width - gridW) / 2f).coerceAtLeast(0f)
        for (i in 0 until len) {
            val day = firstDay + i
            val idx = offset + i
            val cx = x0 + (idx % 7) * step + step / 2f
            val cy = (idx / 7) * step + step / 2f
            val p = h.progress(em[day], day, today)
            val col = if (day > today) empty.copy(alpha = empty.alpha * 0.6f) else cellFill(color, p, empty)
            drawCircle(col, r, Offset(cx, cy))
        }
    }
}
