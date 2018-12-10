package com.blockchain.nabu

import com.blockchain.nabu.models.NabuOfflineTokenResponse
import io.reactivex.Single

interface NabuToken {

    fun fetchNabuToken(): Single<NabuOfflineTokenResponse>
}
