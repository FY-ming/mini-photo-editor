package com.example.mini_photo_editor.ui.editor

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * 可拖拽、可缩放的裁剪框覆盖层
 *
 * - 返回 getCropRect() -> Rect (view 坐标, left/top/right/bottom)
 * - setVisible(true/false) 控制显示
 * - 支持拖拽角点、边缘与整体拖动
 * - 内置「取消」「确认」按钮（右上），通过回调传出结果
 */
class CropOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    // 绘制相关
    private val maskPaint = Paint().apply { color = Color.parseColor("#99000000") } // 半透明遮罩
    private val framePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density * 2
        isAntiAlias = true
    }
    private val handlePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val guidePaint = Paint().apply {
        color = Color.parseColor("#66FFFFFF")
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density
        isAntiAlias = true
    }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = resources.displayMetrics.density * 12
        isAntiAlias = true
    }
    private val confirmRectPaint = Paint().apply {
        color = Color.parseColor("#FF6200EE")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // 裁剪框（View 坐标系）
    private val cropRect = RectF()

    // 抓手半径（dp）
    private val handleRadius = resources.displayMetrics.density * 8

    // 最小尺寸（像素）
    private val minSize = (resources.displayMetrics.density * 56).toFloat()

    // 操作状态
    private enum class DragMode { NONE, MOVE, RESIZE_LT, RESIZE_RT, RESIZE_LB, RESIZE_RB, RESIZE_LEFT, RESIZE_TOP, RESIZE_RIGHT, RESIZE_BOTTOM }
    private var dragMode = DragMode.NONE
    private var lastX = 0f
    private var lastY = 0f

    // 右上角确认/取消按钮区域
    private val btnPadding = resources.displayMetrics.density * 8
    private val btnHeight = resources.displayMetrics.density * 28
    private val btnWidth = resources.displayMetrics.density * 72
    private val btnGap = resources.displayMetrics.density * 8

    // 监听回调
    private var confirmListener: ((Rect) -> Unit)? = null
    private var cancelListener: (() -> Unit)? = null

    fun setOnCropConfirmListener(listener: (Rect) -> Unit) {
        confirmListener = listener
    }

    fun setOnCropCancelListener(listener: () -> Unit) {
        cancelListener = listener
    }

    init {
        // 初始裁剪框延后到 onSizeChanged 中根据视图大小设置（居中，80% 宽度）
        isClickable = true
        isFocusable = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (cropRect.isEmpty) {
            val pad = w * 0.08f
            val left = pad
            val right = w - pad
            val cw = right - left
            val ch = cw * 0.75f // 默认 4:3 比例（可按需改）
            val top = (h - ch) / 2f
            val bottom = top + ch
            cropRect.set(left, top, right, bottom)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 绘制遮罩（四个矩形）
        val w = width.toFloat()
        val h = height.toFloat()
        // 上
        canvas.drawRect(0f, 0f, w, cropRect.top, maskPaint)
        // bottom
        canvas.drawRect(0f, cropRect.bottom, w, h, maskPaint)
        // left
        canvas.drawRect(0f, cropRect.top, cropRect.left, cropRect.bottom, maskPaint)
        // right
        canvas.drawRect(cropRect.right, cropRect.top, w, cropRect.bottom, maskPaint)

        // 绘制裁剪框边线
        canvas.drawRect(cropRect, framePaint)

        // 绘制 3x3 指示线
        val oneThirdW = cropRect.width() / 3f
        val oneThirdH = cropRect.height() / 3f
        for (i in 1..2) {
            canvas.drawLine(cropRect.left + oneThirdW * i, cropRect.top, cropRect.left + oneThirdW * i, cropRect.bottom, guidePaint)
            canvas.drawLine(cropRect.left, cropRect.top + oneThirdH * i, cropRect.right, cropRect.top + oneThirdH * i, guidePaint)
        }

        // 绘制四个抓手（圆点）
        val handles = arrayOf(
            Pair(cropRect.left, cropRect.top),      // LT
            Pair(cropRect.right, cropRect.top),     // RT
            Pair(cropRect.left, cropRect.bottom),   // LB
            Pair(cropRect.right, cropRect.bottom)   // RB
        )
        for ((x, y) in handles) {
            canvas.drawCircle(x, y, handleRadius, handlePaint)
        }

        // 绘制右上角按钮： 取消 | 应用
        val btnRight = width - btnPadding
        val confirmLeft = btnRight - btnWidth
        val cancelRight = confirmLeft - btnGap
        val cancelLeft = cancelRight - btnWidth
        val btnTop = btnPadding
        val btnBottom = btnTop + btnHeight

        // 取消按钮
        canvas.drawRect(cancelLeft, btnTop, cancelRight, btnBottom, confirmRectPaint.apply { alpha = 180 })
        canvas.drawText("取消", cancelLeft + btnWidth / 2 - textPaint.measureText("取消") / 2, btnTop + btnHeight / 2 + textPaint.textSize / 2 - 6, textPaint)

        // 确认按钮（高亮）
        canvas.drawRect(confirmLeft, btnTop, btnRight, btnBottom, confirmRectPaint)
        canvas.drawText("确定", confirmLeft + btnWidth / 2 - textPaint.measureText("确定") / 2, btnTop + btnHeight / 2 + textPaint.textSize / 2 - 6, textPaint)
    }

    // public: 获取裁剪区域（整数 Rect，View 坐标）
    fun getCropRect(): Rect {
        return Rect(cropRect.left.toInt(), cropRect.top.toInt(), cropRect.right.toInt(), cropRect.bottom.toInt())
    }

    fun show() {
        visibility = VISIBLE
    }

    fun hide() {
        visibility = GONE
    }

    // 辅助：检查点是否在圆抓手范围内
    private fun isInHandle(x: Float, y: Float, hx: Float, hy: Float): Boolean {
        val dx = x - hx
        val dy = y - hy
        return sqrt(dx * dx + dy * dy) <= handleRadius * 1.7f
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        // 先检测是否按到右上角的取消/确定按钮
        val btnRight = width - btnPadding
        val confirmLeft = btnRight - btnWidth
        val cancelRight = confirmLeft - btnGap
        val cancelLeft = cancelRight - btnWidth
        val btnTop = btnPadding
        val btnBottom = btnTop + btnHeight

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // 按到了取消/确认
                if (x in cancelLeft..cancelRight && y in btnTop..btnBottom) {
                    cancelListener?.invoke()
                    return true
                }
                if (x in confirmLeft..btnRight && y in btnTop..btnBottom) {
                    // 确认：回调裁剪矩形（整数）
                    confirmListener?.invoke(getCropRect())
                    return true
                }

                // 检查角点（优先）
                when {
                    isInHandle(x, y, cropRect.left, cropRect.top) -> dragMode = DragMode.RESIZE_LT
                    isInHandle(x, y, cropRect.right, cropRect.top) -> dragMode = DragMode.RESIZE_RT
                    isInHandle(x, y, cropRect.left, cropRect.bottom) -> dragMode = DragMode.RESIZE_LB
                    isInHandle(x, y, cropRect.right, cropRect.bottom) -> dragMode = DragMode.RESIZE_RB
                    // 检查边缘（宽度内）
                    x in (cropRect.left - handleRadius)..(cropRect.left + handleRadius) && y in cropRect.top..cropRect.bottom -> dragMode = DragMode.RESIZE_LEFT
                    x in (cropRect.right - handleRadius)..(cropRect.right + handleRadius) && y in cropRect.top..cropRect.bottom -> dragMode = DragMode.RESIZE_RIGHT
                    y in (cropRect.top - handleRadius)..(cropRect.top + handleRadius) && x in cropRect.left..cropRect.right -> dragMode = DragMode.RESIZE_TOP
                    y in (cropRect.bottom - handleRadius)..(cropRect.bottom + handleRadius) && x in cropRect.left..cropRect.right -> dragMode = DragMode.RESIZE_BOTTOM
                    // 整体移动（在裁剪区域内）
                    x in cropRect.left..cropRect.right && y in cropRect.top..cropRect.bottom -> dragMode = DragMode.MOVE
                    else -> dragMode = DragMode.NONE
                }

                lastX = x
                lastY = y
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = x - lastX
                val dy = y - lastY

                when (dragMode) {
                    DragMode.MOVE -> {
                        moveBy(dx, dy)
                    }
                    DragMode.RESIZE_LT -> {
                        resizeLeftTop(dx, dy)
                    }
                    DragMode.RESIZE_RT -> {
                        resizeRightTop(dx, dy)
                    }
                    DragMode.RESIZE_LB -> {
                        resizeLeftBottom(dx, dy)
                    }
                    DragMode.RESIZE_RB -> {
                        resizeRightBottom(dx, dy)
                    }
                    DragMode.RESIZE_LEFT -> resizeLeft(dx)
                    DragMode.RESIZE_RIGHT -> resizeRight(dx)
                    DragMode.RESIZE_TOP -> resizeTop(dy)
                    DragMode.RESIZE_BOTTOM -> resizeBottom(dy)
                    else -> {}
                }

                lastX = x
                lastY = y
                invalidate()
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                dragMode = DragMode.NONE
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    // 移动裁剪框（约束边界）
    private fun moveBy(dx: Float, dy: Float) {
        val w = width.toFloat()
        val h = height.toFloat()
        var left = cropRect.left + dx
        var top = cropRect.top + dy
        var right = cropRect.right + dx
        var bottom = cropRect.bottom + dy

        // 限制边界
        val shiftX = when {
            left < 0f -> -left
            right > w -> w - right
            else -> 0f
        }
        val shiftY = when {
            top < 0f -> -top
            bottom > h -> h - bottom
            else -> 0f
        }
        left += shiftX; right += shiftX
        top += shiftY; bottom += shiftY

        cropRect.set(left, top, right, bottom)
    }

    // 每种缩放策略（保证最小尺寸和不越界）
    private fun resizeLeft(dx: Float) {
        val newLeft = (cropRect.left + dx).coerceAtMost(cropRect.right - minSize).coerceAtLeast(0f)
        cropRect.left = newLeft
    }
    private fun resizeRight(dx: Float) {
        val w = width.toFloat()
        val newRight = (cropRect.right + dx).coerceAtLeast(cropRect.left + minSize).coerceAtMost(w)
        cropRect.right = newRight
    }
    private fun resizeTop(dy: Float) {
        val newTop = (cropRect.top + dy).coerceAtMost(cropRect.bottom - minSize).coerceAtLeast(0f)
        cropRect.top = newTop
    }
    private fun resizeBottom(dy: Float) {
        val h = height.toFloat()
        val newBottom = (cropRect.bottom + dy).coerceAtLeast(cropRect.top + minSize).coerceAtMost(h)
        cropRect.bottom = newBottom
    }

    private fun resizeLeftTop(dx: Float, dy: Float) {
        resizeLeft(dx)
        resizeTop(dy)
    }
    private fun resizeRightTop(dx: Float, dy: Float) {
        resizeRight(dx)
        resizeTop(dy)
    }
    private fun resizeLeftBottom(dx: Float, dy: Float) {
        resizeLeft(dx)
        resizeBottom(dy)
    }
    private fun resizeRightBottom(dx: Float, dy: Float) {
        resizeRight(dx)
        resizeBottom(dy)
    }
}
