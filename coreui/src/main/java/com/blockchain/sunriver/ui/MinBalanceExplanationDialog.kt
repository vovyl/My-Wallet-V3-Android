package com.blockchain.sunriver.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v4.app.DialogFragment
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.blockchain.account.DefaultAccountDataManager
import info.blockchain.balance.CryptoValue
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.koin.android.ext.android.inject
import piuk.blockchain.androidcore.data.exchangerate.FiatExchangeRates
import piuk.blockchain.androidcore.data.exchangerate.toFiat
import piuk.blockchain.androidcoreui.R

class MinBalanceExplanationDialog : DialogFragment() {

    private val compositeDisposable = CompositeDisposable()

    init {
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.FullscreenDialog)
    }

    private val xlmDefaultAccountManager: DefaultAccountDataManager by inject()
    private val exchangeRates: FiatExchangeRates by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(
        R.layout.dialog_min_balance_explainer,
        container,
        false
    ).apply {
        isFocusableInTouchMode = true
        requestFocus()
        dialog.window.setWindowAnimations(R.style.DialogNoAnimations)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_general)
        toolbar.setTitle(R.string.minimum_balance_explanation_title)
        toolbar.setNavigationOnClickListener { dismiss() }

        view.findViewById<Button>(R.id.button_continue).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.minimum_balance_url))))
        }
    }

    override fun onResume() {
        super.onResume()
        compositeDisposable += xlmDefaultAccountManager.getBalanceAndMin()
            .map {
                Values(
                    it.minimumBalance,
                    it.balance,
                    CryptoValue.lumensFromStroop(100.toBigInteger()) // Tech debt AND-1663 Repeated Hardcoded fee
                )
            }
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorReturn { Values(CryptoValue.ZeroXlm, CryptoValue.ZeroXlm, CryptoValue.ZeroXlm) }
            .subscribeBy {
                view?.run {
                    updateText(R.id.textview_balance, it.balance)
                    updateText(R.id.textview_reserve, it.min)
                    updateText(R.id.textview_fee, it.fee)
                    updateText(R.id.textview_spendable, it.spendable)
                }
            }
    }

    private fun View.updateText(@IdRes textViewId: Int, cryptoValue: CryptoValue) {
        findViewById<TextView>(textViewId).text = formatWithFiat(cryptoValue)
    }

    override fun onPause() {
        compositeDisposable.clear()
        super.onPause()
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    private fun formatWithFiat(
        cryptoValue: CryptoValue
    ) = "${cryptoValue.toStringWithSymbol()} (${cryptoValue.toFiat(this.exchangeRates).toStringWithSymbol()})"
}

private class Values(val min: CryptoValue, val balance: CryptoValue, val fee: CryptoValue) {
    val spendable: CryptoValue = balance - min - fee
}
