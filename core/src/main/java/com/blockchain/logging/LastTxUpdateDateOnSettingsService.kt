package com.blockchain.logging

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import piuk.blockchain.androidcore.data.settings.SettingsService
import java.util.Calendar
import java.util.GregorianCalendar

internal class LastTxUpdateDateOnSettingsService(private val settingsService: SettingsService) : LastTxUpdater {

    override fun updateLastTxTime(): Completable {
        val date = GregorianCalendar().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return settingsService.updateLastTxTime(date.timeInMillis.toString())
            .ignoreElements()
            .onErrorComplete()
            .subscribeOn(Schedulers.io())
    }
}