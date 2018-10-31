package com.blockchain.rx

import io.reactivex.Observable
import io.reactivex.functions.BiFunction

/**
 * Every time a value is seen on the sample stream, emits the latest from the source stream.
 */
fun <T, U> Observable<T>.sampleEvery(sampler: Observable<U>): Observable<T> =
    sampler.withLatestFrom(this, BiFunction { _, t -> t })
