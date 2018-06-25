package piuk.blockchain.android.ui.buysell.payment.bank.addaccount

import piuk.blockchain.android.R
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import piuk.blockchain.androidcoreui.ui.customviews.ToastCustom
import javax.inject.Inject

class AddBankAccountPresenter @Inject constructor() : BasePresenter<AddBankAccountView>() {

    override fun onViewReady() = Unit

    internal fun onConfirmClicked() {
        if (view.iban.isEmpty()) {
            view.showToast(R.string.buy_sell_add_account_iban_empty, ToastCustom.TYPE_ERROR)
            return
        }

        if (view.bic.isEmpty()) {
            view.showToast(R.string.buy_sell_add_account_bic_empty, ToastCustom.TYPE_ERROR)
            return
        }

        view.goToAddAddress(view.iban, view.bic)
    }
}