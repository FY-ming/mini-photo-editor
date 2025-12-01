package com.example.mini_photo_editor.ui.editor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.mini_photo_editor.R
import com.google.android.material.appbar.MaterialToolbar
import androidx.core.net.toUri
import com.example.mini_photo_editor.ui.editor.opengl.GLRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL

class EditorFragment : DialogFragment(R.layout.fragment_editor) {
//    临时处理图像视图
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
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
//
//            // 临时方案：使用 ImageView 显示图片，跳过 OpenGL
//            val imageView = view.findViewById<ImageView>(R.id.iv_editor_preview) // 确保布局中有这个ImageView
//            if (imageView != null) {
//                Glide.with(this)
//                    .load(imageUri)
//                    .into(imageView)
//                println("✅ 图片已加载到 ImageView")
//            } else {
//                println("❌ 找不到 ImageView，请检查布局文件")
//            }
//        } else {
//            println("❌ 没有接收到图片URI")
//        }
//    }
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var glRenderer: GLRenderer
    private var currentBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置返回按钮
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            dismiss()
        }

        // 初始化 OpenGL
        initOpenGL(view)

//        // 接收图片URI（暂时先打印日志）
//        val imageUriString = arguments?.getString("imageUri")
//        if (!imageUriString.isNullOrEmpty()) {
//            val imageUri = imageUriString.toUri()
//            println("🎨 编辑器收到图片: $imageUri")
//            println("⚠️  OpenGL 画布已初始化，但图片渲染待实现")
//        }
        // 加载并显示图片
        loadAndDisplayImage()
    }

    private fun initOpenGL(view: View) {
        println("🚀 初始化 OpenGL ES")

        glSurfaceView = view.findViewById(R.id.gl_surface_view)
        glRenderer = GLRenderer()

        // 配置 OpenGL ES 3.0
        glSurfaceView.setEGLContextClientVersion(3)
        glSurfaceView.setRenderer(glRenderer)

        // 设置为持续渲染模式（调试用，后续可以改为按需渲染）
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        println("✅ OpenGL ES 初始化完成")
    }

    private fun loadAndDisplayImage() {
        val imageUriString = arguments?.getString("imageUri")
        if (!imageUriString.isNullOrEmpty()) {
            val imageUri = imageUriString.toUri()
            println("🎨 开始加载图片: $imageUri")

            // 在后台线程加载图片
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    // 从 URI 加载 Bitmap
                    val bitmap = if (imageUri.scheme == "content") {
                        // 对于 content:// URI，使用 ContentResolver
                        requireContext().contentResolver.openInputStream(imageUri)?.use { inputStream ->
                            BitmapFactory.decodeStream(inputStream)
                        }
                    } else {
                        // 对于其他 URI（如文件、网络）
                        BitmapFactory.decodeStream(URL(imageUri.toString()).openStream())
                    }

                    bitmap?.let {
                        println("✅ 图片加载成功: ${it.width}x${it.height}")

                        // 切换到主线程更新渲染器
                        launch(Dispatchers.Main) {
                            currentBitmap = it
                            glRenderer.setBitmap(it)
                            glSurfaceView.requestRender() // 请求重绘
                        }
                    } ?: run {
                        println("❌ 图片加载失败: Bitmap 为 null")
                    }

                } catch (e: Exception) {
                    println("❌ 图片加载异常: ${e.message}")
                    e.printStackTrace()
                }
            }
        } else {
            println("⚠️ 没有接收到图片URI")
        }
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView?.onResume()
        println("▶️ 编辑器恢复")
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView?.onPause()
        println("⏸️ 编辑器暂停")
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清理 Bitmap 资源
        currentBitmap?.recycle()
        currentBitmap = null
        println("🗑️ 编辑器销毁，资源已清理")
    }
}