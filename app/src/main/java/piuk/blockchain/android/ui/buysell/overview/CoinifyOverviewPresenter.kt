package piuk.blockchain.android.ui.buysell.overview

import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.androidbuysell.datamanagers.CoinifyDataManager
import piuk.blockchain.androidbuysell.services.ExchangeService
import piuk.blockchain.androidcore.data.metadata.MetadataManager
import piuk.blockchain.androidcore.utils.extensions.applySchedulers
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import timber.log.Timber
import javax.inject.Inject

class CoinifyOverviewPresenter @Inject constructor(
        private val exchangeService: ExchangeService,
        private val coinifyDataManager: CoinifyDataManager,
        private val metadataManager: MetadataManager
): BasePresenter<CoinifyOverviewView>() {

    override fun onViewReady() {
        exchangeService.getExchangeMetaData()
                .doOnSubscribe { view.updateList(listOf(BuySellButtons())) }
                .addToCompositeDisposable(this)
                .applySchedulers()
                .map { it.coinify.trades }
                .subscribe(
                        {
                            Timber.d(it.toString())
                        },
                        {
                            Timber.e(it)
                        }
                )

        // TODO: Listen for completed coinify trades and convert them to metadata if necessary
        // If any transactions are incomplete, continue polling
        exchangeService.getExchangeMetaData()
                .addToCompositeDisposable(this)
                .applySchedulers()
                .map { it.coinify.token }
                .flatMap { coinifyDataManager.getTrades(it) }
                .toList()
                .subscribe(
                        {
                            Timber.d(it.toString())
                        },
                        {
                            Timber.e(it)
                        }
                )
    }

}