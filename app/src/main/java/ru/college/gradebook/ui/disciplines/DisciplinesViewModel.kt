package ru.college.gradebook.ui.disciplines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import ru.college.gradebook.data.GradebookRepository
import ru.college.gradebook.data.SettingsStore
import ru.college.gradebook.domain.GradeMath
import ru.college.gradebook.domain.RiskAnalyzer
import ru.college.gradebook.domain.RiskReason
import ru.college.gradebook.domain.RiskSettings
import java.time.LocalDate

data class DisciplineUi(
    val id: Long,
    val name: String,
    val teacher: String?,
    val gradeCount: Int,
    val averageText: String,
    val averageValue: Double?,
    val isRisk: Boolean,
    val reasons: List<RiskReason>,
    val thresholdText: String
)

data class DisciplinesUiState(
    val items: List<DisciplineUi> = emptyList(),
    val isEmpty: Boolean = true,
    val riskyCount: Int = 0
)

class DisciplinesViewModel(
    private val repository: GradebookRepository,
    private val settings: SettingsStore
) : ViewModel() {

    val state: StateFlow<DisciplinesUiState> = combine(
        repository.observeDisciplineSummaries(),
        settings.threshold
    ) { summaries, threshold ->
        val riskSettings = RiskSettings(threshold = threshold)
        val today = LocalDate.now()
        val items = summaries.map { summary ->
            val risk = RiskAnalyzer.assess(
                average = summary.average,
                gradeCount = summary.gradeCount,
                lastGradeDate = summary.lastGradeDate,
                today = today,
                settings = riskSettings
            )
            DisciplineUi(
                id = summary.discipline.id,
                name = summary.discipline.name,
                teacher = summary.discipline.teacherName,
                gradeCount = summary.gradeCount,
                averageText = GradeMath.formatAverage(summary.average),
                averageValue = summary.average,
                isRisk = risk.isRisk,
                reasons = risk.reasons,
                thresholdText = GradeMath.formatAverage(threshold)
            )
        }
        DisciplinesUiState(
            items = items,
            isEmpty = items.isEmpty(),
            riskyCount = items.count { it.isRisk }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DisciplinesUiState())
}

class DisciplinesViewModelFactory(
    private val repository: GradebookRepository,
    private val settings: SettingsStore
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        DisciplinesViewModel(repository, settings) as T
}
