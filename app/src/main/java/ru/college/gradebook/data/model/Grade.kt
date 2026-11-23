package ru.college.gradebook.data.model

import java.time.LocalDate

/**
 * Учебная оценка по дисциплине.
 *
 * @param id первичный ключ; 0 означает «ещё не сохранена в базе»;
 * @param disciplineId ссылка на [Discipline], внешний ключ ON DELETE CASCADE;
 * @param value значение оценки в пятибалльной системе (2..5);
 * @param date дата выставления оценки;
 * @param workType вид работы;
 * @param comment необязательный комментарий.
 */
data class Grade(
    val id: Long = 0L,
    val disciplineId: Long,
    val value: Int,
    val date: LocalDate,
    val workType: WorkType,
    val comment: String? = null
) {
    init {
        require(value in MIN_VALUE..MAX_VALUE) {
            "Оценка должна быть в диапазоне $MIN_VALUE..$MAX_VALUE, получено $value"
        }
    }

    companion object {
        const val MIN_VALUE = 2
        const val MAX_VALUE = 5
    }
}
