package com.example.mini_photo_editor.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mini_photo_editor.R
import com.example.mini_photo_editor.ui.home.data.BannerItem
import com.example.mini_photo_editor.ui.home.data.BannerType

class BannerAdapter : RecyclerView.Adapter<BannerAdapter.ViewHolder>() {

    private var items: List<BannerItem> = emptyList()
    var onItemClick: ((BannerItem) -> Unit)? = null

    fun submitList(newItems: List<BannerItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivBanner: ImageView = itemView.findViewById(R.id.iv_banner)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_banner_title)

        // 在ViewHolder类内部绑定数据
        fun bind(item: BannerItem) {
            // 设置图片
            ivBanner.setImageResource(item.imageRes)
            tvTitle.text = item.title

            // 根据类型设置不同的样式
            val textColor = when (item.type) {
                BannerType.INSPIRATION -> R.color.white
                BannerType.TUTORIAL -> R.color.primary_blue
                BannerType.TEMPLATE -> R.color.accent
            }
            tvTitle.setTextColor(ContextCompat.getColor(itemView.context, textColor))
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_banner_item, parent, false)
        return ViewHolder(view).apply {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(items[position])
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item) // 调用ViewHolder的bind方法
    }

    override fun getItemCount(): Int = items.size
}