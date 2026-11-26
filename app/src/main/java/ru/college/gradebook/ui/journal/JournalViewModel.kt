package ru.college.gradebook.ui.journal

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
import kotlinx.coroutines.launch
import ru.college.gradebook.data.GradebookRepository
import ru.college.gradebook.data.model.WorkType
import ru.college.gradebook.domain.GradeFilter
import ru.college.gradebook.domain.GradeMath
import ru.college.gradebook.domain.GradeSort
import ru.college.gradebook.domain.Period
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
    val totalCount: Int = 0,
    val filteredCount: Int = 0,
    val filteredAverageText: String = "—",
    val filterActive: Boolean = false,
    val workType: WorkType? = null,
    val sort: GradeSort = GradeSort.DATE_DESC,
    val grades: List<GradeUi> = emptyList(),
    val isEmpty: Boolean = true,
    val isEmptyByFilter: Boolean = false,
    val loaded: Boolean = false
)

class JournalViewModel(
    private val repository: GradebookRepository,
    private val disciplineId: Long
) : ViewModel() {

    private val _filter = MutableStateFlow(GradeFilter())
    val filter: StateFlow<GradeFilter> = _filter.asStateFlow()

    val state: StateFlow<JournalUiState> = combine(
        repository.observeGrades(disciplineId),
        repository.observeDisciplines(),
        _filter
    ) { grades, disciplines, filter ->
        val discipline = disciplines.firstOrNull { it.id == disciplineId }
        val visible = filter.apply(grades)
        JournalUiState(
            title = discipline?.name.orEmpty(),
            teacher = discipline?.teacherName,
            averageText = GradeMath.formatAverage(GradeMath.average(grades.map { it.value })),
            totalCount = grades.size,
            filteredCount = visible.size,
            filteredAverageText = GradeMath.formatAverage(GradeMath.average(visible.map { it.value })),
            filterActive = filter.isActive,
            workType = filter.workType,
            sort = filter.sort,
            grades = visible.map { grade ->
                GradeUi(
                    id = grade.id,
                    value = grade.value,
                    date = grade.date,
                    typeTitle = grade.workType.title,
                    comment = grade.comment
                )
            },
            isEmpty = grades.isEmpty(),
            isEmptyByFilter = grades.isNotEmpty() && visible.isEmpty(),
            loaded = true
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JournalUiState())

    fun setWorkType(type: WorkType?) = _filter.update { it.copy(workType = type) }

    fun setPeriod(period: Period?) = _filter.update { it.copy(period = period) }

    fun setSort(sort: GradeSort) = _filter.update { it.copy(sort = sort) }

    fun resetFilters() = _filter.update { GradeFilter(sort = it.sort) }

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
