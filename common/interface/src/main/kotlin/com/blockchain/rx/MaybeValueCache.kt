package com.blockchain.rx

import io.reactivex.Maybe
import java.util.concurrent.atomic.AtomicReference

/**
 * Unlike [Maybe.cache], this will not cache errors or empty.
 */
fun <T> Maybe<T>.maybeCache(): Maybe<T> = MaybeValueCache(this).cached

private class MaybeValueCache<T>(maybe: Maybe<T>) {

    private val cachedValue = AtomicReference<T?>(null)

    private val cacheMaybe = Maybe.defer {
        cachedValue.get().let { value ->
            if (value != null) {
                Maybe.just<T>(value)
            } else {
                Maybe.empty<T>()
            }
        }
    }

    val cached: Maybe<T> = cacheMaybe
        .switchIfEmpty(
            maybe.doOnSuccess {
                cachedValue.set(it)
            })
}
