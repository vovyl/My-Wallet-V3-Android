package piuk.blockchain.androidbuysellui.injector;

import dagger.Module;
import dagger.Provides;
import info.blockchain.wallet.payload.PayloadManager;

@Module
public class BuySellApplicationModule {

    @Provides
    protected PayloadManager providePayloadManager() {
        return PayloadManager.getInstance();
    }
}
