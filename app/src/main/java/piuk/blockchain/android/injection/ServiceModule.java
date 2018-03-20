package piuk.blockchain.android.injection;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.blockchain.wallet.contacts.Contacts;
import info.blockchain.wallet.payload.PayloadManager;
import info.blockchain.wallet.prices.PriceApi;
import info.blockchain.wallet.settings.SettingsManager;
import piuk.blockchain.android.data.contacts.ContactsService;
import piuk.blockchain.android.data.exchangerate.ExchangeRateService;
import piuk.blockchain.android.data.exchange.ExchangeService;
import piuk.blockchain.android.data.rxjava.RxBus;
import piuk.blockchain.android.data.settings.SettingsService;

@Module
class ServiceModule {

    @Provides
    @Singleton
    SettingsService provideSettingsService() {
        return new SettingsService(new SettingsManager());
    }

    @Provides
    @Singleton
    ExchangeService provideExchangeService(PayloadManager payloadManager, RxBus rxBus) {
        return new ExchangeService(payloadManager, rxBus);
    }

    @Provides
    @Singleton
    ContactsService provideContactsService() {
        return new ContactsService(new Contacts());
    }

    @Provides
    @Singleton
    ExchangeRateService provideExchangeRateService() {
        return new ExchangeRateService(new PriceApi());
    }
}
