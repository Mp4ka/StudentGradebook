package ru.college.gradebook.ui.journal

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import ru.college.gradebook.GradebookApp
import ru.college.gradebook.R
import ru.college.gradebook.databinding.ActivityJournalBinding
import ru.college.gradebook.ui.applySystemBarInsets
import ru.college.gradebook.ui.edit.GradeEditActivity

/** S3: журнал оценок выбранной дисциплины с добавлением, изменением и удалением. */
class JournalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJournalBinding
    private lateinit var disciplineName: String
    private var disciplineId: Long = 0L

    private val viewModel: JournalViewModel by viewModels {
        JournalViewModelFactory(
            (application as GradebookApp).repository,
            intent.getLongExtra(EXTRA_DISCIPLINE_ID, 0L)
        )
    }

    private val editLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // Журнал обновляется сам: он подписан на Flow из базы.
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
        binding.emptyAction.setOnClickListener { openEditor(null) }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.toolbar.title = state.title.ifEmpty { disciplineName }
                    binding.toolbar.subtitle = state.teacher.orEmpty()
                    binding.average.text = state.averageText
                    binding.count.text = resources.getQuantityString(
                        R.plurals.grades_count, state.gradeCount, state.gradeCount
                    )
                    adapter.submitList(state.grades)

                    binding.empty.visibility = if (state.isEmpty) View.VISIBLE else View.GONE
                    binding.list.visibility = if (state.isEmpty) View.GONE else View.VISIBLE
                    binding.summary.visibility = if (state.isEmpty) View.GONE else View.VISIBLE
                }
            }
        }
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
