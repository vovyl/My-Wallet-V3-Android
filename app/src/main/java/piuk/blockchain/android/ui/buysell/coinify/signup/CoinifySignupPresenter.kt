package piuk.blockchain.android.ui.buysell.coinify.signup

import piuk.blockchain.androidbuysell.datamanagers.CoinifyDataManager
import piuk.blockchain.androidcore.data.currency.CurrencyState
import piuk.blockchain.androidcore.data.payload.PayloadDataManager
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import javax.inject.Inject

class CoinifySignupPresenter @Inject constructor(
    private val coinifyDataManager: CoinifyDataManager,
    private val payloadDataManager: PayloadDataManager,
    private val currencyState: CurrencyState
) : BasePresenter<CoinifySignupView>() {

    private var countryCode: String? = null

    override fun onViewReady() {

        // WIP - Start specific step of account creation
        // or select country
        // or verify email
        // or verify identity
        view.onStartWelcome()

        // Or if user has account
//        view.onStartCoinifyOverview()
    }

    fun setCountryCode(selectedCountryCode: String) {
        countryCode = selectedCountryCode
    }

    fun signup(verifiedEmailAddress: String) {

        countryCode?.run {
            coinifyDataManager.getEmailTokenAndSignUp(
                    payloadDataManager.guid,
                    payloadDataManager.sharedKey,
                    verifiedEmailAddress,
                    currencyState.fiatUnit,
                    this)
                    .doOnError {
                        //TODO Handle any error's coming from Coinify
                    }
                    .subscribe { _, _ ->
                        // no-op
                    }
        } ?: onViewReady()
    }
}