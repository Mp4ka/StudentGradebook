package ru.college.gradebook.ui.dynamics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import ru.college.gradebook.data.GradebookRepository
import ru.college.gradebook.data.model.Discipline
import ru.college.gradebook.domain.Analytics
import ru.college.gradebook.domain.GradeMath
import ru.college.gradebook.domain.MonthlyStats

data class DynamicsUiState(
    val disciplines: List<Discipline> = emptyList(),
    val selectedDisciplineId: Long? = null,
    val months: List<MonthlyStats> = emptyList(),
    val averageText: String = "—",
    val gradeCount: Int = 0,
    val hasData: Boolean = false
)

class DynamicsViewModel(private val repository: GradebookRepository) : ViewModel() {

    private val selected = MutableStateFlow<Long?>(null)
    val selectedDisciplineId: StateFlow<Long?> = selected.asStateFlow()

    val state: StateFlow<DynamicsUiState> = combine(
        repository.observeDisciplines(),
        repository.observeAllGrades(),
        selected
    ) { disciplines, grades, selectedId ->
        val visible = if (selectedId == null) {
            grades
        } else {
            grades.filter { it.disciplineId == selectedId }
        }
        DynamicsUiState(
            disciplines = disciplines,
            selectedDisciplineId = selectedId,
            months = Analytics.monthly(visible),
            averageText = GradeMath.formatAverage(GradeMath.average(visible.map { it.value })),
            gradeCount = visible.size,
            hasData = visible.isNotEmpty()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DynamicsUiState())

    fun selectDiscipline(id: Long?) = selected.update { id }
}

class DynamicsViewModelFactory(
    private val repository: GradebookRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        DynamicsViewModel(repository) as T
}
