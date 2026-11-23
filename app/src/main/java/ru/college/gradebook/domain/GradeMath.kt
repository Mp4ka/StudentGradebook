package ru.college.gradebook.domain

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.ceil

/**
 * Правила расчёта среднего балла и целевой оценки.
 *
 * Все правила согласованы на первом спринте (см. docs/RULES.md):
 *  * допустимый диапазон оценок — целые числа 2..5;
 *  * средний балл — среднее арифметическое без весов;
 *  * отображение — округление до двух знаков, половина вверх, запятая;
 *  * при отсутствии оценок средний балл не определён (null, «—»).
 */
object GradeMath {

    const val MIN_GRADE = 2
    const val MAX_GRADE = 5

    private const val EPS = 1e-9
    private const val DISPLAY_SCALE = 2

    /** Среднее арифметическое или null, если оценок нет. */
    fun average(values: List<Int>): Double? {
        if (values.isEmpty()) return null
        require(values.all { it in MIN_GRADE..MAX_GRADE }) {
            "Оценки должны быть в диапазоне $MIN_GRADE..$MAX_GRADE"
        }
        return values.sum().toDouble() / values.size
    }

    /** Строка для интерфейса: «4,25», «4,5», «5», «—» для пустого списка. */
    fun formatAverage(average: Double?): String {
        if (average == null) return "—"
        val rounded = BigDecimal.valueOf(average)
            .setScale(DISPLAY_SCALE, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
        return rounded.replace('.', ',')
    }

    /**
     * Оценка, которая нужна за следующую работу, чтобы средний балл достиг
     * [target]. Возвращает null, если цель недостижима одной оценкой
     * (требуется выше 5), и [MIN_GRADE], если цель уже достигнута.
     */
    fun requiredNextGrade(currentSum: Int, count: Int, target: Double): Int? {
        require(count >= 0) { "Количество оценок не может быть отрицательным" }
        require(currentSum >= 0) { "Сумма оценок не может быть отрицательной" }
        require(target in MIN_GRADE.toDouble()..MAX_GRADE.toDouble()) {
            "Целевой балл должен быть в диапазоне $MIN_GRADE..$MAX_GRADE"
        }

        val needed = target * (count + 1) - currentSum
        if (needed > MAX_GRADE + EPS) return null
        if (needed <= MIN_GRADE + EPS) return MIN_GRADE
        return ceil(needed - EPS).toInt()
    }
}
