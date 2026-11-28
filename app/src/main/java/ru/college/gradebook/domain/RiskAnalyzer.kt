package ru.college.gradebook.domain

import java.time.LocalDate

/** Настройки индикатора риска. */
data class RiskSettings(
    val threshold: Double = 3.5,
    val minGrades: Int = 3,
    val periodDays: Long = 30
)

/** Причина, по которой дисциплина требует внимания. */
enum class RiskReason {
    NO_GRADES,
    LOW_AVERAGE,
    FEW_GRADES,
    NO_RECENT_GRADES
}

/** Оценка риска по дисциплине: причины и значения, которые их объясняют. */
data class DisciplineRisk(
    val reasons: List<RiskReason>,
    val average: Double?,
    val gradeCount: Int,
    val lastGradeDate: LocalDate?
) {
    val isRisk: Boolean get() = reasons.isNotEmpty()
}

/**
 * Индикатор риска: выделяет дисциплины, которым нужно внимание.
 *
 * Правила:
 *  * нет ни одной оценки;
 *  * средний балл ниже порога, заданного пользователем;
 *  * слишком мало оценок (меньше [RiskSettings.minGrades]);
 *  * нет оценок за последние [RiskSettings.periodDays] дней.
 */
object RiskAnalyzer {

    fun assess(
        average: Double?,
        gradeCount: Int,
        lastGradeDate: LocalDate?,
        today: LocalDate = LocalDate.now(),
        settings: RiskSettings = RiskSettings()
    ): DisciplineRisk {
        val reasons = mutableListOf<RiskReason>()

        if (gradeCount == 0) {
            reasons += RiskReason.NO_GRADES
            return DisciplineRisk(reasons, average, gradeCount, lastGradeDate)
        }

        if (average != null && average < settings.threshold) {
            reasons += RiskReason.LOW_AVERAGE
        }
        if (gradeCount < settings.minGrades) {
            reasons += RiskReason.FEW_GRADES
        }
        if (lastGradeDate == null || lastGradeDate.isBefore(today.minusDays(settings.periodDays))) {
            reasons += RiskReason.NO_RECENT_GRADES
        }

        return DisciplineRisk(reasons, average, gradeCount, lastGradeDate)
    }
}
