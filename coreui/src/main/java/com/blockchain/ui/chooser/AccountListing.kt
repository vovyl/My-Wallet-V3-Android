package com.blockchain.ui.chooser

import info.blockchain.balance.CryptoCurrency
import io.reactivex.Single

interface AccountListing {

    fun accountList(cryptoCurrency: CryptoCurrency): Single<List<AccountChooserItem>>

    fun importedList(cryptoCurrency: CryptoCurrency): Single<List<AccountChooserItem>>
}
