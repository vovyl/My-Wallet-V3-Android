package piuk.blockchain.androidcore.data.settings

import io.reactivex.Single

class Email(
    val address: String,
    val verified: Boolean
)

interface EmailUpdater {

    fun email(): Single<Email>

    /**
     * Does nothing when email is unchanged and verified
     */
    fun updateEmail(email: String): Single<Email>

    /**
     * Always sends a new email, even if verified
     */
    fun resendEmail(email: String): Single<Email>
}
