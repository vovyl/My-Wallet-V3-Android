package piuk.blockchain.android.ui.buy;

import android.support.annotation.Nullable;

import javax.inject.Inject;

import piuk.blockchain.android.R;
import piuk.blockchain.androidbuysell.datamanagers.BuyDataManager;
import piuk.blockchain.androidcore.data.access.AccessState;
import piuk.blockchain.androidcore.data.api.EnvironmentConfig;
import piuk.blockchain.androidcore.data.payload.PayloadDataManager;
import piuk.blockchain.androidcore.data.walletoptions.WalletOptionsDataManager;
import piuk.blockchain.androidcoreui.ui.base.BasePresenter;
import piuk.blockchain.androidcoreui.ui.base.UiState;
import piuk.blockchain.androidcoreui.ui.customviews.ToastCustom;
import piuk.blockchain.androidcoreui.utils.logging.Logging;
import timber.log.Timber;

/**
 * Created by justin on 4/27/17.
 */

public class BuyPresenter extends BasePresenter<BuyView> {

    private BuyDataManager buyDataManager;
    private PayloadDataManager payloadDataManager;
    private WalletOptionsDataManager walletOptionsDataManager;
    private AccessState accessState;
    private EnvironmentConfig environmentConfig;

    @Inject
    BuyPresenter(BuyDataManager buyDataManager,
                 PayloadDataManager payloadDataManager,
                 WalletOptionsDataManager walletOptionsDataManager,
                 AccessState accessState,
                 EnvironmentConfig environmentConfig) {

        this.buyDataManager = buyDataManager;
        this.payloadDataManager = payloadDataManager;
        this.walletOptionsDataManager = walletOptionsDataManager;
        this.accessState = accessState;
        this.environmentConfig = environmentConfig;
    }

    @Override
    public void onViewReady() {
        attemptPageSetup();
    }

    Boolean isNewlyCreated() {
        return accessState.isNewlyCreated();
    }

    void reloadExchangeDate() {
        buyDataManager.reloadExchangeData();
    }

    private void attemptPageSetup() {
        getView().setUiState(UiState.LOADING);

        getCompositeDisposable().add(payloadDataManager.loadNodes()
                .subscribe(loaded -> {
                    if (loaded) {
                        getCompositeDisposable().add(
                                buyDataManager
                                        .getWebViewLoginDetails()
                                        .subscribe(
                                                webViewLoginDetails -> getView().setWebViewLoginDetails(webViewLoginDetails),
                                                throwable -> {
                                                    Logging.INSTANCE.logException(throwable);
                                                    getView().setUiState(UiState.FAILURE);
                                                }));
                    } else {
                        // Not set up, most likely has a second password enabled
                        if (payloadDataManager.isDoubleEncrypted()) {
                            getView().showSecondPasswordDialog();
                            getView().setUiState(UiState.EMPTY);
                        } else {
                            generateMetadataNodes();
                        }
                    }
                }, throwable -> {
                    Logging.INSTANCE.logException(throwable);
                    getView().setUiState(UiState.FAILURE);
                }));
    }

    void decryptAndGenerateMetadataNodes(@Nullable String secondPassword) {
        if (!payloadDataManager.validateSecondPassword(secondPassword)) {
            getView().showToast(R.string.invalid_password, ToastCustom.TYPE_ERROR);
            getView().showSecondPasswordDialog();
            getView().setUiState(UiState.EMPTY);
        } else {
            try {
                payloadDataManager.decryptHDWallet(environmentConfig.getBitcoinNetworkParameters(), secondPassword);
                getCompositeDisposable().add(
                        payloadDataManager.generateNodes()
                                .subscribe(
                                        this::attemptPageSetup,
                                        throwable -> getView().setUiState(UiState.FAILURE)));
            } catch (Exception e) {
                Logging.INSTANCE.logException(e);
                Timber.e(e);
            }
        }
    }

    private void generateMetadataNodes() {
        getCompositeDisposable().add(
                payloadDataManager.generateNodes()
                        .subscribe(
                                this::attemptPageSetup,
                                throwable -> {
                                    Logging.INSTANCE.logException(throwable);
                                    getView().setUiState(UiState.FAILURE);
                                }));
    }

    String getCurrentServerUrl() {
        return walletOptionsDataManager.getBuyWebviewWalletLink();
    }
}
