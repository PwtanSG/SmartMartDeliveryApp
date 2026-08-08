package iss.nus.edu.sg.smartmartdeliveryapp.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class DonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var inProgressCount = 0
    private var completedCount = 0

    private val chartPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 44f
        strokeCap = Paint.Cap.BUTT
    }

    private val totalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#342F3E")
        textAlign = Paint.Align.CENTER
        textSize = 52f
        isFakeBoldText = true
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#77717E")
        textAlign = Paint.Align.CENTER
        textSize = 35f
    }

    fun setData(inProgress: Int, completed: Int) {
        inProgressCount = inProgress
        completedCount = completed
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val total = inProgressCount + completedCount
        val padding = chartPaint.strokeWidth / 2f + 8f

        val chartBounds = RectF(
            padding,
            padding,
            width - padding,
            height - padding
        )

        if (total == 0) {
            chartPaint.color = Color.parseColor("#E4DFEA")
            canvas.drawArc(chartBounds, 0f, 360f, false, chartPaint)
        } else {
            val inProgressAngle =
                inProgressCount.toFloat() / total * 360f

            val completedAngle =
                completedCount.toFloat() / total * 360f

            chartPaint.color = Color.parseColor("#6A4FB3")
            canvas.drawArc(
                chartBounds,
                -90f,
                inProgressAngle,
                false,
                chartPaint
            )

            chartPaint.color = Color.parseColor("#198754")
            canvas.drawArc(
                chartBounds,
                -90f + inProgressAngle,
                completedAngle,
                false,
                chartPaint
            )
        }

        val centerX = width / 2f
        val centerY = height / 2f

        canvas.drawText(
            total.toString(),
            centerX,
            centerY,
            totalPaint
        )

        canvas.drawText(
            "Total orders",
            centerX,
            centerY + 40f,
            labelPaint
        )
    }
}