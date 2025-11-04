package com.jksalcedo.passvault.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jksalcedo.passvault.R
import com.jksalcedo.passvault.data.PasswordEntry
import java.text.DateFormat
import java.util.Date

class PVAdapter : RecyclerView.Adapter<PVAdapter.VH>() {

    private var items: List<PasswordEntry> = emptyList()

    var onItemClick: ((PasswordEntry) -> Unit)? = null

    fun submitList(list: List<PasswordEntry>?) {
        items = list ?: emptyList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_password_entry, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick?.invoke(item) }
        notifyItemInserted(position)
    }

    override fun getItemCount(): Int = items.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val tvUpdatedAt: TextView = itemView.findViewById(R.id.tvUpdatedAt)

        fun bind(entry: PasswordEntry) {
            tvTitle.text = entry.title
            tvUsername.text = entry.username ?: ""
            val dateText = DateFormat.getDateInstance().format(Date(entry.updatedAt))
            tvUpdatedAt.text = buildString {
                append(dateText)
            }
        }
    }
}