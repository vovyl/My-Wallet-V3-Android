package piuk.blockchain.android.ui.buysell.coinify.signup.identity_in_review

import piuk.blockchain.androidcoreui.ui.base.View

interface CoinifyIdentityInReviewView: View {

    fun onShowLoading()

    fun onFinish()

    fun onShowCompleted()

    fun onShowReviewing()

    fun onShowPending()

    fun onShowRejected()

    fun onShowExpired()

    fun onShowFailed()

    fun onShowDocumentsRequested()
}