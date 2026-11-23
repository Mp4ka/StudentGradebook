package ru.college.gradebook.data.model

/** Вид учебной работы. В базовой версии вес у всех работ одинаковый. */
enum class WorkType(val title: String, val shortTitle: String) {
    CONTROL("Контрольная работа", "КР"),
    PRACTICE("Практическая работа", "ПР"),
    LAB("Лабораторная работа", "ЛР"),
    ANSWER("Устный ответ", "ОТВ"),
    TEST("Тест", "ТЕСТ"),
    OTHER("Другое", "ДР")
}
