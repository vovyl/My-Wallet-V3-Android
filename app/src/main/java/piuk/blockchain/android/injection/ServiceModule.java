package piuk.blockchain.android.injection;

import info.blockchain.wallet.api.FeeApi;
import info.blockchain.wallet.api.WalletApi;
import info.blockchain.wallet.contacts.Contacts;
import info.blockchain.wallet.ethereum.EthAccountApi;
import info.blockchain.wallet.payment.Payment;
import info.blockchain.wallet.prices.PriceApi;
import info.blockchain.wallet.settings.SettingsManager;
import info.blockchain.wallet.shapeshift.ShapeShiftApi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.subjects.ReplaySubject;
import piuk.blockchain.androidbuysell.services.BuyConditions;
import piuk.blockchain.android.data.fingerprint.FingerprintAuth;
import piuk.blockchain.android.data.fingerprint.FingerprintAuthImpl;
import piuk.blockchain.androidcore.data.walletoptions.WalletOptionsState;

@Module
class ServiceModule {

    @Provides
    @Singleton
    SettingsManager provideSettingsManager() {
        return new SettingsManager();
    }

    @Provides
    @Singleton
    Contacts provideContacts() {
        return new Contacts();
    }

    @Provides
    WalletApi provideWalletApi() {
        return new WalletApi();
    }

    @Provides
    Payment providePayment() {
        return new Payment();
    }

    @Provides
    FeeApi provideFeeApi() {
        return new FeeApi();
    }

    @Provides
    PriceApi providePriceApi() {
        return new PriceApi();
    }

    @Provides
    ShapeShiftApi provideShapeShiftApi() {
        return new ShapeShiftApi();
    }

    @Provides
    FingerprintAuth provideFingerprintAuth() {
        return new FingerprintAuthImpl();
    }

    @Provides
    EthAccountApi provideEthAccountApi() {
        return new EthAccountApi();
    }

    @Provides
    BuyConditions provideBuyConditions() {
        return BuyConditions.getInstance(
                ReplaySubject.create(1),
                ReplaySubject.create(1),
                ReplaySubject.create(1));
    }

    @Provides
    WalletOptionsState provideWalletOptionsState() {
        return WalletOptionsState.getInstance(ReplaySubject.create(1), ReplaySubject.create(1));
    }

}
