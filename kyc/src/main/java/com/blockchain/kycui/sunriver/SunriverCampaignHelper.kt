package com.blockchain.kycui.sunriver

import com.blockchain.exceptions.MetadataNotFoundException
import com.blockchain.kyc.datamanagers.nabu.NabuDataManager
import com.blockchain.kyc.models.nabu.CampaignData
import com.blockchain.kyc.models.nabu.KycState
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
import io.reactivex.functions.Function3
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
            kycStatusHelper.getKycStatus(),
            getCampaignList(),
            Function3 { userState: UserState, kycState: KycState, campaigns: List<String> ->
                Triple(userState, kycState, campaigns)
            }
        ).map { (userState, kycState, campaigns) ->
            if (kycState == KycState.Verified && campaigns.contains("SUNRIVER")) {
                SunriverCardType.Complete
            } else if (kycState != KycState.Verified &&
                userState == UserState.Created &&
                campaigns.contains("SUNRIVER")
            ) {
                SunriverCardType.FinishSignUp
            } else {
                SunriverCardType.JoinWaitList
            }
        }

    fun registerCampaignAndSignUpIfNeeded(xlmAccount: AccountReference.Xlm, campaignData: CampaignData): Completable =
        fetchOfflineToken.onErrorResumeNext {
            if (it is MetadataNotFoundException) {
                createUserAndStoreInMetadata()
                    .map { pair -> pair.second }
            } else {
                Single.error(it)
            }
        }.flatMapCompletable { registerSunriverCampaign(it, xlmAccount, campaignData) }

    private fun registerSunriverCampaign(
        token: NabuOfflineTokenResponse,
        xlmAccount: AccountReference.Xlm,
        campaignData: CampaignData
    ): Completable =
        nabuDataManager.registerCampaign(
            token,
            RegisterCampaignRequest.registerSunriver(
                xlmAccount.accountId,
                campaignData.campaignCode,
                campaignData.campaignEmail,
                campaignData.newUser
            ),
            campaignData.campaignName
        ).subscribeOn(Schedulers.io())

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
