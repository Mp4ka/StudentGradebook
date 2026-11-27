package ru.college.gradebook.domain

import kotlin.math.ceil

/** Результат расчёта целевого балла. */
sealed interface TargetResult {
    /** Цель достижима: за следующую работу нужна оценка [grade]. */
    data class Needed(val grade: Int) : TargetResult

    /** Цель недостижима одной оценкой: максимум, который можно получить, — [maxAverage]. */
    data class Unreachable(val maxAverage: Double) : TargetResult

    /** Цель уже достигнута: любая допустимая оценка её не опустит. */
    data object Achieved : TargetResult
}

/**
 * Калькулятор целевого балла.
 *
 * Требуемая оценка: `x = T × (n + 1) − S`, где T — цель, n — количество оценок,
 * S — их сумма. Оценка целая, поэтому берётся ближайшее целое сверху.
 */
object TargetCalculator {

    const val MIN_GRADE = 2
    const val MAX_GRADE = 5

    fun evaluate(currentSum: Int, count: Int, target: Double): TargetResult {
        require(target >= MIN_GRADE - 1e-9 && target <= MAX_GRADE + 1e-9) {
            "Целевой балл должен быть в диапазоне $MIN_GRADE..$MAX_GRADE"
        }
        require(count >= 0) { "Количество оценок не может быть отрицательным" }
        require(currentSum >= 0) { "Сумма оценок не может быть отрицательной" }

        val needed = target * (count + 1) - currentSum
        if (needed > MAX_GRADE + 1e-9) {
            return TargetResult.Unreachable((currentSum + MAX_GRADE).toDouble() / (count + 1))
        }
        if (needed <= MIN_GRADE + 1e-9) {
            return TargetResult.Achieved
        }
        return TargetResult.Needed(ceil(needed - 1e-9).toInt().coerceIn(MIN_GRADE, MAX_GRADE))
    }
}
