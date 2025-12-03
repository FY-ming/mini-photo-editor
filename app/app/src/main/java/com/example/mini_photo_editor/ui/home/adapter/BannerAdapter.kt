package com.example.mini_photo_editor.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mini_photo_editor.R
import com.example.mini_photo_editor.ui.home.customview.ShineEffectView
import com.example.mini_photo_editor.ui.home.data.BannerItem
import com.example.mini_photo_editor.ui.home.data.BannerType
import com.example.mini_photo_editor.ui.home.data.MediaType

class BannerAdapter : RecyclerView.Adapter<BannerAdapter.ViewHolder>() {

    private var items: List<BannerItem> = emptyList()
    var onItemClick: ((BannerItem) -> Unit)? = null

    fun submitList(newItems: List<BannerItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 照片视图
        val ivBanner: ImageView = itemView.findViewById(R.id.iv_banner)

        // 类型标签、标题、描述
        val tvType: TextView = itemView.findViewById(R.id.tv_type)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvDesc: TextView = itemView.findViewById(R.id.tv_desc)

        // 视频视图
        val videoThumbView: AppCompatImageView = itemView.findViewById(R.id.iv_video_thumb)
        val playButton: ImageView = itemView.findViewById(R.id.iv_play)
        // 扫光视图
        val shineView: ShineEffectView = itemView.findViewById(R.id.shine_view)

        // 在ViewHolder类内部绑定数据
        fun bind(item: BannerItem) {
            // 使用Glide加载图片（自动识别GIF）
            Glide.with(itemView.context)
                .load(item.imageRes)
                .into(ivBanner)

            // 设置图片图片、标题、描述信息
            ivBanner.setImageResource(item.imageRes)
            tvTitle.text = item.title
            tvDesc.text = item.description

            // 根据类型设置不同标题的样式
            val textColor = when (item.type) {
                BannerType.PHOTO -> R.color.white
                BannerType.GIF -> R.color.primary_blue
                BannerType.VIDEO -> R.color.accent
            }
            tvTitle.setTextColor(ContextCompat.getColor(itemView.context, textColor))

            // 根据BannerType设置类型标签
            tvType.text = when (item.type) {
                BannerType.PHOTO -> "类型：照片"
                BannerType.GIF -> "类型：GIF"
                BannerType.VIDEO -> "类型：视频"
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_banner_item_with_shine, parent, false)
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

        // 设置扫光颜色和速度
        holder.shineView.setShineColor(item.shineColor)
        holder.shineView.setShineSpeed(item.shineSpeed)

        // 启动动画
        holder.shineView.startAnimation()

        // 根据媒体类型显示不同的界面
        when (item.mediaType) {
            MediaType.IMAGE -> {
                // 图片：显示图片视图，隐藏视频视图
                holder.ivBanner.visibility = View.VISIBLE
                holder.videoThumbView.visibility = View.GONE
                holder.playButton.visibility = View.GONE
            }
            MediaType.GIF -> {
                // GIF：可以显示GIF或使用占位图
                holder.ivBanner.visibility = View.VISIBLE
                holder.videoThumbView.visibility = View.GONE
                holder.playButton.visibility = View.GONE
            }
            MediaType.VIDEO -> {
                // 视频：显示视频缩略图
                holder.ivBanner.visibility = View.GONE
                holder.videoThumbView.visibility = View.VISIBLE
                holder.playButton.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int = items.size
}