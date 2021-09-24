package com.example.banner

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

@SuppressLint("ViewConstructor")
class Indicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val radius: Float,
    private val color: Int
) : View(context, attrs, defStyleAttr) {

    private val fillPaint = Paint().also {
        it.color = color
        it.style = Paint.Style.FILL
        it.isAntiAlias = true
    }

    private val strokePaint = Paint().also {
        it.color = color
        it.style = Paint.Style.STROKE
        it.isAntiAlias = true
        it.strokeWidth = 2f
    }

    enum class Type {
        FILL, STROKE
    }

    private var type = Type.STROKE

    fun updateType(type: Type) {
        this.type = type
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val targetSpec = MeasureSpec.makeMeasureSpec((radius * 2).toInt(), MeasureSpec.EXACTLY)
        setMeasuredDimension(targetSpec, targetSpec)
    }

    override fun onDraw(canvas: Canvas?) {
        if (type == Type.STROKE)
            canvas?.drawCircle(radius, radius, radius - 5, strokePaint)
        else
            canvas?.drawCircle(radius, radius, radius - 5, fillPaint)
    }
}