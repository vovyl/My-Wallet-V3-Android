package info.blockchain.balance

import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should throw the Exception`
import org.amshove.kluent.`with message`
import org.junit.Test

class CryptoValueSubtractionTest {

    @Test
    fun `can subtract BTC`() {
        100.bitcoin() - 30.bitcoin() `should equal` 70.bitcoin()
    }

    @Test
    fun `can subtract BCH`() {
        200.123.bitcoinCash() - 100.003.bitcoinCash() `should equal` 100.12.bitcoinCash()
    }

    @Test
    fun `can subtract ETHER`() {
        1.23.ether() - 2.345.ether() `should equal` (-1.115).ether()
    }

    @Test
    fun `can't subtract ETHER and BTC`() {
        {
            1.23.ether() - 2.345.bitcoin()
        } `should throw the Exception` ValueTypeMismatchException::class `with message` "Can't subtract ETH and BTC"
    }

    @Test
    fun `can't subtract BTC and BCH`() {
        {
            1.bitcoin() - 1.bitcoinCash()
        } `should throw the Exception` ValueTypeMismatchException::class `with message` "Can't subtract BTC and BCH"
    }
}
