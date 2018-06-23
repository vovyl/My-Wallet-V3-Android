package piuk.blockchain.android.ui.buysell.payment.card

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import piuk.blockchain.android.R
import piuk.blockchain.androidcore.utils.helperfunctions.consume
import piuk.blockchain.androidcore.utils.helperfunctions.unsafeLazy
import piuk.blockchain.androidcoreui.ui.base.BaseAuthActivity
import piuk.blockchain.androidcoreui.utils.extensions.getResolvedColor
import piuk.blockchain.androidcoreui.utils.extensions.goneIf
import kotlinx.android.synthetic.main.activity_card_payment_complete.button_close as buttonClose
import kotlinx.android.synthetic.main.activity_card_payment_complete.constraint_layout_payment_status as constraintLayout
import kotlinx.android.synthetic.main.activity_card_payment_complete.image_view_tick as imageViewTick
import kotlinx.android.synthetic.main.activity_card_payment_complete.konfetti_view_success as konfetti
import kotlinx.android.synthetic.main.activity_card_payment_complete.text_view_failure_message as textViewFailureMessage
import kotlinx.android.synthetic.main.activity_card_payment_complete.text_view_success_message as textViewSuccessMessage
import kotlinx.android.synthetic.main.activity_card_payment_complete.text_view_success_title as textViewSuccessTitle
import kotlinx.android.synthetic.main.toolbar_general.toolbar_general as toolBar

class CoinifyPaymentCompleteActivity : BaseAuthActivity() {

    private val state by unsafeLazy { intent.getSerializableExtra(EXTRA_PAYMENT_STATE) as PaymentState }
    private val colors by unsafeLazy {
        arrayOf(
                getResolvedColor(R.color.secondary_yellow_medium),
                getResolvedColor(R.color.primary_blue_light),
                getResolvedColor(R.color.secondary_pink_light),
                getResolvedColor(R.color.secondary_red_light),
                getResolvedColor(R.color.product_green_medium)
        ).toIntArray()
    }
    private val successViews by unsafeLazy {
        listOf(
                konfetti,
                textViewSuccessTitle,
                imageViewTick,
                textViewSuccessMessage,
                buttonClose
        )
    }
    // TODO: Complete layout for failure once Rosana updates designs
    private val failureViews by unsafeLazy {
        listOf(textViewFailureMessage)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_payment_complete)

        buttonClose.setOnClickListener { onSupportNavigateUp() }

        when (state) {
            PaymentState.SUCCESS -> displaySuccess()
            PaymentState.CANCELLED -> displayCancelled()
            PaymentState.EXPIRED -> displayExpired()
            PaymentState.DECLINED, PaymentState.REJECTED, PaymentState.FAILED -> displayRejected()
            PaymentState.PENDING -> displayReviewing()
        }
    }

    private fun displaySuccess() {
        updateVisibility(true)

        setupToolbar(toolBar, R.string.buy_sell_card_order_created_title)

        konfetti.post { streamFromTop(colors) }
        konfetti.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                consume { burstFromCenter(colors, event.x, event.y) }
            } else false
        }
    }

    private fun updateVisibility(success: Boolean) {
        successViews.forEach { it.goneIf { !success } }
        failureViews.forEach { it.goneIf { success } }
    }

    private fun displayCancelled() {
        updateVisibility(false)
        textViewFailureMessage.text = "Cancelled $state"
    }

    private fun displayExpired() {
        updateVisibility(false)
        textViewFailureMessage.text = "Expired $state"
    }

    private fun displayRejected() {
        updateVisibility(false)
        textViewFailureMessage.text = "Rejected $state"
    }

    private fun displayReviewing() {
        updateVisibility(false)
        textViewFailureMessage.text = "Reviewing $state"
    }

    private fun streamFromTop(colors: IntArray) {
        konfetti.build()
                .addColors(*colors)
                .setDirection(0.0, 359.0)
                .setSpeed(4f, 7f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000)
                .addShapes(Shape.RECT, Shape.CIRCLE)
                .addSizes(Size(12), Size(16, 6f))
                .setPosition(-50f, konfetti.width + 50f, -50f, -50f)
                .streamFor(300, 5000L)
    }

    private fun burstFromCenter(colors: IntArray, x: Float, y: Float) {
        if (!canIHaveMoreConfetti()) return
        konfetti.build()
                .addColors(*colors)
                .setDirection(0.0, 359.0)
                .setSpeed(4f, 7f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000)
                .addShapes(Shape.RECT, Shape.CIRCLE)
                .addSizes(Size(12), Size(16, 6f))
                .setPosition(x, y)
                .burst(100)
    }

    private fun canIHaveMoreConfetti(): Boolean = konfetti.getActiveSystems().size <= 3

    override fun onSupportNavigateUp(): Boolean = consume { finish() }

    companion object {

        private const val EXTRA_PAYMENT_STATE =
                "piuk.blockchain.android.ui.buysell.payment.card.EXTRA_REDIRECT_URL"

        fun start(
                activity: Activity,
                paymentState: PaymentState
        ) {
            Intent(activity, CoinifyPaymentCompleteActivity::class.java).apply {
                putExtra(EXTRA_PAYMENT_STATE, paymentState)
            }.run { activity.startActivity(this) }
        }

    }

}