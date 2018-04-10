package piuk.blockchain.androidbuysell.services;

import info.blockchain.wallet.api.data.Settings;
import info.blockchain.wallet.api.data.WalletOptions;

import io.reactivex.subjects.ReplaySubject;
import piuk.blockchain.androidbuysell.models.ExchangeData;

public class BuyConditions {

    private static BuyConditions instance;

    private ReplaySubject<WalletOptions> walletOptionsSource;
    private ReplaySubject<Settings> walletSettingsSource;
    private ReplaySubject<ExchangeData> exchangeDataSource;

    private BuyConditions(ReplaySubject<WalletOptions> walletOptionsSource,
                          ReplaySubject<Settings> walletSettingsSource,
                          ReplaySubject<ExchangeData> exchangeDataSource) {
        this.walletOptionsSource = walletOptionsSource;
        this.walletSettingsSource = walletSettingsSource;
        this.exchangeDataSource = exchangeDataSource;
    }

    public static BuyConditions getInstance(ReplaySubject<WalletOptions> walletOptionsSubject,
                                            ReplaySubject<Settings> walletSettingsSubject,
                                            ReplaySubject<ExchangeData> coinifyWhitelistedSubject) {
        if (instance == null)
            instance = new BuyConditions(walletOptionsSubject, walletSettingsSubject, coinifyWhitelistedSubject);
        return instance;
    }

    public ReplaySubject<WalletOptions> getWalletOptionsSource() {
        return walletOptionsSource;
    }

    public ReplaySubject<Settings> getWalletSettingsSource() {
        return walletSettingsSource;
    }

    public ReplaySubject<ExchangeData> getExchangeDataSource() {
        return exchangeDataSource;
    }

    public void wipe() {
        instance = null;
    }
}
