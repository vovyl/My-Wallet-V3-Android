package piuk.blockchain.android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import io.reactivex.subjects.ReplaySubject;
import piuk.blockchain.android.data.access.AccessState;
import piuk.blockchain.android.data.bitcoincash.BchDataManager;
import piuk.blockchain.android.data.ethereum.EthDataManager;
import piuk.blockchain.android.data.exchange.BuyConditions;
import piuk.blockchain.android.data.exchange.BuyDataManager;
import piuk.blockchain.androidcore.data.shapeshift.ShapeShiftDataManager;
import piuk.blockchain.android.data.websocket.WebSocketService;
import piuk.blockchain.android.injection.Injector;
import piuk.blockchain.android.ui.dashboard.DashboardPresenter;
import piuk.blockchain.android.util.OSUtil;
import piuk.blockchain.androidcore.utils.PrefsUtil;

public class LogoutActivity extends AppCompatActivity {

    @Inject protected BuyDataManager buyDataManager;
    @Inject protected EthDataManager ethDataManager;
    @Inject protected BchDataManager bchDataManager;
    @Inject protected ShapeShiftDataManager shapeShiftDataManager;
    @Inject protected OSUtil osUtil;

    {
        Injector.getInstance().getPresenterComponent().inject(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null && getIntent().getAction() != null) {
            if (getIntent().getAction().equals(AccessState.LOGOUT_ACTION)) {
                Intent intent = new Intent(this, WebSocketService.class);

                PrefsUtil prefsUtil = new PrefsUtil(this);

                //When user logs out, assume onboarding has been completed
                prefsUtil.setValue(PrefsUtil.KEY_ONBOARDING_COMPLETE, true);

                if (osUtil.isServiceRunning(WebSocketService.class)) {
                    stopService(intent);
                }

                // TODO: 21/02/2018 I'm not sure this is a great way to reset things, but it'll
                // do for now until we've had a rethink. Should individual datamanagers get
                // Rx events and handle their own state during logout?
                buyDataManager.wipe();
                ethDataManager.clearEthAccountDetails();
                bchDataManager.clearBchAccountDetails();
                shapeShiftDataManager.clearShapeShiftData();
                DashboardPresenter.onLogout();

                BuyConditions.getInstance(
                        ReplaySubject.create(1),
                        ReplaySubject.create(1),
                        ReplaySubject.create(1))
                        .wipe();

                AccessState.getInstance().setIsLoggedIn(false);
                finishAffinity();
            }
        }
    }
}
