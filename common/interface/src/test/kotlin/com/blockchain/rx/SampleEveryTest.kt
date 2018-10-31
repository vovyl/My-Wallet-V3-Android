package com.blockchain.rx

import io.reactivex.subjects.PublishSubject
import org.amshove.kluent.`should equal`
import org.junit.Test

class SampleEveryTest {

    private val subject = PublishSubject.create<Int>()
    private val sampler = PublishSubject.create<Unit>()

    @Test
    fun `sample none`() {
        subject.sampleEvery(sampler).test().assertNotComplete().assertValueCount(0)
    }

    @Test
    fun `no sample taken without sample signal`() {
        val test = subject.sampleEvery(sampler).test()

        subject.onNext(1)

        test.assertNotComplete().assertValueCount(0)
    }

    @Test
    fun `no sample taken without sample value`() {
        val test = subject.sampleEvery(sampler).test()

        sampler.onNext(Unit)

        test.assertNotComplete().assertValueCount(0)
    }

    @Test
    fun `sample one`() {
        val test = subject.sampleEvery(sampler).test()

        subject.onNext(1)
        sampler.onNext(Unit)

        test.assertNotComplete().values() `should equal` listOf(1)
    }

    @Test
    fun `sample different values`() {
        val test = subject.sampleEvery(sampler).test()

        subject.onNext(3)
        sampler.onNext(Unit)
        subject.onNext(4)
        sampler.onNext(Unit)

        test.assertNotComplete().values() `should equal` listOf(3, 4)
    }

    @Test
    fun `complete by sampler`() {
        val test = subject.sampleEvery(sampler).test()

        sampler.onComplete()

        test.assertComplete()
    }

    @Test
    fun `complete by subject`() {
        val test = subject.sampleEvery(sampler).test()

        sampler.onComplete()

        test.assertComplete()
    }

    @Test
    fun `sample one value twice`() {
        val test = subject.sampleEvery(sampler).test()

        subject.onNext(3)
        sampler.onNext(Unit)
        sampler.onNext(Unit)

        test.assertNotComplete().values() `should equal` listOf(3, 3)
    }

    @Test
    fun `sample value once, replaced by another value`() {
        val test = subject.sampleEvery(sampler).test()

        subject.onNext(3)
        subject.onNext(4)
        sampler.onNext(Unit)

        test.assertNotComplete().values() `should equal` listOf(4)
    }

    @Test
    fun `sampler of different type`() {
        val samplerString = PublishSubject.create<String>()

        val test = subject.sampleEvery(samplerString).test()

        subject.onNext(1)
        samplerString.onNext("Test1")
        subject.onNext(2)
        subject.onNext(3)
        samplerString.onNext("Test2")

        test.assertNotComplete().values() `should equal` listOf(1, 3)
    }
}
