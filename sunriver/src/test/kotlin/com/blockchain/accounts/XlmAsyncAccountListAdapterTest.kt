package com.blockchain.accounts

import com.nhaarman.mockito_kotlin.mock
import info.blockchain.balance.AccountReference
import io.reactivex.Maybe
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should equal`
import org.junit.Test

class XlmAsyncAccountListAdapterTest {

    @Test
    fun `XlmAsyncAccountListAdapter account list`() {
        val accountReference = AccountReference.Xlm("Xlm Account", "GABC")
        (XlmAsyncAccountListAdapter(mock {
            on { maybeDefaultAccount() } `it returns` Maybe.just(accountReference)
        }) as AsyncAccountList)
            .accounts().test().values().single() `should equal` listOf(accountReference)
    }

    @Test
    fun `XlmAsyncAccountListAdapter account list - empty`() {
        (XlmAsyncAccountListAdapter(mock {
            on { maybeDefaultAccount() } `it returns` Maybe.empty()
        }) as AsyncAccountList)
            .accounts().test().values().single() `should equal` emptyList()
    }
}
