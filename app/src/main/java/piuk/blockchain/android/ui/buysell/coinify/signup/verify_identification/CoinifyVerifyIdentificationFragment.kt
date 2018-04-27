package piuk.blockchain.android.ui.buysell.coinify.signup.verify_identification

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import piuk.blockchain.android.R
import piuk.blockchain.android.injection.Injector
import piuk.blockchain.android.ui.buysell.coinify.signup.CoinifyFlowListener
import piuk.blockchain.androidcore.utils.annotations.Thunk
import piuk.blockchain.androidcoreui.ui.base.BaseFragment
import piuk.blockchain.androidcoreui.utils.extensions.inflate
import piuk.blockchain.androidcoreui.utils.extensions.toast
import javax.inject.Inject

class CoinifyVerifyIdentificationFragment :
    BaseFragment<CoinifyVerifyIdentificationView, CoinifyVerifyIdentificationPresenter>(),
    CoinifyVerifyIdentificationView {

    @Inject
    lateinit var presenterCoinify: CoinifyVerifyIdentificationPresenter
    private var signUpListener: CoinifyFlowListener? = null

    init {
        Injector.INSTANCE.presenterComponent.inject(this)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = container?.inflate(R.layout.fragment_coinify_verify_identification)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toast("iSignThis Coming soon!")
        activity?.finish()

//        Timber.d("vos get arg: "+arguments?.getString(REDIRECT_URL))
//
//        if (AndroidUtils.is21orHigher()) {
//            CookieManager.getInstance().setAcceptThirdPartyCookies(buysell_webview, true)
//            WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
//        }
//
////        buysell_webview.setWebViewClient(EmailAwareWebViewClient())
//        buysell_webview.setWebChromeClient(object : WebChromeClient() {
//            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//            override fun onPermissionRequest(request: PermissionRequest) {
////                permissionRequest = request
//                requestScanPermissions()
//            }
//        })
////        buysell_webview.addJavascriptInterface(?, ?)
//        buysell_webview.getSettings().setJavaScriptEnabled(true)
//        buysell_webview.loadUrl(arguments?.getString(REDIRECT_URL))
//
//        onViewReady()
    }

    @Thunk
    internal fun requestScanPermissions() {
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is CoinifyFlowListener) {
            signUpListener = context
        } else {
            throw RuntimeException("$context must implement CoinifyFlowListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        signUpListener = null
    }

    override fun onStartOverview() {
        signUpListener?.requestStartOverview()
    }

    override fun createPresenter() = presenterCoinify

    override fun getMvpView() = this

    companion object {

        const val REDIRECT_URL = "piuk.blockchain.android.ui.buysell.coinify.signup.verify_identification.REDIRECT_URL"

        @JvmStatic
        fun newInstance(redirectUrl: String): CoinifyVerifyIdentificationFragment {
            return CoinifyVerifyIdentificationFragment().apply {
                arguments = Bundle().apply { putString(REDIRECT_URL, redirectUrl) }
            }
        }
    }
}