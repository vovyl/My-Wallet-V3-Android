package com.blockchain.rx

import io.reactivex.Maybe
import org.amshove.kluent.`should be`
import org.junit.Test

class MaybeValueCacheTest {

    @Test
    fun `regular cache empty behaviour`() {
        var subscriptions = 0
        val cached = Maybe.empty<Int>().doOnSubscribe { subscriptions++ }.cache()
        cached.test().assertNoValues().assertComplete()
        cached.test().assertNoValues().assertComplete()
        subscriptions `should be` 1
    }

    @Test
    fun `maybeCache empty behaviour`() {
        var subscriptions = 0
        val cached = Maybe.empty<Int>().doOnSubscribe { subscriptions++ }.maybeCache()
        cached.test().assertNoValues().assertComplete()
        cached.test().assertNoValues().assertComplete()
        subscriptions `should be` 2
    }

    @Test
    fun `regular cache error behaviour`() {
        var subscriptions = 0
        val cached = Maybe.error<Int>(Exception("X")).doOnSubscribe { subscriptions++ }.cache()
        cached.test().assertNoValues().assertErrorMessage("X")
        cached.test().assertNoValues().assertErrorMessage("X")
        subscriptions `should be` 1
    }

    @Test
    fun `maybeCache error behaviour`() {
        var subscriptions = 0
        val cached = Maybe.error<Int>(Exception("X")).doOnSubscribe { subscriptions++ }.maybeCache()
        cached.test().assertNoValues().assertErrorMessage("X")
        cached.test().assertNoValues().assertErrorMessage("X")
        subscriptions `should be` 2
    }

    @Test
    fun `regular cache single behaviour`() {
        var subscriptions = 0
        val cached = Maybe.just(10).doOnSubscribe { subscriptions++ }.cache()
        cached.test().assertValue(10).assertValueCount(1).assertComplete()
        cached.test().assertValue(10).assertValueCount(1).assertComplete()
        subscriptions `should be` 1
    }

    @Test
    fun `maybeCache single behaviour`() {
        var subscriptions = 0
        val cached = Maybe.just(10).doOnSubscribe { subscriptions++ }.maybeCache()
        cached.test().assertValue(10).assertValueCount(1).assertComplete()
        cached.test().assertValue(10).assertValueCount(1).assertComplete()
        subscriptions `should be` 1
    }

    @Test
    fun `regular cache before subscriptions`() {
        var subscriptions = 0
        Maybe.just(10).doOnSubscribe { subscriptions++ }.cache()
        subscriptions `should be` 0
    }

    @Test
    fun `maybeCache before subscriptions`() {
        var subscriptions = 0
        Maybe.just(10).doOnSubscribe { subscriptions++ }.maybeCache()
        subscriptions `should be` 0
    }

    @Test
    fun `maybeCache empty, becoming full behaviour`() {
        var subscriptions = 0
        var value: String? = null
        val subject = Maybe.defer {
            value.let {
                if (it != null) {
                    Maybe.just(it)
                } else {
                    Maybe.empty<String>()
                }
            }
        }
        val cached = subject.doOnSubscribe { subscriptions++ }.maybeCache()
        cached.test().assertNoValues().assertComplete()
        subscriptions `should be` 1
        value = "Hello"
        cached.test().assertValue("Hello").assertValueCount(1).assertComplete()
        subscriptions `should be` 2
        cached.test().assertValue("Hello").assertValueCount(1).assertComplete()
        subscriptions `should be` 2
        value = "World"
        cached.test().assertValue("Hello").assertValueCount(1).assertComplete()
        subscriptions `should be` 2
        value = null
        cached.test().assertValue("Hello").assertValueCount(1).assertComplete()
        subscriptions `should be` 2
    }
}
