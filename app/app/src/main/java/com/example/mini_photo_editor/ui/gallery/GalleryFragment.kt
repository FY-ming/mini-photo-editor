package com.example.mini_photo_editor.ui.gallery

import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.example.mini_photo_editor.R
import com.example.mini_photo_editor.data.model.MediaItem
import com.example.mini_photo_editor.ui.editor.EditorFragment
import com.example.mini_photo_editor.ui.gallery.adapter.MediaAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.jar.Manifest

class GalleryFragment : DialogFragment(R.layout.fragment_gallery) {

    private companion object {
        private const val READ_EXTERNAL_STORAGE_REQUEST = 100
    }

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

        // 加载测试用例
//      loadTestData()
        // 加载媒体照片
//      loadMediaData()

        // 检查权限并加载数据
        checkPermissionsAndLoadData()
    }

            private fun checkPermissionsAndLoadData() {
                if (hasReadStoragePermission()) {
                    loadMediaData()
                } else {
                    requestReadStoragePermission()
                }
            }

            private fun hasReadStoragePermission(): Boolean {
                return ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }

            private fun requestReadStoragePermission() {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_EXTERNAL_STORAGE_REQUEST
                )
            }

            override fun onRequestPermissionsResult(
                requestCode: Int,
                permissions: Array<out String>,
                grantResults: IntArray
            ) {
                if (requestCode == READ_EXTERNAL_STORAGE_REQUEST) {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        loadMediaData()
                    } else {
                        println("❌ 用户拒绝了存储权限")
                        // 可以显示一个提示或者使用测试数据
                        loadTestDataAsFallback()
                    }
                }
            }

            private fun loadTestDataAsFallback() {
                viewLifecycleOwner.lifecycleScope.launch {
                    val testItems = MediaItem.createTestItems()
                    adapter.submitList(testItems)
                    println("🔄 使用测试数据作为回退方案")
                }
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

        // 设置网格布局
        recyclerView.layoutManager = GridLayoutManager(context, 3) // 3列网格

        // 设置点击回调
        adapter.onItemClick = { mediaItem ->
            println("📸 选择了图片: ${mediaItem.displayName}")
            navigateToEditor(mediaItem.uri)
        }

        recyclerView.adapter = adapter
    }

    private fun loadMediaData() {
        viewLifecycleOwner.lifecycleScope.launch {
            println("🔄 开始加载媒体数据...")

            // 先尝试加载真实数据
            val realItems = loadImagesFromMediaStore()
            println("📱 真实数据加载完成: ${realItems.size} 项")

            if (realItems.isNotEmpty()) {
                // 如果有真实数据，就显示真实数据
                adapter.submitList(realItems)
                println("✅ 已显示真实数据")
            } else {
                // 如果没有真实数据，才回退到测试数据
                val testItems = MediaItem.createTestItems()
                adapter.submitList(testItems)
                println("🔄 没有真实数据，使用测试数据: ${testItems.size} 项")
            }
        }
    }

    private suspend fun loadImagesFromMediaStore(): List<MediaItem> = withContext(Dispatchers.IO) {
        // 这里先简单实现，后续再处理权限
        val mediaItems = mutableListOf<MediaItem>()

        println("🎯 开始查询媒体库...")

        val projection = arrayOf(
            android.provider.MediaStore.Images.Media._ID,
            android.provider.MediaStore.Images.Media.DISPLAY_NAME,
            android.provider.MediaStore.Images.Media.DATE_ADDED
        )

        val sortOrder = "${android.provider.MediaStore.Images.Media.DATE_ADDED} DESC"

        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

//        context?.contentResolver?.query(
//            queryUri,
//            projection,
//            null,  // selection
//            null,  // selectionArgs
//            sortOrder
//        )?.use { cursor ->
//            while (cursor.moveToNext()) {
//                try {
//                    val mediaItem = MediaItem.fromCursor(cursor)
//                    mediaItems.add(mediaItem)
//                } catch (e: Exception) {
//                    println("解析媒体项失败: ${e.message}")
//                }
//            }
//        }
//
//        return@withContext mediaItems
        println("🔍 查询URI: $queryUri")

        try {
            context?.contentResolver?.query(
                queryUri,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                println("📊 查询到 ${cursor.count} 条记录")

                while (cursor.moveToNext()) {
                    try {
                        val mediaItem = MediaItem.fromCursor(cursor)
                        mediaItems.add(mediaItem)
                        println("✅ 加载图片: ${mediaItem.displayName}")
                    } catch (e: Exception) {
                        println("❌ 解析媒体项失败: ${e.message}")
                    }
                }
            } ?: println("❌ 查询结果为空或失败")

        } catch (e: SecurityException) {
            println("🔐 权限异常: ${e.message}")
        } catch (e: Exception) {
            println("💥 查询异常: ${e.message}")
        }

        println("🎉 最终加载了 ${mediaItems.size} 张图片")
        return@withContext mediaItems
    }

    private fun navigateToEditor(imageUri: Uri) {
        try {
            // 1. 创建编辑器对话框
            val editorFragment = EditorFragment().apply {
                // 传递图片URI
                arguments = Bundle().apply {
                    putString("imageUri", imageUri.toString())
                }
            }

            // 2. 先关闭相册对话框
            dismiss()

            // 3. 显示编辑器对话框
            editorFragment.show(parentFragmentManager, "editor_dialog")

        } catch (e: Exception) {
            println("跳转失败: ${e.message}")
            dismiss() // 确保相册对话框关闭
        }
    }
}