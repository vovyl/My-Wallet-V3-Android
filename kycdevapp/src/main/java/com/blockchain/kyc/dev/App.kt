package com.blockchain.kyc.dev

import android.app.Application
import android.content.Context
import com.blockchain.injection.kycModule
import com.blockchain.kyc.datamanagers.nabu.NabuDataManager
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
                Single.just(true).delay(1, TimeUnit.SECONDS)
        } as PhoneVerificationQuery
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
            object : NabuDataManager {

                override fun createBasicUser(
                    firstName: String,
                    lastName: String,
                    dateOfBirth: String,
                    offlineTokenResponse: NabuOfflineTokenResponse
                ): Completable {
                    Timber.d("Create basic user: $firstName, $lastName, $dateOfBirth")
                    return Completable.timer(2, TimeUnit.SECONDS)
                }

                override fun getUser(offlineTokenResponse: NabuOfflineTokenResponse): Single<NabuUser> {
                    return Single.just(
                        NabuUser(
                            firstName = "John",
                            lastName = "Doe",
                            email = "jdoe@email.com",
                            mobile = null,
                            dob = "2000-01-02",
                            mobileVerified = false,
                            address = null,
                            state = UserState.Created,
                            kycState = KycState.None,
                            insertedAt = null,
                            updatedAt = null,
                            tags = null
                        )
                    )
                }

                override fun getCountriesList(scope: Scope): Single<List<NabuCountryResponse>> {
                    return Single.just(
                        listOf(
                            NabuCountryResponse("DE", "Germany", listOf("KYC"), listOf("EEA")),
                            NabuCountryResponse("GB", "United Kingdom", listOf("KYC"), listOf("EEA"))
                        )
                    )
                }

                override fun updateUserWalletInfo(
                    offlineTokenResponse: NabuOfflineTokenResponse,
                    jwt: String
                ): Single<NabuUser> {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun addAddress(
                    offlineTokenResponse: NabuOfflineTokenResponse,
                    line1: String,
                    line2: String?,
                    city: String,
                    state: String?,
                    postCode: String,
                    countryCode: String
                ): Completable {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun recordCountrySelection(
                    offlineTokenResponse: NabuOfflineTokenResponse,
                    jwt: String,
                    countryCode: String,
                    stateCode: String?,
                    notifyWhenAvailable: Boolean
                ): Completable {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun getOnfidoApiKey(offlineTokenResponse: NabuOfflineTokenResponse): Single<String> {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun submitOnfidoVerification(
                    offlineTokenResponse: NabuOfflineTokenResponse,
                    applicantId: String
                ): Completable {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun getStatesList(countryCode: String, scope: Scope): Single<List<NabuStateResponse>> {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun getSupportedDocuments(
                    offlineTokenResponse: NabuOfflineTokenResponse,
                    countryCode: String
                ): Single<List<SupportedDocuments>> {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun registerCampaign(
                    offlineTokenResponse: NabuOfflineTokenResponse,
                    campaignRequest: RegisterCampaignRequest,
                    campaignName: String
                ): Completable {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun getCampaignList(offlineTokenResponse: NabuOfflineTokenResponse): Single<List<String>> {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun requestJwt(): Single<String> {
                    return Single.just("JWT1234")
                }

                override fun getAuthToken(jwt: String): Single<NabuOfflineTokenResponse> {
                    return Single.just(NabuOfflineTokenResponse("User123", "Token456"))
                }

                override fun <T> authenticate(
                    offlineToken: NabuOfflineTokenResponse,
                    singleFunction: (NabuSessionTokenResponse) -> Single<T>
                ): Single<T> {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun clearAccessToken() {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun invalidateToken() {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun currentToken(offlineToken: NabuOfflineTokenResponse): Single<NabuSessionTokenResponse> {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            } as NabuDataManager
        }
    }
}
