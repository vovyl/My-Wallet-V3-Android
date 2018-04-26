package piuk.blockchain.android.ui.buysell.coinify.signup

import android.support.v4.app.Fragment
import piuk.blockchain.androidcoreui.ui.base.View

interface CoinifySignupView: View {

    fun onStartWelcome()

    fun onStartSelectCountry()

    fun onStartVerifyEmail()

    // Get to know you
    fun onStartCreateAccountCompleted()

    // Webview
    fun onStartVerifyIdentification()

    fun onStartOverview()

    fun onStartInvalidCountry()

    fun onProgressUpdate(currentFragment: Fragment)

    fun showToast(errorDescription: String)
}