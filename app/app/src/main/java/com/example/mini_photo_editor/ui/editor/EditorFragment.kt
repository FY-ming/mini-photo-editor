package com.example.mini_photo_editor.ui.editor

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.example.mini_photo_editor.R
import com.google.android.material.appbar.MaterialToolbar
import androidx.core.net.toUri
import com.bumptech.glide.Glide

class EditorFragment : DialogFragment(R.layout.fragment_editor) {

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // 设置返回按钮
//        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
//        toolbar.setNavigationOnClickListener {
//            dismiss() // 关闭编辑器，返回主页
//        }
//
//        // 接收并处理图片
//        val imageUriString = arguments?.getString("imageUri")
//        if (!imageUriString.isNullOrEmpty()) {
//            val imageUri = imageUriString.toUri()
//            println("编辑器加载图片: $imageUri")
//            // TODO: 在这里显示图片
//        }
//    }
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置返回按钮
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            dismiss() // 关闭编辑器，返回主页
        }

        // 接收并处理图片
        val imageUriString = arguments?.getString("imageUri")
        if (!imageUriString.isNullOrEmpty()) {
            val imageUri = imageUriString.toUri()
            println("编辑器加载图片: $imageUri")

            // 临时方案：使用 ImageView 显示图片，跳过 OpenGL
            val imageView = view.findViewById<ImageView>(R.id.iv_editor_preview) // 确保布局中有这个ImageView
            if (imageView != null) {
                Glide.with(this)
                    .load(imageUri)
                    .into(imageView)
                println("✅ 图片已加载到 ImageView")
            } else {
                println("❌ 找不到 ImageView，请检查布局文件")
            }
        } else {
            println("❌ 没有接收到图片URI")
        }
    }
}