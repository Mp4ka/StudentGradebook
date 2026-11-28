package ru.college.gradebook.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.college.gradebook.domain.RiskSettings

/** Пользовательские настройки: порог риска хранится в SharedPreferences. */
class SettingsStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    private val _threshold = MutableStateFlow(prefs.getFloat(KEY_THRESHOLD, 3.5f).toDouble())
    val threshold: StateFlow<Double> = _threshold.asStateFlow()

    fun setThreshold(value: Double) {
        val normalized = value.coerceIn(2.0, 5.0)
        prefs.edit().putFloat(KEY_THRESHOLD, normalized.toFloat()).apply()
        _threshold.value = normalized
    }

    fun riskSettings(): RiskSettings = RiskSettings(threshold = _threshold.value)

    companion object {
        private const val NAME = "gradebook_settings"
        private const val KEY_THRESHOLD = "risk_threshold"
    }
}
