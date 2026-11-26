package ru.college.gradebook

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ru.college.gradebook.data.model.Discipline
import ru.college.gradebook.data.model.Grade
import ru.college.gradebook.domain.GradeMath
import ru.college.gradebook.ui.applySystemBarInsets
import ru.college.gradebook.ui.disciplines.DisciplinesActivity
import ru.college.gradebook.ui.dynamics.DynamicsActivity

/**
 * Главный экран: средний балл и количество записей из Room, вход в список дисциплин.
 * Разделы «Динамика» и «Целевой балл» появятся в следующих спринтах.
 */
class MainActivity : AppCompatActivity() {

    private val repository by lazy { (application as GradebookApp).repository }

    private lateinit var averageView: TextView
    private lateinit var statsView: TextView
    private lateinit var emptyTitleView: TextView
    private lateinit var emptyBodyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(android.R.id.content).applySystemBarInsets()

        averageView = findViewById(R.id.tv_average)
        statsView = findViewById(R.id.tv_stats)
        emptyTitleView = findViewById(R.id.tv_empty_title)
        emptyBodyView = findViewById(R.id.tv_empty_body)

        findViewById<View>(R.id.card_disciplines).setOnClickListener {
            startActivity(Intent(this, DisciplinesActivity::class.java))
        }
        findViewById<View>(R.id.card_dynamics).setOnClickListener {
            startActivity(Intent(this, DynamicsActivity::class.java))
        }
        bindSection(R.id.card_target, getString(R.string.menu_target))

        observeData()
    }

    private fun observeData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    repository.observeAllGrades(),
                    repository.observeDisciplines()
                ) { grades, disciplines -> grades to disciplines }
                    .collect { (grades, disciplines) -> render(grades, disciplines) }
            }
        }
    }

    private fun render(grades: List<Grade>, disciplines: List<Discipline>) {
        val average = GradeMath.average(grades.map { it.value })
        averageView.text = GradeMath.formatAverage(average)

        val hasData = grades.isNotEmpty()
        statsView.text = if (hasData) {
            resources.getQuantityString(R.plurals.grades_count, grades.size, grades.size) +
                " · " +
                resources.getQuantityString(R.plurals.disciplines_count, disciplines.size, disciplines.size)
        } else {
            ""
        }

        emptyTitleView.visibility = if (hasData) View.GONE else View.VISIBLE
        emptyBodyView.visibility = if (hasData) View.GONE else View.VISIBLE
    }

    private fun bindSection(cardId: Int, title: String) {
        findViewById<View>(cardId).setOnClickListener {
            Toast.makeText(
                this,
                getString(R.string.soon_template, title),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
