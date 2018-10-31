package com.blockchain.ui.extensions

import android.view.View
import com.blockchain.rx.sampleEvery
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

fun View.throttledClicks(): Observable<Unit> =
    clicks()
        .throttledClicks()

fun <U> Observable<U>.throttledClicks(): Observable<U> =
    throttleFirst(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())

fun <T> Observable<T>.sampleThrottledClicks(view: View): Observable<T> = sampleThrottledClicks(view.clicks())

fun <T, U> Observable<T>.sampleThrottledClicks(clicks: Observable<U>): Observable<T> =
    sampleEvery(clicks.throttledClicks())
