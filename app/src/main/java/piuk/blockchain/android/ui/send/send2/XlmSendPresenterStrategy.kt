package piuk.blockchain.android.ui.send.send2

import android.content.Intent
import com.blockchain.sunriver.XlmDataManager
import com.blockchain.sunriver.fromStellarUri
import com.blockchain.transactions.SendDetails
import com.blockchain.transactions.SendFundsResult
import com.blockchain.transactions.SendFundsResultLocalizer
import com.blockchain.transactions.TransactionSender
import com.blockchain.transactions.sendFundsOrThrow
import com.blockchain.ui.extensions.sampleThrottledClicks
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import info.blockchain.balance.withMajorValueOrZero
import info.blockchain.wallet.api.data.FeeOptions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import piuk.blockchain.android.R
import piuk.blockchain.android.ui.send.SendView
import piuk.blockchain.android.ui.send.external.SendConfirmationDetails
import piuk.blockchain.android.ui.send.external.SendPresenterStrategy
import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.androidcore.data.currency.CurrencyState
import piuk.blockchain.androidcore.data.exchangerate.FiatExchangeRates
import piuk.blockchain.androidcore.data.exchangerate.toFiat
import timber.log.Timber
import java.util.concurrent.TimeUnit

class XlmSendPresenterStrategy(
    currencyState: CurrencyState,
    private val xlmDataManager: XlmDataManager,
    private val xlmTransactionSender: TransactionSender,
    private val fiatExchangeRates: FiatExchangeRates,
    private val sendFundsResultLocalizer: SendFundsResultLocalizer
) : SendPresenterStrategy<SendView>() {

    private val currency: CryptoCurrency by lazy { currencyState.cryptoCurrency }
    private var addressSubject = PublishSubject.create<String>()
    private var cryptoTextSubject = PublishSubject.create<CryptoValue>()
    private var continueClick = PublishSubject.create<Unit>()
    private var submitPaymentClick = PublishSubject.create<Unit>()
    private fun fees() = xlmDataManager.fees()

    private val allSendRequests: Observable<SendDetails> =
        Observables.combineLatest(
            xlmDataManager.defaultAccount().toObservable(),
            cryptoTextSubject,
            addressSubject
        ).map { (accountReference, value, address) ->
            SendDetails(
                from = accountReference,
                toAddress = address,
                value = value
            )
        }

    private val confirmationDetails: Observable<SendConfirmationDetails> =
        allSendRequests.sampleThrottledClicks(continueClick).map {
            val fees = fees()
            SendConfirmationDetails(
                from = it.from,
                to = it.toAddress,
                amount = it.value,
                fees = fees,
                fiatAmount = it.value.toFiat(fiatExchangeRates),
                fiatFees = fees.toFiat(fiatExchangeRates)
            )
        }

    private val submitConfirmationDetails: Observable<SendConfirmationDetails> =
        confirmationDetails.sampleThrottledClicks(submitPaymentClick)

    override fun onContinueClicked() {
        continueClick.onNext(Unit)
    }

    private var max: CryptoValue = CryptoValue.ZeroXlm
    private var autoClickAmount: CryptoValue? = null

    override fun onSpendMaxClicked() {
        view.updateCryptoAmount(autoClickAmount ?: max)
    }

    override fun onBroadcastReceived() {
        TODO("AND-1535")
    }

    override fun onResume() {
    }

    override fun onCurrencySelected(currency: CryptoCurrency) {
        when (currency) {
            CryptoCurrency.XLM -> xlmSelected()
            else -> throw IllegalArgumentException("This presented is not for $currency")
        }
    }

    private fun xlmSelected() {
        view.hideFeePriority()
        view.setFeePrioritySelection(0)
        view.disableFeeDropdown()
        view.setCryptoMaxLength(15)
        view.showMinBalanceLearnMore()
        calculateMax()
    }

    private fun calculateMax() {
        xlmDataManager.getMaxSpendableAfterFees()
            .observeOn(AndroidSchedulers.mainThread())
            .addToCompositeDisposable(this)
            .subscribeBy {
                updateMaxAvailable(it)
            }
    }

    private fun updateMaxAvailable(balanceAfterFee: CryptoValue) {
        max = balanceAfterFee
        view.updateMaxAvailable(balanceAfterFee, CryptoValue.ZeroXlm)
    }

    override fun handleURIScan(untrimmedscanData: String?) {
        val (public, cryptoValue) = untrimmedscanData?.fromStellarUri() ?: return
        val fiatValue = cryptoValue.toFiat(fiatExchangeRates)
        view.updateCryptoAmount(cryptoValue)
        view.updateFiatAmount(fiatValue)
        cryptoTextSubject.onNext(cryptoValue)
        view.updateReceivingAddress(public.accountId)
    }

    override fun handlePrivxScan(scanData: String?) {}

    override fun clearReceivingObject() {}

    override fun selectSendingAccount(data: Intent?, currency: CryptoCurrency) {}

    override fun selectReceivingAccount(data: Intent?, currency: CryptoCurrency) {}

    override fun selectDefaultOrFirstFundedSendingAccount() {
        xlmDataManager.defaultAccount()
            .addToCompositeDisposable(this)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onError = { Timber.e(it) }) {
                view.updateSendingAddress(it.label)
                view.updateFeeAmount(fees().toStringWithSymbol())
            }
    }

    override fun submitPayment() {
        submitPaymentClick.onNext(Unit)
    }

    override fun shouldShowAdvancedFeeWarning(): Boolean = false

    override fun onCryptoTextChange(cryptoText: String) {
        cryptoTextSubject.onNext(currency.withMajorValueOrZero(cryptoText))
    }

    override fun onAddressTextChange(address: String) {
        addressSubject.onNext(address)
    }

    override fun spendFromWatchOnlyBIP38(pw: String, scanData: String) {}

    override fun setWarnWatchOnlySpend(warn: Boolean) {}

    override fun onNoSecondPassword() {}

    override fun onSecondPasswordValidated(validateSecondPassword: String) {}

    override fun disableAdvancedFeeWarning() {}

    override fun getBitcoinFeeOptions(): FeeOptions? = null

    override fun onViewReady() {
        confirmationDetails
            .addToCompositeDisposable(this)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onError = { Timber.e(it) }) { details ->
                view.showPaymentDetails(details)
            }

        allSendRequests
            .debounce(200, TimeUnit.MILLISECONDS)
            .addToCompositeDisposable(this)
            .flatMapSingle { sendDetails ->
                xlmTransactionSender.dryRunSendFunds(sendDetails)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSuccess {
                        view.setSendButtonEnabled(it.success)
                        if (!it.success && !sendDetails.toAddress.isEmpty()) {
                            autoClickAmount = it.errorValue
                            view.updateWarning(sendFundsResultLocalizer.localize(it))
                        } else {
                            autoClickAmount = null
                            view.clearWarning()
                        }
                    }
                    .doOnError {
                        view.hideMaxAvailable()
                    }
                    .onErrorReturnItem(
                        SendFundsResult(
                            errorCode = 1,
                            sendDetails = sendDetails,
                            hash = null,
                            confirmationDetails = null
                        )
                    )
            }
            .subscribeBy(onError =
            { Timber.e(it) })

        submitConfirmationDetails
            .addToCompositeDisposable(this)
            .observeOn(AndroidSchedulers.mainThread())
            .flatMapCompletable { confirmationDetails ->
                val sendDetails = SendDetails(
                    from = confirmationDetails.from,
                    value = confirmationDetails.amount,
                    toAddress = confirmationDetails.to
                )
                xlmTransactionSender.sendFundsOrThrow(
                    sendDetails
                )
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe {
                        view.showProgressDialog(R.string.app_name)
                    }
                    .doFinally {
                        view.dismissProgressDialog()
                        view.dismissConfirmationDialog()
                    }
                    .doOnSuccess {
                        view.showTransactionSuccess(confirmationDetails.amount.currency)
                    }
                    .doOnError {
                        view.showTransactionFailed()
                    }
                    .toCompletable()
                    .onErrorComplete()
            }
            .subscribeBy(onError =
            { Timber.e(it) })
    }
}
