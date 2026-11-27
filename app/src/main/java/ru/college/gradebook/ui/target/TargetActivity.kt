package ru.college.gradebook.ui.target

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import ru.college.gradebook.GradebookApp
import ru.college.gradebook.R
import ru.college.gradebook.databinding.ActivityTargetBinding
import ru.college.gradebook.domain.TargetResult
import ru.college.gradebook.ui.applySystemBarInsets
import java.util.Locale

/** S6: калькулятор целевого балла. */
class TargetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTargetBinding
    private var ignoreSpinnerCallback = false
    private var ignoreSeekCallback = false

    private val viewModel: TargetViewModel by viewModels {
        TargetViewModelFactory((application as GradebookApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTargetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsets()

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.disciplineSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (ignoreSpinnerCallback) return
                viewModel.selectDiscipline(viewModel.state.value.disciplines.getOrNull(position)?.id)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        binding.targetSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) viewModel.setTarget(2.0 + progress / 10.0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: TargetUiState) {
        val names = state.disciplines.map { it.name }
        binding.disciplineSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, names
        )
        ignoreSpinnerCallback = true
        val index = state.disciplines.indexOfFirst { it.id == state.selectedDisciplineId }
            .coerceAtLeast(0)
        binding.disciplineSpinner.setSelection(index, false)
        ignoreSpinnerCallback = false

        binding.average.text = state.averageText
        binding.count.text = resources.getQuantityString(
            R.plurals.grades_count, state.gradeCount, state.gradeCount
        )
        binding.targetValue.text = format(state.target)

        ignoreSeekCallback = true
        binding.targetSeek.progress = ((state.target - 2.0) * 10).toInt().coerceIn(0, 30)
        ignoreSeekCallback = false

        when (val result = state.result) {
            is TargetResult.Needed -> {
                binding.resultTitle.setText(R.string.target_needed_title)
                binding.resultValue.text = result.grade.toString()
                binding.resultValue.setTextColor(ContextCompat.getColor(this, R.color.brand_primary))
                binding.resultNote.text = getString(R.string.target_needed_note, format(state.target))
            }
            is TargetResult.Unreachable -> {
                binding.resultTitle.setText(R.string.target_unreachable_title)
                binding.resultValue.text = format(result.maxAverage)
                binding.resultValue.setTextColor(ContextCompat.getColor(this, R.color.risk_ink))
                binding.resultNote.setText(R.string.target_unreachable_note)
            }
            TargetResult.Achieved -> {
                binding.resultTitle.setText(R.string.target_achieved_title)
                binding.resultValue.text = "✓"
                binding.resultValue.setTextColor(ContextCompat.getColor(this, R.color.grade_5_ink))
                binding.resultNote.setText(R.string.target_achieved_note)
            }
            null -> {
                binding.resultTitle.setText(R.string.target_no_data_title)
                binding.resultValue.text = "—"
                binding.resultValue.setTextColor(ContextCompat.getColor(this, R.color.ink_muted))
                binding.resultNote.setText(R.string.target_no_data_note)
            }
        }

        binding.targetSeek.isEnabled = state.hasData
    }

    private fun format(value: Double): String =
        String.format(Locale.US, "%.1f", value).replace('.', ',')
}
