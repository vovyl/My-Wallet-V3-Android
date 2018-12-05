package com.blockchain.sunriver

import com.blockchain.testutils.lumens
import org.amshove.kluent.`should equal to`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should throw`
import org.junit.Test

class XlmAddressValidationKtTest {

    @Test
    fun `valid account id`() {
        "GDYULVJK2T6G7HFUC76LIBKZEMXPKGINSG6566EPWJKCLXTYVWJ7XPY4".isValidXlmQr() `should equal to` true
    }

    @Test
    fun `valid uri is valid address`() {
        ("web+stellar:pay?destination=GCALNQQBXAPZ2WIRSDDBMSTAKCUH5SG6U76YBFLQLIXJTF7FE5AX7AOO&amount" +
            "=120.1234567&memo=skdjfasf&msg=pay%20me%20with%20lumens").isValidXlmQr() `should equal to` true
    }

    @Test
    fun `valid uri with invalid address`() {
        ("web+stellar:pay?destination=GCALNQQBXAPZ2WIRSDDBMSTAKCUH5SG6U76YBFLQLIXJTF7FE5AX7AOP&amount" +
            "=120.1234567&memo=skdjfasf&msg=pay%20me%20with%20lumens").isValidXlmQr() `should equal to` false
    }

    @Test
    fun `invalid account id`() {
        "GDYULVJK2T6G7HFUC76LIBKZEMXPKGINSG6566EPWJKCLXTYVWJ7XPY".isValidXlmQr() `should equal to` false
        "14GfsnN74Th8Ejd18SPc89874ZsMkHXC1a".isValidXlmQr() `should equal to` false
        "0xadd8f16c9146b5c5eeb3c7777522ecaaf4fe275f".isValidXlmQr() `should equal to` false
        "qq3a7yvxtj3f4x4wgrk65mrlrxdeqjegpvm8vldl63".isValidXlmQr() `should equal to` false
        "".isValidXlmQr() `should equal to` false
    }

    @Test
    fun `parses valid uri scheme and returns amount`() {
        ("web+stellar:pay?destination=GCALNQQBXAPZ2WIRSDDBMSTAKCUH5SG6U76YBFLQLIXJTF7FE5AX7AOO" +
            "&amount=120.1234567&memo=skdjfasf&msg=pay%20me%20with%20lumens")
            .fromStellarUri()
            .apply {
                this.value `should equal` 120.1234567.lumens()
                this.public.neuter().accountId `should equal` "GCALNQQBXAPZ2WIRSDDBMSTAKCUH5SG6U76YBFLQLIXJTF7FE5AX7AOO"
            }
    }

    @Test
    fun `parses valid uri scheme with no amount, returns zero value`() {
        ("web+stellar:pay?destination=GCALNQQBXAPZ2WIRSDDBMSTAKCUH5SG6U76YBFLQLIXJTF7FE5AX7AOO" +
            "&memo=skdjfasf&msg=pay%20me%20with%20lumens")
            .fromStellarUri()
            .apply {
                this.value `should equal` 0.lumens()
                this.public.neuter().accountId `should equal` "GCALNQQBXAPZ2WIRSDDBMSTAKCUH5SG6U76YBFLQLIXJTF7FE5AX7AOO"
            }
    }

    @Test
    fun `parses valid address and returns zero value`() {
        "GCALNQQBXAPZ2WIRSDDBMSTAKCUH5SG6U76YBFLQLIXJTF7FE5AX7AOO"
            .fromStellarUri()
            .apply {
                this.value `should equal` 0.lumens()
                this.public.neuter().accountId `should equal` "GCALNQQBXAPZ2WIRSDDBMSTAKCUH5SG6U76YBFLQLIXJTF7FE5AX7AOO"
            }
    }

    @Test
    fun `throws exception if address is invalid`() {
        {
            "invalid".fromStellarUri()
        } `should throw` InvalidAccountIdException::class
    }
}