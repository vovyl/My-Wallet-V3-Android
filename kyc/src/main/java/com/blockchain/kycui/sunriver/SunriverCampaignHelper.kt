package com.blockchain.kycui.sunriver

import com.blockchain.exceptions.MetadataNotFoundException
import com.blockchain.kyc.datamanagers.nabu.NabuDataManager
import com.blockchain.kyc.models.nabu.NabuApiException
import com.blockchain.kyc.models.nabu.NabuErrorCodes
import com.blockchain.kyc.models.nabu.RegisterCampaignRequest
import com.blockchain.kyc.models.nabu.UserState
import com.blockchain.kycui.extensions.fetchNabuToken
import com.blockchain.kycui.settings.KycStatusHelper
import com.blockchain.nabu.models.NabuOfflineTokenResponse
import com.blockchain.nabu.models.mapToMetadata
import com.blockchain.remoteconfig.FeatureFlag
import info.blockchain.balance.AccountReference
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import piuk.blockchain.androidcore.data.metadata.MetadataManager
import piuk.blockchain.androidcore.utils.helperfunctions.unsafeLazy

class SunriverCampaignHelper(
    private val featureFlag: FeatureFlag,
    private val nabuDataManager: NabuDataManager,
    private val metadataManager: MetadataManager,
    private val kycStatusHelper: KycStatusHelper
) {

    private val fetchOfflineToken by unsafeLazy { metadataManager.fetchNabuToken() }

    fun getCampaignCardType(): Single<SunriverCardType> =
        featureFlag.enabled
            .flatMap { enabled -> if (enabled) getCardsForUserState() else Single.just(SunriverCardType.None) }

    private fun getCardsForUserState(): Single<SunriverCardType> =
        Single.zip(
            kycStatusHelper.getUserState(),
            getCampaignList(),
            BiFunction { state: UserState, campaigns: List<String> -> state to campaigns }
        ).map { (state, campaigns) ->
            when (state) {
                UserState.Active -> if (campaigns.contains("SUNRIVER")) {
                    SunriverCardType.Complete
                } else {
                    SunriverCardType.JoinWaitList
                }
                UserState.Created -> SunriverCardType.FinishSignUp
                else -> SunriverCardType.JoinWaitList
            }
        }

    fun registerCampaignAndSignUpIfNeeded(xlmAccount: AccountReference.Xlm): Completable =
        fetchOfflineToken.onErrorResumeNext {
            if (it is MetadataNotFoundException) {
                createUserAndStoreInMetadata()
                    .map { pair -> pair.second }
            } else {
                Single.error(it)
            }
        }.flatMapCompletable { registerSunriverCampaign(it, xlmAccount) }

    private fun registerSunriverCampaign(
        token: NabuOfflineTokenResponse,
        xlmAccount: AccountReference.Xlm
    ): Completable =
        nabuDataManager.registerCampaign(
            token,
            RegisterCampaignRequest.registerSunriver(xlmAccount.accountId),
            "sunriver"
        ).onErrorResumeNext { throwable ->
            if (throwable is NabuApiException && throwable.getErrorCode() == NabuErrorCodes.AlreadyRegistered) {
                Completable.complete()
            } else {
                Completable.error(throwable)
            }
        }.subscribeOn(Schedulers.io())

    private fun createUserAndStoreInMetadata(): Single<Pair<String, NabuOfflineTokenResponse>> =
        nabuDataManager.requestJwt()
            .subscribeOn(Schedulers.io())
            .flatMap { jwt ->
                nabuDataManager.getAuthToken(jwt)
                    .subscribeOn(Schedulers.io())
                    .flatMap { tokenResponse ->
                        metadataManager.saveToMetadata(tokenResponse.mapToMetadata())
                            .toSingle { jwt to tokenResponse }
                    }
            }

    private fun getCampaignList(): Single<List<String>> = fetchOfflineToken.flatMap {
        nabuDataManager.getCampaignList(it)
    }.onErrorReturn { emptyList() }
}

sealed class SunriverCardType {

    object None : SunriverCardType()
    object JoinWaitList : SunriverCardType()
    object FinishSignUp : SunriverCardType()
    object Complete : SunriverCardType()
}
