package com.blockchain.sunriver.derivation

import io.github.novacrypto.bip39.SeedCalculator
import org.amshove.kluent.`should equal`
import org.junit.Test

/**
 * Test cases from https://github.com/stellar/stellar-protocol/blob/master/ecosystem/sep-0005.md
 */
class XlmAccountDerivationTest {

    @Test
    fun `case 1 - 12 words`() {
        derive("illness spike retreat truth genius clock brain pass fit cave bargain toe")
            .apply {
                accountId `should equal` "GDRXE2BQUC3AZNPVFSCEZ76NJ3WWL25FYFK6RGZGIEKWE4SOOHSUJUJ6"
                secret `should equal` "SBGWSG6BTNCKCOB3DIFBGCVMUPQFYPA2G4O34RMTB343OYPXU5DJDVMN"
            }
    }

    @Test
    fun `case 2 - 15 words`() {
        derive(
            mnemonic = "resource asthma orphan phone ice canvas " +
                "fire useful arch jewel impose vague theory cushion top"
        )
            .apply {
                accountId `should equal` "GAVXVW5MCK7Q66RIBWZZKZEDQTRXWCZUP4DIIFXCCENGW2P6W4OA34RH"
                secret `should equal` "SAKS7I2PNDBE5SJSUSU2XLJ7K5XJ3V3K4UDFAHMSBQYPOKE247VHAGDB"
            }
    }

    @Test
    fun `case 3 - 24 words`() {
        derive(
            mnemonic = "bench hurt jump file august wise shallow faculty impulse spring exact slush " +
                "thunder author capable act festival slice deposit sauce coconut afford frown better"
        )
            .apply {
                accountId `should equal` "GC3MMSXBWHL6CPOAVERSJITX7BH76YU252WGLUOM5CJX3E7UCYZBTPJQ"
                secret `should equal` "SAEWIVK3VLNEJ3WEJRZXQGDAS5NVG2BYSYDFRSH4GKVTS5RXNVED5AX7"
            }
    }

    @Test
    fun `case 4a - 24 words + passphrase - account 0`() {
        derive(
            mnemonic = "cable spray genius state float twenty onion head street palace net private " +
                "method loan turn phrase state blanket interest dry amazing dress blast tube",
            passphrase = "p4ssphr4se"
        ).apply {
            accountId `should equal` "GDAHPZ2NSYIIHZXM56Y36SBVTV5QKFIZGYMMBHOU53ETUSWTP62B63EQ"
            secret `should equal` "SAFWTGXVS7ELMNCXELFWCFZOPMHUZ5LXNBGUVRCY3FHLFPXK4QPXYP2X"
        }
    }

    @Test
    fun `case 4b - 24 words + passphrase - account 1`() {
        derive(
            mnemonic = "cable spray genius state float twenty onion head street palace net private " +
                "method loan turn phrase state blanket interest dry amazing dress blast tube",
            passphrase = "p4ssphr4se",
            account = 1
        ).apply {
            accountId `should equal` "GDY47CJARRHHL66JH3RJURDYXAMIQ5DMXZLP3TDAUJ6IN2GUOFX4OJOC"
            secret `should equal` "SBQPDFUGLMWJYEYXFRM5TQX3AX2BR47WKI4FDS7EJQUSEUUVY72MZPJF"
        }
    }
}

private fun derive(mnemonic: String, passphrase: String = "", account: Int = 0) =
    deriveXlmAccountKeyPair(mnemonic, passphrase, account)
        .let { TestResult(it.accountId, String(it.secret)) }

private class TestResult(val accountId: String, val secret: String)

/**
 * Derives an account from the mnemonic.
 * The account is at the path m/44'/148'/account' on the Ed25519 Curve
 */
private fun deriveXlmAccountKeyPair(
    mnemonic: String,
    passphrase: String,
    account: Int = 0
) =
    deriveXlmAccountKeyPair(
        SeedCalculator().calculateSeed(mnemonic, passphrase),
        account
    )
