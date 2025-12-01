package com.example.mini_photo_editor.ui.editor

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.example.mini_photo_editor.R
import com.google.android.material.appbar.MaterialToolbar
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.mini_photo_editor.ui.editor.opengl.GLRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL
import kotlin.math.sqrt

class EditorFragment : DialogFragment(R.layout.fragment_editor) {
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var glRenderer: GLRenderer
    private var currentBitmap: Bitmap? = null

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isScaling = false
    private var startDistance = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
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

        loadAndDisplayImage()

        // 设置重置按钮
        view.findViewById<Button>(R.id.btn_reset).setOnClickListener {
            println("🔄 用户点击重置按钮")
            glRenderer.resetTransform()
            glSurfaceView.requestRender()
        }

        // 添加触摸监听
        setupTouchListener()
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        glSurfaceView.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    // 单指按下
                    lastTouchX = event.x
                    lastTouchY = event.y
                    true
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    // 第二根手指按下，开始缩放
                    if (event.pointerCount == 2) {
                        isScaling = true
                        startDistance = getFingerDistance(event)
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isScaling && event.pointerCount == 2) {
                        // 双指缩放
                        val currentDistance = getFingerDistance(event)
                        val scaleFactor = currentDistance / startDistance

                        glRenderer.scale(scaleFactor)
                        glSurfaceView.requestRender()

                        startDistance = currentDistance
                    } else if (!isScaling && event.pointerCount == 1) {
                        // 单指平移
                        val dx = (event.x - lastTouchX) / glSurfaceView.width * 2
                        val dy = (event.y - lastTouchY) / glSurfaceView.height * 2

                        glRenderer.translate(dx, -dy) // -dy更正竖直方向照片平移方向
                        glSurfaceView.requestRender()

                        lastTouchX = event.x
                        lastTouchY = event.y
                    }
                    true
                }

                MotionEvent.ACTION_POINTER_UP -> {
                    // 一根手指抬起，结束缩放
                    if (event.pointerCount == 2) {
                        isScaling = false
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    // 所有手指抬起
                    isScaling = false
                    v.performClick()  // 关键修复
                    true
                }

                else -> false
            }
        }
    }

    private fun getFingerDistance(event: MotionEvent): Float {
        val dx = event.getX(0) - event.getX(1)
        val dy = event.getY(0) - event.getY(1)
        return sqrt(dx * dx + dy * dy)
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
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
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
        glSurfaceView.onResume()
        println("▶️ 编辑器恢复")
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
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