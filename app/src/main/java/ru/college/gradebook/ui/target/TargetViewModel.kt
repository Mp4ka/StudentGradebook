package ru.college.gradebook.ui.target

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
import ru.college.gradebook.domain.GradeMath
import ru.college.gradebook.domain.TargetCalculator
import ru.college.gradebook.domain.TargetResult

data class TargetUiState(
    val disciplines: List<Discipline> = emptyList(),
    val selectedDisciplineId: Long? = null,
    val title: String = "",
    val averageText: String = "—",
    val gradeCount: Int = 0,
    val target: Double = 4.4,
    val result: TargetResult? = null,
    val hasData: Boolean = false
)

class TargetViewModel(private val repository: GradebookRepository) : ViewModel() {

    private val selected = MutableStateFlow<Long?>(null)
    private val target = MutableStateFlow(4.4)

    val selectedDisciplineId: StateFlow<Long?> = selected.asStateFlow()
    val targetValue: StateFlow<Double> = target.asStateFlow()

    val state: StateFlow<TargetUiState> = combine(
        repository.observeDisciplines(),
        repository.observeAllGrades(),
        selected,
        target
    ) { disciplines, grades, selectedId, targetValue ->
        val discipline = disciplines.firstOrNull { it.id == selectedId } ?: disciplines.firstOrNull()
        val visible = if (discipline == null) emptyList() else grades.filter { it.disciplineId == discipline.id }
        val values = visible.map { it.value }
        val sum = values.sum()
        val count = values.size

        TargetUiState(
            disciplines = disciplines,
            selectedDisciplineId = discipline?.id,
            title = discipline?.name.orEmpty(),
            averageText = GradeMath.formatAverage(GradeMath.average(values)),
            gradeCount = count,
            target = targetValue,
            result = if (count == 0) null else TargetCalculator.evaluate(sum, count, targetValue),
            hasData = count > 0
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TargetUiState())

    fun selectDiscipline(id: Long?) = selected.update { id }

    fun setTarget(value: Double) {
        target.update { value.coerceIn(TargetCalculator.MIN_GRADE.toDouble(), TargetCalculator.MAX_GRADE.toDouble()) }
    }
}

class TargetViewModelFactory(
    private val repository: GradebookRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        TargetViewModel(repository) as T
}
