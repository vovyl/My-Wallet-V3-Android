package piuk.blockchain.android.ui.dashboard

import io.reactivex.Single

interface DashboardData {
    fun getPieChartData(): Single<PieChartsState.Data>
}
