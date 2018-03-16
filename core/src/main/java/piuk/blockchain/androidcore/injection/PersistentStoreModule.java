package piuk.blockchain.androidcore.injection;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import piuk.blockchain.androidcore.data.settings.SettingsService;
import piuk.blockchain.androidcore.data.settings.datastore.SettingsDataStore;
import piuk.blockchain.androidcore.data.settings.datastore.SettingsMemoryStore;

@Module
public class PersistentStoreModule {

    @Provides
    @Singleton
    SettingsDataStore provideSettingsDataStore(SettingsService settingsService) {
        return new SettingsDataStore(new SettingsMemoryStore(), settingsService.getSettingsObservable());
    }
}
