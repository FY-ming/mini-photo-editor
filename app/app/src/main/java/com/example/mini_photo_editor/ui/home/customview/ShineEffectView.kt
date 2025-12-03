package com.example.mini_photo_editor.ui.home.customview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.example.mini_photo_editor.R

/**
 * 扫光效果自定义View
 * 用于轮播图项，增强视觉效果
 */
class ShineEffectView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var shinePosition = -200f
    private var shineWidth = 150f
    private var shineColor = Color.WHITE
    private var shineAlpha = 0.7f
    private var isAnimating = false

    private var animator: ValueAnimator? = null

    init {
        setupFromAttributes(attrs)
        setupPaint()
    }

    private fun setupFromAttributes(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.ShineEffectView)

            shineWidth = typedArray.getDimension(
                R.styleable.ShineEffectView_shineWidth,
                150f
            )

            shineColor = typedArray.getColor(
                R.styleable.ShineEffectView_shineColor,
                Color.WHITE
            )

            shineAlpha = typedArray.getFloat(
                R.styleable.ShineEffectView_shineAlpha,
                0.7f
            )

            typedArray.recycle()
        }
    }

    private fun setupPaint() {
        paint.apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
    }

    fun setShineColor(color: Int) {
        shineColor = color
        invalidate()
    }

    fun setShineWidth(width: Float) {
        shineWidth = width
        invalidate()
    }

    fun setShineAlpha(alpha: Float) {
        shineAlpha = alpha.coerceIn(0f, 1f)
        invalidate()
    }

    fun startAnimation() {
        if (!isAnimating) {
            isAnimating = true

            animator?.cancel()
            animator = ValueAnimator.ofFloat(-shineWidth, width + shineWidth).apply {
                duration = 2000
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
                interpolator = LinearInterpolator()

                addUpdateListener { animation ->
                    shinePosition = animation.animatedValue as Float
                    invalidate()
                }

                start()
            }
        }
    }

    fun stopAnimation() {
        isAnimating = false
        animator?.cancel()
        animator = null
    }

    fun setShineSpeed(speed: Float) {
        animator?.duration = (2000 / speed.coerceAtLeast(0.1f)).toLong()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // 重置动画
        if (isAnimating) {
            stopAnimation()
            startAnimation()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isAnimating && width > 0 && height > 0) {
            // 创建扫光渐变
            val colors = intArrayOf(
                Color.TRANSPARENT,
                Color.argb(
                    (shineAlpha * 255).toInt(),
                    Color.red(shineColor),
                    Color.green(shineColor),
                    Color.blue(shineColor)
                ),
                Color.TRANSPARENT
            )

            val positions = floatArrayOf(0f, 0.5f, 1f)

            val gradient = LinearGradient(
                shinePosition, 0f,
                shinePosition + shineWidth, 0f,
                colors, positions,
                Shader.TileMode.CLAMP
            )

            paint.shader = gradient

            // 绘制扫光矩形
            canvas.drawRect(
                shinePosition, 0f,
                shinePosition + shineWidth, height.toFloat(),
                paint
            )
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isAnimating) {
            startAnimation()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }
}