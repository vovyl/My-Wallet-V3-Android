package piuk.blockchain.android.ui.send

import android.content.Intent
import android.support.design.widget.Snackbar
import com.blockchain.transactions.Memo
import com.blockchain.ui.chooser.AccountChooserActivity
import com.fasterxml.jackson.databind.ObjectMapper
import info.blockchain.api.data.UnspentOutputs
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import info.blockchain.wallet.api.Environment
import info.blockchain.wallet.api.data.FeeOptions
import info.blockchain.wallet.coin.GenericMetadataAccount
import info.blockchain.wallet.ethereum.EthereumAccount
import info.blockchain.wallet.exceptions.HDWalletException
import info.blockchain.wallet.payload.data.Account
import info.blockchain.wallet.payload.data.LegacyAddress
import info.blockchain.wallet.payment.Payment
import info.blockchain.wallet.util.FormatsUtil
import info.blockchain.wallet.util.PrivateKeyFactory
import info.blockchain.wallet.util.Tools
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import org.apache.commons.lang3.tuple.Pair
import org.bitcoinj.core.Address
import org.bitcoinj.core.ECKey
import org.web3j.crypto.RawTransaction
import org.web3j.utils.Convert
import piuk.blockchain.android.R
import piuk.blockchain.android.data.cache.DynamicFeeCache
import piuk.blockchain.android.ui.account.ItemAccount
import piuk.blockchain.android.ui.account.PaymentConfirmationDetails
import piuk.blockchain.android.ui.receive.WalletAccountHelper
import piuk.blockchain.android.ui.send.external.SendPresenterStrategy
import piuk.blockchain.android.util.StringUtils
import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.androidcore.data.api.EnvironmentConfig
import piuk.blockchain.androidcore.data.bitcoincash.BchDataManager
import piuk.blockchain.androidcore.data.currency.CurrencyFormatManager
import piuk.blockchain.androidcore.data.currency.CurrencyState
import piuk.blockchain.androidcore.data.currency.ETHDenomination
import piuk.blockchain.androidcore.data.ethereum.EthDataManager
import piuk.blockchain.androidcore.data.ethereum.models.CombinedEthModel
import piuk.blockchain.androidcore.data.exchangerate.ExchangeRateDataManager
import piuk.blockchain.androidcore.data.exchangerate.FiatExchangeRates
import piuk.blockchain.androidcore.data.exchangerate.toFiat
import piuk.blockchain.androidcore.data.fees.FeeDataManager
import piuk.blockchain.androidcore.data.payload.PayloadDataManager
import piuk.blockchain.androidcore.data.payments.SendDataManager
import piuk.blockchain.androidcore.utils.PrefsUtil
import piuk.blockchain.androidcore.utils.extensions.applySchedulers
import piuk.blockchain.androidcore.utils.extensions.emptySubscribe
import piuk.blockchain.androidcore.utils.helperfunctions.unsafeLazy
import piuk.blockchain.androidcoreui.utils.logging.Logging
import piuk.blockchain.androidcoreui.utils.logging.PaymentSentEvent
import timber.log.Timber
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.util.HashMap
import java.util.concurrent.TimeUnit

class OriginalSendPresenterStrategy(
    private val walletAccountHelper: WalletAccountHelper,
    private val payloadDataManager: PayloadDataManager,
    private val currencyState: CurrencyState,
    private val ethDataManager: EthDataManager,
    private val prefsUtil: PrefsUtil,
    private val exchangeRateFactory: ExchangeRateDataManager,
    private val stringUtils: StringUtils,
    private val sendDataManager: SendDataManager,
    private val dynamicFeeCache: DynamicFeeCache,
    private val feeDataManager: FeeDataManager,
    private val privateKeyFactory: PrivateKeyFactory,
    private val environmentSettings: EnvironmentConfig,
    private val bchDataManager: BchDataManager,
    private val currencyFormatManager: CurrencyFormatManager,
    private val exchangeRates: FiatExchangeRates,
    environmentConfig: EnvironmentConfig
) : SendPresenterStrategy<SendView>() {

    override fun onCurrencySelected(currency: CryptoCurrency) {
        currencyState.cryptoCurrency = currency
        when (currency) {
            CryptoCurrency.BTC -> onBitcoinChosen()
            CryptoCurrency.ETHER -> onEtherChosen()
            CryptoCurrency.BCH -> onBitcoinCashChosen()
            else -> throw IllegalArgumentException("This send presenter is not for $currency")
        }
    }

    override fun selectSendingAccount(data: Intent?, currency: CryptoCurrency) {
        when (currency) {
            CryptoCurrency.BTC -> selectSendingAccountBtc(data)
            CryptoCurrency.BCH -> selectSendingAccountBch(data)
            else -> throw IllegalArgumentException("Sending account is not for $currency")
        }
    }

    override fun selectReceivingAccount(data: Intent?, currency: CryptoCurrency) {
        when (currency) {
            CryptoCurrency.BTC -> selectReceivingAccountBtc(data)
            CryptoCurrency.BCH -> selectReceivingAccountBch(data)
            else -> throw IllegalArgumentException("Receiving account is not for $currency")
        }
    }

    private val pendingTransaction by unsafeLazy { PendingTransaction() }
    private val unspentApiResponsesBtc by unsafeLazy { HashMap<String, UnspentOutputs>() }
    private val unspentApiResponsesBch by unsafeLazy { HashMap<String, UnspentOutputs>() }
    private val networkParameters = environmentConfig.bitcoinNetworkParameters

    private var feeOptions: FeeOptions? = null
    private var textChangeSubject = PublishSubject.create<String>()
    private var absoluteSuggestedFee = BigInteger.ZERO
    private var maxAvailable = BigInteger.ZERO
    private var verifiedSecondPassword: String? = null

    /**
     * External changes.
     * Possible currency change, Account/address archive, Balance change
     */
    override fun onBroadcastReceived() {
        resetAccountList()
    }

    override fun onViewReady() {
        resetAccountList()
        setupTextChangeSubject()

        if (environmentSettings.environment == Environment.TESTNET) {
            currencyState.cryptoCurrency = CryptoCurrency.BTC
            view.hideCurrencyHeader()
        }
    }

    override fun onResume() {
        when (currencyState.cryptoCurrency) {
            CryptoCurrency.BTC -> onBitcoinChosen()
            CryptoCurrency.ETHER -> onEtherChosen()
            CryptoCurrency.BCH -> onBitcoinCashChosen()
            CryptoCurrency.XLM -> xlmNotSupported()
        }
    }

    private fun onBitcoinChosen() {
        currencyState.cryptoCurrency = CryptoCurrency.BTC
        view.showFeePriority()
        view.enableFeeDropdown()
        view.setCryptoMaxLength(17)
        resetState()
        calculateSpendableAmounts(spendAll = false, amountToSendText = "0")
        view.enableInput()
    }

    private fun onEtherChosen() {
        currencyState.cryptoCurrency = CryptoCurrency.ETHER
        view.hideFeePriority()
        view.setFeePrioritySelection(0)
        view.disableFeeDropdown()
        view.setCryptoMaxLength(30)
        resetState()
    }

    private fun onBitcoinCashChosen() {
        currencyState.cryptoCurrency = CryptoCurrency.BCH
        view.hideFeePriority()
        view.setFeePrioritySelection(0)
        view.disableFeeDropdown()
        view.setCryptoMaxLength(17)
        resetState()
        calculateSpendableAmounts(spendAll = false, amountToSendText = "0")
        view.enableInput()
    }

    private fun resetState() {
        compositeDisposable.clear()
        pendingTransaction.clear()
        view?.setSendButtonEnabled(true)
        absoluteSuggestedFee = BigInteger.ZERO
        view.updateFeeAmount("")
        resetAccountList()
        selectDefaultOrFirstFundedSendingAccount()
        view.hideMaxAvailable()
        clearCryptoAmount()
        clearReceivingAddress()
    }

    override fun onContinueClicked() {
        view?.showProgressDialog(R.string.app_name)

        checkManualAddressInput()

        when (currencyState.cryptoCurrency) {
            CryptoCurrency.BTC -> {
                Observable.just(validateBitcoinTransaction())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doAfterTerminate { view?.dismissProgressDialog() }
                    .addToCompositeDisposable(this)
                    .subscribe({
                        if (it.left) {
                            if (pendingTransaction.isWatchOnly) {
                                // returns to spendFromWatchOnly*BIP38 -> showPaymentReview()
                                val address =
                                    pendingTransaction.sendingObject.accountObject as LegacyAddress
                                view.showSpendFromWatchOnlyWarning((address).address)
                            } else if (pendingTransaction.isWatchOnly && verifiedSecondPassword != null) {
                                // Second password already verified
                                showPaymentReview()
                            } else {
                                // Checks if second pw needed then -> onNoSecondPassword()
                                view.showSecondPasswordDialog()
                            }
                        } else {
                            view.showSnackbar(it.right, Snackbar.LENGTH_LONG)
                        }
                    }, { Timber.e(it) })
            }
            CryptoCurrency.ETHER -> {
                validateEtherTransaction()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doAfterTerminate { view?.dismissProgressDialog() }
                    .doOnError { Timber.e(it) }
                    .addToCompositeDisposable(this)
                    .subscribe(
                        {
                            when {
                            //  Checks if second pw needed then -> onNoSecondPassword()
                                it.left -> view.showSecondPasswordDialog()
                                it.right == R.string.eth_support_contract_not_allowed -> view.showEthContractSnackbar()
                                else -> view.showSnackbar(it.right, Snackbar.LENGTH_LONG)
                            }
                        },
                        {
                            view.showSnackbar(
                                R.string.unexpected_error,
                                Snackbar.LENGTH_LONG
                            )
                            view.finishPage()
                        }
                    )
            }
            CryptoCurrency.BCH -> {
                isValidBitcoincashAddress()
                    .map {
                        if (!it) {
                            // Warn user if address is in base58 format since this might be a btc address
                            pendingTransaction.warningText =
                                stringUtils.getString(R.string.bch_address_warning)
                            pendingTransaction.warningSubText =
                                stringUtils.getString(R.string.bch_address_warning_subtext)
                        }
                    }
                    .flatMap { Observable.just(validateBitcoinCashTransaction()) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .doAfterTerminate { view?.dismissProgressDialog() }
                    .addToCompositeDisposable(this)
                    .subscribe(
                        {
                            if (it.left) {
                                if (pendingTransaction.isWatchOnly) {
                                    // returns to spendFromWatchOnly*BIP38 -> showPaymentReview()
                                    val address =
                                        pendingTransaction.sendingObject.accountObject as LegacyAddress
                                    view.showSpendFromWatchOnlyWarning((address).address)
                                } else if (pendingTransaction.isWatchOnly && verifiedSecondPassword != null) {
                                    // Second password already verified
                                    showPaymentReview()
                                } else {
                                    // Checks if second pw needed then -> onNoSecondPassword()
                                    view.showSecondPasswordDialog()
                                }
                            } else {
                                view.showSnackbar(it.right, Snackbar.LENGTH_LONG)
                            }
                        },
                        { Timber.e(it) }
                    )
            }
            CryptoCurrency.XLM -> xlmNotSupported()
        }
    }

    /**
     * Executes transaction
     */
    override fun submitPayment() {
        when (currencyState.cryptoCurrency) {
            CryptoCurrency.BTC -> submitBitcoinTransaction()
            CryptoCurrency.ETHER -> submitEthTransaction()
            CryptoCurrency.BCH -> submitBchTransaction()
            CryptoCurrency.XLM -> xlmNotSupported()
        }
    }

    private fun submitBitcoinTransaction() {
        view.showProgressDialog(R.string.app_name)

        getBtcChangeAddress()!!
            .addToCompositeDisposable(this)
            .doOnError {
                view.dismissProgressDialog()
                view.dismissConfirmationDialog()
                view.showTransactionFailed()
            }
            .map { pendingTransaction.changeAddress = it }
            .flatMap { getBtcKeys() }
            .flatMap {
                sendDataManager.submitBtcPayment(
                    pendingTransaction.unspentOutputBundle,
                    it,
                    pendingTransaction.receivingAddress,
                    pendingTransaction.changeAddress,
                    pendingTransaction.bigIntFee,
                    pendingTransaction.bigIntAmount
                )
            }
            .subscribe(
                { hash ->
                    Logging.logCustom(
                        PaymentSentEvent()
                            .putSuccess(true)
                            .putAmountForRange(
                                CryptoValue(
                                    CryptoCurrency.BTC,
                                    pendingTransaction.bigIntAmount
                                )
                            )
                    )

                    clearBtcUnspentResponseCache()
                    view.dismissProgressDialog()
                    view.dismissConfirmationDialog()
                    incrementBtcReceiveAddress()
                    handleSuccessfulPayment(hash, CryptoCurrency.BTC)
                },
                {
                    Timber.e(it)
                    view.dismissProgressDialog()
                    view.dismissConfirmationDialog()
                    view.showSnackbar(
                        stringUtils.getString(R.string.transaction_failed),
                        it.message,
                        Snackbar.LENGTH_INDEFINITE
                    )

                    Logging.logCustom(
                        PaymentSentEvent()
                            .putSuccess(false)
                            .putAmountForRange(
                                CryptoValue(
                                    CryptoCurrency.BTC,
                                    pendingTransaction.bigIntAmount
                                )
                            )
                    )
                }
            )
    }

    private fun submitBchTransaction() {
        view.showProgressDialog(R.string.app_name)

        pendingTransaction.receivingAddress =
            getFullBitcoinCashAddressFormat(pendingTransaction.receivingAddress)

        getBchChangeAddress()!!
            .addToCompositeDisposable(this)
            .doOnError {
                view.dismissProgressDialog()
                view.dismissConfirmationDialog()
                view.showTransactionFailed()
            }
            .map { pendingTransaction.changeAddress = it }
            .flatMap { getBchKeys() }
            .flatMap {
                sendDataManager.submitBchPayment(
                    pendingTransaction.unspentOutputBundle,
                    it,
                    pendingTransaction.receivingAddress,
                    pendingTransaction.changeAddress,
                    pendingTransaction.bigIntFee,
                    pendingTransaction.bigIntAmount
                )
            }
            .subscribe(
                { hash ->
                    Logging.logCustom(
                        PaymentSentEvent()
                            .putSuccess(true)
                            .putAmountForRange(
                                CryptoValue(
                                    CryptoCurrency.BCH,
                                    pendingTransaction.bigIntAmount
                                )
                            )
                    )

                    clearBchUnspentResponseCache()
                    view.dismissProgressDialog()
                    view.dismissConfirmationDialog()
                    incrementBchReceiveAddress()
                    handleSuccessfulPayment(hash, CryptoCurrency.BCH)
                },
                {
                    Timber.e(it)
                    view.dismissProgressDialog()
                    view.dismissConfirmationDialog()
                    view.showSnackbar(
                        stringUtils.getString(R.string.transaction_failed),
                        it.message,
                        Snackbar.LENGTH_INDEFINITE
                    )

                    Logging.logCustom(
                        PaymentSentEvent()
                            .putSuccess(false)
                            .putAmountForRange(
                                CryptoValue(
                                    CryptoCurrency.BCH,
                                    pendingTransaction.bigIntAmount
                                )
                            )
                    )
                }
            )
    }

    private fun getBtcKeys(): Observable<List<ECKey>> {
        return if (pendingTransaction.isHD(currencyState.cryptoCurrency)) {
            val account = pendingTransaction.sendingObject.accountObject as Account

            if (payloadDataManager.isDoubleEncrypted) {
                payloadDataManager.decryptHDWallet(networkParameters, verifiedSecondPassword)
            }
            Observable.just(
                payloadDataManager.getHDKeysForSigning(
                    account,
                    pendingTransaction.unspentOutputBundle
                )
            )
        } else {
            val legacyAddress = pendingTransaction.sendingObject.accountObject as LegacyAddress

            if (legacyAddress.tag == PendingTransaction.WATCH_ONLY_SPEND_TAG) {
                val ecKey = Tools.getECKeyFromKeyAndAddress(
                    legacyAddress.privateKey,
                    legacyAddress.address
                )
                Observable.just(listOf(ecKey))
            } else {
                Observable.just(
                    listOf(
                        payloadDataManager.getAddressECKey(
                            legacyAddress,
                            verifiedSecondPassword
                        )!!
                    )
                )
            }
        }
    }

    private fun getBchKeys(): Observable<List<ECKey>> {
        return if (pendingTransaction.isHD(currencyState.cryptoCurrency)) {
            // TODO(accountObject should rather contain keys for signing, not metadata)
            val account = pendingTransaction.sendingObject.accountObject as GenericMetadataAccount

            if (payloadDataManager.isDoubleEncrypted) {
                payloadDataManager.decryptHDWallet(networkParameters, verifiedSecondPassword)
                bchDataManager.decryptWatchOnlyWallet(payloadDataManager.mnemonic)
            }

            val hdAccountList = bchDataManager.getAccountList()
            val acc = hdAccountList.find {
                it.node.serializePubB58(environmentSettings.bitcoinCashNetworkParameters) == account.xpub
            } ?: throw HDWalletException("No matching private key found for ${account.xpub}")

            Observable.just(
                bchDataManager.getHDKeysForSigning(
                    acc,
                    pendingTransaction.unspentOutputBundle.spendableOutputs
                )
            )
        } else {
            val legacyAddress = pendingTransaction.sendingObject.accountObject as LegacyAddress

            if (legacyAddress.tag == PendingTransaction.WATCH_ONLY_SPEND_TAG) {
                val ecKey = Tools.getECKeyFromKeyAndAddress(
                    legacyAddress.privateKey,
                    legacyAddress.address
                )
                Observable.just(listOf(ecKey))
            } else {
                Observable.just(
                    listOf(
                        payloadDataManager.getAddressECKey(
                            legacyAddress,
                            verifiedSecondPassword
                        )!!
                    )
                )
            }
        }
    }

    private fun getBtcChangeAddress(): Observable<String>? {
        return if (pendingTransaction.isHD(currencyState.cryptoCurrency)) {
            val account = pendingTransaction.sendingObject.accountObject as Account
            payloadDataManager.getNextChangeAddress(account)
        } else {
            val legacyAddress = pendingTransaction.sendingObject.accountObject as LegacyAddress
            Observable.just(legacyAddress.address)
        }
    }

    private fun getBchChangeAddress(): Observable<String>? {
        return if (pendingTransaction.isHD(currencyState.cryptoCurrency)) {
            val account = pendingTransaction.sendingObject.accountObject as GenericMetadataAccount
            val position =
                bchDataManager.getAccountMetadataList().indexOfFirst { it.xpub == account.xpub }
            bchDataManager.getNextChangeCashAddress(position)
        } else {
            val legacyAddress = pendingTransaction.sendingObject.accountObject as LegacyAddress
            Observable.just(
                Address.fromBase58(
                    environmentSettings.bitcoinCashNetworkParameters,
                    legacyAddress.address
                ).toCashAddress()
            )
        }
    }

    private fun submitEthTransaction() {
        createEthTransaction()
            .addToCompositeDisposable(this)
            .flatMap {
                if (payloadDataManager.isDoubleEncrypted) {
                    payloadDataManager.decryptHDWallet(networkParameters, verifiedSecondPassword)
                }

                val ecKey = EthereumAccount.deriveECKey(
                    payloadDataManager.wallet!!.hdWallets[0].masterKey,
                    0
                )
                return@flatMap ethDataManager.signEthTransaction(it, ecKey)
            }
            .flatMap { ethDataManager.pushEthTx(it) }
            .flatMap { ethDataManager.setLastTxHashObservable(it, System.currentTimeMillis()) }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { view.showProgressDialog(R.string.app_name) }
            .doOnError {
                view.showSnackbar(
                    R.string.transaction_failed,
                    Snackbar.LENGTH_INDEFINITE
                )
            }
            .doOnTerminate {
                view.dismissProgressDialog()
                view.dismissConfirmationDialog()
            }
            .subscribe(
                {
                    Logging.logCustom(
                        PaymentSentEvent()
                            .putSuccess(true)
                            .putAmountForRange(
                                CryptoValue(
                                    CryptoCurrency.ETHER,
                                    pendingTransaction.bigIntAmount
                                )
                            )
                    )
                    // handleSuccessfulPayment(...) clears PendingTransaction object
                    handleSuccessfulPayment(it, CryptoCurrency.ETHER)
                },
                {
                    Timber.e(it)
                    Logging.logCustom(
                        PaymentSentEvent()
                            .putSuccess(false)
                            .putAmountForRange(
                                CryptoValue(
                                    CryptoCurrency.ETHER,
                                    pendingTransaction.bigIntAmount
                                )
                            )
                    )
                    view.showSnackbar(
                        R.string.transaction_failed,
                        Snackbar.LENGTH_INDEFINITE
                    )
                }
            )
    }

    private fun createEthTransaction(): Observable<RawTransaction> {
        val feeGwei = BigDecimal.valueOf(feeOptions!!.regularFee)
        val feeWei = Convert.toWei(feeGwei, Convert.Unit.GWEI)

        return ethDataManager.fetchEthAddress()
            .map { ethDataManager.getEthResponseModel()!!.getNonce() }
            .map {
                ethDataManager.createEthTransaction(
                    nonce = it,
                    to = pendingTransaction.receivingAddress,
                    gasPriceWei = feeWei.toBigInteger(),
                    gasLimitGwei = BigInteger.valueOf(feeOptions!!.gasLimit),
                    weiValue = pendingTransaction.bigIntAmount
                )
            }
    }

    private fun clearBtcUnspentResponseCache() {
        if (pendingTransaction.isHD(currencyState.cryptoCurrency)) {
            val account = pendingTransaction.sendingObject.accountObject as Account
            unspentApiResponsesBtc.remove(account.xpub)
        } else {
            val legacyAddress = pendingTransaction.sendingObject.accountObject as LegacyAddress
            unspentApiResponsesBtc.remove(legacyAddress.address)
        }
    }

    private fun clearBchUnspentResponseCache() {
        if (pendingTransaction.isHD(currencyState.cryptoCurrency)) {
            val account = pendingTransaction.sendingObject.accountObject as GenericMetadataAccount
            unspentApiResponsesBch.remove(account.xpub)
        } else {
            val legacyAddress = pendingTransaction.sendingObject.accountObject as LegacyAddress
            unspentApiResponsesBch.remove(legacyAddress.address)
        }
    }

    private fun incrementBtcReceiveAddress() {
        if (pendingTransaction.isHD(currencyState.cryptoCurrency)) {
            val account = pendingTransaction.sendingObject.accountObject as Account
            payloadDataManager.incrementChangeAddress(account)
            payloadDataManager.incrementReceiveAddress(account)
            updateInternalBtcBalances()
        }
    }

    private fun incrementBchReceiveAddress() {
        if (pendingTransaction.isHD(currencyState.cryptoCurrency)) {
            val account = pendingTransaction.sendingObject.accountObject as GenericMetadataAccount
            bchDataManager.incrementNextChangeAddress(account.xpub)
            bchDataManager.incrementNextReceiveAddress(account.xpub)
            updateInternalBchBalances()
        }
    }

    private fun handleSuccessfulPayment(hash: String, cryptoCurrency: CryptoCurrency): String {
        view?.showTransactionSuccess(cryptoCurrency)

        pendingTransaction.clear()
        unspentApiResponsesBtc.clear()
        unspentApiResponsesBch.clear()

        return hash
    }

    /**
     * Update balance immediately after spend - until refresh from server
     */
    private fun updateInternalBtcBalances() {
        try {
            val totalSent = pendingTransaction.bigIntAmount.add(pendingTransaction.bigIntFee)
            if (pendingTransaction.isHD(currencyState.cryptoCurrency)) {
                val account = pendingTransaction.sendingObject.accountObject as Account
                payloadDataManager.subtractAmountFromAddressBalance(
                    account.xpub,
                    totalSent.toLong()
                )
            } else {
                val address = pendingTransaction.sendingObject.accountObject as LegacyAddress
                payloadDataManager.subtractAmountFromAddressBalance(
                    address.address,
                    totalSent.toLong()
                )
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    /**
     * Update balance immediately after spend - until refresh from server
     */
    private fun updateInternalBchBalances() {
        try {
            val totalSent = pendingTransaction.bigIntAmount.add(pendingTransaction.bigIntFee)
            if (pendingTransaction.isHD(currencyState.cryptoCurrency)) {
                val account =
                    pendingTransaction.sendingObject.accountObject as GenericMetadataAccount
                bchDataManager.subtractAmountFromAddressBalance(account.xpub, totalSent)
            } else {
                val address = pendingTransaction.sendingObject.accountObject as LegacyAddress
                bchDataManager.subtractAmountFromAddressBalance(address.address, totalSent)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onNoSecondPassword() {
        showPaymentReview()
    }

    override fun onSecondPasswordValidated(secondPassword: String) {
        verifiedSecondPassword = secondPassword
        showPaymentReview()
    }

    private fun showPaymentReview() {
        val paymentDetails = getConfirmationDetails()
        var allowFeeChange = true

        when (currencyState.cryptoCurrency) {
            CryptoCurrency.BTC -> if (paymentDetails.isLargeTransaction) view.showLargeTransactionWarning()
            CryptoCurrency.ETHER -> allowFeeChange = false
            CryptoCurrency.BCH -> allowFeeChange = false
            CryptoCurrency.XLM -> xlmNotSupported()
        }

        view.showPaymentDetails(getConfirmationDetails(), null, null, allowFeeChange)
    }

    private fun checkManualAddressInput() {
        val address = view.getReceivingAddress()
        address?.let {
            // Only if valid address so we don't override with a label
            when (currencyState.cryptoCurrency) {
                CryptoCurrency.BTC ->
                    if (FormatsUtil.isValidBitcoinAddress(address)) pendingTransaction.receivingAddress =
                        address
                CryptoCurrency.ETHER ->
                    if (FormatsUtil.isValidEthereumAddress(address)) pendingTransaction.receivingAddress =
                        address
                CryptoCurrency.BCH -> {
                    if (FormatsUtil.isValidBitcoinCashAddress(
                            environmentSettings.bitcoinCashNetworkParameters,
                            address
                        ) ||
                        FormatsUtil.isValidBitcoinAddress(address)
                    )
                        pendingTransaction.receivingAddress = address
                }
                CryptoCurrency.XLM -> xlmNotSupported()
            }
        }
    }

    private fun getFullBitcoinCashAddressFormat(cashAddress: String): String {
        return if (!cashAddress.startsWith(environmentSettings.bitcoinCashNetworkParameters.bech32AddressPrefix) &&
            FormatsUtil.isValidBitcoinCashAddress(
                environmentSettings.bitcoinCashNetworkParameters,
                cashAddress
            )
        ) {
            environmentSettings.bitcoinCashNetworkParameters.bech32AddressPrefix +
                environmentSettings.bitcoinCashNetworkParameters.bech32AddressSeparator.toChar() +
                cashAddress
        } else {
            cashAddress
        }
    }

    private fun getConfirmationDetails(): PaymentConfirmationDetails {
        val pendingTransaction = pendingTransaction

        val details = PaymentConfirmationDetails()

        details.fromLabel = pendingTransaction.sendingObject.label
        details.toLabel = pendingTransaction.displayableReceivingLabel.removeBchUri()

        details.cryptoUnit = currencyState.cryptoCurrency.symbol
        details.fiatUnit = exchangeRates.fiatUnit
        details.fiatSymbol = currencyFormatManager.getFiatSymbol(
            currencyFormatManager.fiatCountryCode,
            view.locale
        )

        when (currencyState.cryptoCurrency) {
            CryptoCurrency.BTC -> {
                details.isLargeTransaction = isLargeTransaction()
                details.btcSuggestedFee = currencyFormatManager.getTextFromSatoshis(
                    absoluteSuggestedFee,
                    getDefaultDecimalSeparator()
                )

                details.cryptoTotal = currencyFormatManager.getTextFromSatoshis(
                    pendingTransaction.total,
                    getDefaultDecimalSeparator()
                )
                details.cryptoAmount = currencyFormatManager.getTextFromSatoshis(
                    pendingTransaction.bigIntAmount,
                    getDefaultDecimalSeparator()
                )
                details.cryptoFee = currencyFormatManager.getTextFromSatoshis(
                    pendingTransaction.bigIntFee,
                    getDefaultDecimalSeparator()
                )

                details.fiatFee = currencyFormatManager.getFormattedFiatValueFromSelectedCoinValue(
                    pendingTransaction.bigIntFee.toBigDecimal()
                )
                details.fiatAmount =
                    currencyFormatManager.getFormattedFiatValueFromSelectedCoinValue(
                        pendingTransaction.bigIntAmount.toBigDecimal()
                    )
                details.fiatTotal =
                    currencyFormatManager.getFormattedFiatValueFromSelectedCoinValue(
                        pendingTransaction.total.toBigDecimal()
                    )
            }
            CryptoCurrency.ETHER -> {

                var ethAmount = Convert.fromWei(
                    pendingTransaction.bigIntAmount.toString(),
                    Convert.Unit.ETHER
                )
                var ethFee =
                    Convert.fromWei(pendingTransaction.bigIntFee.toString(), Convert.Unit.ETHER)

                ethAmount = ethAmount.setScale(8, RoundingMode.HALF_UP).stripTrailingZeros()
                ethFee = ethFee.setScale(8, RoundingMode.HALF_UP).stripTrailingZeros()

                val ethTotal = ethAmount.add(ethFee)

                details.cryptoAmount = ethAmount.toString()
                details.cryptoFee = ethFee.toString()
                details.cryptoTotal = ethTotal.toString()

                details.fiatFee =
                    currencyFormatManager.getFormattedFiatValueFromSelectedCoinValue(
                        coinValue = ethFee,
                        convertEthDenomination = ETHDenomination.ETH
                    )
                details.fiatAmount =
                    currencyFormatManager.getFormattedFiatValueFromSelectedCoinValue(
                        coinValue = ethAmount,
                        convertEthDenomination = ETHDenomination.ETH
                    )
                details.fiatTotal =
                    currencyFormatManager.getFormattedFiatValueFromSelectedCoinValue(
                        coinValue = ethTotal,
                        convertEthDenomination = ETHDenomination.ETH
                    )
            }
            CryptoCurrency.BCH -> {

                details.cryptoTotal = currencyFormatManager.getTextFromSatoshis(
                    pendingTransaction.total,
                    getDefaultDecimalSeparator()
                )
                details.cryptoAmount = currencyFormatManager.getTextFromSatoshis(
                    pendingTransaction.bigIntAmount,
                    getDefaultDecimalSeparator()
                )
                details.cryptoFee = currencyFormatManager.getTextFromSatoshis(
                    pendingTransaction.bigIntFee,
                    getDefaultDecimalSeparator()
                )

                details.fiatFee = currencyFormatManager.getFormattedFiatValueFromSelectedCoinValue(
                    pendingTransaction.bigIntFee.toBigDecimal()
                )
                details.fiatAmount =
                    currencyFormatManager.getFormattedFiatValueFromSelectedCoinValue(
                        pendingTransaction.bigIntAmount.toBigDecimal()
                    )
                details.fiatTotal =
                    currencyFormatManager.getFormattedFiatValueFromSelectedCoinValue(
                        pendingTransaction.total.toBigDecimal()
                    )

                details.warningText = pendingTransaction.warningText
                details.warningSubtext = pendingTransaction.warningSubText
            }
            CryptoCurrency.XLM -> xlmNotSupported()
        }

        return details
    }

    private fun resetAccountList() {
        setReceiveHint(getAddressList().size)
    }

    private fun clearReceivingAddress() {
        view.updateReceivingAddress("")
    }

    override fun clearReceivingObject() {
        pendingTransaction.receivingObject = null
    }

    private fun clearCryptoAmount() {
        view.updateCryptoAmount(CryptoValue.zero(currencyState.cryptoCurrency))
    }

    private fun getAddressList(): List<ItemAccount> = walletAccountHelper.getAccountItems(currencyState.cryptoCurrency)

    private fun setReceiveHint(accountsCount: Int) {
        view.updateReceivingHintAndAccountDropDowns(currencyState.cryptoCurrency, accountsCount)
    }

    override fun selectDefaultOrFirstFundedSendingAccount() {
        val accountItem = walletAccountHelper.getDefaultOrFirstFundedAccount()
        view.updateSendingAddress(accountItem.label ?: accountItem.address!!)
        pendingTransaction.sendingObject = accountItem
    }

    /**
     * Get cached dynamic fee from new Fee options endpoint
     */
    private fun getSuggestedFee() {
        val observable = when (currencyState.cryptoCurrency) {
            CryptoCurrency.BTC -> feeDataManager.btcFeeOptions
                .doOnSubscribe { feeOptions = dynamicFeeCache.btcFeeOptions!! }
                .doOnNext { dynamicFeeCache.btcFeeOptions = it }

            CryptoCurrency.ETHER -> feeDataManager.ethFeeOptions
                .doOnSubscribe { feeOptions = dynamicFeeCache.ethFeeOptions!! }
                .doOnNext { dynamicFeeCache.ethFeeOptions = it }

            CryptoCurrency.BCH -> feeDataManager.bchFeeOptions
                .doOnSubscribe { feeOptions = dynamicFeeCache.bchFeeOptions!! }
                .doOnNext { dynamicFeeCache.bchFeeOptions = it }

            CryptoCurrency.XLM -> xlmNotSupported()
        }

        observable.addToCompositeDisposable(this)
            .subscribe(
                { /* No-op */ },
                {
                    Timber.e(it)
                    view.showSnackbar(
                        R.string.confirm_payment_fee_sync_error,
                        Snackbar.LENGTH_LONG
                    )
                    view.finishPage()
                }
            )
    }

    override fun getBitcoinFeeOptions(): FeeOptions? = dynamicFeeCache.btcFeeOptions

    private fun getFeePerKbFromPriority(@FeeType.FeePriorityDef feePriorityTemp: Int): BigInteger {
        getSuggestedFee()

        if (feeOptions == null) {
            // This is a stopgap in case of failure to prevent crashes.
            return BigInteger.ZERO
        }

        return when (feePriorityTemp) {
            FeeType.FEE_OPTION_CUSTOM -> BigInteger.valueOf(view.getCustomFeeValue() * 1000)
            FeeType.FEE_OPTION_PRIORITY -> BigInteger.valueOf(feeOptions!!.priorityFee * 1000)
            FeeType.FEE_OPTION_REGULAR -> BigInteger.valueOf(feeOptions!!.regularFee * 1000)
            else -> BigInteger.valueOf(feeOptions!!.regularFee * 1000)
        }
    }

    /**
     * Retrieves unspent api data in memory. If not in memory yet, it will be retrieved and added.
     */
    private fun getUnspentApiResponse(address: String): Observable<UnspentOutputs> {
        when (currencyState.cryptoCurrency) {
            CryptoCurrency.BTC -> {
                return if (payloadDataManager.getAddressBalance(address).toLong() > 0) {
                    return if (unspentApiResponsesBtc.containsKey(address)) {
                        Observable.just(unspentApiResponsesBtc[address])
                    } else {
                        sendDataManager.getUnspentOutputs(address)
                    }
                } else {
                    Observable.error(Throwable("No funds - skipping call to unspent API"))
                }
            }
            CryptoCurrency.BCH -> {
                return if (bchDataManager.getAddressBalance(address).toLong() > 0) {
                    return if (unspentApiResponsesBch.containsKey(address)) {
                        Observable.just(unspentApiResponsesBch[address])
                    } else {
                        sendDataManager.getUnspentBchOutputs(address)
                    }
                } else {
                    Observable.error(Throwable("No funds - skipping call to unspent API"))
                }
            }
            else -> throw IllegalArgumentException("${currencyState.cryptoCurrency} does not use unspent endpoint")
        }
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getSuggestedAbsoluteFee(
        coins: UnspentOutputs,
        amountToSend: CryptoValue,
        feePerKb: BigInteger
    ): BigInteger {
        val spendableCoins = sendDataManager.getSpendableCoins(coins, amountToSend, feePerKb)
        return spendableCoins.absoluteFee
    }

    /**
     * Update absolute fee with smallest denomination of crypto currency (satoshi, wei, etc)
     */
    private fun updateFee(fee: BigInteger) {
        absoluteSuggestedFee = fee

        val cryptoValue = CryptoValue(currencyState.cryptoCurrency, absoluteSuggestedFee)

        view.updateFeeAmount(cryptoValue, exchangeRates)
    }

    private fun updateMaxAvailable(balanceAfterFee: BigInteger) {
        maxAvailable = balanceAfterFee
        view.showMaxAvailable()

        // Format for display
        view.updateMaxAvailable(
            stringUtils.getString(R.string.max_available) +
                " ${currencyFormatManager.getFormattedSelectedCoinValueWithUnit(maxAvailable)}"
        )

        if (balanceAfterFee <= Payment.DUST) {
            view.updateMaxAvailable(stringUtils.getString(R.string.insufficient_funds))
            view.updateMaxAvailableColor(R.color.product_red_medium)
        } else {
            view.updateMaxAvailableColor(R.color.primary_blue_accent)
        }
    }

    override fun onCryptoTextChange(cryptoText: String) {
        textChangeSubject.onNext(cryptoText)
    }

    override fun onAddressTextChange(address: String) {}

    /**
     * Calculate amounts on crypto text change
     */
    private fun setupTextChangeSubject() {
        textChangeSubject.debounce(300, TimeUnit.MILLISECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                calculateSpendableAmounts(spendAll = false, amountToSendText = it)
            }
            .emptySubscribe()
    }

    override fun onSpendMaxClicked() {
        calculateSpendableAmounts(spendAll = true, amountToSendText = null)
    }

    private fun calculateSpendableAmounts(spendAll: Boolean, amountToSendText: String?) {
        view.setSendButtonEnabled(true)
        view.hideMaxAvailable()
        view.clearWarning()

        val feePerKb = getFeePerKbFromPriority(view.getFeePriority())

        when (currencyState.cryptoCurrency) {
            CryptoCurrency.BTC -> calculateUnspentBtc(spendAll, amountToSendText, feePerKb)
            CryptoCurrency.ETHER -> getEthAccountResponse(spendAll, amountToSendText)
            CryptoCurrency.BCH -> calculateUnspentBch(spendAll, amountToSendText, feePerKb)
            CryptoCurrency.XLM -> xlmNotSupported()
        }
    }

    private fun calculateUnspentBtc(
        spendAll: Boolean,
        amountToSendText: String?,
        feePerKb: BigInteger
    ) {

        if (pendingTransaction.sendingObject == null ||
            pendingTransaction.sendingObject.address == null
        ) {
            // This shouldn't happen, but handle case anyway in case of low memory scenario
            onBitcoinCashChosen()
            return
        }

        val address = pendingTransaction.sendingObject.address!!

        getUnspentApiResponse(address)
            .debounce(200, TimeUnit.MILLISECONDS)
            .applySchedulers()
            .subscribe(
                { coins ->
                    val amountToSend = currencyFormatManager.getSatoshisFromText(
                        amountToSendText,
                        getDefaultDecimalSeparator()
                    )

                    // Future use. There might be some unconfirmed funds. Not displaying a warning currently (to line up with iOS and Web wallet)
                    if (coins.notice != null) {
                        view.updateWarning(coins.notice)
                    } else {
                        view.clearWarning()
                    }

                    updateFee(getSuggestedAbsoluteFee(
                        coins,
                        CryptoValue.bitcoinFromSatoshis(amountToSend),
                        feePerKb
                    ))

                    suggestedFeePayment(coins, CryptoValue.bitcoinFromSatoshis(amountToSend), spendAll, feePerKb)
                },
                { throwable ->
                    Timber.e(throwable)
                    // No unspent outputs
                    updateMaxAvailable(BigInteger.ZERO)
                    updateFee(BigInteger.ZERO)
                    pendingTransaction.unspentOutputBundle = null
                }
            )
    }

    private fun calculateUnspentBch(
        spendAll: Boolean,
        amountToSendText: String?,
        feePerKb: BigInteger
    ) {

        if (pendingTransaction.sendingObject == null ||
            pendingTransaction.sendingObject.address == null
        ) {
            // This shouldn't happen, but handle case anyway in case of low memory scenario
            onBitcoinCashChosen()
            return
        }

        val address = pendingTransaction.sendingObject.address!!

        getUnspentApiResponse(address)
            .debounce(200, TimeUnit.MILLISECONDS)
            .applySchedulers()
            .subscribe(
                { coins ->
                    val amountToSend = currencyFormatManager.getSatoshisFromText(
                        amountToSendText,
                        getDefaultDecimalSeparator()
                    )

                    // Future use. There might be some unconfirmed funds. Not displaying a warning currently
                    // (to line up with iOS and Web wallet)
                    if (coins.notice != null) {
                        view.updateWarning(coins.notice)
                    } else {
                        view.clearWarning()
                    }

                    updateFee(getSuggestedAbsoluteFee(
                        coins,
                        CryptoValue.bitcoinCashFromSatoshis(amountToSend),
                        feePerKb
                    ))

                    suggestedFeePayment(coins, CryptoValue.bitcoinCashFromSatoshis(amountToSend), spendAll, feePerKb)
                },
                { throwable ->
                    Timber.e(throwable)
                    // No unspent outputs
                    updateMaxAvailable(BigInteger.ZERO)
                    updateFee(BigInteger.ZERO)
                    pendingTransaction.unspentOutputBundle = null
                }
            )
    }

    /**
     * Payment will use suggested dynamic fee
     */
    @Throws(UnsupportedEncodingException::class)
    private fun suggestedFeePayment(
        coins: UnspentOutputs,
        amountToSend: CryptoValue,
        spendAll: Boolean,
        feePerKb: BigInteger
    ) {
        var amount = amountToSend.amount

        // Calculate sweepable amount to display max available
        val sweepBundle = sendDataManager.getMaximumAvailable(amountToSend.currency, coins, feePerKb)
        val sweepableAmount = sweepBundle.left

        updateMaxAvailable(sweepableAmount)

        if (spendAll) {
            amount = sweepableAmount
            view?.updateCryptoAmount(CryptoValue(currencyState.cryptoCurrency, sweepableAmount))
        }

        val unspentOutputBundle = sendDataManager.getSpendableCoins(coins, amountToSend, feePerKb)

        pendingTransaction.bigIntAmount = amount
        pendingTransaction.unspentOutputBundle = unspentOutputBundle
        pendingTransaction.bigIntFee = pendingTransaction.unspentOutputBundle.absoluteFee
    }

    private fun getEthAccountResponse(spendAll: Boolean, amountToSendText: String?) {
        view.showMaxAvailable()

        if (ethDataManager.getEthResponseModel() == null) {
            ethDataManager.fetchEthAddress()
                .addToCompositeDisposable(this)
                .doOnError { view.showSnackbar(R.string.api_fail, Snackbar.LENGTH_INDEFINITE) }
                .subscribe { calculateUnspentEth(it, spendAll, amountToSendText) }
        } else {
            ethDataManager.getEthResponseModel()?.let {
                calculateUnspentEth(it, spendAll, amountToSendText)
            }
        }
    }

    private fun calculateUnspentEth(
        combinedEthModel: CombinedEthModel,
        spendAll: Boolean,
        amountToSendText: String?
    ) {

        val amountToSendSanitised = if (amountToSendText.isNullOrEmpty()) "0" else amountToSendText

        val gwei = BigDecimal.valueOf(feeOptions!!.gasLimit * feeOptions!!.regularFee)
        val wei = Convert.toWei(gwei, Convert.Unit.GWEI)

        updateFee(wei.toBigInteger())
        pendingTransaction.bigIntFee = wei.toBigInteger()

        val addressResponse = combinedEthModel.getAddressResponse()
        maxAvailable = addressResponse!!.balance.minus(wei.toBigInteger())
        maxAvailable = maxAvailable.max(BigInteger.ZERO)

        val availableEth = Convert.fromWei(maxAvailable.toString(), Convert.Unit.ETHER)
        if (spendAll) {
            view?.updateCryptoAmount(CryptoValue.etherFromMajor(availableEth ?: BigDecimal.ZERO))
            pendingTransaction.bigIntAmount = availableEth.toBigInteger()
        } else {
            pendingTransaction.bigIntAmount =
                currencyFormatManager.getWeiFromText(
                    amountToSendSanitised,
                    getDefaultDecimalSeparator()
                )
        }

        // Format for display
        val number = currencyFormatManager.getFormattedEthValue(
            availableEth,
            ETHDenomination.ETH
        )
        view.updateMaxAvailable("${stringUtils.getString(R.string.max_available)} $number")

        // No dust in Ethereum
        if (maxAvailable <= BigInteger.ZERO) {
            view.updateMaxAvailable(stringUtils.getString(R.string.insufficient_funds))
            view.updateMaxAvailableColor(R.color.product_red_medium)
        } else {
            view.updateMaxAvailableColor(R.color.primary_blue_accent)
        }

        // Check if any pending ether txs exist and warn user
        isLastEthTxPending()
            .addToCompositeDisposable(this)
            .subscribe(
                {
                    /* No-op */
                },
                { Timber.e(it) }
            )
    }

    @Suppress("CascadeIf")
    override fun handleURIScan(untrimmedscanData: String?) {
        if (untrimmedscanData == null) return

        var scanData = untrimmedscanData.trim { it <= ' ' }
            .replace("ethereum:", "")
        val address: String
        val amount: String?

        scanData = FormatsUtil.getURIFromPoorlyFormedBIP21(scanData)

        if (FormatsUtil.isValidBitcoinCashAddress(
                environmentSettings.bitcoinCashNetworkParameters,
                scanData
            )
        ) {
            onBitcoinCashChosen()
            address = scanData
        } else if (FormatsUtil.isBitcoinUri(scanData)) {
            onBitcoinChosen()
            address = FormatsUtil.getBitcoinAddress(scanData)
            amount = FormatsUtil.getBitcoinAmount(scanData)

            if (address.isEmpty() && amount == "0.0000" && scanData.contains("bitpay")) {
                view.showSnackbar(R.string.error_bitpay_not_supported, Snackbar.LENGTH_LONG)
                return
            }

            // Convert to correct units
            try {
                val cryptoValue = CryptoValue(currencyState.cryptoCurrency, amount.toBigInteger())
                val fiatValue = cryptoValue.toFiat(exchangeRates)
                view?.updateCryptoAmount(cryptoValue)
                view?.updateFiatAmount(fiatValue)
            } catch (e: Exception) {
                // ignore
            }
        } else if (FormatsUtil.isValidEthereumAddress(scanData)) {
            onEtherChosen()
            address = scanData
            view?.updateCryptoAmount(CryptoValue.zero(currencyState.cryptoCurrency))
        } else if (FormatsUtil.isValidBitcoinAddress(scanData)) {
            if (currencyState.cryptoCurrency == CryptoCurrency.BTC) {
                onBitcoinChosen()
                address = scanData
            } else {
                onBitcoinCashChosen()
                address = scanData
            }
        } else {
            onBitcoinChosen()
            view.showSnackbar(R.string.invalid_address, Snackbar.LENGTH_LONG)
            return
        }

        if (address != "") {
            pendingTransaction.receivingObject = null
            pendingTransaction.receivingAddress = address
            view.updateReceivingAddress(address.removeBchUri())
        }
    }

    override fun handlePrivxScan(scanData: String?) {
        if (scanData == null) return

        val format = privateKeyFactory.getFormat(scanData)

        if (format == null) {
            view?.showSnackbar(R.string.privkey_error, Snackbar.LENGTH_LONG)
            return
        }

        when (format) {
            PrivateKeyFactory.BIP38 -> view?.showBIP38PassphrasePrompt(scanData) // BIP38 needs passphrase
            else -> spendFromWatchOnlyNonBIP38(format, scanData)
        }
    }

    private fun spendFromWatchOnlyNonBIP38(format: String, scanData: String) {
        try {
            val key = privateKeyFactory.getKey(format, scanData)
            val legacyAddress = pendingTransaction.sendingObject.accountObject as LegacyAddress
            setTempLegacyAddressPrivateKey(legacyAddress, key)
        } catch (e: Exception) {
            view?.showSnackbar(R.string.no_private_key, Snackbar.LENGTH_LONG)
            Timber.e(e)
        }
    }

    override fun spendFromWatchOnlyBIP38(pw: String, scanData: String) {
        sendDataManager.getEcKeyFromBip38(
            pw,
            scanData,
            environmentSettings.bitcoinNetworkParameters
        ).addToCompositeDisposable(this)
            .subscribe(
                {
                    val legacyAddress =
                        pendingTransaction.sendingObject.accountObject as LegacyAddress
                    setTempLegacyAddressPrivateKey(legacyAddress, it)
                },
                { view?.showSnackbar(R.string.bip38_error, Snackbar.LENGTH_LONG) }
            )
    }

    private fun setTempLegacyAddressPrivateKey(legacyAddress: LegacyAddress, key: ECKey?) {
        if (key != null && key.hasPrivKey() && legacyAddress.address == key.toAddress(
                environmentSettings.bitcoinNetworkParameters
            ).toString()
        ) {

            // Create copy, otherwise pass by ref will override private key in wallet payload
            val tempLegacyAddress = LegacyAddress()
            tempLegacyAddress.setPrivateKeyFromBytes(key.privKeyBytes)
            tempLegacyAddress.address =
                key.toAddress(environmentSettings.bitcoinNetworkParameters).toString()
            tempLegacyAddress.label = legacyAddress.label
            tempLegacyAddress.tag = PendingTransaction.WATCH_ONLY_SPEND_TAG
            pendingTransaction.sendingObject.accountObject = tempLegacyAddress

            showPaymentReview()
        } else {
            view?.showSnackbar(R.string.invalid_private_key, Snackbar.LENGTH_LONG)
        }
    }

    private fun onSendingBtcLegacyAddressSelected(legacyAddress: LegacyAddress) {
        var label = legacyAddress.label
        if (label.isNullOrEmpty()) {
            label = legacyAddress.address
        }

        pendingTransaction.sendingObject = ItemAccount(
            label,
            null,
            null,
            null,
            legacyAddress,
            legacyAddress.address
        )

        view.updateSendingAddress(label)
        calculateSpendableAmounts(false, "0")
    }

    private fun onSendingBtcAccountSelected(account: Account) {
        var label = account.label
        if (label.isNullOrEmpty()) {
            label = account.xpub
        }

        pendingTransaction.sendingObject = ItemAccount(
            label,
            null,
            null,
            null,
            account,
            account.xpub
        )

        view.updateSendingAddress(label)
        calculateSpendableAmounts(false, "0")
    }

    private fun onReceivingBtcLegacyAddressSelected(legacyAddress: LegacyAddress) {
        var label = legacyAddress.label
        if (label.isNullOrEmpty()) {
            label = legacyAddress.address
        }

        pendingTransaction.receivingObject = ItemAccount(
            label,
            null,
            null,
            null,
            legacyAddress,
            legacyAddress.address
        )
        pendingTransaction.receivingAddress = legacyAddress.address

        view.updateReceivingAddress(label)

        if (legacyAddress.isWatchOnly && shouldWarnWatchOnly()) {
            view.showWatchOnlyWarning(legacyAddress.address)
        }
    }

    private fun onSendingBchLegacyAddressSelected(legacyAddress: LegacyAddress) {

        var cashAddress = legacyAddress.address

        if (!FormatsUtil.isValidBitcoinCashAddress(
                environmentSettings.bitcoinCashNetworkParameters,
                legacyAddress.address
            ) &&
            FormatsUtil.isValidBitcoinAddress(legacyAddress.address)
        ) {
            cashAddress = Address.fromBase58(
                environmentSettings.bitcoinCashNetworkParameters,
                legacyAddress.address
            ).toCashAddress()
        }

        var label = legacyAddress.label
        if (label.isNullOrEmpty()) {
            label = cashAddress.removeBchUri()
        }

        pendingTransaction.sendingObject = ItemAccount(
            label,
            null,
            null,
            null,
            legacyAddress,
            legacyAddress.address
        )

        view.updateSendingAddress(label)
        calculateSpendableAmounts(false, "0")
    }

    private fun onSendingBchAccountSelected(account: GenericMetadataAccount) {
        var label = account.label
        if (label.isNullOrEmpty()) {
            label = account.xpub
        }

        pendingTransaction.sendingObject = ItemAccount(
            label,
            null,
            null,
            null,
            account,
            account.xpub
        )

        view.updateSendingAddress(label)
        calculateSpendableAmounts(false, "0")
    }

    private fun onReceivingBchLegacyAddressSelected(legacyAddress: LegacyAddress) {

        var cashAddress = legacyAddress.address

        if (!FormatsUtil.isValidBitcoinCashAddress(
                environmentSettings.bitcoinCashNetworkParameters,
                legacyAddress.address
            ) &&
            FormatsUtil.isValidBitcoinAddress(legacyAddress.address)
        ) {
            cashAddress = Address.fromBase58(
                environmentSettings.bitcoinCashNetworkParameters,
                legacyAddress.address
            ).toCashAddress()
        }

        var label = legacyAddress.label
        if (label.isNullOrEmpty()) {
            label = cashAddress.removeBchUri()
        }

        pendingTransaction.receivingObject = ItemAccount(
            label,
            null,
            null,
            null,
            legacyAddress,
            cashAddress
        )
        pendingTransaction.receivingAddress = cashAddress

        view.updateReceivingAddress(label.removeBchUri())

        if (legacyAddress.isWatchOnly && shouldWarnWatchOnly()) {
            view.showWatchOnlyWarning(cashAddress)
        }
    }

    private fun String.removeBchUri(): String = this.replace("bitcoincash:", "")

    private fun shouldWarnWatchOnly(): Boolean =
        prefsUtil.getValue(PREF_WARN_WATCH_ONLY_SPEND, true)

    override fun setWarnWatchOnlySpend(warn: Boolean) {
        prefsUtil.setValue(PREF_WARN_WATCH_ONLY_SPEND, warn)
    }

    private fun onReceivingBtcAccountSelected(account: Account) {
        var label = account.label
        if (label.isNullOrEmpty()) {
            label = account.xpub
        }

        pendingTransaction.receivingObject = ItemAccount(
            label,
            null,
            null,
            null,
            account,
            account.xpub
        )

        view.updateReceivingAddress(label)

        payloadDataManager.getNextReceiveAddress(account)
            .doOnNext { pendingTransaction.receivingAddress = it }
            .addToCompositeDisposable(this)
            .subscribe(
                { /* No-op */ },
                { view.showSnackbar(R.string.unexpected_error, Snackbar.LENGTH_LONG) }
            )
    }

    private fun onReceivingBchAccountSelected(account: GenericMetadataAccount) {
        var label = account.label
        if (label.isNullOrEmpty()) {
            label = account.xpub
        }

        pendingTransaction.receivingObject = ItemAccount(
            label,
            null,
            null,
            null,
            account,
            account.xpub
        )

        view.updateReceivingAddress(label)

        val position =
            bchDataManager.getAccountMetadataList().indexOfFirst { it.xpub == account.xpub }

        bchDataManager.getNextReceiveCashAddress(position)
            .doOnNext { pendingTransaction.receivingAddress = it }
            .addToCompositeDisposable(this)
            .subscribe(
                { /* No-op */ },
                { view.showSnackbar(R.string.unexpected_error, Snackbar.LENGTH_LONG) }
            )
    }

    private fun selectSendingAccountBtc(data: Intent?) {
        try {
            val type: Class<*> =
                Class.forName(data?.getStringExtra(AccountChooserActivity.EXTRA_SELECTED_OBJECT_TYPE))
            val any = ObjectMapper().readValue(
                data!!.getStringExtra(AccountChooserActivity.EXTRA_SELECTED_ITEM),
                type
            )

            when (any) {
                is LegacyAddress -> onSendingBtcLegacyAddressSelected(any)
                is Account -> onSendingBtcAccountSelected(any)
                else -> throw IllegalArgumentException("No method for handling $type available")
            }
        } catch (e: ClassNotFoundException) {
            Timber.e(e)
            selectDefaultOrFirstFundedSendingAccount()
        } catch (e: IOException) {
            Timber.e(e)
            selectDefaultOrFirstFundedSendingAccount()
        }
    }

    private fun selectSendingAccountBch(data: Intent?) {
        try {
            val type: Class<*> =
                Class.forName(data?.getStringExtra(AccountChooserActivity.EXTRA_SELECTED_OBJECT_TYPE))
            val any = ObjectMapper().readValue(
                data!!.getStringExtra(AccountChooserActivity.EXTRA_SELECTED_ITEM),
                type
            )

            when (any) {
                is LegacyAddress -> onSendingBchLegacyAddressSelected(any)
                is GenericMetadataAccount -> onSendingBchAccountSelected(any)
                else -> throw IllegalArgumentException("No method for handling $type available")
            }
        } catch (e: ClassNotFoundException) {
            Timber.e(e)
            selectDefaultOrFirstFundedSendingAccount()
        } catch (e: IOException) {
            Timber.e(e)
            selectDefaultOrFirstFundedSendingAccount()
        }
    }

    private fun selectReceivingAccountBtc(data: Intent?) {
        try {
            val type: Class<*> =
                Class.forName(data?.getStringExtra(AccountChooserActivity.EXTRA_SELECTED_OBJECT_TYPE))
            val any = ObjectMapper().readValue(
                data?.getStringExtra(AccountChooserActivity.EXTRA_SELECTED_ITEM),
                type
            )

            when (any) {
                is LegacyAddress -> onReceivingBtcLegacyAddressSelected(any)
                is Account -> onReceivingBtcAccountSelected(any)
                else -> throw IllegalArgumentException("No method for handling $type available")
            }
        } catch (e: ClassNotFoundException) {
            Timber.e(e)
        } catch (e: IOException) {
            Timber.e(e)
        }
    }

    private fun selectReceivingAccountBch(data: Intent?) {
        try {
            val type: Class<*> =
                Class.forName(data?.getStringExtra(AccountChooserActivity.EXTRA_SELECTED_OBJECT_TYPE))
            val any = ObjectMapper().readValue(
                data?.getStringExtra(AccountChooserActivity.EXTRA_SELECTED_ITEM),
                type
            )

            when (any) {
                is LegacyAddress -> onReceivingBchLegacyAddressSelected(any)
                is GenericMetadataAccount -> onReceivingBchAccountSelected(any)
                else -> throw IllegalArgumentException("No method for handling $type available")
            }
        } catch (e: ClassNotFoundException) {
            Timber.e(e)
        } catch (e: IOException) {
            Timber.e(e)
        }
    }

    private fun isValidBitcoinAmount(bAmount: BigInteger?): Boolean {
        if (bAmount == null) {
            return false
        }

        // Test that amount is more than dust
        if (bAmount.compareTo(Payment.DUST) == -1) {
            return false
        }

        // Test that amount does not exceed btc limit
        if (bAmount.compareTo(BigInteger.valueOf(2_100_000_000_000_000L)) == 1) {
            clearCryptoAmount()
            return false
        }

        // Test that amount is not zero
        return bAmount >= BigInteger.ZERO
    }

    private fun validateBitcoinTransaction(): Pair<Boolean, Int> {
        var validated = true
        var errorMessage = R.string.unexpected_error

        if (pendingTransaction.receivingAddress == null ||
            !FormatsUtil.isValidBitcoinAddress(pendingTransaction.receivingAddress)
        ) {
            errorMessage = R.string.invalid_bitcoin_address
            validated = false
        } else if (pendingTransaction.bigIntAmount == null || !isValidBitcoinAmount(
                pendingTransaction.bigIntAmount
            )
        ) {
            errorMessage = R.string.invalid_amount
            validated = false
        } else if (pendingTransaction.unspentOutputBundle == null ||
            pendingTransaction.unspentOutputBundle.spendableOutputs == null
        ) {
            errorMessage = R.string.no_confirmed_funds
            validated = false
        } else if (maxAvailable == null || maxAvailable.compareTo(pendingTransaction.bigIntAmount) == -1) {
            errorMessage = R.string.insufficient_funds
            validated = false
        } else if (pendingTransaction.unspentOutputBundle.spendableOutputs.isEmpty()) {
            errorMessage = R.string.insufficient_funds
            validated = false
        }

        return Pair.of(validated, errorMessage)
    }

    private fun isValidBitcoincashAddress() =
        Observable.just(
            FormatsUtil.isValidBitcoinCashAddress(
                environmentSettings.bitcoinCashNetworkParameters,
                pendingTransaction.receivingAddress
            )
        )

    private fun validateBitcoinCashTransaction(): Pair<Boolean, Int> {
        var validated = true
        var errorMessage = R.string.unexpected_error

        if (pendingTransaction.receivingAddress.isNullOrEmpty()) {
            errorMessage = R.string.bch_invalid_address
            validated = false

            // Same amount validation as bitcoin
        } else if (pendingTransaction.bigIntAmount == null ||
            !isValidBitcoinAmount(pendingTransaction.bigIntAmount)
        ) {
            errorMessage = R.string.invalid_amount
            validated = false
        } else if (pendingTransaction.unspentOutputBundle == null ||
            pendingTransaction.unspentOutputBundle.spendableOutputs == null
        ) {
            errorMessage = R.string.no_confirmed_funds
            validated = false
        } else if (maxAvailable == null || maxAvailable.compareTo(pendingTransaction.bigIntAmount) == -1) {
            errorMessage = R.string.insufficient_funds
            validated = false
        } else if (pendingTransaction.unspentOutputBundle.spendableOutputs.isEmpty()) {
            errorMessage = R.string.insufficient_funds
            validated = false
        }

        return Pair.of(validated, errorMessage)
    }

    private fun isValidEtherAmount(bAmount: BigInteger?): Boolean {
        return (bAmount != null && bAmount >= BigInteger.ZERO)
    }

    private fun validateEtherTransaction(): Observable<Pair<Boolean, Int>> {
        if (pendingTransaction.receivingAddress == null) {
            return Observable.just(Pair.of(false, R.string.eth_invalid_address))
        } else {
            return ethDataManager.getIfContract(pendingTransaction.receivingAddress)
                .map { isContract ->
                    var validated = true
                    var errorMessage = R.string.unexpected_error

                    // Validate not contract
                    if (isContract) {
                        errorMessage = R.string.eth_support_contract_not_allowed
                        validated = false
                    }
                    Pair.of(validated, errorMessage)
                }.map { errorPair ->
                    if (errorPair.left) {
                        var validated = true
                        var errorMessage = R.string.unexpected_error

                        // Validate address
                        if (pendingTransaction.receivingAddress == null ||
                            !FormatsUtil.isValidEthereumAddress(
                                pendingTransaction.receivingAddress
                            )
                        ) {
                            errorMessage = R.string.eth_invalid_address
                            validated = false
                        }

                        // Validate amount
                        if (!isValidEtherAmount(pendingTransaction.bigIntAmount) ||
                            pendingTransaction.bigIntAmount <= BigInteger.ZERO
                        ) {
                            errorMessage = R.string.invalid_amount
                            validated = false
                        }

                        // Validate sufficient funds
                        if (maxAvailable.compareTo(pendingTransaction.bigIntAmount) == -1) {
                            errorMessage = R.string.insufficient_funds
                            validated = false
                        }
                        Pair.of(validated, errorMessage)
                    } else {
                        errorPair
                    }
                }.flatMap { errorPair ->
                    if (errorPair.left) {
                        // Validate address does not have unconfirmed funds
                        isLastEthTxPending()
                    } else {
                        Observable.just(errorPair)
                    }
                }
        }
    }

    private fun isLastEthTxPending() =
        ethDataManager.isLastTxPending()
            .observeOn(AndroidSchedulers.mainThread())
            .map { hasUnconfirmed: Boolean ->

                if (hasUnconfirmed) {
                    view?.disableInput()
                    view?.updateMaxAvailable(stringUtils.getString(R.string.eth_unconfirmed_wait))
                    view?.updateMaxAvailableColor(R.color.product_red_medium)
                } else {
                    view.enableInput()
                }

                val errorMessage = R.string.eth_unconfirmed_wait
                Pair.of(!hasUnconfirmed, errorMessage)
            }

    /**
     * Returns true if bitcoin transaction is large by checking against 3 criteria:
     *
     * If the fee > $0.50
     * If the Tx size is over 1kB
     * If the ratio of fee/amount is over 1%
     */
    private fun isLargeTransaction(): Boolean {
        val usdValue = CryptoValue(CryptoCurrency.BTC, absoluteSuggestedFee)
            .toFiat(exchangeRateFactory, "USD")
        val txSize = sendDataManager.estimateSize(
            pendingTransaction.unspentOutputBundle.spendableOutputs.size,
            2
        ) // assume change
        val relativeFee =
            absoluteSuggestedFee.toDouble() / pendingTransaction.bigIntAmount.toDouble() * 100.0

        return usdValue.toBigDecimal() > SendModel.LARGE_TX_FEE.toBigDecimal() &&
            txSize > SendModel.LARGE_TX_SIZE &&
            relativeFee > SendModel.LARGE_TX_PERCENTAGE
    }

    override fun disableAdvancedFeeWarning() {
        prefsUtil.setValue(PrefsUtil.KEY_WARN_ADVANCED_FEE, false)
    }

    override fun shouldShowAdvancedFeeWarning(): Boolean {
        return prefsUtil.getValue(PrefsUtil.KEY_WARN_ADVANCED_FEE, true)
    }

    override fun onMemoChange(memo: Memo) {}

    companion object {

        private const val PREF_WARN_WATCH_ONLY_SPEND = "pref_warn_watch_only_spend"
    }
}

/**
 * The [OriginalSendPresenterStrategy] only covers BTC/BCH/ETH, for Xlm, it has its own:
 * [piuk.blockchain.android.ui.send.send2.XlmSendPresenterStrategy]
 */
private fun xlmNotSupported(): Nothing = throw IllegalArgumentException("Xlm is not supported by this presenter")
