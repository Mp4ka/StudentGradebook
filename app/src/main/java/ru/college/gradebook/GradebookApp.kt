package ru.college.gradebook

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.college.gradebook.data.GradebookRepository
import ru.college.gradebook.data.db.GradebookDatabase

/**
 * Точка сборки зависимостей: база и репозиторий создаются лениво, при первом
 * запуске база заполняется демонстрационным набором.
 */
class GradebookApp : Application() {

    val database: GradebookDatabase by lazy { GradebookDatabase.get(this) }

    val repository: GradebookRepository by lazy { GradebookRepository(database) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch { repository.seedDemoDataIfEmpty() }
    }
}
