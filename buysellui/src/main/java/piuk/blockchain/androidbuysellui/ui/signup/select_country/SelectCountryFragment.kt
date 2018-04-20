package piuk.blockchain.androidbuysellui.ui.signup.select_country

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_select_country.*
import piuk.blockchain.androidbuysellui.R
import piuk.blockchain.androidbuysellui.injector.BuySellInjector
import piuk.blockchain.androidbuysellui.ui.signup.SignupActivity
import piuk.blockchain.androidcoreui.ui.base.BaseFragment
import piuk.blockchain.androidcoreui.ui.customviews.ToastCustom
import piuk.blockchain.androidcoreui.utils.extensions.inflate
import javax.inject.Inject


class SelectCountryFragment: BaseFragment<SelectCountryView, SelectCountryPresenter>(),
        SelectCountryView {

    @Inject
    lateinit var presenter: SelectCountryPresenter

    init {
        BuySellInjector.INSTANCE.presenterComponent.inject(this)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = container?.inflate(R.layout.fragment_select_country)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buyandsellChooseCountryContinueButton.setOnClickListener {
            presenter.collectDataAndContinue(countryPicker.currentItemPosition)
        }

        onViewReady()
    }

    private fun broadcastIntent(action: String, countryCode: String) {
        activity?.run {
            val intent = Intent(action).apply {
                this.putExtra(COUNTRY_CODE, countryCode)
            }
            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(intent)
        }
    }

    override fun onStartVerifyEmail(countryCode: String) {
        broadcastIntent(SignupActivity.ACTION_NAVIGATE_VERIFY_EMAIL, countryCode)
    }

    override fun createPresenter() = presenter

    override fun getMvpView() = this

    override fun onSetCountryPickerData(countryNameList: List<String>) {
        countryPicker.data = countryNameList
    }

    override fun onAutoSelectCountry(position: Int) {
        countryPicker.selectedItemPosition = position
    }

    companion object {

        const val COUNTRY_CODE = "piuk.blockchain.androidbuysellui.ui.signup.select_country.COUNTRY_CODE"

        @JvmStatic
        fun newInstance(): SelectCountryFragment {
            return SelectCountryFragment()
        }
    }
}