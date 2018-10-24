package piuk.blockchain.androidcore.data.payload

import com.blockchain.android.testutils.rxInit
import com.blockchain.ui.password.SecondPasswordHandler
import com.blockchain.wallet.Seed
import com.blockchain.wallet.SeedAccess
import com.blockchain.wallet.SeedAccessWithoutPrompt
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import io.reactivex.Maybe
import org.amshove.kluent.`it returns`
import org.amshove.kluent.mock
import org.junit.Rule
import org.junit.Test

class PromptingSeedAccessAdapterTest {

    @get:Rule
    val initRx = rxInit {
        mainTrampoline()
    }

    @Test
    fun `seed prompt if required`() {
        val theSeed: Seed = mock()
        val seedAccessWithoutPrompt: SeedAccessWithoutPrompt = mock {
            on { seed } `it returns` Maybe.empty()
            on { seed(any()) } `it returns` Maybe.just(theSeed)
        }
        val secondPasswordHandler: SecondPasswordHandler = mock()
        val seedAccess: SeedAccess = PromptingSeedAccessAdapter(seedAccessWithoutPrompt, secondPasswordHandler)

        val test = seedAccess.seedPromptIfRequired.test()

        argumentCaptor<SecondPasswordHandler.ResultListenerEx>().apply {
            verify(secondPasswordHandler).validateExtended(capture())
            firstValue.onSecondPasswordValidated("ABCDEF")
        }

        verify(seedAccessWithoutPrompt).seed("ABCDEF")

        test.assertValue(theSeed)
            .assertComplete()
    }

    @Test
    fun `no seed prompt if not required`() {
        val theSeed: Seed = mock()
        val seedAccessWithoutPrompt: SeedAccessWithoutPrompt = mock {
            on { seed } `it returns` Maybe.just(theSeed)
        }
        val secondPasswordHandler: SecondPasswordHandler = mock()
        val seedAccess: SeedAccess = PromptingSeedAccessAdapter(seedAccessWithoutPrompt, secondPasswordHandler)

        seedAccess.seedPromptIfRequired
            .test()
            .assertValue(theSeed)
            .assertComplete()

        verifyZeroInteractions(secondPasswordHandler)
    }

    @Test
    fun `insist on seed prompt - prompt given`() {
        val theSeed: Seed = mock()
        val seedAccessWithoutPrompt: SeedAccessWithoutPrompt = mock {
            on { seed } `it returns` Maybe.empty()
            on { seed(any()) } `it returns` Maybe.just(theSeed)
        }
        val secondPasswordHandler: SecondPasswordHandler = mock {
            on { hasSecondPasswordSet } `it returns` true
        }
        val seedAccess: SeedAccess = PromptingSeedAccessAdapter(seedAccessWithoutPrompt, secondPasswordHandler)

        val test = seedAccess.seedForcePrompt.test()

        argumentCaptor<SecondPasswordHandler.ResultListenerEx>().apply {
            verify(secondPasswordHandler).validateExtended(capture())
            firstValue.onSecondPasswordValidated("ABCDEF")
        }

        verify(seedAccessWithoutPrompt).seed("ABCDEF")

        test.assertValue(theSeed)
            .assertComplete()
    }

    @Test
    fun `insist on seed prompt - but no second password set still returns seed`() {
        val theSeed: Seed = mock()
        val seedAccessWithoutPrompt: SeedAccessWithoutPrompt = mock {
            on { seed } `it returns` Maybe.just(theSeed)
        }
        val secondPasswordHandler: SecondPasswordHandler = mock {
            on { hasSecondPasswordSet } `it returns` false
        }
        val seedAccess: SeedAccess = PromptingSeedAccessAdapter(seedAccessWithoutPrompt, secondPasswordHandler)

        val test = seedAccess.seedForcePrompt.test()

        verify(seedAccessWithoutPrompt, never()).seed(any())
        verify(secondPasswordHandler, never()).validateExtended(any())

        test.assertValue(theSeed)
            .assertComplete()
    }

    @Test
    fun `insist on seed prompt - cancel pressed`() {
        val theSeed: Seed = mock()
        val seedAccessWithoutPrompt: SeedAccessWithoutPrompt = mock {
            on { seed } `it returns` Maybe.just(theSeed)
        }
        val secondPasswordHandler: SecondPasswordHandler = mock {
            on { hasSecondPasswordSet } `it returns` true
        }
        val seedAccess: SeedAccess = PromptingSeedAccessAdapter(seedAccessWithoutPrompt, secondPasswordHandler)

        val test = seedAccess.seedForcePrompt.test()

        argumentCaptor<SecondPasswordHandler.ResultListenerEx>().apply {
            verify(secondPasswordHandler).validateExtended(capture())
            firstValue.onCancelled()
        }

        verify(seedAccessWithoutPrompt, never()).seed(any())
        verify(secondPasswordHandler).validateExtended(any())
        verify(seedAccessWithoutPrompt, never()).seed

        test.assertNoValues()
            .assertComplete()
    }
}
