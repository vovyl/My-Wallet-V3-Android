package com.blockchain.morph.ui.homebrew.exchange

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.blockchain.morph.ui.R
import info.blockchain.balance.FiatValue
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import java.util.Locale

class ExchangeActivityViewModel : ViewModel() {

    var currentValue: Long = 0
}

class ExchangeActivity : AppCompatActivity() {

    companion object {

        private var Currency = "CURRENCY"

        fun intent(context: Context, fiatCurrency: String) =
            Intent(context, ExchangeActivity::class.java).apply {
                putExtra(Currency, fiatCurrency)
            }
    }

    private val compositeDisposable = CompositeDisposable()

    private lateinit var vm: ExchangeActivityViewModel

    private lateinit var currency: String

    private lateinit var largeValueLeftHandSide: TextView
    private lateinit var largeValue: TextView
    private lateinit var largeValueRightHandSide: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homebrew_exchange)

        vm = ViewModelProviders.of(this).get(ExchangeActivityViewModel::class.java)
        currency = intent.getStringExtra(Currency) ?: "USD"

        largeValueLeftHandSide = findViewById(R.id.largeValueLeftHandSide)
        largeValue = findViewById(R.id.largeValue)
        largeValueRightHandSide = findViewById(R.id.largeValueRightHandSide)
    }

    override fun onResume() {
        super.onResume()
        val keyboard = findViewById<IntegerKeyboardView>(R.id.numericKeyboard)
        compositeDisposable += keyboard
            .valueChanges
            .doOnNext {
                vm.currentValue = it
            }
            .map { FiatValue.fromMinor(currency, it).toParts(Locale.getDefault()) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { parts ->
                largeValueLeftHandSide.text = parts.symbol
                largeValue.text = parts.major
                largeValueRightHandSide.text = parts.minor
            }
        keyboard.value = vm.currentValue
    }

    override fun onPause() {
        compositeDisposable.clear()
        super.onPause()
    }
}
