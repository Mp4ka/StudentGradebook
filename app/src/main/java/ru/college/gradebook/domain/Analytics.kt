package ru.college.gradebook.domain

import ru.college.gradebook.data.model.Grade
import java.time.YearMonth

/** Средний балл за месяц: количество оценок и среднее (null, если записей нет). */
data class MonthlyStats(
    val month: YearMonth,
    val count: Int,
    val average: Double?
)

object Analytics {

    /** Средние баллы по месяцам в хронологическом порядке. */
    fun monthly(grades: List<Grade>): List<MonthlyStats> =
        grades
            .groupBy { YearMonth.from(it.date) }
            .toSortedMap()
            .map { (month, list) ->
                MonthlyStats(
                    month = month,
                    count = list.size,
                    average = GradeMath.average(list.map { it.value })
                )
            }

    /**
     * Самая низкая и самая высокая точки динамики: нужны, чтобы подписать график
     * и обратить внимание студента на просадку.
     */
    fun lowest(months: List<MonthlyStats>): MonthlyStats? =
        months.filter { it.average != null }.minByOrNull { it.average!! }

    fun highest(months: List<MonthlyStats>): MonthlyStats? =
        months.filter { it.average != null }.maxByOrNull { it.average!! }
}
