package piuk.blockchain.android.ui.send

import com.blockchain.testutils.lumens
import com.blockchain.testutils.stroops
import com.blockchain.testutils.usd
import info.blockchain.balance.AccountReference
import org.amshove.kluent.`should equal`
import org.junit.Before
import org.junit.Test
import piuk.blockchain.android.ui.send.external.SendConfirmationDetails
import java.util.Locale

class SendConfirmationDetailsMapperTest {

    @Before
    fun setupLocal() {
        Locale.setDefault(Locale.US)
    }

    @Test
    fun `can map to PaymentConfirmationDetails`() {
        SendConfirmationDetails(
            from = AccountReference.Xlm("My account", ""),
            to = "Some Address",
            amount = 100.1.lumens(),
            fees = 99.stroops(),
            fiatAmount = 1234.45.usd(),
            fiatFees = 0.20.usd()
        )
            .toPaymentConfirmationDetails()
            .apply {
                this.cryptoUnit `should equal` "XLM"
                this.cryptoAmount `should equal` "100.1"
                this.cryptoFee `should equal` "0.000099"
                this.cryptoTotal `should equal` "100.100099"

                this.fiatUnit `should equal` "USD"
                this.fiatSymbol `should equal` "$"
                this.fiatAmount `should equal` "1,234.45"
                this.fiatFee `should equal` "0.20"
                this.fiatTotal `should equal` "1,234.65"

                this.fromLabel `should equal` "My account"
                this.toLabel `should equal` "Some Address"
            }
    }
}
