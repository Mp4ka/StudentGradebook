package ru.college.gradebook.data.db

import androidx.room.TypeConverter
import ru.college.gradebook.data.model.WorkType
import java.time.LocalDate

/**
 * Конвертеры Room для типов, которых нет в SQLite:
 *  * [LocalDate] хранится как epochDay (целое число дней), а не как строка —
 *    так сортировка и сравнение периодов работают без разбора формата;
 *  * [WorkType] хранится именем константы, русские подписи остаются в ресурсах.
 */
class Converters {

    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? = value?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun fromWorkType(type: WorkType?): String? = type?.name

    @TypeConverter
    fun toWorkType(name: String?): WorkType? = name?.let { runCatching { WorkType.valueOf(it) }.getOrNull() }
}
