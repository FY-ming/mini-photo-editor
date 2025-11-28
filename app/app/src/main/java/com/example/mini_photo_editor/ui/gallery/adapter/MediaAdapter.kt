package com.example.mini_photo_editor.ui.gallery.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.mini_photo_editor.data.model.MediaItem
import com.example.mini_photo_editor.R

class MediaAdapter : RecyclerView.Adapter<MediaAdapter.ViewHolder>() {

    private var items: List<MediaItem> = emptyList()
    var onItemClick: ((MediaItem) -> Unit)? = null

    fun submitList(newItems: List<MediaItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 这里需要图片项的布局文件
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.iv_gallery_image)

        fun bind(item: MediaItem) {
            // TODO: 使用Coil加载图片
            // 临时显示一个占位图
            imageView.setImageResource(R.drawable.ic_photo)

            itemView.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }
}