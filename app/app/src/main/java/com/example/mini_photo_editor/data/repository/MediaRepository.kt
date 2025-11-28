package com.example.mini_photo_editor.data.repository

import android.content.Context
import com.example.mini_photo_editor.data.model.MediaItem

class MediaRepository(private val context: Context) {
    suspend fun loadGalleryImages(): List<MediaItem> {
        // MediaStore基础查询实现
        return MediaItem.createTestItems()
    }
}