package com.example.mini_photo_editor.ui.home.data

import com.example.mini_photo_editor.R

sealed class MediaType {
    object IMAGE : MediaType()
    object GIF : MediaType()
    object VIDEO : MediaType()
}

enum class BannerType {
    PHOTO, // 图片
    GIF,    // GIF
    VIDEO     // 视频
}
data class BannerItem(
    val id: Int,
    val title: String,
    val description: String = "",
    val imageRes: Int = 0, // 本地资源
    val mediaUrl: String = "", // 网络URL
    val mediaType: MediaType = MediaType.IMAGE,
    val shineColor: Int = 0xFFFFFFFF.toInt(),
    val shineSpeed: Float = 1.0f,
    val type: BannerType = BannerType.PHOTO
) {
    companion object {
        fun createTutorialItems(): List<BannerItem> {
            return listOf(
                // 第1张：静态图片教程（白色扫光）
                BannerItem(
                    id = 1,
                    title = "基础图片+扫光",
                    description = "自定义view展示",
                    imageRes = R.drawable.banner_photo1,
                    mediaType = MediaType.IMAGE,
                    type = BannerType.PHOTO,
                    shineColor = 0xFFFFFFFF.toInt(),
                    shineSpeed = 3.0f
                ),
                // 第2张：静态图片教程（蓝色扫光）
                BannerItem(
                    id = 2,
                    title = "基础图片+扫光2",
                    description = "自定义view展示2",
                    imageRes = R.drawable.banner_photo2,
                    mediaType = MediaType.IMAGE,
                    type = BannerType.PHOTO,
                    shineColor = 0xFF2196F3.toInt(), // 蓝色
                    shineSpeed = 2.0f
                ),
                // 第3张：GIF效果展示（黄色扫光）
                BannerItem(
                    id = 3,
                    title = "GIF",
                    description = "多媒体GIF效果展示",
                    imageRes = R.drawable.gif1,
                    mediaType = MediaType.GIF,
                    type = BannerType.GIF,
                    shineColor = 0xFFFFC107.toInt(), // 黄色
                    shineSpeed = 1.0f
                ),
                // 第4张：视频教程（青色扫光）
                BannerItem(
                    id = 4,
                    title = "视频",
                    description = "多媒体视频展示",
                    mediaUrl = "https://example.com/tutorial.mp4",
                    mediaType = MediaType.VIDEO,
                    type = BannerType.VIDEO,
                    shineColor = 0xFF00BCD4.toInt(), // 青色
                    shineSpeed = 0.5f
                )
            )
        }
    }
}



//package com.example.mini_photo_editor.ui.home.data
//
//data class BannerItem(
//    val id: Int,
//    val title: String,
//    val imageRes: Int, // 或者使用图片URL
//    val type: BannerType = BannerType.INSPIRATION
//)
//
//enum class BannerType {
//    INSPIRATION, // 发现灵感
//    TUTORIAL,    // 教程
//    TEMPLATE     // 模板
//}