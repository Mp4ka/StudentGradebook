package ru.college.gradebook.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.college.gradebook.data.GradebookRepository
import ru.college.gradebook.domain.GradeMath
import java.time.LocalDate

data class GradeUi(
    val id: Long,
    val value: Int,
    val date: LocalDate,
    val typeTitle: String,
    val comment: String?
)

data class JournalUiState(
    val title: String = "",
    val teacher: String? = null,
    val averageText: String = "—",
    val gradeCount: Int = 0,
    val grades: List<GradeUi> = emptyList(),
    val isEmpty: Boolean = true,
    val loaded: Boolean = false
)

class JournalViewModel(
    private val repository: GradebookRepository,
    private val disciplineId: Long
) : ViewModel() {

    val state: StateFlow<JournalUiState> =
        combine(repository.observeGrades(disciplineId), repository.observeDisciplines()) { grades, disciplines ->
            val discipline = disciplines.firstOrNull { it.id == disciplineId }
            JournalUiState(
                title = discipline?.name.orEmpty(),
                teacher = discipline?.teacherName,
                averageText = GradeMath.formatAverage(GradeMath.average(grades.map { it.value })),
                gradeCount = grades.size,
                grades = grades.map { grade ->
                    GradeUi(
                        id = grade.id,
                        value = grade.value,
                        date = grade.date,
                        typeTitle = grade.workType.title,
                        comment = grade.comment
                    )
                },
                isEmpty = grades.isEmpty(),
                loaded = true
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JournalUiState())

    fun deleteGrade(gradeId: Long) {
        viewModelScope.launch { repository.deleteGrade(gradeId) }
    }
}

class JournalViewModelFactory(
    private val repository: GradebookRepository,
    private val disciplineId: Long
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        JournalViewModel(repository, disciplineId) as T
}
