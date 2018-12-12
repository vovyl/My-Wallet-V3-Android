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
        return settingsDataManager
            .updateEmail(email)
            .toJustEmail()
    }

    override fun resendEmail(): Single<Email> {
        return email()
            .flatMap {
                updateEmail(it.address)
            }
    }
}

private fun Observable<Settings>.toJustEmail() =
    map { Email(it.smsNumber ?: "", it.isEmailVerified) }
        .single(Email("", false))
