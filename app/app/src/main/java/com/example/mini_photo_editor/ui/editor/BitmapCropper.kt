package com.example.mini_photo_editor.ui.editor

import android.graphics.Bitmap
import android.graphics.Rect

object BitmapCropper {

    /**
     * 裁剪Bitmap
     * @param bitmap 原图
     * @param cropRect 需要裁剪的矩形区域（像素坐标）
     * @return 裁剪后的Bitmap
     */
    fun crop(bitmap: Bitmap, cropRect: Rect): Bitmap {
        return Bitmap.createBitmap(
            bitmap,
            cropRect.left,
            cropRect.top,
            cropRect.width(),
            cropRect.height()
        )
    }
}
