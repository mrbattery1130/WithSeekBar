package com.mrbattery.withseekbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

class WithSeekBar : View {

    companion object {
        const val TAG = "VolumeSeekBar"
    }

    private var _progress: Int = 0
    private var _max: Int = 100
    private var _progressColor: Int = Color.RED
    private var _progressBackgroundColor: Int = Color.BLUE
    private var _thumbColor: Int = Color.WHITE
    private var _thumbSizeRatio: Float = 0.7f
    private var _reverseProgress: Boolean = false

    var progress: Int
        get() = _progress
        set(value) {
            _progress = value
            onSeekBarChangeListener?.onProgressChanged?.let { it(this, progress, max) }
            invalidate()
        }
    var max: Int
        get() = _max
        set(value) {
            _max = value
            invalidate()
        }
    var progressColor: Int
        get() = _progressColor
        set(value) {
            _progressColor = value
            invalidate()
        }
    var progressBackgroundColor: Int
        get() = _progressBackgroundColor
        set(value) {
            _progressBackgroundColor = value
            invalidate()
        }
    var thumbColor: Int
        get() = _thumbColor
        set(value) {
            _thumbColor = value
            invalidate()
        }
    var thumbSizeRatio: Float
        get() = _thumbSizeRatio
        set(value) {
            _thumbSizeRatio = value
            invalidate()
        }
    var reverseProgress: Boolean
        get() = _reverseProgress
        set(value) {
            _reverseProgress = value
            invalidate()
        }

    private lateinit var progressPaint: Paint
    private lateinit var bgPaint: Paint
    private lateinit var thumbPaint: Paint

    private var widthAvailable: Float = 0f
    private var paddingStartEnd: Float = 0f
    private var downX: Float = 0f
    private var downY: Float = 0f
    private var downProgress: Int = progress

    private var progressRectF = RectF()
    private var bgRectF = RectF()
    private var thumbRectF = RectF()

    var onSeekBarChangeListener: OnSeekBarChangeListener? = null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(attrs, R.styleable.WithSeekBar, defStyle, 0)

        _progress = a.getInt(R.styleable.WithSeekBar_progress, progress)
        _max = a.getInt(R.styleable.WithSeekBar_max, max)
        _progressColor = a.getColor(R.styleable.WithSeekBar_progress_color, progressColor)
        _progressBackgroundColor =
            a.getColor(R.styleable.WithSeekBar_progress_background_color, progressBackgroundColor)
        _thumbColor = a.getColor(R.styleable.WithSeekBar_thumb_color, thumbColor)
        _thumbSizeRatio = a.getFloat(R.styleable.WithSeekBar_thumb_size_ratio, thumbSizeRatio)
        _reverseProgress = a.getBoolean(R.styleable.WithSeekBar_reverse_progress, reverseProgress)

        a.recycle()


        // 初始化背景圆环画笔
        bgPaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true // 设置抗锯齿
            color = progressBackgroundColor
        }

        // 初始化进度圆环画笔
        progressPaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true // 设置抗锯齿
            color = progressColor
        }
        // 初始化进度圆环画笔
        thumbPaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true // 设置抗锯齿
            color = thumbColor
        }


    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = x
                downY = y
                downProgress = progress
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                when {
                    width == height -> return true
                    width > height -> {
                        if (x < 0) return true
                        val deltaX = if (!reverseProgress) x - downX else downX - x
                        progress =
                            max(
                                0,
                                min(
                                    downProgress + (max * deltaX / widthAvailable).toInt(),
                                    max
                                )
                            )
                        return true
                    }
                    width < height -> {
                        if (y > height) return true
                        val deltaY = if (!reverseProgress) y - downY else downY - y
                        progress =
                            max(
                                0,
                                min(
                                    downProgress - (max * deltaY / widthAvailable).toInt(),
                                    max
                                )
                            )
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                this.performClick()
                onSeekBarChangeListener?.onProgressConfirmed?.let { it(this, progress, max) }
            }
            else -> {
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paddingStartEnd = min(width, height) / 2f
        widthAvailable = abs(width - height).toFloat()

        val progressLength = widthAvailable * progress / max

        //Draw background
        bgRectF.left = 0f
        bgRectF.top = 0f
        bgRectF.right = width.toFloat()
        bgRectF.bottom = height.toFloat()
        canvas.drawRoundRect(bgRectF, paddingStartEnd, paddingStartEnd, bgPaint)

        //Draw progress
        if (width > height) {
            progressRectF.left =
                if (!reverseProgress) 0f else widthAvailable - progressLength
            progressRectF.top = 0f
            progressRectF.right =
                if (!reverseProgress) progressLength + paddingStartEnd * 2 else width.toFloat()
            progressRectF.bottom = height.toFloat()
        } else {
            progressRectF.left = 0f
            progressRectF.top =
                if (!reverseProgress) widthAvailable - progressLength else 0f
            progressRectF.right = width.toFloat()
            progressRectF.bottom =
                if (!reverseProgress) height.toFloat() else progressLength + paddingStartEnd * 2
        }
        canvas.drawRoundRect(progressRectF, paddingStartEnd, paddingStartEnd, progressPaint)

        //Draw thumb
        if (width > height) {
            thumbRectF.left =
                if (!reverseProgress) progressLength + paddingStartEnd * (1 - thumbSizeRatio)
                else widthAvailable - progressLength + paddingStartEnd * (1 - thumbSizeRatio)
            thumbRectF.top = paddingStartEnd * (1 - thumbSizeRatio)
            thumbRectF.right = thumbRectF.left + paddingStartEnd * 2 * thumbSizeRatio
            thumbRectF.bottom = height - paddingStartEnd * (1 - thumbSizeRatio)

        } else {
            thumbRectF.left = paddingStartEnd * (1 - thumbSizeRatio)
            thumbRectF.top =
                if (!reverseProgress) widthAvailable - progressLength + paddingStartEnd * (1 - thumbSizeRatio)
                else progressLength + paddingStartEnd * (1 - thumbSizeRatio)
            thumbRectF.right = width.toFloat() - paddingStartEnd * (1 - thumbSizeRatio)
            thumbRectF.bottom = thumbRectF.top + paddingStartEnd * 2 * thumbSizeRatio
        }
        canvas.drawOval(thumbRectF, thumbPaint)
    }

    interface OnSeekBarChangeListener {
        var onProgressChanged: (v: View, progress: Int, max: Int) -> Unit
        var onProgressConfirmed: (v: View, progress: Int, max: Int) -> Unit
    }
}