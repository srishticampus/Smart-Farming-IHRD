package com.project.agritech.utlis

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class HalfArcProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var progress = 0  // Default progress is 0%
    private val maxProgress = 100  // Max progress is 100%

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY // Background arc color
        style = Paint.Style.STROKE
        strokeWidth = 20f
        strokeCap = Paint.Cap.ROUND
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE // Change color as needed
        style = Paint.Style.STROKE
        strokeWidth = 20f
        strokeCap = Paint.Cap.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        val size = min(width, height)

        val rectF = RectF(20f, 20f, size - 20f, size)  // Half-circle bounds

        // Draw background arc
        canvas.drawArc(rectF, 180f, 180f, false, backgroundPaint)

        // Draw progress arc based on current value
        val sweepAngle = (progress / maxProgress.toFloat()) * 180f
        canvas.drawArc(rectF, 180f, sweepAngle, false, progressPaint)
    }

    // ✅ Add this function to update progress dynamically
    fun setArcProgress(value: Int) {
        progress = value.coerceIn(0, 100) // Keep progress within 0-100
        invalidate() // Refresh the view
    }
}
