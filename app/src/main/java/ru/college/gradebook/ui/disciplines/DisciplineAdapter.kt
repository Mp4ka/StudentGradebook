package ru.college.gradebook.ui.disciplines

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.college.gradebook.R
import ru.college.gradebook.databinding.ItemDisciplineBinding

class DisciplineAdapter(
    private val onClick: (DisciplineUi) -> Unit
) : ListAdapter<DisciplineUi, DisciplineAdapter.Holder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemDisciplineBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class Holder(private val binding: ItemDisciplineBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DisciplineUi) {
            val context = binding.root.context
            binding.name.text = item.name
            binding.teacher.text = item.teacher ?: context.getString(R.string.teacher_unknown)
            binding.count.text = context.resources.getQuantityString(
                R.plurals.grades_count, item.gradeCount, item.gradeCount
            )
            binding.average.text = item.averageText
            val average = item.averageValue
            binding.average.setBackgroundResource(
                when {
                    item.isRisk -> R.drawable.bg_badge_risk
                    average != null && average >= GOOD_AVERAGE -> R.drawable.bg_badge_green
                    else -> R.drawable.bg_badge_ok
                }
            )
            binding.average.setTextColor(
                context.getColor(
                    when {
                        item.isRisk -> R.color.risk_ink
                        average == null -> R.color.ink_muted
                        average >= GOOD_AVERAGE -> R.color.grade_5_ink
                        else -> R.color.brand_primary
                    }
                )
            )
            binding.riskChip.visibility = if (item.isRisk) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<DisciplineUi>() {
        override fun areItemsTheSame(oldItem: DisciplineUi, newItem: DisciplineUi) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: DisciplineUi, newItem: DisciplineUi) =
            oldItem == newItem
    }

    private companion object {
        const val GOOD_AVERAGE = 4.5
    }
}
