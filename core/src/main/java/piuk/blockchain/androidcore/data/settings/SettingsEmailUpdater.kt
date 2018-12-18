package piuk.blockchain.androidcore.data.settings

import info.blockchain.wallet.api.data.Settings
import io.reactivex.Observable
import io.reactivex.Single

internal class SettingsEmailUpdater(
    private val settingsDataManager: SettingsDataManager
) : EmailUpdater {

    override fun email(): Single<Email> {
        return settingsDataManager.fetchSettings()
            .toJustEmail()
    }

    override fun updateEmail(email: String): Single<Email> {
        return email()
            .flatMap { existing ->
                if (!existing.verified || existing.address != email) {
                    settingsDataManager.updateEmail(email)
                        .toJustEmail()
                } else {
                    Single.just(existing)
                }
            }
    }

    override fun resendEmail(email: String): Single<Email> {
        return settingsDataManager
            .updateEmail(email)
            .toJustEmail()
    }
}

private fun Observable<Settings>.toJustEmail() =
    map { Email(it.email ?: "", it.isEmailVerified) }
        .single(Email("", false))
