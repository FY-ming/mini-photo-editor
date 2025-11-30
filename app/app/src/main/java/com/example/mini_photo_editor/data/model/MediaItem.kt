package com.example.mini_photo_editor.data.model

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toUri

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
                    uri = "content://media/external/images/media/1".toUri(),
                    displayName = "测试图片1",
                    dateAdded = System.currentTimeMillis()
                ),
                MediaItem(
                    id = 2,
                    uri = "content://media/external/images/media/2".toUri(),
                    displayName = "测试图片2",
                    dateAdded = System.currentTimeMillis() - 1000
                ),
                MediaItem(
                    id = 3,
                    uri = "content://media/external/images/media/3".toUri(),
                    displayName = "测试图片3",
                    dateAdded = System.currentTimeMillis() - 2000
                )
            )
        }

        /**
         * 从数据库Cursor创建MediaItem对象
         */
        fun fromCursor(cursor: Cursor): MediaItem {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
            val displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
            val dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED))

            return MediaItem(
                id = id,
                uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                ),
                displayName = displayName ?: "未命名",
                dateAdded = dateAdded
            )
        }
    }
}