package piuk.blockchain.android.injection

import com.blockchain.remoteconfig.FeatureFlag
import dagger.Module
import dagger.Provides
import info.blockchain.wallet.api.WalletApi
import piuk.blockchain.android.kyc.KycServerSideFeatureFlag

@Module
class KycModule {

    @Provides
    fun provideFeatureFlag(walletApi: dagger.Lazy<WalletApi>): FeatureFlag =
        KycServerSideFeatureFlag(walletApi.get())
}