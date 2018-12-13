package com.blockchain.kyc.services.nabu

import com.blockchain.kyc.api.nabu.Nabu
import com.blockchain.kyc.extensions.wrapErrorMessage
import com.blockchain.kyc.models.nabu.TierUpdateJson
import com.blockchain.kyc.models.nabu.TiersJson
import com.blockchain.nabu.NabuToken
import io.reactivex.Completable
import io.reactivex.Single

internal class NabuTierService(
    private val endpoint: Nabu,
    private val nabuToken: NabuToken
) : TierService, TierUpdater {

    override fun tiers(): Single<TiersJson> =
        nabuToken.fetchNabuToken().flatMap {
            endpoint.getTiers(it.authHeader)
        }.wrapErrorMessage()

    override fun setUserTier(tier: Int): Completable =
        nabuToken.fetchNabuToken()
            .flatMapCompletable {
                endpoint.setTier(
                    TierUpdateJson(tier),
                    it.authHeader
                )
            }
}
