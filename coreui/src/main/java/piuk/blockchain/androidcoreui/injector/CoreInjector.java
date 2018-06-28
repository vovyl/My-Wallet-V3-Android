package piuk.blockchain.androidcoreui.injector;

import android.content.Context;

public enum CoreInjector {

    INSTANCE;

    private CoreApplicationComponent applicationComponent;

    public static CoreInjector getInstance() {
        return INSTANCE;
    }

    public void init(Context applicationContext) {

        applicationComponent = DaggerCoreApplicationComponent.builder()
                .contextModule(new ContextModule(applicationContext))
                .build();
    }

    public CoreApplicationComponent getAppComponent() {
        return applicationComponent;
    }
}

