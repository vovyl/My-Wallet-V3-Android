package com.blockchain.morph.ui.homebrew.exchange

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.transition.AutoTransition
import android.support.transition.TransitionManager
import android.support.v4.app.Fragment
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.SwitchCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.TextView
import com.blockchain.balance.colorRes
import com.blockchain.balance.layerListDrawableRes
import com.blockchain.extensions.px
import com.blockchain.morph.exchange.mvi.ExchangeIntent
import com.blockchain.morph.exchange.mvi.ExchangeViewState
import com.blockchain.morph.exchange.mvi.Fix
import com.blockchain.morph.exchange.mvi.Maximums
import com.blockchain.morph.exchange.mvi.QuoteValidity
import com.blockchain.morph.exchange.mvi.SimpleFieldUpdateIntent
import com.blockchain.morph.exchange.mvi.SwapIntent
import com.blockchain.morph.exchange.mvi.ToggleFiatCryptoIntent
import com.blockchain.morph.exchange.mvi.ToggleFromToIntent
import com.blockchain.morph.exchange.mvi.isBase
import com.blockchain.morph.exchange.mvi.isCounter
import com.blockchain.morph.ui.R
import com.blockchain.morph.ui.customviews.CurrencyTextView
import com.blockchain.morph.ui.customviews.ThreePartText
import com.blockchain.morph.ui.homebrew.exchange.host.HomebrewHostActivityListener
import com.blockchain.ui.chooser.AccountChooserActivity
import com.blockchain.ui.chooser.AccountMode
import com.blockchain.ui.extensions.throttledClicks
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.checkedChanges
import info.blockchain.balance.CryptoValue
import info.blockchain.balance.FiatValue
import info.blockchain.balance.Money
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import piuk.blockchain.androidcoreui.utils.ParentActivityDelegate
import piuk.blockchain.androidcoreui.utils.extensions.getResolvedColor
import piuk.blockchain.androidcoreui.utils.extensions.getResolvedDrawable
import piuk.blockchain.androidcoreui.utils.extensions.inflate
import piuk.blockchain.androidcoreui.utils.extensions.invisibleIf
import piuk.blockchain.androidcoreui.utils.extensions.setAnimationListener
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

internal class ExchangeFragment : Fragment() {

    companion object {

        private const val ARGUMENT_CURRENCY = "ARGUMENT_CURRENCY"

        fun bundleArgs(fiatCurrency: String): Bundle = Bundle().apply {
            putString(ARGUMENT_CURRENCY, fiatCurrency)
        }
    }

    private val compositeDisposable = CompositeDisposable()
    private val activityListener: HomebrewHostActivityListener by ParentActivityDelegate(this)

    private lateinit var currency: String

    private lateinit var largeValue: CurrencyTextView
    private lateinit var smallValue: TextView
    private lateinit var keyboard: FloatKeyboardView
    private lateinit var selectSendAccountButton: Button
    private lateinit var selectReceiveAccountButton: Button
    private lateinit var exchangeButton: Button
    private lateinit var feedback: TextView
    private lateinit var switch: SwitchCompat
    private lateinit var exchangeRates: Button
    private lateinit var root: ConstraintLayout
    private lateinit var keyboardGroup: ConstraintLayout
    private lateinit var showKeyboard: Button
    private lateinit var baseToCounter: TextView
    private lateinit var baseToFiat: TextView
    private lateinit var counterToFiat: TextView

    private lateinit var exchangeModel: ExchangeModel

    private var keyboardVisible = true

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        val provider = (context as? ExchangeViewModelProvider)
            ?: throw Exception("Host activity must support ExchangeViewModelProvider")
        exchangeModel = provider.exchangeViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_homebrew_exchange)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityListener.setToolbarTitle(R.string.morph_new_exchange)

        currency = arguments?.getString(ARGUMENT_CURRENCY) ?: "USD"

        largeValue = view.findViewById(R.id.largeValue)
        smallValue = view.findViewById(R.id.smallValue)
        keyboard = view.findViewById(R.id.numericKeyboard)
        selectSendAccountButton = view.findViewById(R.id.select_from_account_button)
        selectReceiveAccountButton = view.findViewById(R.id.select_to_account_button)
        exchangeButton = view.findViewById(R.id.exchange_action_button)
        feedback = view.findViewById(R.id.feedback)
        switch = view.findViewById(R.id.switch_fix)
        exchangeRates = view.findViewById(R.id.button_exchange_rates)
        root = view.findViewById(R.id.constraint_layout_exchange)
        keyboardGroup = view.findViewById(R.id.layout_keyboard_group)
        showKeyboard = view.findViewById(R.id.button_show_keyboard)
        baseToCounter = view.findViewById(R.id.text_view_base_to_counter)
        baseToFiat = view.findViewById(R.id.text_view_base_to_fiat)
        counterToFiat = view.findViewById(R.id.text_view_counter_to_fiat)

        selectSendAccountButton.setOnClickListener {
            AccountChooserActivity.startForResult(
                requireActivity(),
                AccountMode.Exchange,
                REQUEST_CODE_CHOOSE_SENDING_ACCOUNT,
                R.string.from
            )
        }
        selectReceiveAccountButton.setOnClickListener {
            AccountChooserActivity.startForResult(
                requireActivity(),
                AccountMode.Exchange,
                REQUEST_CODE_CHOOSE_RECEIVING_ACCOUNT,
                R.string.to
            )
        }
        exchangeButton.setOnClickListener {
            activityListener.launchConfirmation()
        }

        setupExpandButton()
    }

    private fun setupExpandButton() {
        val expandIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.vector_expand_less)!!
        expandIcon.setBounds(0, 0, 32.px, 32.px)
        DrawableCompat.wrap(expandIcon)
        DrawableCompat.setTint(expandIcon, getResolvedColor(R.color.primary_navy_medium))
        val span = SpannableString(" ")
        val image = ImageSpan(expandIcon, ImageSpan.ALIGN_BASELINE)
        span.setSpan(image, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        showKeyboard.text = span
    }

    private fun animateKeyboard(visible: Boolean) {
        applyExpandButtonConstraints(visible)
        if (!visible) applyKeyboardConstraints(0f)
        val height = keyboardGroup.height.toFloat()

        TranslateAnimation(
            0f,
            0f,
            if (visible) 0f else height,
            if (visible) height else 0f
        ).apply {
            duration = 300
            fillAfter = true
            interpolator = AccelerateDecelerateInterpolator()
            setAnimationListener {
                onAnimationEnd {
                    if (visible) applyKeyboardConstraints(height)
                }
            }
        }.run { keyboardGroup.startAnimation(this) }
    }

    private fun applyKeyboardConstraints(height: Float) {
        ConstraintSet().apply {
            clone(root)
            setTranslationY(R.id.layout_keyboard_group, height)
        }.run {
            TransitionManager.beginDelayedTransition(root)
            applyTo(root)
        }
    }

    private fun applyExpandButtonConstraints(show: Boolean) {
        ConstraintSet().apply {
            clone(root)
            setVisibility(R.id.button_show_keyboard, if (show) View.VISIBLE else View.INVISIBLE)
        }.run {
            TransitionManager.beginDelayedTransition(
                root,
                AutoTransition().apply { duration = if (show) 400 else 100 }
            )
            applyTo(root)
        }
    }

    override fun onResume() {
        super.onResume()

        keyboard.setMaximums(
            Maximums(
                maxDigits = 11,
                maxIntLength = 6
            )
        )

        compositeDisposable +=
            Observable.merge(
                allTextUpdates(),
                checkChangeToIntent(switch) { ToggleFromToIntent() },
                clicksToIntents(R.id.imageview_switch_currency) { ToggleFiatCryptoIntent() },
                clicksToIntents(R.id.imageview_switch_from_to) { SwapIntent() }
            ).subscribeBy {
                exchangeModel.inputEventSink.onNext(it)
            }

        val exchangeIndicator = view!!.findViewById<View>(R.id.imageView_exchange_indicator)
        val receiveIndicator = view!!.findViewById<View>(R.id.imageView_receive_indicator)
        compositeDisposable += exchangeModel
            .exchangeViewStates
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                when (it.fix) {
                    Fix.BASE_FIAT -> displayFiatLarge(it.fromFiat, it.fromCrypto, it.decimalCursor)
                    Fix.BASE_CRYPTO -> displayCryptoLarge(it.fromCrypto, it.fromFiat, it.decimalCursor)
                    Fix.COUNTER_FIAT -> displayFiatLarge(it.toFiat, it.toCrypto, it.decimalCursor)
                    Fix.COUNTER_CRYPTO -> displayCryptoLarge(it.toCrypto, it.toFiat, it.decimalCursor)
                }
                selectSendAccountButton.setButtonGraphicsAndTextFromCryptoValue(it.fromCrypto)
                selectReceiveAccountButton.setButtonGraphicsAndTextFromCryptoValue(it.toCrypto)
                exchangeIndicator.invisibleIf(it.fix.isCounter)
                receiveIndicator.invisibleIf(it.fix.isBase)
                keyboard.setValue(it.lastUserValue.userDecimalPlaces, it.lastUserValue.toBigDecimal())
                exchangeButton.text = getString(
                    R.string.exchange_x_for_y,
                    it.fromAccount.cryptoCurrency.symbol,
                    it.toAccount.cryptoCurrency.symbol
                )
                exchangeButton.isEnabled = it.isValid()
                updateUserFeedBack(it)

                exchangeRates.text = it.formatBaseToCounter()
                baseToCounter.text = it.formatBaseToCounter()
                baseToFiat.text = it.formatBaseToFiat()
                counterToFiat.text = it.formatCounterToFiat()
            }

        compositeDisposable +=
            exchangeRates.throttledClicks()
                .mergeWith(showKeyboard.throttledClicks())
                .subscribeBy(
                    onNext = {
                        animateKeyboard(keyboardVisible)
                        keyboardVisible = !keyboardVisible
                    }
                )
    }

    private fun updateUserFeedBack(exchangeViewState: ExchangeViewState) {
        feedback.text = exchangeViewState.isValidMessage()
    }

    private fun clicksToIntents(@IdRes id: Int, function: () -> ExchangeIntent) =
        clicksToIntents(view!!.findViewById<View>(id), function)

    private fun clicksToIntents(view: View, function: () -> ExchangeIntent) =
        view.clicks().map { function() }

    private fun checkChangeToIntent(switch: SwitchCompat, function: () -> ExchangeIntent) =
        switch.checkedChanges()
            .skipInitialValue()
            .map { function() }

    private fun displayFiatLarge(fiatValue: FiatValue, cryptoValue: CryptoValue, decimalCursor: Int) {
        val parts = fiatValue.toStringParts()
        largeValue.setText(ThreePartText(parts.symbol, parts.major, if (decimalCursor != 0) parts.minor else ""))

        val fromCryptoString = cryptoValue.toStringWithSymbol()
        smallValue.text = fromCryptoString
    }

    @SuppressLint("SetTextI18n")
    private fun displayCryptoLarge(cryptoValue: CryptoValue, fiatValue: FiatValue, decimalCursor: Int) {
        largeValue.setText(ThreePartText("", cryptoValue.formatExactly(decimalCursor) + " " + cryptoValue.symbol(), ""))

        val fromFiatString = fiatValue.toStringWithSymbol()
        smallValue.text = fromFiatString
    }

    private fun allTextUpdates(): Observable<ExchangeIntent> {
        return keyboard.viewStates
            .doOnNext {
                if (it.shake) {
                    val animShake = AnimationUtils.loadAnimation(
                        requireContext(),
                        R.anim.fingerprint_failed_shake
                    )
                    largeValue.startAnimation(animShake)
                }
                view!!.findViewById<View>(R.id.numberBackSpace).isEnabled = it.previous != null
            }
            .map {
                SimpleFieldUpdateIntent(it.userDecimal, it.decimalCursor)
            }
    }

    override fun onPause() {
        compositeDisposable.clear()
        super.onPause()
    }

    private val customCryptoEntryFormat: DecimalFormat =
        (NumberFormat.getInstance(Locale.getDefault()) as DecimalFormat)

    private fun CryptoValue.formatExactly(decimalPlacesForCrypto: Int): String {
        val show = when (decimalPlacesForCrypto) {
            0 -> 0
            1 -> 1
            else -> decimalPlacesForCrypto - 1
        }
        return customCryptoEntryFormat
            .apply {
                minimumFractionDigits = show
                maximumFractionDigits = decimalPlacesForCrypto
            }.format(toMajorUnitDouble())
    }

    private fun ExchangeViewState.isValidMessage() =
        when (validity()) {
            QuoteValidity.Valid,
            QuoteValidity.NoQuote,
            QuoteValidity.MissMatch -> ""
            QuoteValidity.UnderMinTrade -> getString(
                R.string.under_min,
                minTradeLimit?.toStringWithSymbol()
            )
            QuoteValidity.OverMaxTrade -> getString(
                R.string.over_max,
                maxTradeLimit?.toStringWithSymbol()
            )
            QuoteValidity.OverUserBalance -> getString(
                R.string.over_max,
                maxSpendable?.toStringWithSymbol()
            )
        }

    private fun ExchangeViewState.formatBaseToCounter(): String =
        formatRate(fromCrypto, toCrypto, latestQuote?.baseToCounterRate)

    private fun ExchangeViewState.formatBaseToFiat(): String =
        formatRate(fromCrypto, toFiat, latestQuote?.baseToFiatRate)

    private fun ExchangeViewState.formatCounterToFiat(): String =
        formatRate(toCrypto, toFiat, latestQuote?.counterToFiatRate)

    private fun formatRate(from: Money, to: Money, rate: BigDecimal?): String =
        rate?.let { getRatesString(from, it, to) } ?: getPlaceholderString(from, to)

    private fun getRatesString(base: Money, rate: BigDecimal, counter: Money) =
        getString(R.string.morph_exchange_rate_formatted, base.currencyCode, rate, counter.currencyCode)

    private fun getPlaceholderString(from: Money, to: Money): String =
        getString(R.string.morph_exchange_rate_placeholder, from.currencyCode, to.currencyCode)
}

private fun Money.formatOrSymbolForZero() =
    if (isZero) {
        symbol()
    } else {
        toStringWithSymbol()
    }

private fun Button.setButtonGraphicsAndTextFromCryptoValue(cryptoValue: CryptoValue) {
    val fromCryptoString = cryptoValue.formatOrSymbolForZero()
    setBackgroundResource(cryptoValue.currency.colorRes())
    setCryptoLeftImageIfZero(cryptoValue)
    text = fromCryptoString
}

private fun Button.setCryptoLeftImageIfZero(cryptoValue: CryptoValue) {
    if (cryptoValue.isZero) {
        setCompoundDrawablesWithIntrinsicBounds(
            context.getResolvedDrawable(
                cryptoValue.currency.layerListDrawableRes()
            ), null, null, null
        )
    } else {
        setCompoundDrawables(null, null, null, null)
    }
}

internal const val REQUEST_CODE_CHOOSE_RECEIVING_ACCOUNT = 800
internal const val REQUEST_CODE_CHOOSE_SENDING_ACCOUNT = 801
