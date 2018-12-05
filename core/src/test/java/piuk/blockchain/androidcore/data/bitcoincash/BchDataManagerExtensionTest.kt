package piuk.blockchain.androidcore.data.bitcoincash

import com.nhaarman.mockito_kotlin.KStubbing
import com.nhaarman.mockito_kotlin.mock
import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoCurrency
import info.blockchain.wallet.coin.GenericMetadataAccount
import io.reactivex.Observable
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not be`
import org.junit.Test

class BchDataManagerExtensionTest {

    @Test
    fun `can get receive cash address via Generic account`() {
        val bchDataManager = mock<BchDataManager> {
            given4ActiveAccounts()
            on { getNextReceiveCashAddress(2) } `it returns` Observable.just("RECEIVE_ACC2")
        }
        bchDataManager `should not be` null

        bchDataManager.nextReceiveCashAddress(account("XPUB2"))
            .test().values().single() `should equal` "RECEIVE_ACC2"
    }

    @Test
    fun `can get receive cash address via Account Reference`() {
        val bchDataManager = mock<BchDataManager> {
            given4ActiveAccounts()
            on { getNextReceiveCashAddress(1) } `it returns` Observable.just("RECEIVE_ACC1")
        }
        bchDataManager `should not be` null

        bchDataManager.nextReceiveCashAddress(
            AccountReference.BitcoinLike(CryptoCurrency.BCH, "", "XPUB1")
        ).test().values().single() `should equal` "RECEIVE_ACC1"
    }

    @Test
    fun `can get change cash address via Generic account`() {
        val bchDataManager = mock<BchDataManager> {
            given4ActiveAccounts()
            on { getNextChangeCashAddress(3) } `it returns` Observable.just("CHANGE_ACC3")
        }
        bchDataManager `should not be` null

        bchDataManager.nextChangeCashAddress(account("XPUB3"))
            .test().values().single() `should equal` "CHANGE_ACC3"
    }

    @Test
    fun `can get change cash address via AccountReference`() {
        val bchDataManager = mock<BchDataManager> {
            given4ActiveAccounts()
            on { getNextChangeCashAddress(0) } `it returns` Observable.just("CHANGE_ACC0")
        }
        bchDataManager `should not be` null

        bchDataManager.nextChangeCashAddress(
            AccountReference.BitcoinLike(CryptoCurrency.BCH, "", "XPUB0")
        ).test().values().single() `should equal` "CHANGE_ACC0"
    }
}

private fun account(xpub: String) = GenericMetadataAccount().apply { this.xpub = xpub }

private fun KStubbing<BchDataManager>.given4ActiveAccounts() {
    on { getActiveAccounts() } `it returns` listOf(
        account("XPUB0"),
        account("XPUB1"),
        account("XPUB2"),
        account("XPUB3")
    )
}
