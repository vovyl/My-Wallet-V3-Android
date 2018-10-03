package info.blockchain.balance

import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

class XlmCryptoValueTest {

    @Test
    fun `ZeroXlm is same instance as from zero`() {
        CryptoValue.ZeroXlm `should be` CryptoValue.zero(CryptoCurrency.XLM)
    }

    @Test
    fun `format zero`() {
        CryptoValue.ZeroXlm
            .toStringWithSymbol(Locale.US) `should equal` "0 XLM"
    }

    @Test
    fun `format 1`() {
        CryptoCurrency.XLM.withMajorValue(BigDecimal.ONE)
            .toStringWithSymbol(Locale.US) `should equal` "1.0 XLM"
    }

    @Test
    fun `create via lumensFromMajor method`() {
        CryptoValue.lumensFromMajor(123.45.toBigDecimal()) `should equal`
            CryptoValue.fromMajor(CryptoCurrency.XLM, 123.45.toBigDecimal())
    }

    @Test
    fun `create via lumensFromStroop method`() {
        CryptoValue.lumensFromStroop(1234567.toBigInteger()) `should equal` 1.234567.lumens()
    }

    @Test
    fun `create via constructor`() {
        CryptoValue(CryptoCurrency.XLM, 9876543.toBigInteger()) `should equal` 9.876543.lumens()
    }

    @Test
    fun `format fractions`() {
        0.1.lumens().toStringWithSymbol(Locale.US) `should equal` "0.1 XLM"
        0.01.lumens().toStringWithSymbol(Locale.US) `should equal` "0.01 XLM"
        0.001.lumens().toStringWithSymbol(Locale.US) `should equal` "0.001 XLM"
        0.0001.lumens().toStringWithSymbol(Locale.US) `should equal` "0.0001 XLM"
        0.00001.lumens().toStringWithSymbol(Locale.US) `should equal` "0.00001 XLM"
        0.000001.lumens().toStringWithSymbol(Locale.US) `should equal` "0.000001 XLM"
    }

    @Test
    fun `format in French locale`() {
        1234.56789.lumens().toStringWithSymbol(Locale.FRANCE) `should equal` "1 234,56789 XLM"
    }
}
