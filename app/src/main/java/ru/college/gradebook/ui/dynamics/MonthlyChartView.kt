package ru.college.gradebook.ui.dynamics

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import ru.college.gradebook.R
import ru.college.gradebook.domain.MonthlyStats
import kotlin.math.max

/**
 * Простой столбчатый график среднего балла по месяцам.
 * Шкала фиксирована от 2 до 5 — так столбцы честно сравнимы между собой.
 */
class MonthlyChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var months: List<MonthlyStats> = emptyList()

    private val density = resources.displayMetrics.density
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.ink)
        textSize = 12 * density
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.ink_muted)
        textSize = 11 * density
        textAlign = Paint.Align.CENTER
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.stroke)
        strokeWidth = 1 * density
    }

    private val monthNames = listOf(
        "янв", "фев", "мар", "апр", "май", "июн",
        "июл", "авг", "сен", "окт", "ноя", "дек"
    )

    fun setData(data: List<MonthlyStats>) {
        months = data
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val desired = (200 * density).toInt()
        setMeasuredDimension(width, resolveSize(desired, heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (months.isEmpty()) return

        val left = 34 * density
        val right = width - 12 * density
        val top = 22 * density
        val bottom = height - 26 * density
        val chartHeight = bottom - top
        if (chartHeight <= 0 || right <= left) return

        for (value in 3..5) {
            val y = bottom - chartHeight * ((value - 2f) / 3f)
            canvas.drawLine(left, y, right, y, gridPaint)
        }

        val slot = (right - left) / months.size
        val barWidth = minOf(slot * 0.55f, 46 * density)
        months.forEachIndexed { index, stats ->
            val average = stats.average ?: return@forEachIndexed
            val centerX = left + slot * index + slot / 2
            val heightRatio = ((average - 2.0) / 3.0).coerceIn(0.0, 1.0).toFloat()
            val barHeight = max(chartHeight * heightRatio, 2 * density)
            val rect = RectF(
                centerX - barWidth / 2,
                bottom - barHeight,
                centerX + barWidth / 2,
                bottom
            )

            barPaint.color = when {
                average < 3.5 -> context.getColor(R.color.risk_ink)
                average >= 4.5 -> context.getColor(R.color.grade_5_ink)
                else -> context.getColor(R.color.brand_primary)
            }
            canvas.drawRoundRect(rect, 8 * density, 8 * density, barPaint)

            canvas.drawText(
                String.format("%.2f", average).replace('.', ','),
                centerX,
                rect.top - 6 * density,
                valuePaint
            )
            canvas.drawText(
                monthNames[stats.month.monthValue - 1],
                centerX,
                bottom + 18 * density,
                labelPaint
            )
        }
    }
}
