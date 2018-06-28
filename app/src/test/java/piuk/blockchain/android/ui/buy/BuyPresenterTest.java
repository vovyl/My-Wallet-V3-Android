package piuk.blockchain.android.ui.buy;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.reactivex.Observable;
import piuk.blockchain.androidbuysell.datamanagers.BuyDataManager;
import piuk.blockchain.androidbuysell.models.WebViewLoginDetails;
import piuk.blockchain.androidcore.data.access.AccessState;
import piuk.blockchain.androidcore.data.payload.PayloadDataManager;
import piuk.blockchain.androidcore.data.walletoptions.WalletOptionsDataManager;
import piuk.blockchain.androidcoreui.ui.base.UiState;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BuyPresenterTest {

    private BuyPresenter subject;

    @Mock private BuyView activity;
    @Mock private PayloadDataManager payloadDataManager;
    @Mock private BuyDataManager buyDataManager;
    @Mock private AccessState accessState;
    @Mock private WalletOptionsDataManager walletOptionsDataManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        subject = new BuyPresenter(buyDataManager, payloadDataManager, walletOptionsDataManager, accessState);
        subject.initView(activity);
    }

    @Test
    public void onViewReady() {
        // Arrange
        WebViewLoginDetails webViewLoginDetails = new WebViewLoginDetails("", "",
                "", "");
        when(payloadDataManager.loadNodes()).thenReturn(
                Observable.just(true));
        when(buyDataManager.getWebViewLoginDetails()).thenReturn(
                Observable.just(webViewLoginDetails));

        // Act
        subject.onViewReady();
        // Assert
        verify(activity).setWebViewLoginDetails(webViewLoginDetails);
    }

    @Test
    public void onViewReady_secondPassword() {
        // Arrange
        WebViewLoginDetails webViewLoginDetails = new WebViewLoginDetails("", "",
                "", "");
        when(payloadDataManager.loadNodes()).thenReturn(
                Observable.just(false));
        when(buyDataManager.getWebViewLoginDetails()).thenReturn(
                Observable.just(webViewLoginDetails));
        when(payloadDataManager.isDoubleEncrypted()).thenReturn(
                true);

        // Act
        subject.onViewReady();
        // Assert
        verify(activity).showSecondPasswordDialog();
        verify(activity).setUiState(UiState.EMPTY);
    }

}
