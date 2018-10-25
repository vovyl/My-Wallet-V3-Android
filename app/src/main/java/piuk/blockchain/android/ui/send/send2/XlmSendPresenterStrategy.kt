package piuk.blockchain.android.ui.send.send2

import android.content.Intent
import com.blockchain.sunriver.HorizonKeyPair
import com.blockchain.sunriver.XlmDataManager
import com.blockchain.sunriver.fromStellarUri
import com.blockchain.transactions.TransactionSender
import info.blockchain.balance.AccountReference
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

class XlmSendPresenterStrategy(
    currencyState: CurrencyState,
    private val xlmDataManager: XlmDataManager,
    private val xlmTransactionSender: TransactionSender,
    private val fiatExchangeRates: FiatExchangeRates
) : SendPresenterStrategy<SendView>() {

    private val currency: CryptoCurrency by lazy { currencyState.cryptoCurrency }
    private var cryptoTextSubject = PublishSubject.create<CryptoValue>()
    private var continueClick = PublishSubject.create<Unit>()
    private var submitPaymentClick = PublishSubject.create<Unit>()
    private val fees = CryptoValue.lumensFromStroop(100.toBigInteger()) // TODO("AND-1535")

    private val confirmationDetails: Observable<SendConfirmationDetails> =
        Observables.combineLatest(
            cryptoTextSubject.sample(continueClick).map { value ->
                val toAddress = HorizonKeyPair.createValidatedPublic(view.getReceivingAddress() ?: "")
                SendConfirmationDetails(
                    from = AccountReference.Xlm("No account", ""),
                    to = toAddress.accountId,
                    amount = value,
                    fees = fees,
                    fiatAmount = value.toFiat(fiatExchangeRates),
                    fiatFees = fees.toFiat(fiatExchangeRates)
                )
            },
            xlmDataManager.defaultAccount().toObservable()
        ).map { (details, accountReference) ->
            details.copy(from = accountReference)
        }

    private val submitConfirmationDetails: Observable<SendConfirmationDetails> =
        confirmationDetails.sample(submitPaymentClick)

    override fun onContinueClicked() {
        continueClick.onNext(Unit)
    }

    private var max: CryptoValue = CryptoValue.ZeroXlm

    override fun onSpendMaxClicked() {
        view.updateCryptoAmount(max) // TODO("AND-1535") Needs tests
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
        calculateMax() // TODO("AND-1535") Needs tests
    }

    private fun calculateMax() {
        xlmDataManager.getBalance()
            .observeOn(AndroidSchedulers.mainThread())
            .addToCompositeDisposable(this)
            .subscribeBy {
                updateMaxAvailable(it - fees)
            }
    }

    private fun updateMaxAvailable(balanceAfterFee: CryptoValue) {
        max = balanceAfterFee
        view.showMaxAvailable()
        view.updateMaxAvailable(balanceAfterFee)
        view.updateMaxAvailableColor(R.color.primary_blue_accent)
    }

    override fun handleURIScan(untrimmedscanData: String?) {
        val (public, value) = untrimmedscanData!!.fromStellarUri()
        view.updateCryptoAmount(value)
        cryptoTextSubject.onNext(value)
        // TODO: This doesn't update the fiat amount
        view.updateReceivingAddress(public.accountId)
    }

    override fun handlePrivxScan(scanData: String?) {
        TODO("AND-1535")
    }

    override fun clearReceivingObject() {
    }

    override fun selectSendingAccount(data: Intent?, currency: CryptoCurrency) {
        TODO("AND-1535")
    }

    override fun selectReceivingAccount(data: Intent?, currency: CryptoCurrency) {
        TODO("AND-1535")
    }

    override fun selectDefaultOrFirstFundedSendingAccount() {
        xlmDataManager.defaultAccount()
            .addToCompositeDisposable(this)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onError = { Timber.e(it) }) {
                view.updateSendingAddress(it.label)
                view.updateFeeAmount(fees.toStringWithSymbol())
            }
    }

    override fun submitPayment() {
        submitPaymentClick.onNext(Unit)
    }

    override fun shouldShowAdvancedFeeWarning(): Boolean {
        TODO("AND-1535")
    }

    override fun onCryptoTextChange(cryptoText: String) {
        cryptoTextSubject.onNext(currency.withMajorValueOrZero(cryptoText))
    }

    override fun spendFromWatchOnlyBIP38(pw: String, scanData: String) {
        TODO("AND-1535")
    }

    override fun setWarnWatchOnlySpend(warn: Boolean) {
        TODO("AND-1535")
    }

    override fun onNoSecondPassword() {
        TODO("AND-1535")
    }

    override fun onSecondPasswordValidated(validateSecondPassword: String) {
        TODO("AND-1535")
    }

    override fun disableAdvancedFeeWarning() {
        TODO("AND-1535")
    }

    override fun getBitcoinFeeOptions(): FeeOptions? {
        TODO("AND-1535")
    }

    override fun onViewReady() {
        confirmationDetails
            .addToCompositeDisposable(this)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onError = { Timber.e(it) }) { details ->
                view.showPaymentDetails(details)
            }

        submitConfirmationDetails
            .addToCompositeDisposable(this)
            .observeOn(AndroidSchedulers.mainThread())
            .flatMapCompletable { confirmationDetails ->
                xlmTransactionSender.sendFunds(
                    confirmationDetails.from,
                    confirmationDetails.amount,
                    confirmationDetails.to
                )
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe {
                        view.showProgressDialog(R.string.app_name)
                    }.doOnTerminate {
                        view.dismissProgressDialog()
                        view.dismissConfirmationDialog()
                        view.showTransactionSuccess(confirmationDetails.amount.currency)
                    }
            }
            .subscribeBy(onError = { Timber.e(it) })
    }
}
