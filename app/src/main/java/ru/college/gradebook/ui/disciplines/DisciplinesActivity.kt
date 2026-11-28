package ru.college.gradebook.ui.disciplines

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import ru.college.gradebook.GradebookApp
import ru.college.gradebook.R
import ru.college.gradebook.databinding.ActivityDisciplinesBinding
import ru.college.gradebook.databinding.DialogThresholdBinding
import ru.college.gradebook.ui.applySystemBarInsets
import ru.college.gradebook.ui.journal.JournalActivity
import java.util.Locale

/** S2: список дисциплин с индикатором риска и настройкой порога. */
class DisciplinesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDisciplinesBinding
    private val app by lazy { application as GradebookApp }

    private val viewModel: DisciplinesViewModel by viewModels {
        DisciplinesViewModelFactory(app.repository, app.settings)
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
        binding.toolbar.inflateMenu(R.menu.disciplines_menu)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_threshold) {
                showThresholdDialog()
                true
            } else {
                false
            }
        }
        binding.list.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    adapter.submitList(state.items)
                    binding.empty.visibility = if (state.isEmpty) View.VISIBLE else View.GONE
                    binding.list.visibility = if (state.isEmpty) View.GONE else View.VISIBLE
                    binding.toolbar.subtitle = if (state.riskyCount > 0) {
                        getString(R.string.risk_summary, state.riskyCount, state.items.size)
                    } else {
                        ""
                    }
                }
            }
        }
    }

    private fun showThresholdDialog() {
        val dialogBinding = DialogThresholdBinding.inflate(layoutInflater)
        var value = app.settings.threshold.value

        dialogBinding.thresholdValue.text =
            String.format(Locale.US, "%.1f", value).replace('.', ',')
        dialogBinding.thresholdSeek.progress = ((value - 2.0) * 10).toInt().coerceIn(0, 30)
        dialogBinding.thresholdSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                value = 2.0 + progress / 10.0
                dialogBinding.thresholdValue.text =
                    String.format(Locale.US, "%.1f", value).replace('.', ',')
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        AlertDialog.Builder(this)
            .setTitle(R.string.risk_threshold_dialog_title)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ -> app.settings.setThreshold(value) }
            .show()
    }
}
