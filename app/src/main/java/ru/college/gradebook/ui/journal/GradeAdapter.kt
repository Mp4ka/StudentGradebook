package ru.college.gradebook.ui.journal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.college.gradebook.R
import ru.college.gradebook.databinding.ItemGradeBinding
import java.time.format.DateTimeFormatter

class GradeAdapter(
    private val onEdit: (GradeUi) -> Unit,
    private val onDelete: (GradeUi) -> Unit
) : ListAdapter<GradeUi, GradeAdapter.Holder>(Diff) {

    private val dateFormat = DateTimeFormatter.ofPattern("dd.MM")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemGradeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class Holder(private val binding: ItemGradeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GradeUi) {
            val context = binding.root.context
            binding.value.text = item.value.toString()
            binding.value.setBackgroundResource(valueBackground(item.value))
            binding.value.setTextColor(context.getColor(valueColor(item.value)))
            binding.date.text = item.date.format(dateFormat)
            binding.year.text = item.date.year.toString()
            binding.type.text = item.typeTitle
            binding.comment.text = item.comment?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.grade_no_comment)

            binding.menu.setOnClickListener { anchor ->
                val popup = android.widget.PopupMenu(context, anchor)
                popup.menu.add(0, MENU_EDIT, 0, R.string.action_edit)
                popup.menu.add(0, MENU_DELETE, 1, R.string.action_delete)
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        MENU_EDIT -> onEdit(item)
                        MENU_DELETE -> onDelete(item)
                    }
                    true
                }
                popup.show()
            }
        }
    }

    private fun valueBackground(value: Int): Int = when {
        value >= 5 -> R.drawable.bg_value_5
        value >= 4 -> R.drawable.bg_value_4
        value >= 3 -> R.drawable.bg_value_3
        else -> R.drawable.bg_value_2
    }

    private fun valueColor(value: Int): Int = when {
        value >= 5 -> R.color.grade_5_ink
        value >= 4 -> R.color.grade_4_ink
        value >= 3 -> R.color.grade_3_ink
        else -> R.color.grade_2_ink
    }

    private object Diff : DiffUtil.ItemCallback<GradeUi>() {
        override fun areItemsTheSame(oldItem: GradeUi, newItem: GradeUi) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: GradeUi, newItem: GradeUi) =
            oldItem == newItem
    }

    companion object {
        private const val MENU_EDIT = 1
        private const val MENU_DELETE = 2
    }
}
