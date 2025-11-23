package com.example.mini_photo_editor.data.model

import android.net.Uri

/**
 * 媒体项数据模型
 * 目前包含基础字段，为相册功能做准备
 */
data class MediaItem(
    val id: Long = 0,                    // 媒体ID
    val uri: Uri = Uri.EMPTY,            // 媒体URI
    val displayName: String = "",        // 显示名称
    val dateAdded: Long = 0,             // 添加时间
    val isVideo: Boolean = false         // 是否为视频
) {
    companion object {
        /**
         * 创建测试用的MediaItem
         */
        fun createTestItems(): List<MediaItem> {
            return listOf(
                MediaItem(
                    id = 1,
                    displayName = "测试图片1",
                    dateAdded = System.currentTimeMillis()
                ),
                MediaItem(
                    id = 2,
                    displayName = "测试图片2",
                    dateAdded = System.currentTimeMillis() - 1000
                ),
                MediaItem(
                    id = 3,
                    displayName = "测试图片3",
                    dateAdded = System.currentTimeMillis() - 2000
                )
            )
        }
    }
}