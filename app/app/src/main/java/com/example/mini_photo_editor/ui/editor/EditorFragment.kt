package com.example.mini_photo_editor.ui.editor

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.mini_photo_editor.R
import com.google.android.material.appbar.MaterialToolbar
import androidx.core.net.toUri

class EditorFragment : DialogFragment(R.layout.fragment_editor) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog)
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
            // TODO: 在这里显示图片
        }
    }
}