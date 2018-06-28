package piuk.blockchain.androidcoreui.injector;

import javax.inject.Singleton;

import dagger.Component;
import piuk.blockchain.androidcoreui.ui.base.BaseAuthActivity;

@SuppressWarnings("WeakerAccess")
@Singleton
@Component(modules = {
        ContextModule.class
})
public interface CoreApplicationComponent {
    void inject(BaseAuthActivity baseAuthActivity);
}
