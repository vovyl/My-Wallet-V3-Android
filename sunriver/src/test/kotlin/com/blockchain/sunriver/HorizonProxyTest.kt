package com.blockchain.sunriver

import com.blockchain.koin.sunriverModule
import com.blockchain.network.initRule
import com.blockchain.testutils.getStringFromResource
import com.blockchain.testutils.lumens
import io.fabric8.mockwebserver.DefaultMockServer
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should throw`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.standalone.StandAloneContext
import org.koin.standalone.get
import org.koin.test.AutoCloseKoinTest
import org.stellar.sdk.requests.ErrorResponse

class HorizonProxyTest : AutoCloseKoinTest() {

    private val server = DefaultMockServer()

    @get:Rule
    private val initMockServer = server.initRule()

    @Before
    fun startKoin() {
        StandAloneContext.startKoin(
            listOf(
                sunriverModule
            ),
            extraProperties = mapOf("HorizonURL" to server.url(""))
        )
    }

    @Test
    fun `get xlm balance`() {
        server.expect().get().withPath("/accounts/GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4")
            .andReturn(
                200,
                getStringFromResource("accounts/GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4.json")
            )
            .once()

        val proxy = get<HorizonProxy>()

        val balance =
            proxy.getBalance("GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4")

        balance `should equal` 109969.99997.lumens()
    }

    @Test
    fun `get balance if account does not exist`() {
        server.expect().get().withPath("/accounts/GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4")
            .andReturn(
                404,
                getStringFromResource("accounts/not_found.json")
            )
            .once()

        val proxy = get<HorizonProxy>()

        val balance =
            proxy.getBalance("GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4")

        balance `should equal` 0.lumens()
    }

    @Test
    fun `on any other kind of server error, bubble up exception`() {
        server.expect().get().withPath("/accounts/GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4")
            .andReturn(
                301,
                getStringFromResource("accounts/not_found.json")
            )
            .once()

        val proxy = get<HorizonProxy>();

        {
            proxy.getBalance("GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4")
        } `should throw` ErrorResponse::class
    }
}
