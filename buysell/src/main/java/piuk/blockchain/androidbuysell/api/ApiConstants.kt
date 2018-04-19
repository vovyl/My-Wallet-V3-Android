package piuk.blockchain.androidbuysell.api

/**
 * Base URLs for Coinify, SFOX, iSignThis etc
 */
internal const val COINIFY_LIVE_BASE: String = "https://app-api.coinify.com/"

/**
 * Paths for Coinify
 */
internal const val PATH_COINFY_AUTH: String = "auth"
internal const val PATH_COINFY_SIGNUP: String = "signup"
internal const val PATH_COINFY_TRADES: String = "trades"
internal const val PATH_COINFY_TRADERS: String = "traders"
internal const val PATH_COINFY_SIGNUP_TRADER: String = "$PATH_COINFY_SIGNUP/trader"
internal const val PATH_COINFY_GET_TRADER: String = "$PATH_COINFY_TRADERS/me"
internal const val PATH_COINFY_PREP_KYC: String = "$PATH_COINFY_TRADERS/me/kyc"
internal const val PATH_COINFY_KYC: String = "kyc"
internal const val PATH_COINFY_TRADES_QUOTE: String = "$PATH_COINFY_TRADES/quote"
internal const val PATH_COINFY_TRADES_PAYMENT_METHODS: String = "$PATH_COINFY_TRADES/payment-methods"