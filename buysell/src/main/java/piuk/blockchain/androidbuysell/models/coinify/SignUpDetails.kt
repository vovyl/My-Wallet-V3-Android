package piuk.blockchain.androidbuysell.models.coinify

@Suppress("unused")
data class SignUpDetails(
        val email: String,
        val defaultCurrency: String,
        val profile: Profile,
        val trustedEmailValidationToken: String
) {

    val partnerId: Int = 19
    val generateOfflineToken: Boolean = true

    companion object {

        /**
         * Factory method to return the most simple signup object possible.
         *
         * @return A basic [SignUpDetails] object for initial registration
         */
        fun basicSignUp(
                email: String,
                defaultCurrency: String,
                countryCode: String,
                trustedEmailValidationToken: String
        ): SignUpDetails = SignUpDetails(
                email,
                defaultCurrency,
                Profile(Address(countryCode)),
                trustedEmailValidationToken
        )

    }
}

