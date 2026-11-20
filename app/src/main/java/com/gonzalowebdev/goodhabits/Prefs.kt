package com.gonzalowebdev.goodhabits

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Settings(
    val weekStart: Int = 0,
    val highlightToday: Boolean = false,
    val haptic: Boolean = true,
    val notes: Boolean = true,
    val themeMode: Int = 0,
    val widgetTheme: Int = 0,
    val showBottomBar: Boolean = true,
    val swipeModes: Boolean = true,
    val showFilter: Boolean = false,
    val showCategoryNames: Boolean = true,
    val showCompletedFilter: Boolean = true,
    val showSearch: Boolean = false,
    val completedMode: Int = 0,
    val streakCards: Boolean = false,
    val streakCompact: Boolean = false,
    val streakMonthly: Boolean = false,
    val monthLabels: Boolean = true,
    val dayLabels: Boolean = true,
    val showCategories: Boolean = false,
    val compactDays: Int = 5,
    val compactNames: Boolean = true,
    val viewMode: Int = 0,
    val dailyReminder: Boolean = false,
    val dailyMinute: Int = 1200,
    val lastBackup: Long = 0L
)

object Prefs {
    private lateinit var sp: SharedPreferences
    private val state = MutableStateFlow(Settings())
    val flow: StateFlow<Settings> get() = state

    fun init(c: Context) {
        if (::sp.isInitialized) return
        sp = c.applicationContext.getSharedPreferences("gh_settings", Context.MODE_PRIVATE)
        state.value = read()
    }

    fun get(c: Context): Settings { init(c); return state.value }

    fun update(f: (Settings) -> Settings) {
        val n = f(state.value)
        state.value = n
        write(n)
    }

    private fun read(): Settings {
        val d = Settings()
        return Settings(
            weekStart = sp.getInt("weekStart", d.weekStart),
            highlightToday = sp.getBoolean("highlightToday", d.highlightToday),
            haptic = sp.getBoolean("haptic", d.haptic),
            notes = sp.getBoolean("notes", d.notes),
            themeMode = sp.getInt("themeMode", d.themeMode),
            widgetTheme = sp.getInt("widgetTheme", d.widgetTheme),
            showBottomBar = sp.getBoolean("showBottomBar", d.showBottomBar),
            swipeModes = sp.getBoolean("swipeModes", d.swipeModes),
            showFilter = sp.getBoolean("showFilter", d.showFilter),
            showCategoryNames = sp.getBoolean("showCategoryNames", d.showCategoryNames),
            showCompletedFilter = sp.getBoolean("showCompletedFilter", d.showCompletedFilter),
            showSearch = sp.getBoolean("showSearch", d.showSearch),
            completedMode = sp.getInt("completedMode", d.completedMode),
            streakCards = sp.getBoolean("streakCards", d.streakCards),
            streakCompact = sp.getBoolean("streakCompact", d.streakCompact),
            streakMonthly = sp.getBoolean("streakMonthly", d.streakMonthly),
            monthLabels = sp.getBoolean("monthLabels", d.monthLabels),
            dayLabels = sp.getBoolean("dayLabels", d.dayLabels),
            showCategories = sp.getBoolean("showCategories", d.showCategories),
            compactDays = sp.getInt("compactDays", d.compactDays),
            compactNames = sp.getBoolean("compactNames", d.compactNames),
            viewMode = sp.getInt("viewMode", d.viewMode),
            dailyReminder = sp.getBoolean("dailyReminder", d.dailyReminder),
            dailyMinute = sp.getInt("dailyMinute", d.dailyMinute),
            lastBackup = sp.getLong("lastBackup", d.lastBackup)
        )
    }

    private fun write(s: Settings) {
        sp.edit()
            .putInt("weekStart", s.weekStart)
            .putBoolean("highlightToday", s.highlightToday)
            .putBoolean("haptic", s.haptic)
            .putBoolean("notes", s.notes)
            .putInt("themeMode", s.themeMode)
            .putInt("widgetTheme", s.widgetTheme)
            .putBoolean("showBottomBar", s.showBottomBar)
            .putBoolean("swipeModes", s.swipeModes)
            .putBoolean("showFilter", s.showFilter)
            .putBoolean("showCategoryNames", s.showCategoryNames)
            .putBoolean("showCompletedFilter", s.showCompletedFilter)
            .putBoolean("showSearch", s.showSearch)
            .putInt("completedMode", s.completedMode)
            .putBoolean("streakCards", s.streakCards)
            .putBoolean("streakCompact", s.streakCompact)
            .putBoolean("streakMonthly", s.streakMonthly)
            .putBoolean("monthLabels", s.monthLabels)
            .putBoolean("dayLabels", s.dayLabels)
            .putBoolean("showCategories", s.showCategories)
            .putInt("compactDays", s.compactDays)
            .putBoolean("compactNames", s.compactNames)
            .putInt("viewMode", s.viewMode)
            .putBoolean("dailyReminder", s.dailyReminder)
            .putInt("dailyMinute", s.dailyMinute)
            .putLong("lastBackup", s.lastBackup)
            .apply()
    }
}
