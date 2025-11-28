package com.example.mini_photo_editor.ui.gallery

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.google.android.material.appbar.MaterialToolbar
import com.example.mini_photo_editor.R

class GalleryFragment : DialogFragment(R.layout.fragment_gallery) {  // 改为DialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置对话框样式
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置返回按钮点击事件
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            dismiss()  // 关闭对话框，返回首页
        }

//        // 图片点击事件
//        adapter.onItemClick = { imageUri ->
//            // 选择图片后跳转到编辑器
//            val direction = GalleryFragmentDirections.actionGalleryToEditor(imageUri)
//            findNavController().navigate(direction)
//            dismiss()  // 关闭相册对话框
//        }
    }
}