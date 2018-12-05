package com.blockchain.morph.ui.customviews

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.text.TextPaint

internal class FitTextDrawable(paint: TextPaint) : Drawable() {

    private val textSizePx: Float = paint.textSize
    var textSizeStepPx: Float = 0f
    var textSizeStepRatio: Float = 0.6f

    private val mainTextPaint = Paint(paint)
    private val subTextPaint = Paint(paint)
    private val paints = arrayOf(subTextPaint, mainTextPaint, subTextPaint)
    private val widths = arrayOf(0.0, 0.0, 0.0)

    var text = ThreePartText("", "", "")

    private val tempBounds = Rect()

    fun setColor(@ColorInt textColor: Int) {
        mainTextPaint.color = textColor
        subTextPaint.color = textColor
    }

    override fun draw(canvas: Canvas) {
        canvas.save()
        canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
        drawText(canvas)
        canvas.restore()
    }

    private fun drawText(canvas: Canvas) {
        val availableWidth = bounds.width()
        val availableHeight = bounds.height()

        if (availableWidth <= 0 || availableHeight <= 0) return

        var textSize = textSizePx
        while (textSize > 0) {
            mainTextPaint.textSize = textSize
            subTextPaint.textSize = textSize / 2f
            val newTextSize = stepFontSize(textSize)
            if (newTextSize >= textSize) break
            textSize = newTextSize
            val fontHeight = mainTextPaint.fontMetrics.let { it.descent - it.ascent }
            if (fontHeight > availableHeight) continue
            val mainTextHeight = mainTextPaint.typicalHeight()
            var totalWidth = 0.0
            for (i in 0..2) {
                val paint = paints[i]
                val string = text[i]
                val width = paint.measureText(string).toDouble()
                widths[i] = width
                totalWidth += width
            }
            if (totalWidth > availableWidth) continue
            var offset = 0.0
            for (i in 0..2) {
                val paint = paints[i]
                val string = text[i]
                paint.getTextBounds(string, 0, string.length, tempBounds)
                canvas.save()
                canvas.translate(
                    (offset + (availableWidth - totalWidth) / 2).toFloat(),
                    (availableHeight / 2f) - tempBounds.top - mainTextHeight / 2f
                )
                canvas.drawText(string, 0f, 0f, paint)
                canvas.restore()
                offset += widths[i]
            }
            break
        }
    }

    private fun stepFontSize(textSize: Float) =
        if (textSizeStepPx < textSize && textSizeStepPx > 0) {
            textSize - textSizeStepPx
        } else {
            textSize * textSizeStepRatio
        }

    override fun setAlpha(alpha: Int) {}

    override fun getOpacity() = PixelFormat.OPAQUE

    override fun setColorFilter(colorFilter: ColorFilter) {}

    private fun Paint.typicalHeight() =
        getTextBounds("9", 0, 1, tempBounds)
            .let { tempBounds.bottom - tempBounds.top }
}
