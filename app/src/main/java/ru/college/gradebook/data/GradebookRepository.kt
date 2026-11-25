package ru.college.gradebook.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import ru.college.gradebook.data.db.GradeEntity
import ru.college.gradebook.data.db.GradebookDatabase
import ru.college.gradebook.data.db.DisciplineEntity
import ru.college.gradebook.data.db.DemoData
import ru.college.gradebook.data.model.Discipline
import ru.college.gradebook.data.model.DisciplineSummary
import ru.college.gradebook.data.model.Grade
import ru.college.gradebook.data.model.WorkType
import ru.college.gradebook.domain.GradeMath
import java.time.LocalDate

/**
 * Единая точка доступа к данным для экранов: скрывает Room и отдаёт доменные
 * модели. Все операции с базой — suspend, наблюдение — через Flow.
 */
class GradebookRepository(private val database: GradebookDatabase) {

    private val disciplineDao get() = database.disciplines()
    private val gradeDao get() = database.grades()

    fun observeDisciplines(): Flow<List<Discipline>> =
        disciplineDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    fun observeAllGrades(): Flow<List<Grade>> =
        gradeDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    fun observeGrades(disciplineId: Long): Flow<List<Grade>> =
        gradeDao.observeByDiscipline(disciplineId).map { rows -> rows.map { it.toDomain() } }

    /** Список дисциплин со средним баллом и количеством оценок. */
    fun observeDisciplineSummaries(): Flow<List<DisciplineSummary>> =
        combine(observeDisciplines(), observeAllGrades()) { disciplines, grades ->
            val byDiscipline = grades.groupBy { it.disciplineId }
            disciplines.map { discipline ->
                val values = byDiscipline[discipline.id].orEmpty().map { it.value }
                DisciplineSummary(
                    discipline = discipline,
                    gradeCount = values.size,
                    average = GradeMath.average(values)
                )
            }
        }

    suspend fun getGrade(gradeId: Long): Grade? = gradeDao.findById(gradeId)?.toDomain()

    suspend fun getDiscipline(disciplineId: Long): Discipline? =
        disciplineDao.findById(disciplineId)?.toDomain()

    suspend fun disciplineCount(): Int = disciplineDao.count()

    suspend fun gradeCount(): Int = gradeDao.count()

    suspend fun addDiscipline(name: String, teacherName: String? = null): Long {
        val normalizedName = name.trim()
        require(normalizedName.isNotEmpty()) { "Название дисциплины не может быть пустым" }
        return disciplineDao.insert(
            DisciplineEntity(
                name = normalizedName,
                teacherName = teacherName?.trim()?.takeIf { it.isNotEmpty() }
            )
        )
    }

    suspend fun updateDiscipline(discipline: Discipline) {
        disciplineDao.update(discipline.toEntity())
    }

    /** Удаляет дисциплину; оценки удаляются каскадно на уровне внешнего ключа. */
    suspend fun deleteDiscipline(disciplineId: Long) = disciplineDao.deleteById(disciplineId)

    suspend fun addGrade(
        disciplineId: Long,
        value: Int,
        date: LocalDate,
        workType: WorkType,
        comment: String? = null
    ): Long {
        val grade = Grade(
            disciplineId = disciplineId,
            value = value,
            date = date,
            workType = workType,
            comment = comment?.trim()?.takeIf { it.isNotEmpty() }
        )
        return gradeDao.insert(grade.toEntity())
    }

    suspend fun updateGrade(grade: Grade) = gradeDao.update(grade.toEntity())

    suspend fun deleteGrade(gradeId: Long) = gradeDao.deleteById(gradeId)

    /** Заполняет базу демонстрационным набором, если она пуста. */
    suspend fun seedDemoDataIfEmpty() {
        if (disciplineDao.count() > 0) return
        val ids = DemoData.disciplines.associate { seed ->
            seed.name to disciplineDao.insert(
                DisciplineEntity(name = seed.name, teacherName = seed.teacherName)
            )
        }
        DemoData.grades.forEach { seed ->
            gradeDao.insert(
                GradeEntity(
                    disciplineId = ids.getValue(seed.discipline),
                    value = seed.value,
                    date = LocalDate.parse(seed.date),
                    workType = seed.workType,
                    comment = seed.comment
                )
            )
        }
    }
}

internal fun DisciplineEntity.toDomain() = Discipline(id = id, name = name, teacherName = teacherName)

internal fun Discipline.toEntity() = DisciplineEntity(id = id, name = name, teacherName = teacherName)

internal fun GradeEntity.toDomain() = Grade(
    id = id,
    disciplineId = disciplineId,
    value = value,
    date = date,
    workType = workType,
    comment = comment
)

internal fun Grade.toEntity() = GradeEntity(
    id = id,
    disciplineId = disciplineId,
    value = value,
    date = date,
    workType = workType,
    comment = comment
)
