package com.blockchain.kyc.models.nabu

data class NabuCountryResponse(
    override val code: String,
    override val name: String,
    override val scopes: List<String>,
    val regions: List<String>
) : NabuRegion {

    override val isState: Boolean
        get() = false

    override val parentCountryCode: String
        get() = code

    override val isKycAllowed: Boolean
        get() = scopes.any { it.equals(Scope.Kyc.value, ignoreCase = true) }
}

data class NabuStateResponse(
    override val code: String,
    override val name: String,
    override val scopes: List<String>,
    val countryCode: String
) : NabuRegion {

    override val isState: Boolean
        get() = true

    override val parentCountryCode: String
        get() = countryCode

    override val isKycAllowed: Boolean
        get() = scopes.any { it.equals(Scope.Kyc.value, ignoreCase = true) }
}

interface NabuRegion {

    val isState: Boolean

    val parentCountryCode: String

    val code: String

    val name: String

    val isKycAllowed: Boolean

    val scopes: List<String>
}

// TODO: There will likely be more scopes in future
enum class Scope(val value: String?) {
    Kyc("kyc"),
    None(null);
}