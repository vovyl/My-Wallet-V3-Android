package info.blockchain.balance

import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should throw the Exception`
import org.amshove.kluent.`with message`
import org.junit.Test

class CryptoValueAdditionTest {

    @Test
    fun `can add BTC`() {
        100.bitcoin() + 200.bitcoin() `should equal` 300.bitcoin()
    }

    @Test
    fun `can add BCH`() {
        100.bitcoinCash() + 200.123.bitcoinCash() `should equal` 300.123.bitcoinCash()
    }

    @Test
    fun `can add ETHER`() {
        1.23.ether() + 2.345.ether() `should equal` 3.575.ether()
    }

    @Test
    fun `can't add ETHER and BTC`() {
        {
            1.23.ether() + 2.345.bitcoin()
        } `should throw the Exception` ValueTypeMismatchException::class `with message` "Can't add ETH and BTC"
    }

    @Test
    fun `can't add BTC and BCH`() {
        {
            1.bitcoin() + 1.bitcoinCash()
        } `should throw the Exception` ValueTypeMismatchException::class `with message` "Can't add BTC and BCH"
    }
}
