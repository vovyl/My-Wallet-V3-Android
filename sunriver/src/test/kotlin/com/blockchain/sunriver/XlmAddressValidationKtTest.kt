package com.blockchain.sunriver

import com.blockchain.testutils.lumens
import com.blockchain.transactions.Memo
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
                this.memo `should equal` Memo.None
            }
    }

    @Test
    fun `uri with no memo, returns Memo none`() {
        ("web+stellar:pay?destination=GCALNQQBXAPZ2WIRSDDBMSTAKCUH5SG6U76YBFLQLIXJTF7FE5AX7AOO")
            .fromStellarUri()
            .memo `should equal` Memo.None
    }

    @Test
    fun `uri with memo, but no type, returns text Memo`() {
        ("web+stellar:pay?destination=GCALNQQBXAPZ2WIRSDDBMSTAKCUH5SG6U76YBFLQLIXJTF7FE5AX7AOO&" +
            "memo=Hello")
            .fromStellarUri()
            .memo `should equal` Memo(value = "Hello", type = "text")
    }

    @Test
    fun `uri with memo, text type, returns text Memo`() {
        ("web+stellar:pay?destination=GCALNQQBXAPZ2WIRSDDBMSTAKCUH5SG6U76YBFLQLIXJTF7FE5AX7AOO&" +
            "memo=Hello&" +
            "memo_type=MEMO_TEXT")
            .fromStellarUri()
            .memo `should equal` Memo(value = "Hello", type = "text")
    }

    @Test
    fun `uri with memo, id type, returns id Memo`() {
        ("web+stellar:pay?destination=GCALNQQBXAPZ2WIRSDDBMSTAKCUH5SG6U76YBFLQLIXJTF7FE5AX7AOO&" +
            "memo=1234&" +
            "memo_type=MEMO_ID")
            .fromStellarUri()
            .memo `should equal` Memo(value = "1234", type = "id")
    }

    @Test
    fun `uri with memo, hash type, returns hash Memo`() {
        ("web+stellar:pay?destination=GCALNQQBXAPZ2WIRSDDBMSTAKCUH5SG6U76YBFLQLIXJTF7FE5AX7AOO&" +
            "memo=abcd1234&" +
            "memo_type=MEMO_HASH")
            .fromStellarUri()
            .memo `should equal` Memo(value = "abcd1234", type = "hash")
    }

    @Test
    fun `uri with memo, return type, returns return Memo`() {
        ("web+stellar:pay?destination=GCALNQQBXAPZ2WIRSDDBMSTAKCUH5SG6U76YBFLQLIXJTF7FE5AX7AOO&" +
            "memo=abcd1234&" +
            "memo_type=MEMO_RETURN")
            .fromStellarUri()
            .memo `should equal` Memo(value = "abcd1234", type = "return")
    }

    @Test
    fun `uri with blank memo, hash type, returns no Memo`() {
        ("web+stellar:pay?destination=GCALNQQBXAPZ2WIRSDDBMSTAKCUH5SG6U76YBFLQLIXJTF7FE5AX7AOO&" +
            "memo=%20%20&" +
            "memo_type=MEMO_HASH")
            .fromStellarUri()
            .memo `should equal` Memo.None
    }

    @Test
    fun `uri with encoded memo text is decoded`() {
        ("web+stellar:pay?destination=GCALNQQBXAPZ2WIRSDDBMSTAKCUH5SG6U76YBFLQLIXJTF7FE5AX7AOO&" +
            "memo=Hello%20this%20is%20url%20encoded&" +
            "memo_type=MEMO_TEXT")
            .fromStellarUri()
            .memo `should equal` Memo(value = "Hello this is url encoded", type = "text")
    }

    @Test
    fun `uri with unknown memo type is still returned`() {
        ("web+stellar:pay?destination=GCALNQQBXAPZ2WIRSDDBMSTAKCUH5SG6U76YBFLQLIXJTF7FE5AX7AOO&" +
            "memo=Memo%20text&" +
            "memo_type=MEMO_NEW_TYPE")
            .fromStellarUri()
            .memo `should equal` Memo(value = "Memo text", type = "MEMO_NEW_TYPE")
    }

    @Test
    fun `throws exception if address is invalid`() {
        {
            "invalid".fromStellarUri()
        } `should throw` InvalidAccountIdException::class
    }
}