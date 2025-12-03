package com.example.mini_photo_editor.ui.home.data

data class BannerItem(
    val id: Int,
    val title: String,
    val imageRes: Int, // 或者使用图片URL
    val type: BannerType = BannerType.INSPIRATION
)

enum class BannerType {
    INSPIRATION, // 发现灵感
    TUTORIAL,    // 教程
    TEMPLATE     // 模板
}