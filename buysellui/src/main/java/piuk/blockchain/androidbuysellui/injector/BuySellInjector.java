package piuk.blockchain.androidbuysellui.injector;

import android.content.Context;

import piuk.blockchain.androidcoreui.injector.ContextModule;
import piuk.blockchain.androidcoreui.injector.DaggerBaseApplicationComponent;

public enum BuySellInjector {

    INSTANCE;

    private BuySellApplicationComponent applicationComponent;
    private BuySellPresenterComponent presenterComponent;

    public static BuySellInjector getInstance() {
        return INSTANCE;
    }

    public void init(Context applicationContext) {
        applicationComponent = DaggerBuySellApplicationComponent.builder()
                .contextModule(new ContextModule(applicationContext))
                .build();
    }

    public BuySellPresenterComponent getPresenterComponent() {
        if (presenterComponent == null) {
            presenterComponent = applicationComponent.presenterComponent();
        }
        return presenterComponent;
    }
}

