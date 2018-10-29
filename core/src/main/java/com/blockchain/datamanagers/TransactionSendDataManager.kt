package com.blockchain.datamanagers

import com.blockchain.datamanagers.fees.BitcoinLikeFees
import com.blockchain.datamanagers.fees.EthereumFees
import com.blockchain.datamanagers.fees.FeeType
import com.blockchain.datamanagers.fees.NetworkFees
import com.blockchain.datamanagers.fees.XlmFees
import com.blockchain.account.DefaultAccountDataManager
import com.blockchain.transactions.TransactionSender
import info.blockchain.api.data.UnspentOutputs
import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import info.blockchain.wallet.coin.GenericMetadataAccount
import info.blockchain.wallet.ethereum.EthereumAccount
import info.blockchain.wallet.payload.data.Account
import info.blockchain.wallet.payment.SpendableUnspentOutputs
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.bitcoinj.core.ECKey
import piuk.blockchain.androidcore.data.ethereum.EthDataManager
import piuk.blockchain.androidcore.data.ethereum.EthereumAccountWrapper
import piuk.blockchain.androidcore.data.ethereum.exceptions.TransactionInProgressException
import piuk.blockchain.androidcore.data.payload.PayloadDataManager
import piuk.blockchain.androidcore.data.payments.SendDataManager
import timber.log.Timber
import java.math.BigInteger

class TransactionSendDataManager internal constructor(
    private val payloadDataManager: PayloadDataManager,
    private val ethDataManager: EthDataManager,
    private val sendDataManager: SendDataManager,
    private val addressResolver: AddressResolver,
    private val accountLookup: AccountLookup,
    private val defaultAccountDataManager: DefaultAccountDataManager,
    private val ethereumAccountWrapper: EthereumAccountWrapper,
    private val xlmSender: TransactionSender
) {

    fun executeTransaction(
        amount: CryptoValue,
        destination: String,
        accountReference: AccountReference,
        fees: NetworkFees,
        feeType: FeeType = FeeType.Regular
    ): Single<String> =
        when (amount.currency) {
            CryptoCurrency.BTC -> sendBtcTransaction(
                amount,
                destination,
                accountReference.toJsonAccount(),
                (fees as BitcoinLikeFees).feeForType(feeType)
            )
            CryptoCurrency.ETHER -> sendEthTransaction(
                amount,
                destination,
                accountReference.toJsonAccount(),
                fees as EthereumFees
            )
            CryptoCurrency.BCH -> sendBchTransaction(
                amount,
                destination,
                accountReference.toJsonAccount(),
                (fees as BitcoinLikeFees).feeForType(feeType)
            )
            CryptoCurrency.XLM -> xlmSender.sendFunds(accountReference, amount, destination)
        }

    fun getMaximumSpendable(
        account: AccountReference,
        fees: NetworkFees,
        feeType: FeeType = FeeType.Regular
    ): Single<CryptoValue> =
        when (account) {
            is AccountReference.BitcoinLike ->
                account.getMaximumSpendable(
                    fees as BitcoinLikeFees,
                    feeType
                )
            is AccountReference.Ethereum -> getMaxEther(fees as EthereumFees)
            is AccountReference.Xlm -> defaultAccountDataManager.getMaxSpendableAfterFees()
        }

    fun getFeeForTransaction(
        amount: CryptoValue,
        account: AccountReference,
        fees: NetworkFees,
        feeType: FeeType = FeeType.Regular
    ): Single<CryptoValue> =
        when (account) {
            is AccountReference.BitcoinLike -> calculateBitcoinLikeFee(
                account,
                amount,
                (fees as BitcoinLikeFees).feeForType(feeType)
            )
            is AccountReference.Ethereum -> (fees as EthereumFees).absoluteFeeInWei.just()
            is AccountReference.Xlm -> (fees as XlmFees).perOperationFee.just()
        }

    fun getChangeAddress(
        accountReference: AccountReference
    ): Single<String> =
        addressResolver.addressPairForAccount(accountReference).map { it.changeAddress }

    fun getReceiveAddress(
        accountReference: AccountReference
    ): Single<String> =
        addressResolver.addressPairForAccount(accountReference).map { it.receivingAddress }

    private fun calculateBitcoinLikeFee(
        account: AccountReference.BitcoinLike,
        amount: CryptoValue,
        feePerKb: BigInteger
    ): Single<CryptoValue> =
        getUnspentOutputs(account.xpub, amount.currency)
            .map { getSuggestedAbsoluteFee(it, amount.amount, feePerKb) }
            .map { CryptoValue(amount.currency, it) }

    private fun getSuggestedAbsoluteFee(
        coins: UnspentOutputs,
        amountToSend: BigInteger,
        feePerKb: BigInteger
    ): BigInteger = sendDataManager.getSpendableCoins(coins, amountToSend, feePerKb).absoluteFee

    private fun AccountReference.BitcoinLike.getMaximumSpendable(
        fees: BitcoinLikeFees,
        feeType: FeeType
    ): Single<CryptoValue> =
        getUnspentOutputs(xpub, cryptoCurrency)
            .map {
                CryptoValue(
                    cryptoCurrency,
                    sendDataManager.getMaximumAvailable(it, fees.feeForType(feeType)).left
                )
            }
            .doOnError { Timber.e(it) }
            .onErrorReturn { CryptoValue.zero(cryptoCurrency) }

    private fun getMaxEther(fees: EthereumFees): Single<CryptoValue> =
        ethDataManager.fetchEthAddress()
            .map {
                (it.getAddressResponse()!!.balance - fees.absoluteFeeInWei.amount).max(BigInteger.ZERO)
            }
            .map { CryptoValue.etherFromWei(it) }
            .doOnError { Timber.e(it) }
            .onErrorReturn { CryptoValue.ZeroEth }
            .singleOrError()

    private fun sendBtcTransaction(
        amount: CryptoValue,
        destination: String,
        account: Account,
        feePerKb: BigInteger
    ): Single<String> = sendBitcoinStyleTransaction(
        amount,
        destination,
        account,
        feePerKb,
        addressResolver.getChangeAddress(account)
    )

    private fun sendBchTransaction(
        amount: CryptoValue,
        destination: String,
        account: GenericMetadataAccount,
        feePerKb: BigInteger
    ): Single<String> = sendBitcoinStyleTransaction(
        amount,
        destination,
        account.getHdAccount(),
        feePerKb,
        addressResolver.getChangeAddress(account)
    )

    private fun sendBitcoinStyleTransaction(
        amount: CryptoValue,
        destination: String,
        account: Account,
        feePerKb: BigInteger,
        changeAddress: Single<String>
    ): Single<String> = getSpendableCoins(account.xpub, amount, feePerKb)
        .flatMap { spendable ->
            getSigningKeys(account, spendable)
                .flatMap { signingKeys ->
                    changeAddress
                        .flatMap {
                            submitBitcoinStylePayment(
                                amount,
                                spendable,
                                signingKeys,
                                destination,
                                it,
                                spendable.absoluteFee
                            )
                        }
                }
        }

    private fun sendEthTransaction(
        amount: CryptoValue,
        destination: String,
        account: EthereumAccount,
        fees: EthereumFees
    ): Single<String> =
        ethDataManager.isLastTxPending()
            .singleOrError()
            .doOnSuccess {
                if (it == true)
                    throw TransactionInProgressException("Transaction pending, user cannot send funds at this time")
            }
            .flatMap { _ ->
                ethDataManager.fetchEthAddress()
                    .map {
                        ethDataManager.createEthTransaction(
                            nonce = ethDataManager.getEthResponseModel()!!.getNonce(),
                            to = destination,
                            gasPriceWei = fees.gasPriceInWei,
                            gasLimitGwei = fees.gasLimitInGwei,
                            weiValue = amount.amount
                        )
                    }
                    .map {
                        account.signTransaction(
                            it,
                            ethereumAccountWrapper.deriveECKey(payloadDataManager.masterKey, 0)
                        )
                    }
                    .flatMap { ethDataManager.pushEthTx(it) }
                    .flatMap { ethDataManager.setLastTxHashObservable(it, System.currentTimeMillis()) }
                    .subscribeOn(Schedulers.io())
                    .singleOrError()
            }

    private fun getSpendableCoins(
        address: String,
        amount: CryptoValue,
        feePerKb: BigInteger
    ): Single<SpendableUnspentOutputs> = getUnspentOutputs(address, amount.currency)
        .subscribeOn(Schedulers.io())
        .map { sendDataManager.getSpendableCoins(it, amount.amount, feePerKb) }

    private fun getUnspentOutputs(
        address: String,
        currency: CryptoCurrency
    ): Single<UnspentOutputs> =
        when (currency) {
            CryptoCurrency.BTC -> sendDataManager.getUnspentOutputs(address)
            CryptoCurrency.BCH -> sendDataManager.getUnspentBchOutputs(address)
            CryptoCurrency.ETHER -> throw IllegalArgumentException("Ether does not have unspent outputs")
            CryptoCurrency.XLM -> throw IllegalArgumentException("Xlm does not have unspent outputs")
        }.subscribeOn(Schedulers.io())
            .singleOrError()

    private fun submitBitcoinStylePayment(
        amount: CryptoValue,
        unspent: SpendableUnspentOutputs,
        signingKeys: List<ECKey>,
        depositAddress: String,
        changeAddress: String,
        absoluteFee: BigInteger
    ): Single<String> = when (amount.currency) {
        CryptoCurrency.BTC -> sendDataManager.submitBtcPayment(
            unspent,
            signingKeys,
            depositAddress,
            changeAddress,
            absoluteFee,
            amount.amount
        )
        CryptoCurrency.BCH -> sendDataManager.submitBchPayment(
            unspent,
            signingKeys,
            depositAddress,
            changeAddress,
            absoluteFee,
            amount.amount
        )
        CryptoCurrency.ETHER -> throw IllegalArgumentException("Ether not supported by this method")
        CryptoCurrency.XLM -> throw IllegalArgumentException("XLM not supported by this method")
    }.subscribeOn(Schedulers.io())
        .singleOrError()

    private fun getSigningKeys(
        account: Account,
        spendable: SpendableUnspentOutputs
    ): Single<List<ECKey>> =
        payloadDataManager.getHDKeysForSigning(account, spendable).just()

    private fun GenericMetadataAccount.getHdAccount(): Account =
        payloadDataManager.getAccountForXPub(this.xpub)

    private fun <T> T.just(): Single<T> = Single.just(this)

    private fun BitcoinLikeFees.feeForType(feeType: FeeType): BigInteger = when (feeType) {
        FeeType.Regular -> this.regularFeePerKb
        FeeType.Priority -> this.priorityFeePerKb
    }

    private inline fun <reified T> AccountReference.toJsonAccount() =
        accountLookup.getAccountFromAddressOrXPub(this) as T
}
