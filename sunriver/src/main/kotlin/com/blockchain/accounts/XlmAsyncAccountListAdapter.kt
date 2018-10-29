package com.blockchain.accounts

import com.blockchain.sunriver.XlmDataManager
import info.blockchain.balance.AccountReference
import io.reactivex.Maybe
import io.reactivex.Single

internal class XlmAsyncAccountListAdapter(
    private val xlmDataManager: XlmDataManager
) : AsyncAccountList {

    override fun accounts(): Single<List<AccountReference>> =
        xlmDataManager.maybeDefaultAccount().map { listOf<AccountReference>(it) }
            .switchIfEmpty(Maybe.just(emptyList()))
            .toSingle()
}
