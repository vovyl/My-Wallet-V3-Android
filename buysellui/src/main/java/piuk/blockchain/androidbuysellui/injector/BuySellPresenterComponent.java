package piuk.blockchain.androidbuysellui.injector;

import dagger.Subcomponent;
import piuk.blockchain.androidbuysellui.ui.launcher.BuySellLauncherActivity;
import piuk.blockchain.androidbuysellui.ui.signup.SignupActivity;
import piuk.blockchain.androidbuysellui.ui.signup.welcome.WelcomeFragment;
import piuk.blockchain.androidcore.injection.PresenterScope;

/**
 * Subcomponents have access to all upstream objects in the graph but can have their own scope -
 * they don't need to explicitly state their dependencies as they have access anyway
 */
@SuppressWarnings("NullableProblems")
@PresenterScope
@Subcomponent
public interface BuySellPresenterComponent {

    // Activity/Fragment injection
    void inject(BuySellLauncherActivity buySellLauncherActivity);

    void inject(SignupActivity signupActivity);

    void inject(WelcomeFragment welcomeFragment);
}