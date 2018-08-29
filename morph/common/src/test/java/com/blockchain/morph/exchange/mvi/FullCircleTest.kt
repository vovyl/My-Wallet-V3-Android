package com.blockchain.morph.exchange.mvi

import com.blockchain.testutils.gbp
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import info.blockchain.balance.ExchangeRate
import org.junit.Test

/**
 * Tests that cover the conversion from one field to each of the other three.
 */
class FullCircleTest {

    @Test
    fun `"from crypto" updates the other three fields`() {
        given(
            initial("GBP", CryptoCurrency.BCH to CryptoCurrency.BTC)
        ).on(
            fiatExchangeRateUpdateIntent("GBP", CryptoCurrency.BTC, 7000.toBigDecimal()),
            fiatExchangeRateUpdateIntent("GBP", CryptoCurrency.BCH, 3500.toBigDecimal()),
            coinExchangeRateUpdateIntent(
                ExchangeRate.CryptoToCrypto(
                    CryptoCurrency.BCH,
                    CryptoCurrency.BTC,
                    0.49.toBigDecimal()
                )
            ),
            FieldUpdateIntent(FieldUpdateIntent.Field.FROM_CRYPTO, "5")
        ) {
            assertValue(
                ExchangeViewModel(
                    from = value(
                        userEntered(CryptoValue.bitcoinCashFromMajor(5)),
                        upToDate(17500.gbp())

                    ),
                    to = value(
                        upToDate(CryptoValue.bitcoinFromMajor(2.45.toBigDecimal())),
                        upToDate(17150.gbp())
                    )
                )
            )
        }
    }

    @Test
    fun `"to crypto" updates the other three fields`() {
        given(
            initial("GBP", CryptoCurrency.BCH to CryptoCurrency.BTC)
        ).on(
            fiatExchangeRateUpdateIntent("GBP", CryptoCurrency.BTC, 7000.toBigDecimal()),
            fiatExchangeRateUpdateIntent("GBP", CryptoCurrency.BCH, 3500.toBigDecimal()),
            coinExchangeRateUpdateIntent(
                ExchangeRate.CryptoToCrypto(
                    CryptoCurrency.BCH,
                    CryptoCurrency.BTC,
                    0.49.toBigDecimal()
                )
            ),
            FieldUpdateIntent(FieldUpdateIntent.Field.TO_CRYPTO, "5")
        ) {
            assertValue(
                ExchangeViewModel(
                    from = value(
                        upToDate(CryptoValue.bitcoinCashFromMajor(10.20408163.toBigDecimal())),
                        upToDate(
                            (10.20408163 * 3500).gbp()
                        )
                    ),
                    to = value(
                        userEntered(CryptoValue.bitcoinFromMajor(5.toBigDecimal())),
                        upToDate(35000.gbp())
                    )
                )
            )
        }
    }

    @Test
    fun `"from fiat" updates the other three fields`() {
        given(
            initial("GBP", CryptoCurrency.BCH to CryptoCurrency.BTC)
        ).on(
            fiatExchangeRateUpdateIntent("GBP", CryptoCurrency.BTC, 7000.toBigDecimal()),
            fiatExchangeRateUpdateIntent("GBP", CryptoCurrency.BCH, 3500.toBigDecimal()),
            coinExchangeRateUpdateIntent(
                ExchangeRate.CryptoToCrypto(
                    CryptoCurrency.BCH,
                    CryptoCurrency.BTC,
                    0.49.toBigDecimal()
                )
            ),
            FieldUpdateIntent(FieldUpdateIntent.Field.FROM_FIAT, "35000")
        ) {
            assertValue(
                ExchangeViewModel(
                    from = value(
                        upToDate(CryptoValue.bitcoinCashFromMajor(10)),
                        userEntered(35000.gbp())
                    ),
                    to = value(
                        upToDate(CryptoValue.bitcoinFromMajor(4.9.toBigDecimal())),
                        upToDate(34300.gbp())
                    )
                )
            )
        }
    }

    @Test
    fun `"to fiat" updates the other three fields`() {
        given(
            initial("GBP", CryptoCurrency.BCH to CryptoCurrency.BTC)
        ).on(
            fiatExchangeRateUpdateIntent("GBP", CryptoCurrency.BTC, 7000.toBigDecimal()),
            fiatExchangeRateUpdateIntent("GBP", CryptoCurrency.BCH, 3500.toBigDecimal()),
            coinExchangeRateUpdateIntent(
                ExchangeRate.CryptoToCrypto(
                    CryptoCurrency.BCH,
                    CryptoCurrency.BTC,
                    0.49.toBigDecimal()
                )
            ),
            FieldUpdateIntent(FieldUpdateIntent.Field.TO_FIAT, "35000")
        ) {
            assertValue(
                ExchangeViewModel(
                    from = value(
                        upToDate(CryptoValue.bitcoinCashFromMajor(10.20408163.toBigDecimal())),
                        upToDate((10.20408163 * 3500).gbp())
                    ),
                    to = value(
                        upToDate(CryptoValue.bitcoinFromMajor(5.toBigDecimal())),
                        userEntered(35000.gbp())
                    )
                )
            )
        }
    }

    @Test
    fun `"from fiat" updates the other three fields - with swap`() {
        given(
            initial("GBP", CryptoCurrency.BTC to CryptoCurrency.BCH)
        ).on(
            fiatExchangeRateUpdateIntent("GBP", CryptoCurrency.BTC, 7000.toBigDecimal()),
            fiatExchangeRateUpdateIntent("GBP", CryptoCurrency.BCH, 3500.toBigDecimal()),
            coinExchangeRateUpdateIntent(
                ExchangeRate.CryptoToCrypto(
                    CryptoCurrency.BCH,
                    CryptoCurrency.BTC,
                    0.49.toBigDecimal()
                )
            ),
            coinExchangeRateUpdateIntent(
                ExchangeRate.CryptoToCrypto(
                    CryptoCurrency.BTC,
                    CryptoCurrency.BCH,
                    1.99.toBigDecimal()
                )
            ),
            SwapIntent(),
            FieldUpdateIntent(FieldUpdateIntent.Field.FROM_FIAT, "35000")
        ) {
            assertValue(
                ExchangeViewModel(
                    from = value(
                        upToDate(CryptoValue.bitcoinCashFromMajor(10)),
                        userEntered(35000.gbp())
                    ),
                    to = value(
                        upToDate(CryptoValue.bitcoinFromMajor(4.9.toBigDecimal())),
                        upToDate(34300.gbp())
                    )
                )
            )
        }
    }

    @Test
    fun `"to fiat" updates the other three fields - with swap`() {
        given(
            initial("GBP", CryptoCurrency.BTC to CryptoCurrency.BCH)
        ).on(
            fiatExchangeRateUpdateIntent("GBP", CryptoCurrency.BTC, 7000.toBigDecimal()),
            fiatExchangeRateUpdateIntent("GBP", CryptoCurrency.BCH, 3500.toBigDecimal()),
            coinExchangeRateUpdateIntent(
                ExchangeRate.CryptoToCrypto(
                    CryptoCurrency.BCH,
                    CryptoCurrency.BTC,
                    0.49.toBigDecimal()
                )
            ),
            SwapIntent(),
            FieldUpdateIntent(FieldUpdateIntent.Field.TO_FIAT, "35000")
        ) {
            assertValue(
                ExchangeViewModel(
                    from = value(
                        upToDate(CryptoValue.bitcoinCashFromMajor(10.20408163.toBigDecimal())),
                        upToDate((10.20408163 * 3500).gbp())
                    ),
                    to = value(
                        upToDate(CryptoValue.bitcoinFromMajor(5.toBigDecimal())),
                        userEntered(35000.gbp())
                    )
                )
            )
        }
    }
}
