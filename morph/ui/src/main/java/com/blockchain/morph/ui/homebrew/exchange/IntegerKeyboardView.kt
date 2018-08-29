package com.blockchain.morph.ui.homebrew.exchange

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.blockchain.morph.ui.R
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class IntegerKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val values = BehaviorSubject.create<Long>()

    val valueChanges: Observable<Long> = values

    init {
        inflate(context)
        wireUpNumbers()
        wireUpBackSpace()
    }

    private fun inflate(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.integer_keyboard, this, true)
    }

    private fun wireUpBackSpace() {
        findViewById<View>(R.id.numberBackSpace)
            .apply {
                setOnClickListener(backspaceClick())
                setOnLongClickListener(clearValue())
            }
    }

    private fun wireUpNumbers() {
        numberMap.forEach { (id, value) ->
            findViewById<View>(id)
                .setOnClickListener(numberClick(value))
        }
    }

    var limit = 1000000000L
        set(newLimit) {
            if (limit > 0) {
                value = Math.min(value, newLimit)
                limit = newLimit
            }
        }

    var value: Long = 0
        set(value) {
            if (value in 0..limit) {
                field = value
                values.onNext(value)
            }
        }

    private fun clearValue() = OnLongClickListener { value = 0L; true }

    private fun numberClick(i: Long) = OnClickListener { value = value * 10 + i }

    private fun backspaceClick() = OnClickListener { value /= 10 }
}

private val numberMap = mapOf(
    R.id.number0 to 0L,
    R.id.number1 to 1L,
    R.id.number2 to 2L,
    R.id.number3 to 3L,
    R.id.number4 to 4L,
    R.id.number5 to 5L,
    R.id.number6 to 6L,
    R.id.number7 to 7L,
    R.id.number8 to 8L,
    R.id.number9 to 9L
)
