package piuk.blockchain.android.ui.buysell.coinify.signup

import android.support.v4.app.Fragment
import piuk.blockchain.androidcoreui.ui.base.View

interface CoinifySignupView: View {

    fun onStartWelcome()

    fun onStartSelectCountry()

    fun onStartVerifyEmail()

    fun onStartCreateAccountCompleted()

    fun onStartVerifyIdentification()

    fun onStartOverview()

    fun onProgressUpdate(currentFragment: Fragment)
}