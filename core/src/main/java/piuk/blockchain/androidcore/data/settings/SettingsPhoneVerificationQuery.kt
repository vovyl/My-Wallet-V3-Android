package piuk.blockchain.androidcore.data.settings

import com.blockchain.remoteconfig.FeatureFlag
import io.reactivex.Single

internal class SettingsPhoneVerificationQuery(
    private val settingsDataManager: SettingsDataManager
) : PhoneVerificationQuery {

    override fun needsPhoneVerification(): Single<Boolean> =
        settingsDataManager.fetchSettings()
            .map { !it.isSmsVerified }
            .single(true)
}

internal fun PhoneVerificationQuery.applyFlag(featureFlag: FeatureFlag) =
    object : PhoneVerificationQuery {
        override fun needsPhoneVerification(): Single<Boolean> =
            featureFlag.enabled
                .flatMap { enabled ->
                    if (enabled) {
                        this@applyFlag.needsPhoneVerification()
                    } else {
                        Single.just(false)
                    }
                }
    }
