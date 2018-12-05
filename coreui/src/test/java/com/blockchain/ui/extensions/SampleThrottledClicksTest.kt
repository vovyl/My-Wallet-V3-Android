package com.blockchain.ui.extensions

import com.blockchain.android.testutils.rxInit
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.PublishSubject
import org.amshove.kluent.`should equal`
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

class SampleThrottledClicksTest {

    private val testScheduler = TestScheduler()

    @get:Rule
    val rx = rxInit {
        main(testScheduler)
    }

    private val subject = PublishSubject.create<Int>()
    private val sampler = PublishSubject.create<Unit>()

    @Test
    fun `sample none`() {
        subject.sampleThrottledClicks(sampler).test().assertNotComplete().assertValueCount(0)
    }

    @Test
    fun `no sample taken without sample signal`() {
        val test = subject.sampleThrottledClicks(sampler).test()

        subject.onNext(1)

        test.assertNotComplete().assertValueCount(0)
    }

    @Test
    fun `no sample taken without sample value`() {
        val test = subject.sampleThrottledClicks(sampler).test()

        sampler.onNext(Unit)

        test.assertNotComplete().assertValueCount(0)
    }

    @Test
    fun `sample one`() {
        val test = subject.sampleThrottledClicks(sampler).test()

        subject.onNext(1)
        sampler.onNext(Unit)

        test.assertNotComplete().values() `should equal` listOf(1)
    }

    @Test
    fun `sample two quickly, only one gets through`() {
        val test = subject.sampleThrottledClicks(sampler).test()

        subject.onNext(1)
        sampler.onNext(Unit)
        testScheduler.advanceTimeBy(499, TimeUnit.MILLISECONDS)
        sampler.onNext(Unit)

        test.assertNotComplete().values() `should equal` listOf(1)
    }

    @Test
    fun `sample two quickly, even if value changes`() {
        val test = subject.sampleThrottledClicks(sampler).test()

        subject.onNext(1)
        sampler.onNext(Unit)
        testScheduler.advanceTimeBy(499, TimeUnit.MILLISECONDS)
        subject.onNext(2)
        sampler.onNext(Unit)

        test.assertNotComplete().values() `should equal` listOf(1)
    }

    @Test
    fun `sample two with enough time between to reset throttle`() {
        val test = subject.sampleThrottledClicks(sampler).test()

        subject.onNext(1)
        sampler.onNext(Unit)
        testScheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS)
        sampler.onNext(Unit)

        test.assertNotComplete().values() `should equal` listOf(1, 1)
    }

    @Test
    fun `sample three with enough time between to reset throttle`() {
        val test = subject.sampleThrottledClicks(sampler).test()

        subject.onNext(1)
        sampler.onNext(Unit)
        testScheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS)
        sampler.onNext(Unit)
        subject.onNext(2)
        testScheduler.advanceTimeBy(499, TimeUnit.MILLISECONDS)
        sampler.onNext(Unit)
        test.assertNotComplete().values() `should equal` listOf(1, 1)

        testScheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS)
        test.assertNotComplete().values() `should equal` listOf(1, 1)

        sampler.onNext(Unit)
        test.assertNotComplete().values() `should equal` listOf(1, 1, 2)
    }
}
