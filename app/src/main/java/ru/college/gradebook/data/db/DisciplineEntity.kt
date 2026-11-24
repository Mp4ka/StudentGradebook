package ru.college.gradebook.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Таблица дисциплин. Одна дисциплина связана с множеством оценок. */
@Entity(tableName = "discipline")
data class DisciplineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val teacherName: String? = null
)
