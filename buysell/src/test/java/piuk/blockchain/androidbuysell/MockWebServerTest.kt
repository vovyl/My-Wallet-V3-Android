package piuk.blockchain.androidbuysell

import android.support.annotation.CallSuper
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before

// TODO: This class should be changed to a TestRule - favour composition over inheritance
abstract class MockWebServerTest {

    protected lateinit var server: MockWebServer

    @Before
    @CallSuper
    open fun setUp() {
        server = MockWebServer()
    }

    @After
    @CallSuper
    open fun tearDown() {
        server.shutdown()
    }
}