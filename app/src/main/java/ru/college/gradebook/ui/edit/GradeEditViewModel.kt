package ru.college.gradebook.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.college.gradebook.data.GradebookRepository
import ru.college.gradebook.data.model.Grade
import ru.college.gradebook.data.model.WorkType
import ru.college.gradebook.domain.GradeField
import ru.college.gradebook.domain.GradeForm
import ru.college.gradebook.domain.GradeFormValidator
import java.time.LocalDate

data class GradeEditUiState(
    val isEditing: Boolean = false,
    val disciplineName: String = "",
    val value: Int? = null,
    val date: LocalDate = LocalDate.now(),
    val workType: WorkType? = null,
    val comment: String = "",
    val valueError: String? = null,
    val workTypeError: String? = null,
    val saving: Boolean = false,
    val loaded: Boolean = true
)

class GradeEditViewModel(
    private val repository: GradebookRepository,
    private val disciplineId: Long,
    private val gradeId: Long?
) : ViewModel() {

    private val _state = MutableStateFlow(GradeEditUiState(isEditing = gradeId != null))
    val state: StateFlow<GradeEditUiState> = _state.asStateFlow()

    init {
        if (gradeId != null) {
            _state.update { it.copy(loaded = false) }
            viewModelScope.launch {
                val grade = repository.getGrade(gradeId)
                _state.update { current ->
                    if (grade == null) {
                        current.copy(loaded = true)
                    } else {
                        current.copy(
                            value = grade.value,
                            date = grade.date,
                            workType = grade.workType,
                            comment = grade.comment.orEmpty(),
                            loaded = true
                        )
                    }
                }
            }
        }
    }

    fun selectValue(value: Int) = _state.update {
        it.copy(value = value, valueError = null)
    }

    fun selectWorkType(type: WorkType) = _state.update {
        it.copy(workType = type, workTypeError = null)
    }

    fun selectDate(date: LocalDate) = _state.update { it.copy(date = date) }

    fun updateComment(text: String) = _state.update { it.copy(comment = text) }

    /** Проверяет форму и сохраняет запись; [onSaved] вызывается только при успехе. */
    fun save(onSaved: () -> Unit) {
        val current = _state.value
        if (current.saving) return

        val form = GradeForm(
            disciplineId = disciplineId,
            value = current.value,
            date = current.date,
            workType = current.workType,
            comment = current.comment
        )
        val errors = GradeFormValidator.validate(form)
        if (errors.isNotEmpty()) {
            _state.update {
                it.copy(
                    valueError = errors[GradeField.VALUE],
                    workTypeError = errors[GradeField.WORK_TYPE]
                )
            }
            return
        }

        _state.update { it.copy(saving = true, valueError = null, workTypeError = null) }
        viewModelScope.launch {
            val value = requireNotNull(current.value)
            val workType = requireNotNull(current.workType)
            val comment = current.comment.trim().takeIf { it.isNotEmpty() }
            if (gradeId == null) {
                repository.addGrade(disciplineId, value, current.date, workType, comment)
            } else {
                repository.updateGrade(
                    Grade(
                        id = gradeId,
                        disciplineId = disciplineId,
                        value = value,
                        date = current.date,
                        workType = workType,
                        comment = comment
                    )
                )
            }
            _state.update { it.copy(saving = false) }
            onSaved()
        }
    }
}

class GradeEditViewModelFactory(
    private val repository: GradebookRepository,
    private val disciplineId: Long,
    private val gradeId: Long?
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        GradeEditViewModel(repository, disciplineId, gradeId) as T
}
