package ru.college.gradebook.ui.disciplines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.college.gradebook.data.GradebookRepository
import ru.college.gradebook.domain.GradeMath

data class DisciplineUi(
    val id: Long,
    val name: String,
    val teacher: String?,
    val gradeCount: Int,
    val averageText: String,
    val averageValue: Double?,
    val isRisk: Boolean
)

data class DisciplinesUiState(
    val items: List<DisciplineUi> = emptyList(),
    val isEmpty: Boolean = true
)

class DisciplinesViewModel(private val repository: GradebookRepository) : ViewModel() {

    val state: StateFlow<DisciplinesUiState> = repository.observeDisciplineSummaries()
        .map { summaries ->
            DisciplinesUiState(
                items = summaries.map { summary ->
                    DisciplineUi(
                        id = summary.discipline.id,
                        name = summary.discipline.name,
                        teacher = summary.discipline.teacherName,
                        gradeCount = summary.gradeCount,
                        averageText = GradeMath.formatAverage(summary.average),
                        averageValue = summary.average,
                        isRisk = summary.isRisk
                    )
                },
                isEmpty = summaries.isEmpty()
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DisciplinesUiState())
}

class DisciplinesViewModelFactory(
    private val repository: GradebookRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        DisciplinesViewModel(repository) as T
}
