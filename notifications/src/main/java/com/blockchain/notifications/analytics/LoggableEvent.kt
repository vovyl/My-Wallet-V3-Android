package com.blockchain.notifications.analytics

enum class LoggableEvent(override val eventName: String) : Loggable {

    AccountsAndAddresses("accounts_and_addresses"),
    Backup("backup"),
    BuyBitcoin("buy_bitcoin"),
    Dashboard("dashboard"),
    Exchange("exchange"),
    ExchangeCreate("exchange_create"),
    ExchangeDetailConfirm("exchange_detail_confirm"),
    ExchangeDetailLocked("exchange_detail_locked"),
    ExchangeDetailOverview("exchange_detail_overview"),
    ExchangeHistory("exchange_history"),
    KycAddress("kyc_address"),
    KycComplete("kyc_complete"),
    KycCountry("kyc_country"),
    KycProfile("kyc_profile"),
    KycStates("kyc_states"),
    KycVerifyIdentity("kyc_verify_identity"),
    KycWelcome("kyc_welcome"),
    KycTiers("kyc_tiers"),
    Lockbox("lockbox"),
    Logout("logout"),
    Settings("settings"),
    Support("support"),
    WebLogin("web_login"),
}