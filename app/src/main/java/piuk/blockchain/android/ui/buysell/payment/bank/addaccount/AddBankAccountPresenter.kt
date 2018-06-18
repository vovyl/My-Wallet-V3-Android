package piuk.blockchain.android.ui.buysell.payment.bank.addaccount

import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import piuk.blockchain.android.R
import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.androidbuysell.datamanagers.CoinifyDataManager
import piuk.blockchain.androidbuysell.models.coinify.Account
import piuk.blockchain.androidbuysell.models.coinify.Address
import piuk.blockchain.androidbuysell.models.coinify.Bank
import piuk.blockchain.androidbuysell.models.coinify.BankAccount
import piuk.blockchain.androidbuysell.models.coinify.Holder
import piuk.blockchain.androidbuysell.models.coinify.exceptions.CoinifyApiException
import piuk.blockchain.androidbuysell.services.ExchangeService
import piuk.blockchain.androidcore.utils.extensions.applySchedulers
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import piuk.blockchain.androidcoreui.ui.customviews.ToastCustom
import timber.log.Timber
import javax.inject.Inject

class AddBankAccountPresenter @Inject constructor(
        private val coinifyDataManager: CoinifyDataManager,
        private val exchangeService: ExchangeService
) : BasePresenter<AddBankAccountView>() {

    private val tokenSingle: Single<String>
        get() = exchangeService.getExchangeMetaData()
                .addToCompositeDisposable(this)
                .applySchedulers()
                .singleOrError()
                .map { it.coinify!!.token }

    override fun onViewReady() = Unit

    internal fun onConfirmClicked() {
        if (view.iban.isEmpty()) {
            view.showToast(R.string.buy_sell_add_account_iban_empty, ToastCustom.TYPE_ERROR)
            return
        }
        if (view.bic.isEmpty()){
            view.showToast(R.string.buy_sell_add_account_bic_empty, ToastCustom.TYPE_ERROR)
            return
        }

        tokenSingle.flatMap { token ->
            coinifyDataManager.getTrader(token)
                    .flatMap {
                        coinifyDataManager.addBankAccount(
                                token,
                                BankAccount(
                                        account = Account(
                                                it.defaultCurrency,
                                                null,
                                                view.bic,
                                                view.iban
                                        ),
                                        bank = Bank(address = Address(countryCode = it.profile.address.countryCode)),
                                        holder = Holder(it.profile.name!!, it.profile.address)
                                )
                        )
                    }

        }.doOnSubscribe { view.showProgressDialog() }
                .doOnEvent { _, _ -> view.dismissProgressDialog() }
                .subscribeBy(
                onSuccess = {
                    view.goToConfirmation()
                },
                onError = {
                    Timber.e(it)
                    if (it is CoinifyApiException) {
                        view.showErrorDialog(it.getErrorDescription())
                    } else {
                        view.showToast(R.string.unexpected_error, ToastCustom.TYPE_ERROR)
                    }
                }
        )
    }
}