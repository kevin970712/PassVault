package com.jksalcedo.passvault.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.jksalcedo.passvault.R
import com.jksalcedo.passvault.data.PasswordEntry
import java.text.DateFormat
import java.util.Collections.emptyList
import java.util.Date

class PVAdapter : RecyclerView.Adapter<PVAdapter.VH>() {

    private var _items: MutableLiveData<List<PasswordEntry>> =
        MutableLiveData<List<PasswordEntry>>(emptyList<PasswordEntry>())
    val items: LiveData<List<PasswordEntry>> = _items

    var onItemClick: ((PasswordEntry) -> Unit)? = null

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<PasswordEntry>?) {
        _items.value = (list ?: emptyList())
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_password_entry, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items.value?.getOrNull(position) ?: return
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick?.invoke(item) }
    }

    override fun getItemCount(): Int = items.value?.size ?: 0

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val tvUpdatedAt: TextView = itemView.findViewById(R.id.tvUpdatedAt)
        private val tvCategory: MaterialTextView = itemView.findViewById(R.id.tvCategoryChip)

        fun bind(entry: PasswordEntry) {
            tvTitle.text = entry.title
            tvUsername.text = entry.username ?: ""
            val dateText = DateFormat.getDateInstance().format(Date(entry.updatedAt))
            tvUpdatedAt.text = buildString {
                append(dateText)
            }

            val category = entry.category ?: "General"
            tvCategory.text = category.uppercase()

            // Apply category color
            val colorRes = when (category) {
                "General" -> R.color.category_general
                "Social" -> R.color.category_social
                "Work" -> R.color.category_work
                "Personal" -> R.color.category_personal
                "Finance" -> R.color.category_finance
                "Entertainment" -> R.color.category_entertainment
                else -> R.color.category_general
            }

            val color = itemView.context.getColor(colorRes)
            tvCategory.setTextColor(color)
            tvCategory.background?.setTint(color.and(0x00FFFFFF).or(0x20000000))
        }
    }
}