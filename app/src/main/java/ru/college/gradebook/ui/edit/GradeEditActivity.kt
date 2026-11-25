package ru.college.gradebook.ui.edit

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import ru.college.gradebook.GradebookApp
import ru.college.gradebook.R
import ru.college.gradebook.ui.applySystemBarInsets
import ru.college.gradebook.data.model.WorkType
import ru.college.gradebook.databinding.ActivityGradeEditBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** S4: форма добавления и редактирования оценки с валидацией. */
class GradeEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGradeEditBinding

    private val disciplineId by lazy { intent.getLongExtra(EXTRA_DISCIPLINE_ID, 0L) }
    private val gradeId by lazy {
        intent.getLongExtra(EXTRA_GRADE_ID, -1L).takeIf { it > 0L }
    }

    private val viewModel: GradeEditViewModel by viewModels {
        GradeEditViewModelFactory(
            (application as GradebookApp).repository,
            disciplineId,
            gradeId
        )
    }

    private val dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private var suppressToggleCallback = false
    private var suppressChipCallback = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGradeEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsets()

        binding.toolbar.title = getString(
            if (gradeId == null) R.string.title_new_grade else R.string.title_edit_grade
        )
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.discipline.text = intent.getStringExtra(EXTRA_DISCIPLINE_NAME).orEmpty()

        binding.toggleValue.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked && !suppressToggleCallback) {
                viewModel.selectValue(
                    when (checkedId) {
                        R.id.btn_2 -> 2
                        R.id.btn_3 -> 3
                        R.id.btn_4 -> 4
                        else -> 5
                    }
                )
            }
        }

        binding.chips.setOnCheckedStateChangeListener { _, checkedIds ->
            if (!suppressChipCallback) {
                checkedIds.firstOrNull()?.let { id ->
                    viewModel.selectWorkType(workTypeOf(id))
                }
            }
        }

        binding.date.setOnClickListener { showDatePicker(viewModel.state.value.date) }
        binding.comment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateComment(s?.toString().orEmpty())
            }
        })
        binding.save.setOnClickListener { viewModel.save { finish() } }
        binding.cancel.setOnClickListener { finish() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: GradeEditUiState) {
        binding.date.text = state.date.format(dateFormat)

        suppressToggleCallback = true
        when (state.value) {
            2 -> binding.toggleValue.check(R.id.btn_2)
            3 -> binding.toggleValue.check(R.id.btn_3)
            4 -> binding.toggleValue.check(R.id.btn_4)
            5 -> binding.toggleValue.check(R.id.btn_5)
            else -> binding.toggleValue.clearChecked()
        }
        suppressToggleCallback = false

        if (state.workType != null) {
            val chipId = chipOf(state.workType)
            if (binding.chips.checkedChipId != chipId) {
                suppressChipCallback = true
                binding.chips.check(chipId)
                suppressChipCallback = false
            }
        }

        if (binding.comment.text.toString() != state.comment) {
            binding.comment.setText(state.comment)
            binding.comment.setSelection(state.comment.length)
        }

        binding.valueError.text = state.valueError
        binding.valueError.visibility = if (state.valueError == null) View.GONE else View.VISIBLE
        binding.workTypeError.text = state.workTypeError
        binding.workTypeError.visibility =
            if (state.workTypeError == null) View.GONE else View.VISIBLE

        binding.save.isEnabled = !state.saving
        binding.save.text = getString(
            if (state.saving) R.string.saving else R.string.action_save
        )
    }

    private fun showDatePicker(current: LocalDate) {
        DatePickerDialog(
            this,
            { _, year, month, day -> viewModel.selectDate(LocalDate.of(year, month + 1, day)) },
            current.year,
            current.monthValue - 1,
            current.dayOfMonth
        ).show()
    }

    private fun chipOf(type: WorkType): Int = when (type) {
        WorkType.CONTROL -> R.id.chip_control
        WorkType.PRACTICE -> R.id.chip_practice
        WorkType.LAB -> R.id.chip_lab
        WorkType.ANSWER -> R.id.chip_answer
        WorkType.TEST -> R.id.chip_test
        WorkType.OTHER -> R.id.chip_other
    }

    private fun workTypeOf(chipId: Int): WorkType = when (chipId) {
        R.id.chip_control -> WorkType.CONTROL
        R.id.chip_practice -> WorkType.PRACTICE
        R.id.chip_lab -> WorkType.LAB
        R.id.chip_answer -> WorkType.ANSWER
        R.id.chip_test -> WorkType.TEST
        else -> WorkType.OTHER
    }

    companion object {
        const val EXTRA_DISCIPLINE_ID = "extra_discipline_id"
        const val EXTRA_DISCIPLINE_NAME = "extra_discipline_name"
        const val EXTRA_GRADE_ID = "extra_grade_id"
    }
}
