package ru.college.gradebook.domain

import ru.college.gradebook.data.model.Grade
import ru.college.gradebook.data.model.WorkType
import java.time.LocalDate

/** Порядок вывода оценок в журнале. */
enum class GradeSort(val title: String) {
    DATE_DESC("Сначала новые"),
    DATE_ASC("Сначала старые"),
    VALUE_DESC("По убыванию оценки"),
    VALUE_ASC("По возрастанию оценки")
}

/** Включительный период дат. */
data class Period(val from: LocalDate, val to: LocalDate) {
    init {
        require(!from.isAfter(to)) { "Начало периода позже конца" }
    }

    operator fun contains(date: LocalDate): Boolean =
        !date.isBefore(from) && !date.isAfter(to)
}

/**
 * Фильтр журнала: вид работы, период и сортировка.
 * Логика отделена от UI, чтобы её можно было проверить на JVM.
 */
data class GradeFilter(
    val workType: WorkType? = null,
    val period: Period? = null,
    val sort: GradeSort = GradeSort.DATE_DESC
) {
    val isActive: Boolean get() = workType != null || period != null

    fun matches(grade: Grade): Boolean =
        (workType == null || grade.workType == workType) &&
            (period == null || grade.date in period)

    fun apply(grades: List<Grade>): List<Grade> {
        val filtered = grades.filter(::matches)
        return when (sort) {
            GradeSort.DATE_DESC -> filtered.sortedWith(
                compareByDescending<Grade> { it.date }.thenByDescending { it.id }
            )
            GradeSort.DATE_ASC -> filtered.sortedWith(
                compareBy<Grade> { it.date }.thenBy { it.id }
            )
            GradeSort.VALUE_DESC -> filtered.sortedWith(
                compareByDescending<Grade> { it.value }.thenByDescending { it.date }
            )
            GradeSort.VALUE_ASC -> filtered.sortedWith(
                compareBy<Grade> { it.value }.thenByDescending { it.date }
            )
        }
    }
}
