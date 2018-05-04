package piuk.blockchain.android.ui.buysell.coinify.signup.identity_in_review

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_fragment_coinify_id_in_review.*
import piuk.blockchain.android.R
import piuk.blockchain.android.injection.Injector
import piuk.blockchain.android.ui.buysell.coinify.signup.CoinifyFlowListener
import piuk.blockchain.androidcoreui.ui.base.BaseFragment
import piuk.blockchain.androidcoreui.utils.extensions.inflate
import piuk.blockchain.androidcoreui.utils.extensions.invisible
import piuk.blockchain.androidcoreui.utils.extensions.visible
import javax.inject.Inject

class CoinifyIdentityInReviewFragment : BaseFragment<CoinifyIdentityInReviewView, CoinifyIdentityInReviewPresenter>(),
        CoinifyIdentityInReviewView {

    @Inject
    lateinit var presenter: CoinifyIdentityInReviewPresenter

    init {
        Injector.getInstance().presenterComponent.inject(this)
    }

    private var signUpListener: CoinifyFlowListener? = null

    override fun createPresenter() = presenter

    override fun getMvpView() = this

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = container?.inflate(R.layout.dialog_fragment_coinify_id_in_review)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonContinue.setOnClickListener { closeAndRestartBuySell() }

        onViewReady()
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

    private fun closeAndRestartBuySell() {
        signUpListener?.requestFinish()
    }

    override fun onShowLoading() {
        textviewReviewTitle.setText(R.string.buy_sell_id_verification_in_review_loading)
        textviewReviewMessage.invisible()
        textviewReviewStatus.invisible()
    }

    override fun onShowCompleted() {
        textviewReviewStatus.text = getString(R.string.buy_sell_review_status, getString(R.string.buy_sell_review_status_in_completed))
        textviewReviewTitle.setText(R.string.buy_sell_review_status_thanks)
        textviewReviewMessage.visible()
        textviewReviewStatus.visible()
    }

    override fun onShowReviewing() {
        textviewReviewStatus.text = getString(R.string.buy_sell_review_status, getString(R.string.buy_sell_review_status_in_reviewing))
        textviewReviewTitle.setText(R.string.buy_sell_review_status_thanks)
        textviewReviewMessage.visible()
        textviewReviewStatus.visible()
    }

    override fun onShowPending() {
        textviewReviewStatus.text = getString(R.string.buy_sell_review_status, getString(R.string.buy_sell_review_status_in_pending))
        textviewReviewTitle.setText(R.string.buy_sell_review_in_progress)
        textviewReviewMessage.invisible()
        textviewReviewStatus.visible()
    }

    override fun onShowRejected() {
        textviewReviewStatus.text = getString(R.string.buy_sell_review_status, getString(R.string.buy_sell_review_status_in_rejected))
        textviewReviewTitle.setText(R.string.buy_sell_review_failed)
        textviewReviewMessage.text = getString(R.string.buy_sell_review_status_failed)
        textviewReviewMessage.visible()
        textviewReviewStatus.visible()
    }

    override fun onShowExpired() {
        textviewReviewStatus.text = getString(R.string.buy_sell_review_status, getString(R.string.buy_sell_review_status_in_expired))
        textviewReviewTitle.setText(R.string.buy_sell_review_failed)
        textviewReviewMessage.text = getString(R.string.buy_sell_review_status_failed)
        textviewReviewMessage.visible()
        textviewReviewStatus.visible()
    }

    override fun onShowFailed() {
        textviewReviewStatus.text = getString(R.string.buy_sell_review_status, getString(R.string.buy_sell_review_status_in_failed))
        textviewReviewTitle.setText(R.string.buy_sell_review_failed)
        textviewReviewMessage.text = getString(R.string.buy_sell_review_status_failed)
        textviewReviewMessage.visible()
        textviewReviewStatus.visible()
    }

    override fun onShowDocumentsRequested() {
        textviewReviewStatus.text = getString(R.string.buy_sell_review_status, getString(R.string.buy_sell_review_status_in_docs_requested))
    }

    override fun onFinish() {
        activity?.finish()
    }

    companion object {

        internal fun newInstance(): CoinifyIdentityInReviewFragment {
            return CoinifyIdentityInReviewFragment()
        }

    }
}