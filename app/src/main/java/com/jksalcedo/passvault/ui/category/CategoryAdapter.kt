package com.jksalcedo.passvault.ui.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jksalcedo.passvault.data.Category
import com.jksalcedo.passvault.databinding.ItemCategoryBinding
import android.graphics.Color

class CategoryAdapter(
    private val onDeleteClick: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.tvCategoryName.text = category.name
            binding.tvColorHex.text = category.colorHex

            // Show color preview
            try {
                val color = Color.parseColor(category.colorHex)
                binding.viewColorPreview.setBackgroundColor(color)
            } catch (e: IllegalArgumentException) {
                binding.viewColorPreview.setBackgroundColor(Color.GRAY)
            }

            // Show badge for default categories
            if (category.isDefault) {
                binding.tvDefaultBadge.visibility = android.view.View.VISIBLE
                binding.btnDelete.isEnabled = false
                binding.btnDelete.alpha = 0.5f
            } else {
                binding.tvDefaultBadge.visibility = android.view.View.GONE
                binding.btnDelete.isEnabled = true
                binding.btnDelete.alpha = 1.0f
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(category)
            }
        }
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }
}
