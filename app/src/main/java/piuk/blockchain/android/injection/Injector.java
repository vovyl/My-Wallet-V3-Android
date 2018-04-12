package piuk.blockchain.android.injection;

import android.app.Application;
import android.content.Context;

import piuk.blockchain.androidcore.injection.ApiModule;

/**
 * Created by adambennett on 08/08/2016.
 */

public enum Injector {

    INSTANCE;

    private ApplicationComponent applicationComponent;
    private PresenterComponent presenterComponent;

    public static Injector getInstance() {
        return INSTANCE;
    }

    public void init(Context applicationContext) {
        ApplicationModule applicationModule = new ApplicationModule((Application) applicationContext);
        ApiModule apiModule = new ApiModule();

        initAppComponent(applicationModule, apiModule);
    }

    protected void initAppComponent(ApplicationModule applicationModule, ApiModule apiModule) {
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(applicationModule)
                .apiModule(apiModule)
                .build();

        getPresenterComponent();
    }

    public ApplicationComponent getAppComponent() {
        return applicationComponent;
    }

    public PresenterComponent getPresenterComponent() {
        if (presenterComponent == null) {
            presenterComponent = applicationComponent.presenterComponent();
        }
        return presenterComponent;
    }

}
