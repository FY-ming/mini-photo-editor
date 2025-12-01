package com.example.mini_photo_editor.ui.gallery

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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

class GalleryFragment : DialogFragment(R.layout.fragment_gallery) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MediaAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            println("✅ 用户授予了存储权限")
            loadMediaData()
        } else {
            println("❌ 用户拒绝了存储权限，使用测试数据")
            loadTestDataAsFallback()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化视图
        initViews(view)
        setupRecyclerView()

        // 检查权限并加载数据
        checkPermissionsAndLoadData()
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

    private fun checkPermissionsAndLoadData() {
        println("🔐 checkPermissionsAndLoadData:检查存储权限")

        if (hasReadStoragePermission()) {
            println("✅ checkPermissionsAndLoadData:已有存储权限，加载真实数据")
            loadMediaData()
        } else {
            println("🔐 checkPermissionsAndLoadData:请求存储权限")
            requestAppropriatePermission()
        }
    }
    private fun requestAppropriatePermission() {
        val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 使用 READ_MEDIA_IMAGES
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            // Android 10-12 使用 READ_EXTERNAL_STORAGE
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        println("📱 requestAppropriatePermission请求权限: $permissionToRequest")
        requestPermissionLauncher.launch(permissionToRequest)
    }

    private fun hasReadStoragePermission(): Boolean {
        val permissionToCheck = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        return ContextCompat.checkSelfPermission(
            requireContext(),
            permissionToCheck
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun loadTestDataAsFallback() {
        viewLifecycleOwner.lifecycleScope.launch {
            val testItems = MediaItem.createTestItems()
            adapter.submitList(testItems)
            println("🔄 loadTestDataAsFallback:使用测试数据作为回退方案")
        }
    }

    private fun loadMediaData() {
        viewLifecycleOwner.lifecycleScope.launch {
            println("🔄 loadMediaData:开始加载媒体数据...")

            // 先尝试加载真实数据
            val realItems = loadImagesFromMediaStore()
            println("📱 loadMediaData:真实数据加载完成: ${realItems.size} 项")

            if (realItems.isNotEmpty()) {
                // 如果有真实数据，就显示真实数据
                adapter.submitList(realItems)
                println("✅ loadMediaData:已显示真实数据")
            } else {
                // 如果没有真实数据，才回退到测试数据
                println("⚠️ loadMediaData:真实数据为空，使用测试数据")
                loadTestDataAsFallback()
            }
        }
    }

    private suspend fun loadImagesFromMediaStore(): List<MediaItem> = withContext(Dispatchers.IO) {
        // 这里先简单实现，后续再处理权限
        val mediaItems = mutableListOf<MediaItem>()

        println("🎯 loadImagesFromMediaStore:开始查询媒体库...")

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        println("🔍 loadImagesFromMediaStore:查询URI: $queryUri")

        try {
            context?.contentResolver?.query(
                queryUri,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                println("📊 loadImagesFromMediaStore:查询到 ${cursor.count} 条记录")

                while (cursor.moveToNext()) {
                    try {
                        val mediaItem = MediaItem.fromCursor(cursor)
                        mediaItems.add(mediaItem)
                        println("✅ loadImagesFromMediaStore:加载图片: ${mediaItem.displayName}")
                    } catch (e: Exception) {
                        println("❌ loadImagesFromMediaStore:解析媒体项失败: ${e.message}")
                    }
                }
            } ?: println("❌ loadImagesFromMediaStore:查询结果为空或失败")

        } catch (e: SecurityException) {
            println("🔐 loadImagesFromMediaStore:权限异常: ${e.message}")
        } catch (e: Exception) {
            println("💥 loadImagesFromMediaStore:查询异常: ${e.message}")
        }

        println("🎉 loadImagesFromMediaStore:最终加载了 ${mediaItems.size} 张图片")
        return@withContext mediaItems
    }

    private fun navigateToEditor(imageUri: Uri) {
        try {
            println("🚀 navigateToEditor:跳转到编辑器: $imageUri")

            val editorFragment = EditorFragment().apply {
                arguments = Bundle().apply {
                    putString("imageUri", imageUri.toString())
                }
            }

            // 回到原来的方式，但调整顺序
            editorFragment.show(parentFragmentManager, "editor_dialog")

            // 等一帧再关闭相册，避免看到主页
            view?.postDelayed({
                dismiss()
            }, 50) // 50ms足够

        } catch (e: Exception) {
            println("❌ 跳转失败: ${e.message}")
            dismiss()
        }
    }
}