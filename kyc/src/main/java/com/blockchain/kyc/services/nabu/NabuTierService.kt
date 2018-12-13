package com.blockchain.kyc.services.nabu

import com.blockchain.kyc.api.nabu.Nabu
import com.blockchain.kyc.extensions.wrapErrorMessage
import com.blockchain.kyc.models.nabu.TiersJson
import com.blockchain.nabu.NabuToken
import io.reactivex.Single

internal class NabuTierService(
    private val endpoint: Nabu,
    private val nabuToken: NabuToken
) : TierService {

    override fun tiers(): Single<TiersJson> =
        nabuToken.fetchNabuToken().flatMap {
            endpoint.getTiers(it.authHeader)
        }.wrapErrorMessage()
}
