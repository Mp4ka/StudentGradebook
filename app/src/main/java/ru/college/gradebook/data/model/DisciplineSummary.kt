package ru.college.gradebook.data.model

/**
 * Дисциплина вместе с рассчитанной статистикой: количество оценок и средний балл.
 * Используется списком дисциплин и главным экраном.
 */
data class DisciplineSummary(
    val discipline: Discipline,
    val gradeCount: Int,
    val average: Double?
) {
    /** Дисциплина требует внимания, если средний балл ниже порога. */
    val isRisk: Boolean get() = average != null && average < RISK_THRESHOLD

    companion object {
        const val RISK_THRESHOLD = 3.5
    }
}
