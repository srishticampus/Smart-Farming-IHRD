package com.project.agritech.utlis

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class PHGaugeView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {
    private var paint: Paint? = null
    private var rect: RectF? = null
    private var phValue = 7 // Default PH value

    init {
        init()
    }

    private fun init() {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        rect = RectF()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width
        val height = height
        val strokeWidth = 30
        rect!![strokeWidth.toFloat(), strokeWidth.toFloat(), (width - strokeWidth).toFloat()] =
            (height * 2).toFloat()

        // Draw three arcs for the PH sections
        paint!!.style = Paint.Style.STROKE
        paint!!.strokeWidth = strokeWidth.toFloat()
        paint!!.strokeCap = Paint.Cap.ROUND

        // Left section (Orange - Acidic)
        paint!!.color = Color.parseColor("#FFA500")
        canvas.drawArc(rect!!, 180f, 60f, false, paint!!)

        // Middle section (Green - Neutral)
        paint!!.color = Color.parseColor("#4CAF50")
        canvas.drawArc(rect!!, 240f, 60f, false, paint!!)

        // Right section (Blue - Alkaline)
        paint!!.color = Color.parseColor("#2196F3")
        canvas.drawArc(rect!!, 300f, 60f, false, paint!!)

        // Draw pointer at the PH value
        drawPointer(canvas, phValue)
    }

    private fun drawPointer(canvas: Canvas, ph: Int) {
        val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        pointerPaint.color = Color.GREEN
        pointerPaint.style = Paint.Style.FILL

        val angle = 180 + (ph - 1) * (120f / 14f) // Map PH to angle
        val radius = width / 2.5f
        val pointerX = (width / 2 + radius * cos(Math.toRadians(angle.toDouble()))).toFloat()
        val pointerY = (height / 1.4 + radius * sin(Math.toRadians(angle.toDouble()))).toFloat()

        canvas.drawCircle(pointerX, pointerY, 10f, pointerPaint) // Pointer circle
    }

    fun setPHValue(ph: Int) {
        this.phValue = ph
        invalidate()
    }
}