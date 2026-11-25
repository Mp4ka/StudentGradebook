package ru.college.gradebook.domain

import ru.college.gradebook.data.model.WorkType
import java.time.LocalDate

/** Поля формы оценки, по которым может быть ошибка. */
enum class GradeField { DISCIPLINE, VALUE, WORK_TYPE }

/** Введённые пользователем данные формы оценки. */
data class GradeForm(
    val disciplineId: Long?,
    val value: Int?,
    val date: LocalDate,
    val workType: WorkType?,
    val comment: String?
)

/**
 * Проверка формы до сохранения. Возвращает карту «поле → сообщение»:
 * пустая карта означает, что данные корректны.
 */
object GradeFormValidator {

    const val MIN_VALUE = 2
    const val MAX_VALUE = 5

    fun validate(form: GradeForm): Map<GradeField, String> {
        val errors = mutableMapOf<GradeField, String>()

        if (form.disciplineId == null || form.disciplineId <= 0L) {
            errors[GradeField.DISCIPLINE] = "Выберите дисциплину"
        }

        val value = form.value
        when {
            value == null -> errors[GradeField.VALUE] = "Выберите оценку"
            value !in MIN_VALUE..MAX_VALUE ->
                errors[GradeField.VALUE] = "Оценка должна быть от $MIN_VALUE до $MAX_VALUE"
        }

        if (form.workType == null) {
            errors[GradeField.WORK_TYPE] = "Выберите вид работы"
        }

        return errors
    }
}
