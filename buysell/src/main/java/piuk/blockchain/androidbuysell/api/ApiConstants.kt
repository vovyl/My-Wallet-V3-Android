package piuk.blockchain.androidbuysell.api

/**
 * Base URLs for Coinify, SFOX, iSignThis etc
 */
internal const val COINIFY_BASE: String = "https://app-api.coinify.com"

/**
 * Paths for Coinify
 */
internal const val PATH_COINFY_AUTH: String = "auth"
internal const val PATH_COINFY_SIGNUP: String = "signup"
internal const val PATH_COINFY_SIGNUP_TRADER: String = "$PATH_COINFY_SIGNUP/trader"
internal const val PATH_COINFY_VALIDATE_EMAIL: String = "$PATH_COINFY_SIGNUP/validate-email"

// etc