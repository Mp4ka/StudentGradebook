package ru.college.gradebook.data.db

import ru.college.gradebook.data.model.WorkType

/**
 * Демонстрационный набор для первого запуска: 5 дисциплин и 25 оценок.
 *
 * Числа согласованы с требованиями и прототипами:
 *  * суммa всех оценок — 106, средний балл — 4,24 (106 / 25);
 *  * «Математический анализ» — 38 / 8 = 4,75;
 *  * «Базы данных» — 26 / 6 = 4,33;
 *  * «Физика» — 16 / 5 = 3,20 (дисциплина с меткой «риск»);
 *  * «История» — 14 / 3 = 4,67;
 *  * «Философия» — 12 / 3 = 4,00.
 *
 * Набор используется и в тестах: `DemoDataTest` проверяет, что суммы и средние
 * совпадают с документами, поэтому правка чисел без правки требований упадёт.
 */
object DemoData {

    data class DisciplineSeed(val name: String, val teacherName: String)

    data class GradeSeed(
        val discipline: String,
        val value: Int,
        val date: String,
        val workType: WorkType,
        val comment: String? = null
    )

    val disciplines: List<DisciplineSeed> = listOf(
        DisciplineSeed("Математический анализ", "доц. А. В. Петрова"),
        DisciplineSeed("Базы данных", "доц. И. А. Соколова"),
        DisciplineSeed("Физика", "ст. преп. Н. С. Орлов"),
        DisciplineSeed("История", "проф. Л. М. Гусева"),
        DisciplineSeed("Философия", "доц. Р. К. Белов")
    )

    val grades: List<GradeSeed> = listOf(
        // Математический анализ — 8 оценок, сумма 38
        GradeSeed("Математический анализ", 5, "2026-09-09", WorkType.PRACTICE, "Пределы и непрерывность"),
        GradeSeed("Математический анализ", 5, "2026-09-16", WorkType.CONTROL, "Производная и дифференциал"),
        GradeSeed("Математический анализ", 4, "2026-09-23", WorkType.ANSWER, "Теорема о среднем"),
        GradeSeed("Математический анализ", 5, "2026-09-30", WorkType.PRACTICE, "Интегралы"),
        GradeSeed("Математический анализ", 5, "2026-10-07", WorkType.CONTROL, "Числовые ряды"),
        GradeSeed("Математический анализ", 4, "2026-10-14", WorkType.LAB, "Численные методы"),
        GradeSeed("Математический анализ", 5, "2026-10-21", WorkType.ANSWER, "Функции нескольких переменных"),
        GradeSeed("Математический анализ", 5, "2026-10-28", WorkType.CONTROL, "Итоговая контрольная"),

        // Базы данных — 6 оценок, сумма 26
        GradeSeed("Базы данных", 5, "2026-09-20", WorkType.PRACTICE, "Проектирование ER-модели"),
        GradeSeed("Базы данных", 3, "2026-10-01", WorkType.TEST, "Основы SQL"),
        GradeSeed("Базы данных", 4, "2026-10-15", WorkType.ANSWER, "Транзакции и уровни изоляции"),
        GradeSeed("Базы данных", 5, "2026-10-22", WorkType.LAB, "PostgreSQL: установка и настройка"),
        GradeSeed("Базы данных", 4, "2026-11-05", WorkType.CONTROL, "Индексы и планы запросов"),
        GradeSeed("Базы данных", 5, "2026-11-12", WorkType.PRACTICE, "Нормализация, приведение к 3НФ"),

        // Физика — 5 оценок, сумма 16
        GradeSeed("Физика", 3, "2026-09-11", WorkType.LAB, "Ускорение свободного падения"),
        GradeSeed("Физика", 4, "2026-09-25", WorkType.CONTROL, "Кинематика"),
        GradeSeed("Физика", 3, "2026-10-09", WorkType.LAB, "Молекулярная физика"),
        GradeSeed("Физика", 3, "2026-10-23", WorkType.ANSWER, "Законы термодинамики"),
        GradeSeed("Физика", 3, "2026-11-11", WorkType.CONTROL, "Электричество"),

        // История — 3 оценки, сумма 14
        GradeSeed("История", 5, "2026-09-18", WorkType.ANSWER, "Реформы Александра II"),
        GradeSeed("История", 5, "2026-10-16", WorkType.CONTROL, "Россия в начале XX века"),
        GradeSeed("История", 4, "2026-11-13", WorkType.ANSWER, "Великая Отечественная война"),

        // Философия — 3 оценки, сумма 12
        GradeSeed("Философия", 4, "2026-10-02", WorkType.ANSWER, "Античная философия"),
        GradeSeed("Философия", 4, "2026-10-30", WorkType.TEST, "Немецкая классическая философия"),
        GradeSeed("Философия", 4, "2026-11-20", WorkType.ANSWER, "Философия XX века")
    )
}
