package piuk.blockchain.android.ui.swipetoreceive

import info.blockchain.balance.CryptoCurrency
import io.reactivex.Single
import piuk.blockchain.android.R
import piuk.blockchain.android.data.datamanagers.QrCodeDataManager
import piuk.blockchain.android.util.StringUtils
import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import piuk.blockchain.androidcoreui.ui.base.UiState
import javax.inject.Inject
import kotlin.properties.Delegates

class SwipeToReceivePresenter @Inject constructor(
    private val dataManager: QrCodeDataManager,
    private val swipeToReceiveHelper: SwipeToReceiveHelper,
    private val stringUtils: StringUtils
) : BasePresenter<SwipeToReceiveView>() {

    internal var currencyPosition by Delegates.observable(0) { _, _, new ->
        check(new in 0 until currencyList.size) { "Invalid currency position" }
        onCurrencySelected(currencyList[new])
    }

    private val currencyList = CryptoCurrency.values()

    private val bitcoinAddress: Single<String>
        get() = swipeToReceiveHelper.getNextAvailableBitcoinAddressSingle()
    private val ethereumAddress: Single<String>
        get() = swipeToReceiveHelper.getEthReceiveAddressSingle()
    private val bitcoinCashAddress: Single<String>
        get() = swipeToReceiveHelper.getNextAvailableBitcoinCashAddressSingle()
    private val xlmAddress: Single<String>
        get() = swipeToReceiveHelper.getXlmReceiveAddressSingle()

    override fun onViewReady() {
        currencyPosition = 0
    }

    private class AccountDetails(
        val accountName: String,
        val nextAddress: Single<String>,
        val hasAddresses: Boolean
    )

    private fun onCurrencySelected(cryptoCurrency: CryptoCurrency) {
        view.displayCoinType(
            stringUtils.getFormattedString(
                R.string.swipe_receive_request,
                cryptoCurrency.unit
            )
        )
        view.setUiState(UiState.LOADING)

        val accountDetails = getAccountDetailsFor(cryptoCurrency)

        view.displayReceiveAccount(accountDetails.accountName)

        // Check we actually have addresses stored
        if (!accountDetails.hasAddresses) {
            view.setUiState(UiState.EMPTY)
        } else {
            accountDetails
                .nextAddress
                .doOnSuccess { address ->
                    require(address.isNotEmpty()) { "Returned address is empty, no more addresses available" }
                    view.displayReceiveAddress(
                        address.replace("bitcoincash:", "")
                            .replace("bitcoin:", "")
                    )
                }
                .flatMapObservable { dataManager.generateQrCode(it, DIMENSION_QR_CODE) }
                .addToCompositeDisposable(this)
                .subscribe(
                    {
                        view.displayQrCode(it)
                        view.setUiState(UiState.CONTENT)
                    },
                    { _ -> view.setUiState(UiState.FAILURE) })
        }
    }

    private fun getAccountDetailsFor(cryptoCurrency: CryptoCurrency) =
        when (cryptoCurrency) {
            CryptoCurrency.BTC -> {
                AccountDetails(
                    accountName = swipeToReceiveHelper.getBitcoinAccountName(),
                    nextAddress = bitcoinAddress.map { "bitcoin:$it" },
                    hasAddresses = !swipeToReceiveHelper.getBitcoinReceiveAddresses().isEmpty()
                )
            }
            CryptoCurrency.ETHER -> {
                AccountDetails(
                    accountName = swipeToReceiveHelper.getEthAccountName(),
                    nextAddress = ethereumAddress,
                    hasAddresses = !swipeToReceiveHelper.getEthReceiveAddress().isNullOrEmpty()
                )
            }
            CryptoCurrency.BCH -> {
                AccountDetails(
                    accountName = swipeToReceiveHelper.getBitcoinCashAccountName(),
                    nextAddress = bitcoinCashAddress,
                    hasAddresses = !swipeToReceiveHelper.getBitcoinCashReceiveAddresses().isEmpty()
                )
            }
            CryptoCurrency.XLM -> AccountDetails(
                accountName = swipeToReceiveHelper.getXlmAccountName(),
                nextAddress = xlmAddress,
                hasAddresses = !swipeToReceiveHelper.getXlmReceiveAddress().isNullOrEmpty()
            )
        }

    companion object {

        private const val DIMENSION_QR_CODE = 600
    }
}
