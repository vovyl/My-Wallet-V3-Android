package com.blockchain.kyc.dev

import android.app.Application
import android.content.Context
import com.blockchain.injection.kycModule
import com.blockchain.kyc.datamanagers.nabu.NabuDataManager
import com.blockchain.kyc.models.nabu.Address
import com.blockchain.kyc.models.nabu.KycState
import com.blockchain.kyc.models.nabu.NabuCountryResponse
import com.blockchain.kyc.models.nabu.NabuStateResponse
import com.blockchain.kyc.models.nabu.NabuUser
import com.blockchain.kyc.models.nabu.RegisterCampaignRequest
import com.blockchain.kyc.models.nabu.Scope
import com.blockchain.kyc.models.nabu.SupportedDocuments
import com.blockchain.kyc.models.nabu.UserState
import com.blockchain.metadata.MetadataRepository
import com.blockchain.nabu.NabuToken
import com.blockchain.nabu.models.NabuOfflineTokenResponse
import com.blockchain.nabu.models.NabuSessionTokenResponse
import com.blockchain.notifications.analytics.Loggable
import info.blockchain.wallet.ApiCode
import info.blockchain.wallet.BlockchainFramework
import info.blockchain.wallet.FrameworkInterface
import info.blockchain.wallet.api.Environment
import io.reactivex.Completable
import io.reactivex.Single
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.params.BitcoinMainNetParams
import org.koin.android.ext.android.get
import org.koin.android.ext.android.startKoin
import org.koin.dsl.module.applicationContext
import piuk.blockchain.androidcore.data.access.LogoutTimer
import piuk.blockchain.androidcore.data.settings.PhoneNumber
import piuk.blockchain.androidcore.data.settings.PhoneNumberUpdater
import piuk.blockchain.androidcore.data.settings.PhoneVerificationQuery
import piuk.blockchain.androidcore.utils.PrefsUtil
import retrofit2.Retrofit
import timber.log.Timber
import java.util.concurrent.TimeUnit

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        startKoin(
            this, listOf(
                kycModule,
                fakesModule
            ),
            extraProperties = mapOf(
                "api-code" to "25a6ad13-1633-4dfb-b6ee-9b91cdf0b5c3",
                "app-version" to BuildConfig.VERSION_NAME
            )
        )

        BlockchainFramework.init(object : FrameworkInterface {
            override fun getRetrofitApiInstance(): Retrofit {
                return get("api")
            }

            override fun getRetrofitExplorerInstance(): Retrofit {
                return get("explorer")
            }

            override fun getEnvironment(): Environment {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getBitcoinParams(): NetworkParameters {
                return BitcoinMainNetParams.get()
            }

            override fun getBitcoinCashParams(): NetworkParameters {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getDevice(): String {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getAppVersion(): String {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getDeviceId(): String {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override val apiCode: String
                get() = get<ApiCode>().apiCode
        })
    }
}

val fakesModule = applicationContext {

    bean { PrefsUtil(get()) }

    bean {
        object : PhoneVerificationQuery {
            override fun isPhoneNumberVerified(): Single<Boolean> =
                Single.just(false).delay(1, TimeUnit.SECONDS)
        } as PhoneVerificationQuery
    }

    bean {
        object : PhoneNumberUpdater {
            private var sms: String = ""

            override fun smsNumber(): Single<String> {
                return Single.just(sms).delay(500, TimeUnit.MILLISECONDS)
            }

            override fun updateSms(phoneNumber: PhoneNumber): Single<String> {
                return Single.timer(500, TimeUnit.MILLISECONDS)
                    .doOnSuccess {
                        sms = phoneNumber.sanitized
                    }
                    .map { phoneNumber.sanitized }
            }

            override fun verifySms(code: String): Single<String> {
                return smsNumber()
            }
        } as PhoneNumberUpdater
    }

    bean {
        object : NabuToken {
            override fun fetchNabuToken(): Single<NabuOfflineTokenResponse> {
                return Single.just(NabuOfflineTokenResponse("USER123", "TOKEN456"))
            }
        } as NabuToken
    }

    bean {
        object : com.blockchain.notifications.analytics.EventLogger {
            override fun logEvent(loggable: Loggable) {
                Timber.d(loggable.eventName)
            }
        } as com.blockchain.notifications.analytics.EventLogger
    }

    bean {
        object : LogoutTimer {
            override fun start(context: Context) {}

            override fun stop(context: Context) {}
        } as LogoutTimer
    }

    context("Payload") {

        bean {
            InMemoryMetadataRepository() as MetadataRepository
        }

        factory {
            FakeNabuDataManager() as NabuDataManager
        }
    }
}
