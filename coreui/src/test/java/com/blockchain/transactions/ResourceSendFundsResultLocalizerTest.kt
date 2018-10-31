package com.blockchain.transactions

import android.content.res.Resources
import com.blockchain.testutils.lumens
import com.nhaarman.mockito_kotlin.mock
import info.blockchain.balance.AccountReference
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should equal`
import org.junit.Test
import piuk.blockchain.androidcoreui.R

class ResourceSendFundsResultLocalizerTest {

    private val resources: Resources = mock {
        on { getString(R.string.transaction_submitted) } `it returns` "Success!"
        on { getString(R.string.transaction_failed) } `it returns` "Transaction failed!"
        on { getString(R.string.insufficient_funds) } `it returns` "Insufficient funds!"
        on {
            getString(
                R.string.insufficient_funds_with_max,
                500.lumens().toStringWithSymbol()
            )
        } `it returns` "Insufficient funds, your max is 500 XLM!"
        on {
            getString(
                R.string.transaction_failed_min_send,
                1.5.lumens().toStringWithSymbol()
            )
        } `it returns` "Min balance required is 1.5 XLM!"
        on {
            getString(
                R.string.xlm_transaction_failed_min_balance_new_account,
                1.lumens().toStringWithSymbol()
            )
        } `it returns` "Min balance for new account is 1 XLM!"
        on { getString(R.string.invalid_address) } `it returns` "Invalid address!"
    }

    @Test
    fun `success result`() {
        ResourceSendFundsResultLocalizer(resources)
            .localize(
                SendFundsResult(
                    sendDetails = mock(),
                    errorCode = 0,
                    confirmationDetails = null,
                    hash = "hash"
                )
            ) `should equal` "Success!"
    }

    @Test
    fun `xlm error 0 result (no hash)`() {
        ResourceSendFundsResultLocalizer(resources)
            .localize(
                SendFundsResult(
                    sendDetails = SendDetails(
                        from = AccountReference.Xlm("", ""),
                        toAddress = "",
                        value = mock()
                    ),
                    errorCode = 0,
                    errorValue = 1.lumens(),
                    confirmationDetails = null,
                    hash = null
                )
            ) `should equal` "Transaction failed!"
    }

    @Test
    fun `xlm error 1 result`() {
        ResourceSendFundsResultLocalizer(resources)
            .localize(
                SendFundsResult(
                    sendDetails = SendDetails(
                        from = AccountReference.Xlm("", ""),
                        toAddress = "",
                        value = mock()
                    ),
                    errorCode = 1,
                    errorValue = 1.lumens(),
                    confirmationDetails = null,
                    hash = "hash"
                )
            ) `should equal` "Transaction failed!"
    }

    @Test
    fun `xlm error 2 result`() {
        ResourceSendFundsResultLocalizer(resources)
            .localize(
                SendFundsResult(
                    sendDetails = SendDetails(
                        from = AccountReference.Xlm("", ""),
                        toAddress = "",
                        value = mock()
                    ),
                    errorCode = 2,
                    errorValue = 1.5.lumens(),
                    confirmationDetails = null,
                    hash = "hash"
                )
            ) `should equal` "Min balance required is 1.5 XLM!"
    }

    @Test
    fun `xlm error 3 result`() {
        ResourceSendFundsResultLocalizer(resources)
            .localize(
                SendFundsResult(
                    sendDetails = SendDetails(
                        from = AccountReference.Xlm("", ""),
                        toAddress = "",
                        value = mock()
                    ),
                    errorCode = 3,
                    errorValue = 1.lumens(),
                    confirmationDetails = null,
                    hash = "hash"
                )
            ) `should equal` "Min balance for new account is 1 XLM!"
    }

    @Test
    fun `xlm error 4 result`() {
        ResourceSendFundsResultLocalizer(resources)
            .localize(
                SendFundsResult(
                    sendDetails = SendDetails(
                        from = AccountReference.Xlm("", ""),
                        toAddress = "",
                        value = mock()
                    ),
                    errorCode = 4,
                    confirmationDetails = null,
                    hash = "hash"
                )
            ) `should equal` "Insufficient funds!"
    }

    @Test
    fun `xlm error 5 result`() {
        ResourceSendFundsResultLocalizer(resources)
            .localize(
                SendFundsResult(
                    sendDetails = SendDetails(
                        from = AccountReference.Xlm("", ""),
                        toAddress = "",
                        value = mock()
                    ),
                    errorCode = 5,
                    confirmationDetails = null,
                    hash = "hash"
                )
            ) `should equal` "Invalid address!"
    }

    @Test
    fun `xlm error 4 result with value`() {
        ResourceSendFundsResultLocalizer(resources)
            .localize(
                SendFundsResult(
                    sendDetails = SendDetails(
                        from = AccountReference.Xlm("", ""),
                        toAddress = "",
                        value = mock()
                    ),
                    errorCode = 4,
                    errorValue = 500.lumens(),
                    confirmationDetails = null,
                    hash = "hash"
                )
            ) `should equal` "Insufficient funds, your max is 500 XLM!"
    }

    @Test
    fun `ether error 4 result`() {
        ResourceSendFundsResultLocalizer(resources)
            .localize(
                SendFundsResult(
                    sendDetails = SendDetails(
                        from = AccountReference.Ethereum("", ""),
                        toAddress = "",
                        value = mock()
                    ),
                    errorCode = 4,
                    confirmationDetails = null,
                    hash = "hash"
                )
            ) `should equal` "Transaction failed!"
    }
}
