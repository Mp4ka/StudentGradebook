package ru.college.gradebook.data.model

/**
 * Учебная дисциплина.
 *
 * @param id первичный ключ; 0 означает «ещё не сохранена в базе»;
 * @param name название предмета, обязательное поле, непустое;
 * @param teacherName преподаватель — необязательное поле.
 */
data class Discipline(
    val id: Long = 0L,
    val name: String,
    val teacherName: String? = null
) {
    init {
        require(name.isNotBlank()) { "Название дисциплины не может быть пустым" }
    }
}
