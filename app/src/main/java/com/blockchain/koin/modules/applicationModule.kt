package com.blockchain.koin.modules

import android.content.Context
import com.blockchain.koin.getActivity
import com.blockchain.kycui.settings.KycStatusHelper
import com.blockchain.ui.chooser.AccountListing
import com.blockchain.ui.password.SecondPasswordHandler
import info.blockchain.wallet.util.PrivateKeyFactory
import org.koin.dsl.module.applicationContext
import piuk.blockchain.android.data.datamanagers.TransactionListDataManager
import piuk.blockchain.android.ui.account.SecondPasswordHandlerDialog
import piuk.blockchain.android.ui.chooser.WalletAccountHelperAccountListingAdapter
import piuk.blockchain.android.ui.receive.WalletAccountHelper
import piuk.blockchain.android.ui.swipetoreceive.SwipeToReceiveHelper
import piuk.blockchain.android.util.PrngHelper
import piuk.blockchain.android.util.StringUtils
import piuk.blockchain.androidbuysell.datamanagers.BuyDataManager
import piuk.blockchain.androidcore.data.bitcoincash.BchDataManager
import piuk.blockchain.androidcore.data.ethereum.EthDataManager
import piuk.blockchain.androidcore.utils.PrngFixer
import piuk.blockchain.androidcoreui.utils.DateUtil
import java.util.Locale

val applicationModule = applicationContext {

    factory { StringUtils(get()) }

    factory { get<Context>().resources }

    factory { Locale.getDefault() }

    context("Payload") {

        factory {
            EthDataManager(get(), get(), get(), get(), get(), get(), get())
        }

        factory {
            BchDataManager(get(), get(), get(), get(), get(), get(), get())
        }

        factory {
            BuyDataManager(get(), get(), get(), get(), get())
        }

        factory {
            SwipeToReceiveHelper(get(), get(), get(), get(), get(), get())
        }

        factory { WalletAccountHelper(get(), get(), get(), get(), get(), get(), get()) }

        factory { WalletAccountHelperAccountListingAdapter(get()) }
            .bind(AccountListing::class)

        factory { params ->
            SecondPasswordHandlerDialog(
                params.getActivity(),
                get()
            ) as SecondPasswordHandler
        }

        factory { KycStatusHelper(get(), get(), get()) }

        factory { TransactionListDataManager(get(), get(), get(), get(), get()) }
    }

    factory { DateUtil(get()) }

    bean { PrngHelper(get(), get()) as PrngFixer }

    factory { PrivateKeyFactory() }
}
