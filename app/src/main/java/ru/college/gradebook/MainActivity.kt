package ru.college.gradebook

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Главный экран. На первом спринте это каркас: показан заголовок, пустое
 * состояние сводки и список разделов приложения. Реальная навигация и данные
 * появляются в спринтах 2–3, поэтому пункты разделов показывают пояснение.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindSection(R.id.card_disciplines, getString(R.string.menu_disciplines))
        bindSection(R.id.card_dynamics, getString(R.string.menu_dynamics))
        bindSection(R.id.card_target, getString(R.string.menu_target))
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
