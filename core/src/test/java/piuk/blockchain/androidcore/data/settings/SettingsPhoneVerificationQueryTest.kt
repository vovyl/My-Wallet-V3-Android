package piuk.blockchain.androidcore.data.settings

import com.nhaarman.mockito_kotlin.mock
import info.blockchain.wallet.api.data.Settings
import io.reactivex.Observable
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should be`
import org.junit.Test

class SettingsPhoneVerificationQueryTest {

    @Test
    fun `SMS verified`() {
        SettingsPhoneVerificationQuery(mock {
            on { fetchSettings() } `it returns` Observable.just(
                Settings.fromJson(
                    """
                {"sms_verified": 1}
            """
                )
            )
        }).needsPhoneVerification().test().assertComplete().values().single() `should be` false
    }

    @Test
    fun `Not SMS Verified`() {
        SettingsPhoneVerificationQuery(mock {
            on { fetchSettings() } `it returns` Observable.just(
                Settings.fromJson(
                    """
                {"sms_verified": 0}
            """
                )
            )
        }).needsPhoneVerification().test().assertComplete().values().single() `should be` true
    }

    @Test
    fun `Default`() {
        SettingsPhoneVerificationQuery(mock {
            on { fetchSettings() } `it returns` Observable.just(
                Settings.fromJson("{}")
            )
        }).needsPhoneVerification().test().assertComplete().values().single() `should be` true
    }
}
