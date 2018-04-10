package piuk.blockchain.androidcore.data.walletoptions;

import android.support.annotation.NonNull;

import info.blockchain.wallet.api.data.Settings;
import info.blockchain.wallet.api.data.WalletOptions;
import io.reactivex.subjects.ReplaySubject;

public class WalletOptionsState {

    private static WalletOptionsState instance;

    private ReplaySubject<WalletOptions> walletOptionsSource;
    private ReplaySubject<Settings> walletSettingsSource;

    private WalletOptionsState(ReplaySubject<WalletOptions> walletOptionsSource,
                                     ReplaySubject<Settings> walletSettingsSource) {
        this.walletOptionsSource = walletOptionsSource;
        this.walletSettingsSource = walletSettingsSource;
    }

    public static WalletOptionsState getInstance(ReplaySubject<WalletOptions> walletOptionsSubject,
                                                       ReplaySubject<Settings> walletSettingsSubject) {
        if (instance == null)
            instance = new WalletOptionsState(walletOptionsSubject, walletSettingsSubject);
        return instance;
    }

    @NonNull
    public ReplaySubject<WalletOptions> getWalletOptionsSource() {
        return walletOptionsSource;
    }

    @NonNull
    public ReplaySubject<Settings> getWalletSettingsSource() {
        return walletSettingsSource;
    }

    public void destroy() {
        instance = null;
    }

}
