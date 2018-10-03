package com.blockchain.morph.ui.customviews

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.TextView
import com.blockchain.morph.ui.R

internal class CurrencyTextView : TextView {

    private val drawable: FitTextDrawable

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setWillNotDraw(false)

        drawable = FitTextDrawable(paint)

        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CurrencyTextView,
            0, 0
        )
        try {
            drawable.textSizeStepPx =
                typedArray.getDimension(R.styleable.CurrencyTextView_textSizeStep, 0f)
            drawable.textSizeStepRatio =
                typedArray.getFloat(R.styleable.CurrencyTextView_textSizeStepRatio, 0.6f)
        } finally {
            typedArray.recycle()
        }
    }

    fun setText(text: ThreePartText) {
        if (text != drawable.text) {
            drawable.text = text
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawable.setBounds(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom)
        drawable.setColor(currentTextColor)
        drawable.draw(canvas)
    }
}
