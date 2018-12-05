package com.blockchain.logging

import com.blockchain.android.testutils.rxInit
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.Observable
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should equal`
import org.junit.Rule
import org.junit.Test
import piuk.blockchain.androidcore.data.settings.SettingsService
import java.util.Calendar

class LastTxUpdateDateOnSettingsServiceTest {

    @get:Rule
    val rxSchedulers = rxInit {
        ioTrampoline()
    }

    @Test
    fun `updates time successfully with time set to midnight`() {
        val captor = argumentCaptor<String>()
        val mockSettings = mock<SettingsService> {
            on { updateLastTxTime(captor.capture()) } `it returns` Observable.just(mock())
        }

        LastTxUpdateDateOnSettingsService(mockSettings).updateLastTxTime()
            .test()
            .assertComplete()

        verify(mockSettings).updateLastTxTime(captor.capture())

        val time = Calendar.getInstance().apply { timeInMillis = captor.firstValue.toLong() }
        with(time) {
            get(Calendar.HOUR_OF_DAY) `should equal` 0
            get(Calendar.MINUTE) `should equal` 0
            get(Calendar.SECOND) `should equal` 0
            get(Calendar.MILLISECOND) `should equal` 0
        }
    }

    @Test
    fun `call fails but still triggers complete`() {
        val mockSettings = mock<SettingsService> {
            on { updateLastTxTime(any()) } `it returns` Observable.error { Throwable() }
        }

        LastTxUpdateDateOnSettingsService(mockSettings).updateLastTxTime()
            .test()
            .assertComplete()
    }
}