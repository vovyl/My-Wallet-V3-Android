package com.blockchain.koin.modules

import org.koin.dsl.module.applicationContext
import piuk.blockchain.android.ui.dashboard.DashboardData
import piuk.blockchain.android.ui.dashboard.DashboardDataCalculator
import piuk.blockchain.android.ui.dashboard.DashboardPresenter

val dashboardModule = applicationContext {

    context("Payload") {

        factory {
            DashboardDataCalculator(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get()
            ) as DashboardData
        }

        factory {
            DashboardPresenter(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get()
            )
        }
    }
}
