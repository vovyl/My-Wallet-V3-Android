package com.blockchain.sunriver

import com.blockchain.sunriver.derivation.deriveXlmAccountKeyPair
import com.blockchain.testutils.rxInit
import com.blockchain.wallet.Seed
import com.blockchain.wallet.SeedAccess
import com.nhaarman.mockito_kotlin.mock
import io.github.novacrypto.bip39.SeedCalculator
import io.reactivex.Maybe
import io.reactivex.observers.TestObserver
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should equal`
import org.junit.Rule
import org.junit.Test

class XlmSecretAccessTest {

    @get:Rule
    val rxInit = rxInit {
        computationTrampoline()
    }

    @Test
    fun `account 0`() {
        assertPrivateForPublic(
            public = "GAL3AZF4OQ7RDZTPQ6OPPKPMEYL6ETI6IGM5JWPYZDDYKCKV2TQAEBKC",
            expectedPrivate = "SCKKPZYAPW7YIJSWD5LADTIJ5XMOKM4QAWYTECSP6WNGMQ2Q4M5PVZT2"
        )
    }

    @Test
    fun `account 1`() {
        assertPrivateForPublic(
            public = "GA3CAFTO26XFLYQE7HEIAA2GL6LBQLSRPQWVNQG7AOABQWCSDA247NYY",
            expectedPrivate = "SBXXIAZMUQDJ2T5HQEG3AQCMHBNWBVHCIU4SSGZ7FUZBDLT3HF7ZOGWG"
        )
    }

    @Test
    fun `account 2`() {
        assertPrivateForPublic(
            public = "GBBTVNTLCJZPEMMUB6ZSGHSAYXFD3DDSX53OLLSQQQ6YWBANW2OXELB5",
            expectedPrivate = "SCQKZDEIONICU6CTRDV6RYWUEF3EEBTZXZH4PGKCOYS225XHJPFPC4YE"
        )
    }

    @Test
    fun `accounts 3-20`() {
        for (account in 3..20) {
            val keyPair = deriveXlmAccountKeyPair(seed, account)
            assertPrivateForPublic(
                public = keyPair.accountId,
                expectedPrivate = String(keyPair.secret)
            )
        }
    }

    @Test
    fun `21st account returns empty`() {
        val keyPair = deriveXlmAccountKeyPair(seed, 21)
        searchForPublic(public = keyPair.accountId)
            .assertNoValues()
            .assertComplete()
    }
}

private val seed = SeedCalculator().calculateSeed("some mnemonic", "")

private fun assertPrivateForPublic(public: String, expectedPrivate: String) {
    val test = searchForPublic(public)
    test.values().single().apply {
        String(this.secret) `should equal` expectedPrivate
    }
}

private fun searchForPublic(public: String): TestObserver<HorizonKeyPair.Private> {
    val mockSeed = mockSeed()
    val seedAccess: SeedAccess = mock {
        on { seedForcePrompt } `it returns` Maybe.just(mockSeed)
    }
    return XlmSecretAccess(seedAccess)
        .getPrivate(
            HorizonKeyPair.Public(public)
        ).test()
}

private fun mockSeed(): Seed {
    return mock {
        on { hdSeed } `it returns` seed
    }
}
