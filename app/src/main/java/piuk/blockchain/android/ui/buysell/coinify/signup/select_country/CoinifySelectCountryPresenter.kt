package piuk.blockchain.android.ui.buysell.coinify.signup.select_country

import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import java.util.*
import javax.inject.Inject

class CoinifySelectCountryPresenter @Inject constructor(
//    private val buysellDataManager: BuyDataManager
) : BasePresenter<CoinifySelectCountryView>() {

    private var countryCodeMap = mutableMapOf<String, String>()

    override fun onViewReady() {

        setCountryCodeMap()
//        autoSelectCountry(buysellDataManager.countryCode)
        autoSelectCountry("ZA")
    }

    fun setCountryCodeMap() {

        val unsortedMap = mutableMapOf<String, String>()

        for (code in Locale.getISOCountries()) {
            val loc = Locale("en", code)
            val displayName = loc.getDisplayCountry()

            unsortedMap.put(displayName, code)
        }

        countryCodeMap = unsortedMap.toSortedMap()

        view.onSetCountryPickerData(countryCodeMap.keys.toList())
    }

    private fun autoSelectCountry(countryCode: String) {
        val countryName = countryCodeMap
                .filterValues { it.equals(countryCode) }.keys.firstOrNull() ?: ""

        view.onAutoSelectCountry(countryCodeMap.keys.indexOf(countryName))
    }

    fun collectDataAndContinue(countryPosition: Int) {
        val countryName = countryCodeMap.keys.filterIndexed { index, value -> index == countryPosition }.last()
        val countryCode = countryCodeMap.get(countryName)

        countryCode?.let {

//            buysellDataManager.isInCoinifyCountry(countryCode)
//                    .applySchedulers()
//                    .subscribe { isAllowed ->
//                        if (isAllowed) {
                            view.onStartVerifyEmail(countryCode)
//                        } else {
//                            view.onStartInvalidCountry()
//                        }
//                    }
        }
    }

}