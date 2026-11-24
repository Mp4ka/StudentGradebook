package ru.college.gradebook.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.college.gradebook.data.model.WorkType
import java.time.LocalDate

/**
 * Таблица оценок. Связь с дисциплиной — внешний ключ с каскадным удалением:
 * удаление дисциплины удаляет её оценки, но только после подтверждения в UI.
 */
@Entity(
    tableName = "grade",
    foreignKeys = [
        ForeignKey(
            entity = DisciplineEntity::class,
            parentColumns = ["id"],
            childColumns = ["disciplineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("disciplineId")]
)
data class GradeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val disciplineId: Long,
    val value: Int,
    val date: LocalDate,
    val workType: WorkType,
    val comment: String? = null
)
