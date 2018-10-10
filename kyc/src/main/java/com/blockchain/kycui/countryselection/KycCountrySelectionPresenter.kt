package com.blockchain.kycui.countryselection

import com.blockchain.kyc.datamanagers.nabu.NabuDataManager
import com.blockchain.kyc.models.nabu.NabuRegion
import com.blockchain.kyc.models.nabu.Scope
import com.blockchain.kycui.countryselection.models.CountrySelectionState
import com.blockchain.kycui.countryselection.util.CountryDisplayModel
import com.blockchain.kycui.countryselection.util.toDisplayList
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import piuk.blockchain.androidcore.data.walletoptions.WalletOptionsDataManager
import piuk.blockchain.androidcore.utils.helperfunctions.unsafeLazy
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import piuk.blockchain.kyc.R
import timber.log.Timber

internal class KycCountrySelectionPresenter(
    private val nabuDataManager: NabuDataManager,
    private val walletOptionsDataManager: WalletOptionsDataManager
) : BasePresenter<KycCountrySelectionView>() {

    private val countriesList by unsafeLazy {
        nabuDataManager.getCountriesList(Scope.None)
            .cache()
    }

    private val statesList by unsafeLazy {
        nabuDataManager.getStatesList("US", Scope.None)
            .cache()
    }

    private fun getRegionList() =
        if (view.regionType == RegionType.Country) countriesList else statesList

    override fun onViewReady() {
        compositeDisposable +=
            getRegionList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { view.renderUiState(CountrySelectionState.Loading) }
                .doOnError {
                    view.renderUiState(
                        CountrySelectionState.Error(R.string.kyc_country_selection_connection_error)
                    )
                }
                .doOnSuccess { view.renderUiState(CountrySelectionState.Data(it.toDisplayList())) }
                .subscribeBy(onError = { Timber.e(it) })
    }

    internal fun onRegionSelected(countryDisplayModel: CountryDisplayModel) {
        val regionCode = countryDisplayModel.regionCode
        val countryCode = countryDisplayModel.countryCode
        compositeDisposable +=
            getRegionList()
                .filter { it.isKycAllowed(regionCode) }
                .subscribeBy(
                    onSuccess = { view.continueFlow(countryCode) },
                    onComplete = {
                        when {
                            // Not found, is US, must select state
                            countryDisplayModel.requiresStateSelection() -> view.requiresStateSelection()
                            // Not found, is US state
                            countryDisplayModel.isState -> view.invalidCountry(countryDisplayModel)
                            // Not found, check country against SS
                            else -> checkShapeShift(countryDisplayModel)
                        }
                    },
                    onError = {
                        throw IllegalStateException("Region list should already be cached")
                    }
                )
    }

    private fun checkShapeShift(countryDisplayModel: CountryDisplayModel) {
        compositeDisposable +=
            walletOptionsDataManager.isInShapeShiftCountry(countryDisplayModel.countryCode)
                .subscribeBy(
                    onSuccess = {
                        if (it) {
                            view.redirectToShapeShift()
                        } else {
                            view.invalidCountry(countryDisplayModel)
                        }
                    }
                )
    }

    private fun List<NabuRegion>.isKycAllowed(regionCode: String): Boolean =
        this.any { it.isMatchingRegion(regionCode) && it.isKycAllowed }

    private fun NabuRegion.isMatchingRegion(regionCode: String): Boolean =
        this.code.equals(regionCode, ignoreCase = true)

    private fun CountryDisplayModel.requiresStateSelection(): Boolean =
        this.countryCode.equals("US", ignoreCase = true) && !this.isState

    internal fun onRequestCancelled() {
        compositeDisposable.clear()
    }
}