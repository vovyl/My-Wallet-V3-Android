package piuk.blockchain.android.injection;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;

import info.blockchain.wallet.util.PrivateKeyFactory;

import java.util.Locale;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import piuk.blockchain.android.data.api.EnvironmentSettings;
import piuk.blockchain.androidcore.data.access.AccessState;
import piuk.blockchain.androidcore.data.currency.CurrencyState;


@Module
public class ApplicationModule {

    private final Application application;

    public ApplicationModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Context provideApplicationContext() {
        return application;
    }

    @Provides
    AccessState provideAccessState() {
        return AccessState.getInstance();
    }

    @Provides
    PrivateKeyFactory privateKeyFactory() {
        return new PrivateKeyFactory();
    }

    @Provides
    @Singleton
    NotificationManager provideNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Provides
    CurrencyState provideCurrencyState() {
        return CurrencyState.getInstance();
    }

    @Provides
    Locale provideLocale() {
        return Locale.getDefault();
    }

    @Provides
    @Named("explorer-url")
    String provideExplorerUrl(EnvironmentSettings environmentSettings) {
        return environmentSettings.getExplorerUrl();
    }
}
