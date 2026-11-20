package com.gonzalowebdev.goodhabits

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

const val MODE_YN = 0
const val MODE_COUNT = 1
const val MODE_MEASURE = 2

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val name: String,
    val description: String,
    val colorIdx: Int,
    val icon: String,
    val quit: Boolean,
    val daysMask: Int,
    val mode: Int,
    val target: Double,
    val unit: String,
    val tapTotal: Boolean,
    val sortOrder: Int,
    val archived: Boolean,
    val createdDay: Long
)

fun newHabit() = Habit(
    id = 0, name = "", description = "", colorIdx = 2, icon = "pulse", quit = false,
    daysMask = 127, mode = MODE_YN, target = 1.0, unit = "", tapTotal = false,
    sortOrder = 0, archived = false, createdDay = D.today()
)

@Entity(tableName = "entries", primaryKeys = ["habitId", "day"])
data class Entry(
    val habitId: Long,
    val day: Long,
    val value: Double,
    val slip: Boolean,
    val note: String
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val name: String,
    val sortOrder: Int
)

@Entity(tableName = "habit_categories", primaryKeys = ["habitId", "categoryId"])
data class HabitCategory(val habitId: Long, val categoryId: Long)

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val habitId: Long,
    val daysMask: Int,
    val minute: Int
)

@Dao
interface GhDao {
    @Query("SELECT * FROM habits ORDER BY sortOrder, id") fun habitsFlow(): Flow<List<Habit>>
    @Query("SELECT * FROM entries") fun entriesFlow(): Flow<List<Entry>>
    @Query("SELECT * FROM categories ORDER BY sortOrder, id") fun categoriesFlow(): Flow<List<Category>>
    @Query("SELECT * FROM habit_categories") fun linksFlow(): Flow<List<HabitCategory>>
    @Query("SELECT * FROM reminders") fun remindersFlow(): Flow<List<Reminder>>

    @Query("SELECT * FROM habits ORDER BY sortOrder, id") suspend fun habits(): List<Habit>
    @Query("SELECT * FROM entries") suspend fun entries(): List<Entry>
    @Query("SELECT * FROM categories ORDER BY sortOrder, id") suspend fun categories(): List<Category>
    @Query("SELECT * FROM habit_categories") suspend fun links(): List<HabitCategory>
    @Query("SELECT * FROM reminders") suspend fun reminders(): List<Reminder>
    @Query("SELECT * FROM entries WHERE habitId = :h AND day = :d") suspend fun entry(h: Long, d: Long): Entry?

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun putHabit(h: Habit): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun putEntry(e: Entry)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun putCategory(c: Category): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun putLink(l: HabitCategory)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun putReminder(r: Reminder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun putHabits(l: List<Habit>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun putEntries(l: List<Entry>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun putCategories(l: List<Category>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun putLinks(l: List<HabitCategory>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun putReminders(l: List<Reminder>)

    @Query("DELETE FROM habits WHERE id = :id") suspend fun deleteHabit(id: Long)
    @Query("DELETE FROM entries WHERE habitId = :id") suspend fun deleteEntriesOf(id: Long)
    @Query("DELETE FROM entries WHERE habitId = :h AND day = :d") suspend fun deleteEntry(h: Long, d: Long)
    @Query("DELETE FROM habit_categories WHERE habitId = :id") suspend fun deleteLinksOfHabit(id: Long)
    @Query("DELETE FROM habit_categories WHERE categoryId = :id") suspend fun deleteLinksOfCategory(id: Long)
    @Query("DELETE FROM categories WHERE id = :id") suspend fun deleteCategory(id: Long)
    @Query("DELETE FROM reminders WHERE habitId = :id") suspend fun deleteRemindersOf(id: Long)

    @Query("DELETE FROM habits") suspend fun clearHabits()
    @Query("DELETE FROM entries") suspend fun clearEntries()
    @Query("DELETE FROM categories") suspend fun clearCategories()
    @Query("DELETE FROM habit_categories") suspend fun clearLinks()
    @Query("DELETE FROM reminders") suspend fun clearReminders()

    @Query("UPDATE habits SET sortOrder = :o WHERE id = :id") suspend fun setOrder(id: Long, o: Int)
    @Query("UPDATE habits SET archived = :a WHERE id = :id") suspend fun setArchived(id: Long, a: Boolean)
}

@Database(
    entities = [Habit::class, Entry::class, Category::class, HabitCategory::class, Reminder::class],
    version = 1,
    exportSchema = false
)
abstract class GhDb : RoomDatabase() {
    abstract fun dao(): GhDao

    companion object {
        @Volatile private var inst: GhDb? = null
        fun get(c: Context): GhDb = inst ?: synchronized(this) {
            inst ?: Room.databaseBuilder(c.applicationContext, GhDb::class.java, "goodhabits.db")
                .build().also { inst = it }
        }
    }
}
