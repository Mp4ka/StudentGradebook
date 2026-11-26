package ru.college.gradebook.ui.dynamics

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import ru.college.gradebook.GradebookApp
import ru.college.gradebook.R
import ru.college.gradebook.databinding.ActivityDynamicsBinding
import ru.college.gradebook.domain.Analytics
import ru.college.gradebook.ui.applySystemBarInsets
import java.time.format.DateTimeFormatter
import java.util.Locale

/** S5: динамика среднего балла по месяцам с фильтром по дисциплине. */
class DynamicsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDynamicsBinding
    private var ignoreSpinnerCallback = false

    private val viewModel: DynamicsViewModel by viewModels {
        DynamicsViewModelFactory((application as GradebookApp).repository)
    }

    private val monthFormat = DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDynamicsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsets()

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.disciplineSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (ignoreSpinnerCallback) return
                val selected = viewModel.state.value.disciplines.getOrNull(position - 1)
                viewModel.selectDiscipline(selected?.id)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: DynamicsUiState) {
        val names = buildList {
            add(getString(R.string.all_disciplines))
            addAll(state.disciplines.map { it.name })
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names)
        ignoreSpinnerCallback = true
        binding.disciplineSpinner.adapter = adapter
        val index = state.selectedDisciplineId
            ?.let { id -> state.disciplines.indexOfFirst { it.id == id } + 1 }
            ?: 0
        binding.disciplineSpinner.setSelection(index.coerceAtLeast(0), false)
        ignoreSpinnerCallback = false

        binding.average.text = state.averageText
        binding.count.text = resources.getQuantityString(
            R.plurals.grades_count, state.gradeCount, state.gradeCount
        )
        binding.chart.setData(state.months)

        val low = Analytics.lowest(state.months)
        val high = Analytics.highest(state.months)
        binding.summary.text = if (low != null && high != null && state.months.size > 1) {
            getString(
                R.string.dynamics_summary,
                high.month.format(monthFormat),
                high.average?.let { format(it) }.orEmpty(),
                low.month.format(monthFormat),
                low.average?.let { format(it) }.orEmpty()
            )
        } else {
            ""
        }

        binding.empty.visibility = if (state.hasData) View.GONE else View.VISIBLE
        binding.content.visibility = if (state.hasData) View.VISIBLE else View.GONE
    }

    private fun format(value: Double): String =
        String.format(Locale.US, "%.2f", value).replace('.', ',')
}
