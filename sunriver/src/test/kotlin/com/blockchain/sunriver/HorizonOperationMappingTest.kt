package com.blockchain.sunriver

import com.blockchain.testutils.lumens
import com.nhaarman.mockito_kotlin.mock
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.mock
import org.junit.Test
import org.stellar.sdk.KeyPair
import org.stellar.sdk.responses.operations.CreateAccountOperationResponse
import org.stellar.sdk.responses.operations.ManageDataOperationResponse
import org.stellar.sdk.responses.operations.PaymentOperationResponse
import java.util.Locale

class HorizonOperationMappingTest {

    @Test
    fun `map response rejects unsupported types`() {
        val unsupportedResponse: ManageDataOperationResponse = mock();
        {
            mapOperationResponse(unsupportedResponse, "")
        } `should throw` IllegalArgumentException::class
    }

    @Test
    fun `map payment operation where account is the receiver`() {
        val myAccount = "GDCERC7BR5N6NFK5B74XTTTA5OLC3YPWODQ5CHKRCRU6IVXFYP364JG7"
        val otherAccount = "GBPF72LVHGENTAC6JCBDU6KG6GNTQIHTTIYZGURQQL3CWXEBVNSUVFPL"
        mapOperationResponse(mock<PaymentOperationResponse> {
            on { from } `it returns` KeyPair.fromAccountId(otherAccount)
            on { to } `it returns` KeyPair.fromAccountId(myAccount)
            on { transactionHash } `it returns` "ABCD"
            on { createdAt } `it returns` "TIME"
            on { amount } `it returns` 50.lumens().toStringWithoutSymbol(Locale.US)
        }, myAccount)
            .apply {
                hash `should equal` "ABCD"
                timeStamp `should equal` "TIME"
                from.accountId `should equal` otherAccount
                to.accountId `should equal` myAccount
                total `should equal` 50.lumens()
            }
    }

    @Test
    fun `map payment operation where account is the sender`() {
        val myAccount = "GDCERC7BR5N6NFK5B74XTTTA5OLC3YPWODQ5CHKRCRU6IVXFYP364JG7"
        val otherAccount = "GBPF72LVHGENTAC6JCBDU6KG6GNTQIHTTIYZGURQQL3CWXEBVNSUVFPL"
        mapOperationResponse(mock<PaymentOperationResponse> {
            on { from } `it returns` KeyPair.fromAccountId(myAccount)
            on { to } `it returns` KeyPair.fromAccountId(otherAccount)
            on { transactionHash } `it returns` "ABCD"
            on { createdAt } `it returns` "TIME"
            on { amount } `it returns` 50.lumens().toStringWithoutSymbol(Locale.US)
        }, myAccount)
            .apply {
                hash `should equal` "ABCD"
                timeStamp `should equal` "TIME"
                from.accountId `should equal` myAccount
                to.accountId `should equal` otherAccount
                total `should equal` (-50).lumens()
            }
    }

    @Test
    fun `map create operation where account is the receiver`() {
        val myAccount = "GDCERC7BR5N6NFK5B74XTTTA5OLC3YPWODQ5CHKRCRU6IVXFYP364JG7"
        val otherAccount = "GBPF72LVHGENTAC6JCBDU6KG6GNTQIHTTIYZGURQQL3CWXEBVNSUVFPL"
        mapOperationResponse(mock<CreateAccountOperationResponse> {
            on { funder } `it returns` KeyPair.fromAccountId(otherAccount)
            on { account } `it returns` KeyPair.fromAccountId(myAccount)
            on { transactionHash } `it returns` "ABCD"
            on { createdAt } `it returns` "TIME"
            on { startingBalance } `it returns` 100.lumens().toStringWithoutSymbol(Locale.US)
        }, myAccount)
            .apply {
                hash `should equal` "ABCD"
                timeStamp `should equal` "TIME"
                from.accountId `should equal` otherAccount
                to.accountId `should equal` myAccount
                total `should equal` 100.lumens()
            }
    }

    @Test
    fun `map create operation where account is the sender`() {
        val myAccount = "GDCERC7BR5N6NFK5B74XTTTA5OLC3YPWODQ5CHKRCRU6IVXFYP364JG7"
        val otherAccount = "GBPF72LVHGENTAC6JCBDU6KG6GNTQIHTTIYZGURQQL3CWXEBVNSUVFPL"
        mapOperationResponse(mock<CreateAccountOperationResponse> {
            on { funder } `it returns` KeyPair.fromAccountId(myAccount)
            on { account } `it returns` KeyPair.fromAccountId(otherAccount)
            on { transactionHash } `it returns` "ABCD"
            on { createdAt } `it returns` "TIME"
            on { startingBalance } `it returns` 100.lumens().toStringWithoutSymbol(Locale.US)
        }, myAccount)
            .apply {
                hash `should equal` "ABCD"
                timeStamp `should equal` "TIME"
                from.accountId `should equal` myAccount
                to.accountId `should equal` otherAccount
                total `should equal` (-100).lumens()
            }
    }
}
