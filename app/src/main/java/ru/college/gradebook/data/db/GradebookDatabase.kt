package ru.college.gradebook.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [DisciplineEntity::class, GradeEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class GradebookDatabase : RoomDatabase() {

    abstract fun disciplines(): DisciplineDao

    abstract fun grades(): GradeDao

    companion object {
        const val NAME = "gradebook.db"

        @Volatile
        private var instance: GradebookDatabase? = null

        fun get(context: Context): GradebookDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    GradebookDatabase::class.java,
                    NAME
                ).build().also { instance = it }
            }
    }
}
