package piuk.blockchain.androidcoreui.utils.logging

import org.amshove.kluent.`should equal`
import org.junit.Test

class BalanceLoadedEventTest {

    @Test
    fun `event name`() {
        BalanceLoadedEvent(hasBtcBalance = false, hasBchBalance = false, hasEthBalance = false, hasXlmBalance = false)
            .eventName `should equal` "Balances loaded"
    }

    @Test
    fun `no balances`() {
        BalanceLoadedEvent(hasBtcBalance = false, hasBchBalance = false, hasEthBalance = false, hasXlmBalance = false)
            .buildToMap()
            .also {
                it["Has BTC balance"] `should equal` "false"
                it["Has BCH balance"] `should equal` "false"
                it["Has ETH balance"] `should equal` "false"
                it["Has any balance"] `should equal` "false"
            }
    }

    @Test
    fun `has btc balance`() {
        BalanceLoadedEvent(hasBtcBalance = true, hasBchBalance = false, hasEthBalance = false, hasXlmBalance = false)
            .buildToMap()
            .also {
                it["Has BTC balance"] `should equal` "true"
                it["Has BCH balance"] `should equal` "false"
                it["Has ETH balance"] `should equal` "false"
                it["Has any balance"] `should equal` "true"
            }
    }

    @Test
    fun `has bch balance`() {
        BalanceLoadedEvent(hasBtcBalance = false, hasBchBalance = true, hasEthBalance = false, hasXlmBalance = false)
            .buildToMap()
            .also {
                it["Has BTC balance"] `should equal` "false"
                it["Has BCH balance"] `should equal` "true"
                it["Has ETH balance"] `should equal` "false"
                it["Has any balance"] `should equal` "true"
            }
    }

    @Test
    fun `has eth balance`() {
        BalanceLoadedEvent(hasBtcBalance = false, hasBchBalance = false, hasEthBalance = true, hasXlmBalance = false)
            .buildToMap()
            .also {
                it["Has BTC balance"] `should equal` "false"
                it["Has BCH balance"] `should equal` "false"
                it["Has ETH balance"] `should equal` "true"
                it["Has any balance"] `should equal` "true"
            }
    }

    @Test
    fun `has xml balance`() {
        BalanceLoadedEvent(hasBtcBalance = false, hasBchBalance = false, hasEthBalance = false, hasXlmBalance = true)
            .buildToMap()
            .also {
                it["Has BTC balance"] `should equal` "false"
                it["Has BCH balance"] `should equal` "false"
                it["Has ETH balance"] `should equal` "false"
                it["Has XLM balance"] `should equal` "true"
                it["Has any balance"] `should equal` "true"
            }
    }

    @Test
    fun `all balances`() {
        BalanceLoadedEvent(hasBtcBalance = true, hasBchBalance = true, hasEthBalance = true, hasXlmBalance = true)
            .buildToMap()
            .also {
                it["Has BTC balance"] `should equal` "true"
                it["Has BCH balance"] `should equal` "true"
                it["Has ETH balance"] `should equal` "true"
                it["Has any balance"] `should equal` "true"
            }
    }
}
