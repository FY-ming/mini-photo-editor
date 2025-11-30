package com.example.mini_photo_editor.ui.gallery.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mini_photo_editor.R
import com.example.mini_photo_editor.data.model.MediaItem

class MediaAdapter : ListAdapter<MediaItem, MediaAdapter.ViewHolder>(DIFF_CALLBACK) {

    var onItemClick: ((MediaItem) -> Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_thumbnail)
        val textView: TextView = itemView.findViewById(R.id.tv_name)

        // 移除了 init 中的点击事件，将在 onBindViewHolder 中设置
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        // 设置图片名称
        holder.textView.text = item.displayName

        // 使用 Glide 加载图片缩略图
        Glide.with(holder.itemView.context)
            .load(item.uri)
            .override(200, 200)
            .centerCrop()
            .into(holder.imageView)

        // 设置点击事件 - 这种方式更可靠
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }

        println("🖼️ Adapter绑定位置 $position: ${item.displayName}")
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MediaItem>() {
            override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
                return oldItem.uri == newItem.uri && oldItem.displayName == newItem.displayName
            }
        }
    }
}