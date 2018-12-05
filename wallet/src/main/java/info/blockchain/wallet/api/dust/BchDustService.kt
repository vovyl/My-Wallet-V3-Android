package info.blockchain.wallet.api.dust

import info.blockchain.balance.CryptoCurrency
import info.blockchain.wallet.ApiCode
import info.blockchain.wallet.api.dust.data.DustInput
import io.reactivex.Single

interface DustService {

    fun getDust(cryptoCurrency: CryptoCurrency): Single<DustInput>
}

internal class BchDustService(private val api: DustApi, private val apiCode: ApiCode) : DustService {

    override fun getDust(cryptoCurrency: CryptoCurrency): Single<DustInput> =
        api.getDust(cryptoCurrency.symbol.toLowerCase(), apiCode.apiCode)
}