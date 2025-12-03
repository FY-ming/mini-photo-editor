package com.example.mini_photo_editor.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mini_photo_editor.databinding.ItemRecentEditBinding
import com.example.mini_photo_editor.ui.home.data.RecentEditItem

class RecentEditAdapter : ListAdapter<RecentEditItem, RecentEditAdapter.ViewHolder>(DiffCallback()) {

    var onItemClick: ((RecentEditItem) -> Unit)? = null

    class ViewHolder(
        private val binding: ItemRecentEditBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RecentEditItem) {
            binding.tvTime.text = item.time
            binding.ivPreview.setImageResource(item.imageRes)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentEditBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding).apply {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(getItem(position))
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class DiffCallback : DiffUtil.ItemCallback<RecentEditItem>() {
    override fun areItemsTheSame(oldItem: RecentEditItem, newItem: RecentEditItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: RecentEditItem, newItem: RecentEditItem): Boolean {
        return oldItem == newItem
    }
}