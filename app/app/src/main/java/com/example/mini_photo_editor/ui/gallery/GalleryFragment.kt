package com.example.mini_photo_editor.ui.gallery

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.example.mini_photo_editor.R
import com.example.mini_photo_editor.data.model.MediaItem
import com.example.mini_photo_editor.ui.gallery.adapter.MediaAdapter

class GalleryFragment : DialogFragment(R.layout.fragment_gallery) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MediaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化视图
        initViews(view)
        setupRecyclerView()
        loadTestData()
    }

    private fun initViews(view: View) {
        // 设置返回按钮
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            dismiss()
        }

        // 获取 RecyclerView
        recyclerView = view.findViewById(R.id.rv_gallery)
    }

    private fun setupRecyclerView() {
        adapter = MediaAdapter()

        // 设置图片点击回调
        adapter.onItemClick = { mediaItem ->
            // 选择图片后跳转到编辑器
            navigateToEditor(mediaItem.uri)
        }

        recyclerView.adapter = adapter
    }

    private fun loadTestData() {
        val testItems = MediaItem.createTestItems()
        adapter.submitList(testItems)
    }

    private fun navigateToEditor(imageUri: Uri) {
        // TODO: 跳转到编辑器页
        println("选择了图片: $imageUri")
        dismiss()  // 关闭相册对话框
    }
}