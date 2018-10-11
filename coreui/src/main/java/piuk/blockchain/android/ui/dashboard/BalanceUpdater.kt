package piuk.blockchain.android.ui.dashboard

import io.reactivex.Completable
import piuk.blockchain.androidcore.data.bitcoincash.BchDataManager
import piuk.blockchain.androidcore.data.payload.PayloadDataManager
import timber.log.Timber

class BalanceUpdater(
    private val bchDataManager: BchDataManager,
    private val payloadDataManager: PayloadDataManager
) {
    fun updateBalances(): Completable =
        payloadDataManager.updateAllBalances()
            .andThen(
                Completable.merge(
                    listOf(
                        payloadDataManager.updateAllTransactions(),
                        bchDataManager.updateAllBalances()
                    )
                ).doOnError { Timber.e(it) }
                    .onErrorComplete()
            )
}
