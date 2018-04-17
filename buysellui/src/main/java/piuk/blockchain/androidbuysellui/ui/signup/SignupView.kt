package piuk.blockchain.androidbuysellui.ui.signup

import android.support.v4.app.Fragment
import piuk.blockchain.androidcoreui.ui.base.View

interface SignupView: View {

    fun onStartWelcome()

    fun onStartSelectCountry()

    fun onStartVerifyEmail()

    fun onStartCreateAccountCompleted()

    fun onStartVerifyIdentification()

    fun onStartOverview()

    fun onProgressUpdate(currentFragment: Fragment)
}