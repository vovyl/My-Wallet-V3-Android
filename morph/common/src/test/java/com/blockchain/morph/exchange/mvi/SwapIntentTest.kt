package com.blockchain.morph.exchange.mvi

import com.blockchain.testutils.gbp
import info.blockchain.balance.CryptoValue
import org.junit.Test

class SwapIntentTest {

    @Test
    fun `can swap the "to" and "from" currencies`() {
        given(
            ExchangeViewModel(
                from = value(
                    userEntered(CryptoValue.etherFromMajor(10)),
                    upToDate(100.gbp())
                ),
                to = value(
                    upToDate(CryptoValue.bitcoinCashFromMajor(25)),
                    upToDate(99.gbp())
                )
            )
        ).on(
            SwapIntent()
        ) {
            assertValue(
                ExchangeViewModel(
                    from = value(
                        upToDate(CryptoValue.ZeroBch),
                        upToDate(zeroFiat("GBP"))
                    ),
                    to = value(
                        upToDate(CryptoValue.ZeroEth),
                        upToDate(zeroFiat("GBP"))
                    )
                )
            )
        }
    }
}