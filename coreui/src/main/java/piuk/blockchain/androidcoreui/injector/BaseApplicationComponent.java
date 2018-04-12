package piuk.blockchain.androidcoreui.injector;

import javax.inject.Singleton;

import dagger.Component;
import piuk.blockchain.androidcore.injection.ApiModule;
import piuk.blockchain.androidcoreui.ui.base.BaseAuthActivity;

@SuppressWarnings("WeakerAccess")
@Singleton
@Component(modules = {
        ApiModule.class,
        ContextModule.class
})
public interface BaseApplicationComponent {
    void inject(BaseAuthActivity baseAuthActivity);
}
