package com.blockchain.kyc.models.nabu

enum class NabuErrorStatusCodes(val code: Int) {
    /**
     * The session token has expired, needs reissuing through /auth?userId=userId
     */
    TokenExpired(401),

    /**
     * Conflict. The user's data you're trying to save already exists.
     */
    AlreadyRegistered(409),

    /**
     * Error type not yet specified.
     */
    Unknown(-1);

    companion object {

        fun fromErrorCode(code: Int): NabuErrorStatusCodes =
            NabuErrorStatusCodes.values().firstOrNull { it.code == code } ?: Unknown
    }
}

enum class NabuErrorCodes(val code: Int) {

    /**
     * Error type not yet specified.
     */
    Unknown(-1),

    // Generic HTTP errors
    InternalServerError(1),
    NotFound(2),
    BadMethod(3),
    Conflict(4),

    // generic user input errors
    MissingBody(5),
    MissingParam(6),
    BadParamValue(7),

    // authentication errors
    InvalidCredentials(8),
    WrongPassword(9),
    Wrong2fa(10),
    Bad2fa(11),
    UnknownUser(12),
    InvalidRole(13),
    AlreadyLoggedIn(14),
    InvalidStatus(15),

    // currency ratio errors
    NotSupportedCurrencyPair(16),
    UnknownCurrencyPair(17),
    UnknownCurrency(18),
    CurrencyIsNotFiat(19),
    TooSmallVolume(26),
    TooBigVolume(27),
    ResultCurrencyRatioTooSmall(28),

    // conversion errors
    ProvidedVolumeIsNotDouble(20),
    UnknownConversionType(21),

    // kyc errors
    UserNotActive(22),
    PendingKycReview(23),
    KycAlreadyCompleted(24),
    MaxKycAttempts(25),
    InvalidCountryCode(29),

    // user-onboarding errors
    InvalidJwtToken(30),
    ExpiredJwtToken(31),
    MobileRegisteredAlready(32),
    UserRegisteredAlready(33),
    MissingApiToken(34),
    CouldNotInsertUser(35),
    UserRestored(36),

    // user trading error
    GenericTradingError(37),
    AlbertExecutionError(38),
    UserHasNoCountry(39),
    UserNotFound(40),
    OrderBelowMinLimit(41),
    WrongDepositAmount(42),
    OrderAboveMaxLimit(43),
    RatesApiError(44),
    DailyLimitExceeded(45),
    WeeklyLimitExceeded(46),
    AnnualLimitExceeded(47),
    NotCryptoToCryptoCurrencyPair(48),

    // Campaign Related Errors - These errors are specific
    // to users opting into an air drop campaign. Currently they're
    // used when a user deep links into the application from a campaign
    // related link.
    InvalidCampaign(54),
    InvalidCampaignUser(55),
    CampaignUserAlreadyRegistered(56),
    CampaignExpired(57),
    InvalidCampaignInfo(58),
    CampaignWithdrawalFailed(59),
    TradeForceExecuteError(60),
    CampaignInfoAlreadyUsed(61);

    companion object {

        fun fromErrorCode(code: Int): NabuErrorCodes =
            NabuErrorCodes.values().firstOrNull { it.code == code } ?: Unknown
    }
}

enum class NabuErrorTypes(val type: String) {
    InternalServerError("INTERNAL_SERVER_ERROR"),
    NotFound("NOT_FOUND"),
    BadMethod("BAD_METHOD"),
    Conflict("CONFLICT"),
    MissingBody("MISSING_BODY"),
    MissingParam("MISSING_PARAM"),
    BadParamValue("BAD_PARAM_VALUE"),
    InvalidCredential("INVALID_CREDENTIALS"),
    WrongPassword("WRONG_PASSWORD"),
    Wrong2FA("WRONG_2FA"),
    Bad2FA("BAD_2FA"),
    UnknownUser("UNKNOWN_USER"),
    InvalidRole("INVALID_ROLE"),
    AlreadyLoggedIn("ALREADY_LOGGED_IN"),
    InvalidStatus("INVALID_STATUS"),
    BadParamValues("BAD_PARAM_VALUE"),
    Unknown("UNKNOWN");

    companion object {

        fun fromErrorStatus(type: String): NabuErrorTypes =
            NabuErrorTypes.values().firstOrNull { it.type == type } ?: Unknown
    }
}