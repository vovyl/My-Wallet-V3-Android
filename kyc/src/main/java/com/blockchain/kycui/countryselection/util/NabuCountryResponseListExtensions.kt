package com.blockchain.kycui.countryselection.util

import android.os.Parcelable
import com.blockchain.kyc.models.nabu.NabuRegion
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

private const val asciiOffset = 0x41
private const val flagOffset = 0x1F1E6

fun List<NabuRegion>.toDisplayList(): List<CountryDisplayModel> = this.map {
    CountryDisplayModel(
        it.name,
        if (it.isState) it.code else null,
        it.parentCountryCode,
        it.isState,
        if (it.isState) null else getFlagEmojiFromCountryCode(it.code)
    )
}.sortedWith(compareBy { it.name })

private fun getFlagEmojiFromCountryCode(countryCode: String): String {
    val firstChar = Character.codePointAt(countryCode, 0) - asciiOffset + flagOffset
    val secondChar = Character.codePointAt(countryCode, 1) - asciiOffset + flagOffset
    return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
}

@Parcelize
data class CountryDisplayModel(
    val name: String,
    val state: String? = null,
    val countryCode: String,
    val isState: Boolean = false,
    val flag: String? = null
) : Parcelable {

    val regionCode: String
        get() = if (isState) state!! else countryCode

    @IgnoredOnParcel
    val searchCode = "${name.acronym()};$regionCode;$name"
}

internal fun String.acronym(): String = String(
    toCharArray()
        .filter(Char::isUpperCase)
        .toCharArray()
)
