package piuk.blockchain.androidcore.data.payload

import com.blockchain.ui.password.SecondPasswordHandler
import com.blockchain.ui.password.secondPassword
import com.blockchain.wallet.Seed
import com.blockchain.wallet.SeedAccess
import com.blockchain.wallet.SeedAccessWithoutPrompt
import io.reactivex.Maybe

internal class PromptingSeedAccessAdapter(
    seedAccessWithoutPrompt: SeedAccessWithoutPrompt,
    private val secondPasswordHandler: SecondPasswordHandler
) : SeedAccess,
    SeedAccessWithoutPrompt by seedAccessWithoutPrompt {

    override val seedPromptIfRequired: Maybe<Seed>
        get() =
            Maybe.concat(
                seed,
                promptForSeed
            ).firstElement()

    override val seedForcePrompt: Maybe<Seed>
        get() =
            if (secondPasswordHandler.hasSecondPasswordSet) {
                promptForSeed
            } else {
                seed
            }

    private val promptForSeed: Maybe<Seed> =
        secondPasswordHandler
            .secondPassword()
            .flatMap { password -> seed(password) }
}
