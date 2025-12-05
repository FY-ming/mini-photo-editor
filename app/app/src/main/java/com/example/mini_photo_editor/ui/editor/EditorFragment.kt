package com.example.mini_photo_editor.ui.editor

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import com.example.mini_photo_editor.R
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.mini_photo_editor.ui.editor.opengl.GLRenderer
import androidx.navigation.fragment.findNavController
import com.example.mini_photo_editor.ui.export.ExportFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.math.sqrt
import kotlin.math.min
import kotlin.math.max

/**
 * 编辑器全屏 DialogFragment
 * - 使用 GLSurfaceView + GLRenderer 实现照片预览与操作
 * - 支持裁剪、平移、缩放、重置等功能
 */
class EditorFragment : DialogFragment(R.layout.fragment_editor) {

    // -----------------------------
    // OpenGL 渲染相关
    // -----------------------------
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var glRenderer: GLRenderer
    private var currentBitmap: Bitmap? = null

    // -----------------------------
    // 裁剪框相关
    // -----------------------------
    private lateinit var cropOverlay: CropOverlayView
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isScaling = false
    private var startDistance = 0f
    private var cropRect: Rect? = null     // 当前裁剪框区域（像素坐标）

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)         // 设置全屏 Dialog 样式

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // -----------------------------
        // 顶部工具栏适配刘海/状态栏
        // -----------------------------
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.fitsSystemWindows = true
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(0, top, 0, 0)
            insets
        }

        // 设置顶部工具栏按钮
        setupTopToolbar(view)
        // 设置底部工具按钮
        setupBottomTools(view)

        // 初始化 OpenGL
        initOpenGL(view)
        // 加载传入的图片
        loadAndDisplayImage()

        // 初始化裁剪框，设置监听
        cropOverlay = view.findViewById(R.id.crop_overlay)
        // 监听器
        cropOverlay.setOnCropConfirmListener { rectViewCoords ->
            // rectViewCoords 是 View 坐标系（左/上/右/下）——把它转成 Bitmap 像素坐标并裁剪
            setCropRectFromView(rectViewCoords.left.toFloat(), rectViewCoords.top.toFloat(), rectViewCoords.right.toFloat(), rectViewCoords.bottom.toFloat())

            // 执行裁剪并更新预览
            applyCrop()

            // 隐藏裁剪 overlay
            cropOverlay.hide()
        }

        cropOverlay.setOnCropCancelListener {
            cropOverlay.hide()
        }

        // -----------------------------
        // 添加触摸事件监听器（平移 + 缩放）
        // -----------------------------
        setupTouchListener()
    }

    /**
     * 将裁剪框 View 坐标转换为 Bitmap 像素坐标
     */
    private fun setCropRectFromView(viewLeft: Float, viewTop: Float, viewRight: Float, viewBottom: Float) {
        val bitmap = currentBitmap ?: run {
            println("⚠️ setCropRectFromView: currentBitmap 为 null")
            return
        }

        // 尝试使用 GLRenderer 精确映射
        try {
            // 四个角点在 view 坐标转换为 bitmap 像素
            val p1 = glRenderer.viewPointToBitmapPixel(viewLeft, viewTop, glSurfaceView.width, glSurfaceView.height)
            val p2 = glRenderer.viewPointToBitmapPixel(viewRight, viewTop, glSurfaceView.width, glSurfaceView.height)
            val p3 = glRenderer.viewPointToBitmapPixel(viewLeft, viewBottom, glSurfaceView.width, glSurfaceView.height)
            val p4 = glRenderer.viewPointToBitmapPixel(viewRight, viewBottom, glSurfaceView.width, glSurfaceView.height)

            if (p1 != null && p2 != null && p3 != null && p4 != null) {
                val xs = listOf(p1.first, p2.first, p3.first, p4.first)
                val ys = listOf(p1.second, p2.second, p3.second, p4.second)

                val left = xs.minOrNull() ?: 0
                val right = xs.maxOrNull() ?: bitmap.width
                val top = ys.minOrNull() ?: 0
                val bottom = ys.maxOrNull() ?: bitmap.height

                // 确保在边界内
                val l = left.coerceIn(0, bitmap.width - 1)
                val t = top.coerceIn(0, bitmap.height - 1)
                val r = right.coerceIn(l + 1, bitmap.width)
                val b = bottom.coerceIn(t + 1, bitmap.height)

                cropRect = Rect(l, t, r, b)
                println("➡️ （精确）转换后的裁剪像素坐标: $cropRect")
                return
            } else {
                println("⚠️ renderer 映射失败，退回线性映射")
            }
        } catch (e: Exception) {
            println("⚠️ renderer 映射异常: ${e.message}, 退回比例映射")
        }

        // 若精准映射失败->退回：简单按比例映射（兼容性备用）
        val viewWidth = glSurfaceView.width.toFloat()
        val viewHeight = glSurfaceView.height.toFloat()
        if (viewWidth <= 0 || viewHeight <= 0) {
            println("⚠️ GLSurfaceView 尺寸为 0，无法转换")
            return
        }

        val scaleX = bitmap.width.toFloat() / viewWidth
        val scaleY = bitmap.height.toFloat() / viewHeight

        val realLeft = (viewLeft * scaleX).toInt().coerceIn(0, bitmap.width - 1)
        val realTop = (viewTop * scaleY).toInt().coerceIn(0, bitmap.height - 1)
        val realRight = (viewRight * scaleX).toInt().coerceIn(1, bitmap.width)
        val realBottom = (viewBottom * scaleY).toInt().coerceIn(1, bitmap.height)

        val left = min(realLeft, realRight - 1)
        val top = min(realTop, realBottom - 1)
        val right = max(realRight, left + 1)
        val bottom = max(realBottom, top + 1)

        cropRect = Rect(left, top, right, bottom)
        println("➡️ （退回）转换后的裁剪像素坐标: $cropRect")
    }

    /**
     * 设置顶部工具栏按钮事件
     */
    private fun setupTopToolbar(view: View) {
        // 给容器设置点击事件
        view.findViewById<View>(R.id.btn_back_container).setOnClickListener {
            println("← 点击返回按钮")
            dismiss()
        }

        view.findViewById<View>(R.id.btn_save_container).setOnClickListener {
            println("💾 点击保存按钮")
            exportCurrentImage()
        }
    }

    /**
     * 设置底部工具按钮事件
     */
    private fun setupBottomTools(view: View) {
        // 裁剪按钮
        view.findViewById<View>(R.id.btn_crop).setOnClickListener {
            // 显示裁剪交互
            cropOverlay.show()
        }

        // 滤镜按钮
        view.findViewById<View>(R.id.btn_filter).setOnClickListener {
            showFilterTool()
        }

        // 文字按钮
        view.findViewById<View>(R.id.btn_text).setOnClickListener {
            showTextTool()
        }

        // 贴纸按钮
        view.findViewById<View>(R.id.btn_sticker).setOnClickListener {
            showStickerTool()
        }

        // 涂鸦按钮
        view.findViewById<View>(R.id.btn_draw).setOnClickListener {
            showDrawTool()
        }

        // 重置按钮（原来已定义，保持不变）
        view.findViewById<View>(R.id.btn_reset).setOnClickListener {
            println("🔄 用户点击重置按钮")
            glRenderer.resetTransform()
            glSurfaceView.requestRender()
        }
    }

    // 以下是各个工具的功能实现/占位符
    /**
     * 执行裁剪操作
     */
    private fun applyCrop() {
        val sourceBitmap = currentBitmap ?: return
        val rect = cropRect ?: run {
            println("⚠️ applyCrop: 未设置 cropRect")
            return
        }

        // 在后台线程做 Bitmap.createBitmap（可能会占用时间）
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            try {
                val croppedBitmap = BitmapCropper.crop(sourceBitmap, rect)
                println("✂️ 裁剪完成（后台线程）: ${croppedBitmap.width}x${croppedBitmap.height}")

                // 更新 currentBitmap（主线程）
                withContext(Dispatchers.Main) {
                    // 可安全回收旧 bitmap（如果需要）
                    // sourceBitmap.recycle() // 仅在你确定不会再使用原图时回收
                    currentBitmap = croppedBitmap
                }

                // 在 GL 线程替换纹理并触发渲染
                glSurfaceView.queueEvent {
                    try {
                        glRenderer.replaceBitmapOnGLThread(croppedBitmap)
                    } catch (e: Exception) {
                        println("❌ GL 替换纹理失败: ${e.message}")
                    }
                }

                // 请求主线程渲染（确保 UI 刷新）
                withContext(Dispatchers.Main) {
                    glSurfaceView.requestRender()
                }
            } catch (e: Exception) {
                println("❌ 裁剪异常: ${e.message}")
            }
        }
    }

    // -----------------------------
    // 占位工具函数（滤镜、文字、贴纸、涂鸦）
    // -----------------------------
    private fun showFilterTool() {
        println("🎨 显示滤镜工具")
        // TODO: 实现滤镜功能
        // 1. 显示滤镜列表
        // 2. 应用滤镜效果
        // 3. 实时预览
    }

    private fun showTextTool() {
        println("T 显示文字工具")
        // TODO: 实现文字功能
        // 1. 显示文字输入框
        // 2. 字体、颜色、大小选择
        // 3. 文字位置调整
    }

    private fun showStickerTool() {
        println("😊 显示贴纸工具")
        // TODO: 实现贴纸功能
        // 1. 显示贴纸库
        // 2. 贴纸拖拽、缩放
        // 3. 贴纸图层管理
    }

    private fun showDrawTool() {
        println("🖌️ 显示涂鸦工具")
        // TODO: 实现涂鸦功能
        // 1. 画笔选择（粗细、颜色）
        // 2. 画布绘制
        // 3. 撤销/重做
    }

    /**
     * 设置触摸事件（单指平移 + 双指缩放）
     */
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

    /**
     * 初始化 OpenGL
     */
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

    /**
     * 加载图片并显示
     */
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

    /**
     * 导出当前 Bitmap 到临时文件，并弹出 ExportFragment
     */
    private fun exportCurrentImage() {
        currentBitmap?.let { bitmap ->
            val tempFile = File(requireContext().cacheDir, "temp_crop_${System.currentTimeMillis()}.jpg")
            FileOutputStream(tempFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            }

            val exportDialog = ExportFragment.newInstance(tempFile.absolutePath)
            exportDialog.show(parentFragmentManager, "export_dialog")
        }
    }

}