package com.blockchain.datamanagers.fees

import info.blockchain.balance.CryptoValue
import org.amshove.kluent.`should equal`
import org.junit.Test

class EthereumFeesTest {

    @Test
    fun `given gas price and limit in gwei, should return wei`() {
        // One Gwei = 1000,000,000 wei
        EthereumFees(
            1,
            1
        ).apply {
            gasPriceInWei `should equal` (1000_000_000).toBigInteger()
            gasLimitInGwei `should equal` 1.toBigInteger()
        }
    }

    @Test
    fun `given gas price and limit, should return absolute fee in wei`() {
        EthereumFees(
            5,
            5
        ).apply {
            absoluteFeeInWei `should equal` CryptoValue.etherFromWei(25_000_000_000.toBigInteger())
        }
    }
}