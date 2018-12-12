package piuk.blockchain.androidcore.data.settings

import io.reactivex.Single

class Email(
    val address: String,
    val verified: Boolean
)

interface EmailUpdater {

    fun email(): Single<Email>

    fun updateEmail(email: String): Single<Email>

    fun resendEmail(): Single<Email>
}
