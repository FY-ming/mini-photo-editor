package com.example.mini_photo_editor.ui.home.data

import com.example.mini_photo_editor.R

data class RecentEditItem(
    val id: String,
    val name: String,
    val time: String,
    val imageRes: Int = R.drawable.ic_photo // 默认图标
)