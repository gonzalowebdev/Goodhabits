package com.gonzalowebdev.goodhabits

import org.json.JSONArray
import org.json.JSONObject

object Backup {
    suspend fun export(dao: GhDao): String {
        val root = JSONObject()
        root.put("app", "GoodHabits")
        root.put("version", 1)
        root.put("habits", JSONArray().also { a ->
            dao.habits().forEach { h ->
                a.put(JSONObject().put("id", h.id).put("name", h.name).put("description", h.description)
                    .put("colorIdx", h.colorIdx).put("icon", h.icon).put("quit", h.quit)
                    .put("daysMask", h.daysMask).put("mode", h.mode).put("target", h.target)
                    .put("unit", h.unit).put("tapTotal", h.tapTotal).put("sortOrder", h.sortOrder)
                    .put("archived", h.archived).put("createdDay", h.createdDay))
            }
        })
        root.put("entries", JSONArray().also { a ->
            dao.entries().forEach { e ->
                a.put(JSONObject().put("habitId", e.habitId).put("day", e.day).put("value", e.value)
                    .put("slip", e.slip).put("note", e.note))
            }
        })
        root.put("categories", JSONArray().also { a ->
            dao.categories().forEach { c -> a.put(JSONObject().put("id", c.id).put("name", c.name).put("sortOrder", c.sortOrder)) }
        })
        root.put("links", JSONArray().also { a ->
            dao.links().forEach { l -> a.put(JSONObject().put("habitId", l.habitId).put("categoryId", l.categoryId)) }
        })
        root.put("reminders", JSONArray().also { a ->
            dao.reminders().forEach { r ->
                a.put(JSONObject().put("id", r.id).put("habitId", r.habitId).put("daysMask", r.daysMask).put("minute", r.minute))
            }
        })
        return root.toString(2)
    }

    /** Reemplaza todos los datos. Lanza excepción si el archivo no es válido. */
    suspend fun import(dao: GhDao, text: String) {
        val root = JSONObject(text)
        if (root.optString("app") != "GoodHabits") throw IllegalArgumentException("Archivo no válido")
        val habits = root.getJSONArray("habits").objs().map {
            Habit(
                id = it.getLong("id"), name = it.getString("name"), description = it.optString("description"),
                colorIdx = it.optInt("colorIdx", 0), icon = it.optString("icon", "pulse"), quit = it.optBoolean("quit"),
                daysMask = it.optInt("daysMask", 127), mode = it.optInt("mode"), target = it.optDouble("target", 1.0),
                unit = it.optString("unit"), tapTotal = it.optBoolean("tapTotal"), sortOrder = it.optInt("sortOrder"),
                archived = it.optBoolean("archived"), createdDay = it.optLong("createdDay", D.today())
            )
        }
        val entries = root.getJSONArray("entries").objs().map {
            Entry(it.getLong("habitId"), it.getLong("day"), it.optDouble("value"), it.optBoolean("slip"), it.optString("note"))
        }
        val cats = root.getJSONArray("categories").objs().map {
            Category(it.getLong("id"), it.getString("name"), it.optInt("sortOrder"))
        }
        val links = root.getJSONArray("links").objs().map { HabitCategory(it.getLong("habitId"), it.getLong("categoryId")) }
        val rems = root.getJSONArray("reminders").objs().map {
            Reminder(it.getLong("id"), it.getLong("habitId"), it.optInt("daysMask", 127), it.optInt("minute", 600))
        }
        dao.clearEntries(); dao.clearLinks(); dao.clearReminders(); dao.clearCategories(); dao.clearHabits()
        dao.putHabits(habits); dao.putEntries(entries); dao.putCategories(cats); dao.putLinks(links); dao.putReminders(rems)
    }

    private fun JSONArray.objs(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }
}
