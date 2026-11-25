package ru.college.gradebook.ui.disciplines

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import ru.college.gradebook.GradebookApp
import ru.college.gradebook.databinding.ActivityDisciplinesBinding
import ru.college.gradebook.ui.applySystemBarInsets
import ru.college.gradebook.ui.journal.JournalActivity

/** S2: список дисциплин со средним баллом и переходом в журнал. */
class DisciplinesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDisciplinesBinding

    private val viewModel: DisciplinesViewModel by viewModels {
        DisciplinesViewModelFactory((application as GradebookApp).repository)
    }

    private val adapter = DisciplineAdapter { discipline ->
        startActivity(
            Intent(this, JournalActivity::class.java)
                .putExtra(JournalActivity.EXTRA_DISCIPLINE_ID, discipline.id)
                .putExtra(JournalActivity.EXTRA_DISCIPLINE_NAME, discipline.name)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisciplinesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsets()

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.list.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    adapter.submitList(state.items)
                    binding.empty.visibility = if (state.isEmpty) {
                        android.view.View.VISIBLE
                    } else {
                        android.view.View.GONE
                    }
                    binding.list.visibility = if (state.isEmpty) {
                        android.view.View.GONE
                    } else {
                        android.view.View.VISIBLE
                    }
                }
            }
        }
    }
}
