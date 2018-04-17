package piuk.blockchain.androidbuysellui.injector;

import javax.inject.Singleton;

import dagger.Component;
import piuk.blockchain.androidcoreui.injector.ContextModule;

@SuppressWarnings("WeakerAccess")
@Singleton
@Component(modules = {
        ContextModule.class
})
public interface BuySellApplicationComponent {

    // Subcomponent with its own scope (technically unscoped now that we're not deliberately
    // destroying a module between pages)
    BuySellPresenterComponent presenterComponent();

}
