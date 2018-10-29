package com.blockchain.morph.ui.homebrew.exchange.confirmation

import com.blockchain.datamanagers.TransactionSendDataManager
import com.blockchain.datamanagers.fees.getFeeOptions
import com.blockchain.morph.exchange.mvi.Quote
import com.blockchain.morph.exchange.service.TradeExecutionService
import com.blockchain.morph.ui.R
import com.blockchain.morph.ui.homebrew.exchange.locked.ExchangeLockedModel
import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoValue
import info.blockchain.balance.formatWithUnit
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import piuk.blockchain.androidcore.data.api.EnvironmentConfig
import piuk.blockchain.androidcore.data.bitcoincash.BchDataManager
import piuk.blockchain.androidcore.data.ethereum.exceptions.TransactionInProgressException
import piuk.blockchain.androidcore.data.fees.FeeDataManager
import piuk.blockchain.androidcore.data.payload.PayloadDataManager
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import piuk.blockchain.androidcoreui.ui.customviews.ToastCustom
import timber.log.Timber

class ExchangeConfirmationPresenter internal constructor(
    private val transactionSendDataManager: TransactionSendDataManager,
    private val tradeExecutionService: TradeExecutionService,
    private val feeDataManager: FeeDataManager,
    private val payloadDataManager: PayloadDataManager,
    private val bchDataManager: BchDataManager,
    private val environmentConfig: EnvironmentConfig
) : BasePresenter<ExchangeConfirmationView>() {

    override fun onViewReady() {
        // Ensure user hasn't got a double encrypted wallet
        if (payloadDataManager.isDoubleEncrypted) {
            view.showSecondPasswordDialog()
        } else {
            subscribeToViewState()
        }
    }

    private fun subscribeToViewState() {
        compositeDisposable +=
            view.exchangeViewState
                .flatMapSingle { executeTrade(it.latestQuote!!, it.fromAccount, it.toAccount) }
                .retry()
                .subscribeBy(onError = { Timber.e(it) })
    }

    internal fun updateFee(
        amount: CryptoValue,
        sendingAccount: AccountReference
    ) {
        compositeDisposable +=
            feeDataManager.getFeeOptions(amount.currency)
                .flatMap {
                    transactionSendDataManager.getFeeForTransaction(
                        amount,
                        sendingAccount,
                        it
                    )
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = { view.updateFee(it) },
                    onError = {
                        Timber.e(it)
                        view.showToast(
                            R.string.homebrew_confirmation_error_fetching_fee,
                            ToastCustom.TYPE_ERROR
                        )
                    }
                )
    }

    private fun executeTrade(
        quote: Quote,
        sendingAccount: AccountReference,
        receivingAccount: AccountReference
    ): Single<ExchangeLockedModel> {
        return deriveAddressPair(sendingAccount, receivingAccount)
            .subscribeOn(Schedulers.io())
            .flatMap { (destination, refund) ->
                tradeExecutionService.executeTrade(quote, destination, refund)
                    .subscribeOn(Schedulers.io())
                    .flatMap { transaction ->
                        feeDataManager.getFeeOptions(transaction.deposit.currency)
                            .flatMap {
                                transactionSendDataManager.executeTransaction(
                                    transaction.deposit,
                                    transaction.depositAddress,
                                    sendingAccount,
                                    it
                                ).subscribeOn(Schedulers.io())
                                    .doOnError { Timber.e(it) }
                            }
                            .map {
                                ExchangeLockedModel(
                                    orderId = transaction.id,
                                    value = transaction.fiatValue.toStringWithSymbol(view.locale),
                                    fees = transaction.fee.formatWithUnit(),
                                    sending = transaction.deposit.formatWithUnit(),
                                    sendingCurrency = transaction.deposit.currency,
                                    receiving = transaction.withdrawal.formatWithUnit(),
                                    receivingCurrency = transaction.withdrawal.currency,
                                    accountName = receivingAccount.label
                                )
                            }
                    }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { view.showProgressDialog() }
            .doOnEvent { _, _ -> view.dismissProgressDialog() }
            .doOnError {
                if (it is TransactionInProgressException) {
                    view.displayErrorDialog(R.string.morph_confirmation_eth_pending)
                } else {
                    view.displayErrorDialog(R.string.execution_error_message)
                }
            }
            .doOnSuccess { view.continueToExchangeLocked(it) }
    }

    private fun deriveAddressPair(
        receivingAccount: AccountReference,
        sendingAccount: AccountReference
    ): Single<Pair<String, String>> = Single.zip(
        transactionSendDataManager.getReceiveAddress(
            receivingAccount
        ),
        transactionSendDataManager.getReceiveAddress(
            sendingAccount
        ),
        BiFunction { destination: String, refund: String -> destination to refund }
    )

    internal fun onSecondPasswordValidated(validatedSecondPassword: String) {
        compositeDisposable +=
            decryptPayload(validatedSecondPassword)
                .andThen(decryptBch())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { view.showProgressDialog() }
                .doOnTerminate { view.dismissProgressDialog() }
                .doOnError { Timber.e(it) }
                .subscribeBy(onComplete = { subscribeToViewState() })
    }

    private fun decryptPayload(validatedSecondPassword: String): Completable =
        Completable.fromCallable {
            payloadDataManager.decryptHDWallet(
                environmentConfig.bitcoinNetworkParameters,
                validatedSecondPassword
            )
        }

    private fun decryptBch(): Completable = Completable.fromCallable {
        bchDataManager.decryptWatchOnlyWallet(payloadDataManager.mnemonic)
    }
}
