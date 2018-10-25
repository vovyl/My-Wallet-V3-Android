package piuk.blockchain.android.ui.charts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.ViewGroup
import info.blockchain.balance.CryptoCurrency
import kotlinx.android.synthetic.main.activity_graphs.*
import piuk.blockchain.android.R
import piuk.blockchain.android.injection.Injector
import piuk.blockchain.androidcore.data.charts.TimeSpan
import piuk.blockchain.androidcore.utils.helperfunctions.unsafeLazy
import piuk.blockchain.androidcoreui.ui.base.BaseAuthActivity

class ChartsActivity : BaseAuthActivity(), TimeSpanUpdateListener {

    private val cryptoCurrency: CryptoCurrency by unsafeLazy {
        intent.getSerializableExtra(EXTRA_CRYPTOCURRENCY) as CryptoCurrency
    }
    private val fragments = CryptoCurrency.values().map { ChartsFragment.newInstance(it) }

    init {
        Injector.getInstance().presenterComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graphs)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val adapter = ChartsFragmentPagerAdapter(supportFragmentManager, fragments)

        viewpager.run {
            offscreenPageLimit = 3
            setAdapter(adapter)
            indicator.setViewPager(viewpager)
        }

        when (cryptoCurrency) {
            CryptoCurrency.BTC -> viewpager.currentItem = 0
            CryptoCurrency.ETHER -> viewpager.currentItem = 1
            CryptoCurrency.BCH -> viewpager.currentItem = 2
            CryptoCurrency.XLM -> viewpager.currentItem = 3
        }

        button_close.setOnClickListener { finish() }
    }

    override fun lockScreenOrientation() = Unit

    override fun onTimeSpanUpdated(timeSpan: TimeSpan) {
        fragments.forEach { it.onTimeSpanUpdated(timeSpan) }
    }

    companion object {

        private const val EXTRA_CRYPTOCURRENCY = "piuk.blockchain.android.EXTRA_CRYPTOCURRENCY"

        fun start(context: Context, cryptoCurrency: CryptoCurrency) {
            val intent = Intent(context, ChartsActivity::class.java).apply {
                putExtra(EXTRA_CRYPTOCURRENCY, cryptoCurrency)
            }
            context.startActivity(intent)
        }
    }

    private class ChartsFragmentPagerAdapter internal constructor(
        fragmentManager: FragmentManager,
        private val fragments: List<ChartsFragment>
    ) : FragmentPagerAdapter(fragmentManager) {

        override fun getItem(position: Int): ChartsFragment = fragments[position]

        override fun getCount(): Int = fragments.size
    }
}