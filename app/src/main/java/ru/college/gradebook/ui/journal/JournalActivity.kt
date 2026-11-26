package ru.college.gradebook.ui.journal

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import ru.college.gradebook.GradebookApp
import ru.college.gradebook.R
import ru.college.gradebook.data.model.WorkType
import ru.college.gradebook.databinding.ActivityJournalBinding
import ru.college.gradebook.domain.GradeSort
import ru.college.gradebook.domain.Period
import ru.college.gradebook.ui.applySystemBarInsets
import ru.college.gradebook.ui.edit.GradeEditActivity
import java.time.Instant
import java.time.ZoneOffset

/**
 * S3: журнал оценок. Фильтры по виду работы и периоду, сортировка,
 * добавление, изменение и удаление записей.
 */
class JournalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJournalBinding
    private lateinit var disciplineName: String
    private var disciplineId: Long = 0L
    private var ignoreChipsCallback = false

    private val viewModel: JournalViewModel by viewModels {
        JournalViewModelFactory(
            (application as GradebookApp).repository,
            intent.getLongExtra(EXTRA_DISCIPLINE_ID, 0L)
        )
    }

    private val editLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // Журнал подписан на Flow и обновляется сам.
    }

    private val adapter = GradeAdapter(
        onEdit = { grade -> openEditor(grade.id) },
        onDelete = { grade -> confirmDelete(grade) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJournalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsets()

        disciplineName = intent.getStringExtra(EXTRA_DISCIPLINE_NAME).orEmpty()
        disciplineId = intent.getLongExtra(EXTRA_DISCIPLINE_ID, 0L)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.list.adapter = adapter
        binding.fab.setOnClickListener { openEditor(null) }
        binding.emptyAction.setOnClickListener {
            if (binding.emptyAction.tag == true) viewModel.resetFilters() else openEditor(null)
        }

        binding.typeChips.setOnCheckedStateChangeListener { _, checkedIds ->
            if (ignoreChipsCallback) return@setOnCheckedStateChangeListener
            viewModel.setWorkType(
                when (checkedIds.firstOrNull()) {
                    R.id.chipControl -> WorkType.CONTROL
                    R.id.chipPractice -> WorkType.PRACTICE
                    R.id.chipLab -> WorkType.LAB
                    R.id.chipAnswer -> WorkType.ANSWER
                    R.id.chipTest -> WorkType.TEST
                    else -> null
                }
            )
        }

        binding.periodButton.setOnClickListener { pickPeriod() }
        binding.sortButton.setOnClickListener { showSortMenu() }
        binding.resetButton.setOnClickListener { viewModel.resetFilters() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: JournalUiState) {
        binding.toolbar.title = state.title.ifEmpty { disciplineName }
        binding.toolbar.subtitle = state.teacher.orEmpty()
        binding.average.text = state.averageText
        binding.count.text = resources.getQuantityString(
            R.plurals.grades_count, state.totalCount, state.totalCount
        )
        adapter.submitList(state.grades)

        val chipId = when (state.workType) {
            WorkType.CONTROL -> R.id.chipControl
            WorkType.PRACTICE -> R.id.chipPractice
            WorkType.LAB -> R.id.chipLab
            WorkType.ANSWER -> R.id.chipAnswer
            WorkType.TEST -> R.id.chipTest
            else -> R.id.chipAll
        }
        if (binding.typeChips.checkedChipId != chipId) {
            ignoreChipsCallback = true
            binding.typeChips.check(chipId)
            ignoreChipsCallback = false
        }

        if (state.filterActive) {
            binding.filterInfo.text = getString(
                R.string.filtered_line,
                state.filteredAverageText,
                resources.getQuantityString(
                    R.plurals.grades_count, state.filteredCount, state.filteredCount
                )
            )
            binding.filterInfo.visibility = View.VISIBLE
            binding.resetButton.visibility = View.VISIBLE
        } else {
            binding.filterInfo.visibility = View.GONE
            binding.resetButton.visibility = View.GONE
        }

        val emptyVisible = state.isEmpty || state.isEmptyByFilter
        binding.empty.visibility = if (emptyVisible) View.VISIBLE else View.GONE
        binding.list.visibility = if (emptyVisible) View.GONE else View.VISIBLE
        binding.summary.visibility = if (state.isEmpty) View.GONE else View.VISIBLE

        if (state.isEmptyByFilter) {
            binding.emptyTitle.setText(R.string.empty_filtered_title)
            binding.emptyBody.setText(R.string.empty_filtered_body)
            binding.emptyAction.setText(R.string.action_reset_filter)
            binding.emptyAction.tag = true
        } else {
            binding.emptyTitle.setText(R.string.empty_grades_title)
            binding.emptyBody.setText(R.string.empty_grades_body)
            binding.emptyAction.setText(R.string.action_add_grade)
            binding.emptyAction.tag = false
        }
    }

    private fun pickPeriod() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(R.string.period_pick)
            .build()
        picker.addOnPositiveButtonClickListener { range ->
            val from = Instant.ofEpochMilli(range.first).atZone(ZoneOffset.UTC).toLocalDate()
            val to = Instant.ofEpochMilli(range.second).atZone(ZoneOffset.UTC).toLocalDate()
            viewModel.setPeriod(Period(from, to))
        }
        picker.show(supportFragmentManager, "period")
    }

    private fun showSortMenu() {
        val popup = PopupMenu(this, binding.sortButton)
        GradeSort.entries.forEach { sort ->
            popup.menu.add(0, sort.ordinal, sort.ordinal, sort.title)
        }
        popup.setOnMenuItemClickListener { item ->
            viewModel.setSort(GradeSort.entries[item.itemId])
            true
        }
        popup.show()
    }

    private fun openEditor(gradeId: Long?) {
        val editorIntent = Intent(this, GradeEditActivity::class.java)
            .putExtra(GradeEditActivity.EXTRA_DISCIPLINE_ID, disciplineId)
            .putExtra(GradeEditActivity.EXTRA_DISCIPLINE_NAME, disciplineName)
        if (gradeId != null) {
            editorIntent.putExtra(GradeEditActivity.EXTRA_GRADE_ID, gradeId)
        }
        editLauncher.launch(editorIntent)
    }

    private fun confirmDelete(grade: GradeUi) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_grade_title)
            .setMessage(getString(R.string.delete_grade_message, grade.typeTitle))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                viewModel.deleteGrade(grade.id)
            }
            .show()
    }

    companion object {
        const val EXTRA_DISCIPLINE_ID = "extra_discipline_id"
        const val EXTRA_DISCIPLINE_NAME = "extra_discipline_name"
    }
}
