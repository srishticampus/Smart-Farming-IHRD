package com.project.smartfarming.utlis

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class SoilMoistureGauge @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val arcRect = RectF()

    var moistureLevel: Int = 67 // Default moisture percentage

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        // Set up the arc bounds
        arcRect.set(20f, 20f, width - 20f, height * 2)

        // Draw Background Arc (Gray)
        paint.color = Color.LTGRAY
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 15f
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawArc(arcRect, 180f, 180f, false, paint)

        // Determine arc color based on moisture level
        paint.color = when {
            moistureLevel <= 30 -> Color.BLUE // Cold
            moistureLevel in 31..70 -> Color.GREEN // Optimal
            else -> Color.RED // Hot
        }

        // Draw Foreground Arc (Filled Progress)
        val sweepAngle = (moistureLevel / 100f) * 180
        canvas.drawArc(arcRect, 180f, sweepAngle, false, paint)
    }

    // Function to update moisture level
    fun updateMoistureLevel(level: Int) {
        moistureLevel = level
        invalidate() // Refresh the UI
    }
}
