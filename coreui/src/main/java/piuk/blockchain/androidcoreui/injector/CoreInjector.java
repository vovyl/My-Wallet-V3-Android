package piuk.blockchain.androidcoreui.injector;

import android.app.Application;
import android.content.Context;

public enum CoreInjector {

    INSTANCE;

    private BaseApplicationComponent applicationComponent;

    public static CoreInjector getInstance() {
        return INSTANCE;
    }

    public void init(Context applicationContext) {

        applicationComponent = DaggerBaseApplicationComponent.builder()
                .contextModule(new ContextModule((Application) applicationContext))
                .build();
    }

    public BaseApplicationComponent getAppComponent() {
        return applicationComponent;
    }
}

