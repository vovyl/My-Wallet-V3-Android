package com.blockchain.injection

import com.blockchain.koin.moshiInterceptor
import com.blockchain.kyc.api.nabu.Nabu
import com.blockchain.kyc.datamanagers.nabu.NabuAuthenticator
import com.blockchain.kyc.datamanagers.nabu.NabuDataManager
import com.blockchain.kyc.datamanagers.nabu.NabuDataManagerImpl
import com.blockchain.kyc.datamanagers.onfido.OnfidoDataManager
import com.blockchain.kyc.models.nabu.KycStateAdapter
import com.blockchain.kyc.models.nabu.KycTierStateAdapter
import com.blockchain.kyc.models.nabu.UserStateAdapter
import com.blockchain.kyc.services.nabu.NabuService
import com.blockchain.kyc.services.nabu.NabuTierService
import com.blockchain.kyc.services.nabu.TierService
import com.blockchain.kyc.services.onfido.OnfidoService
import com.blockchain.kyc.services.wallet.RetailWalletTokenService
import com.blockchain.kycui.address.KycHomeAddressPresenter
import com.blockchain.kycui.address.Tier2Decision
import com.blockchain.kycui.address.Tier2DecisionAdapter
import com.blockchain.kycui.countryselection.KycCountrySelectionPresenter
import com.blockchain.kycui.email.entry.KycEmailEntryPresenter
import com.blockchain.kycui.email.validation.KycEmailValidationPresenter
import com.blockchain.kycui.invalidcountry.KycInvalidCountryPresenter
import com.blockchain.kycui.mobile.entry.KycMobileEntryPresenter
import com.blockchain.kycui.mobile.validation.KycMobileValidationPresenter
import com.blockchain.kycui.navhost.KycNavHostPresenter
import com.blockchain.kycui.onfidosplash.OnfidoSplashPresenter
import com.blockchain.kycui.profile.KycProfilePresenter
import com.blockchain.kycui.status.KycStatusPresenter
import com.blockchain.kycui.sunriver.SunriverAirdropRemoteConfig
import com.blockchain.kycui.sunriver.SunriverCampaignHelper
import com.blockchain.nabu.Authenticator
import com.blockchain.nabu.stores.NabuSessionTokenStore
import com.blockchain.remoteconfig.FeatureFlag
import org.koin.dsl.module.applicationContext
import retrofit2.Retrofit

val kycModule = applicationContext {

    bean { NabuSessionTokenStore() }

    bean { OnfidoService(get("kotlin")) }

    bean { NabuService(get("nabu")) }

    bean { RetailWalletTokenService(get(), getProperty("api-code"), get("kotlin")) }

    factory { OnfidoDataManager(get()) }

    context("Payload") {

        factory { KycCountrySelectionPresenter(get()) }

        factory { KycProfilePresenter(get(), get(), get()) }

        factory { KycHomeAddressPresenter(get(), get(), get(), get()) }

        factory { KycMobileEntryPresenter(get(), get(), get()) }

        factory { KycMobileValidationPresenter(get(), get(), get()) }

        factory { KycEmailEntryPresenter(get(), get(), get()) }

        factory { KycEmailValidationPresenter(get(), get(), get()) }

        factory { OnfidoSplashPresenter(get(), get(), get()) }

        factory { KycStatusPresenter(get(), get(), get()) }

        factory { KycNavHostPresenter(get(), get()) }

        factory { KycInvalidCountryPresenter(get(), get()) }

        factory("sunriver") { SunriverAirdropRemoteConfig(get()) }
            .bind(FeatureFlag::class)

        factory { SunriverCampaignHelper(get("sunriver"), get(), get(), get()) }
    }

    moshiInterceptor("kyc") { builder ->
        builder
            .add(KycStateAdapter())
            .add(KycTierStateAdapter())
            .add(UserStateAdapter())
    }
}

val kycNabuModule = applicationContext {

    context("Payload") {

        factory {
            NabuDataManagerImpl(
                get(),
                get(),
                get(),
                getProperty("app-version"),
                get("device-id"),
                get(),
                get()
            ) as NabuDataManager
        }

        factory {
            NabuAuthenticator(get(), get()) as Authenticator
        }

        factory {
            get<Retrofit>("nabu").create(Nabu::class.java)
        }

        factory {
            NabuTierService(get(), get()) as TierService
        }

        factory {
            Tier2DecisionAdapter(get(), get()) as Tier2Decision
        }
    }
}
