package piuk.blockchain.androidbuysellui.injector;

import dagger.Subcomponent;
import piuk.blockchain.androidbuysellui.ui.launcher.BuySellLauncherActivity;
import piuk.blockchain.androidbuysellui.ui.signup.SignupActivity;
import piuk.blockchain.androidbuysellui.ui.signup.select_country.SelectCountryFragment;
import piuk.blockchain.androidbuysellui.ui.signup.verify_email.VerifyEmailFragment;
import piuk.blockchain.androidbuysellui.ui.signup.create_account_completed.CreateAccountCompletedFragment;
import piuk.blockchain.androidbuysellui.ui.signup.create_account_start.CreateAccountStartFragment;
import piuk.blockchain.androidbuysellui.ui.signup.verify_identification.VerifyIdentificationFragment;
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

    void inject(CreateAccountStartFragment createAccountStartFragment);

    void inject(SelectCountryFragment selectCountryFragment);

    void inject(VerifyEmailFragment verifyEmailFragment);

    void inject(CreateAccountCompletedFragment createAccountCompletedFragment);

    void inject(VerifyIdentificationFragment verifyIdentificationFragment);
}