package com.blockchain.koin.modules

import com.blockchain.network.EnvironmentUrls
import com.blockchain.network.modules.MoshiBuilderInterceptorList
import com.blockchain.network.modules.OkHttpInterceptors
import info.blockchain.balance.CryptoCurrency
import info.blockchain.wallet.ApiCode
import io.fabric8.mockwebserver.DefaultMockServer
import okhttp3.OkHttpClient
import org.koin.dsl.module.applicationContext

fun walletApiServiceTestModule(server: DefaultMockServer) = applicationContext {

    bean { OkHttpClient() }

    bean { OkHttpInterceptors(emptyList()) }

    factory {
        object : ApiCode {
            override val apiCode: String
                get() = "test"
        } as ApiCode
    }

    bean {
        MoshiBuilderInterceptorList(
            listOf(get("BigInteger"))
        )
    }

    bean {
        object : EnvironmentUrls {

            override val explorerUrl: String
                get() = throw NotImplementedError()

            override val apiUrl: String
                get() = server.url("")

            override fun websocketUrl(currency: CryptoCurrency): String {
                throw NotImplementedError()
            }
        } as EnvironmentUrls
    }
}