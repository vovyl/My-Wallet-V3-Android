package com.blockchain.koin.modules

import android.app.Activity
import android.support.annotation.VisibleForTesting
import com.blockchain.kycui.navhost.KycNavHostActivity
import com.blockchain.kycui.navhost.models.CampaignType
import com.blockchain.kycui.settings.KycStatusHelper
import com.blockchain.kycui.settings.SettingsKycState
import com.blockchain.morph.MorphMethodSelector
import com.blockchain.morph.ui.homebrew.exchange.history.TradeHistoryActivity
import com.blockchain.notifications.analytics.EventLogger
import com.blockchain.notifications.analytics.LoggableEvent
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import org.koin.dsl.module.applicationContext
import timber.log.Timber

enum class MorphMethodType {
    HomeBrew,
    Kyc
}

/**
 * Yields the morph method enum to use.
 * Asynchronous, so you can ask a server or whatever you need to.
 */
interface MorphMethodTypeSelector : MorphMethodSelector<MorphMethodType>

/**
 * Yields a method you can use to launch the correct trading method or route to KYC.
 * Asynchronous, so you can ask a server or whatever you need to.
 */
interface MorphActivityLauncher : MorphMethodSelector<(Activity) -> Unit>

fun MorphActivityLauncher.launchAsync(activity: Activity) {
    getMorphMethod()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ it(activity) }, { Timber.e(it) })
}

val morphMethodModule = applicationContext {

    context("Payload") {

        bean {
            dynamicSelector(get(), get())
        }
    }

    bean {
        object : MorphActivityLauncher {
            override fun getMorphMethod() =
                get<MorphMethodTypeSelector>()
                    .getMorphMethod()
                    .map {
                        when (it) {
                            MorphMethodType.HomeBrew -> { activity: Activity ->
                                TradeHistoryActivity.start(activity)
                            }
                            MorphMethodType.Kyc -> { activity: Activity ->
                                KycNavHostActivity.start(activity, CampaignType.NativeBuySell)
                            }
                        }
                    }
        } as MorphActivityLauncher
    }
}

@VisibleForTesting
internal fun dynamicSelector(
    kycStatusHelper: KycStatusHelper,
    eventLogger: EventLogger
): MorphMethodTypeSelector =
    object : MorphMethodTypeSelector {
        override fun getMorphMethod(): Single<MorphMethodType> {
            eventLogger.logEvent(LoggableEvent.Exchange)
            return kycStatusHelper.getSettingsKycState()
                .map {
                    when (it) {
                        SettingsKycState.Hidden ->
                            throw IllegalStateException("Morph method fetched but KYC state is hidden")
                        SettingsKycState.Verified -> return@map MorphMethodType.HomeBrew
                        else -> return@map MorphMethodType.Kyc
                    }
                }
        }
    }