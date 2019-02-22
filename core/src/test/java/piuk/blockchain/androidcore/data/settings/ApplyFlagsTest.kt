package piuk.blockchain.androidcore.data.settings

import com.blockchain.remoteconfig.FeatureFlag
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import io.reactivex.Single
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should be`
import org.junit.Test

class ApplyFlagsTest {

    @Test
    fun `when disabled - never queries the inner`() {
        val inner = mock<PhoneVerificationQuery>()
        inner.applyFlag(givenDisabledFeatureFlag())
            .needsPhoneVerification()
            .test()
            .values()
            .single() `should be` false
        verifyZeroInteractions(inner)
    }

    @Test
    fun `when enabled - inner true`() {
        val inner = mock<PhoneVerificationQuery> {
            on { needsPhoneVerification() } `it returns` Single.just(true)
        }
        inner.applyFlag(givenEnabledFeatureFlag())
            .needsPhoneVerification()
            .test()
            .values()
            .single() `should be` true
    }

    @Test
    fun `when enabled - inner false`() {
        val inner = mock<PhoneVerificationQuery> {
            on { needsPhoneVerification() } `it returns` Single.just(false)
        }
        inner.applyFlag(givenEnabledFeatureFlag())
            .needsPhoneVerification()
            .test()
            .values()
            .single() `should be` false
    }

    private fun givenEnabledFeatureFlag(): FeatureFlag =
        mock {
            on { enabled } `it returns` Single.just(true)
        }

    private fun givenDisabledFeatureFlag(): FeatureFlag =
        mock {
            on { enabled } `it returns` Single.just(false)
        }
}
