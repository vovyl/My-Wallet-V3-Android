package piuk.blockchain.android.ui.launcher

import android.net.Uri
import piuk.blockchain.androidcore.utils.PrefsUtil
import timber.log.Timber

private const val KEY_DEEP_LINK_URI = "deeplink_uri"

class DeepLinkPersistence(private val prefsUtil: PrefsUtil) {

    fun pushDeepLink(data: Uri?) {
        Timber.d("DeepLink: Saving uri: $data")
        prefsUtil.setValue(KEY_DEEP_LINK_URI, data.toString())
    }

    fun popUriFromSharedPrefs(): Uri? {
        val uri = prefsUtil.getValue(KEY_DEEP_LINK_URI, null)?.let { Uri.parse(it) }
        Timber.d("DeepLink: Read uri: $uri")
        prefsUtil.removeValue(KEY_DEEP_LINK_URI)
        return uri
    }
}
