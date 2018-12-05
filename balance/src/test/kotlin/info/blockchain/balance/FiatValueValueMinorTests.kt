package info.blockchain.balance

import org.amshove.kluent.`should equal`
import org.junit.Test

class FiatValueValueMinorTests {

    @Test
    fun `value minor gbp`() {
        1.2.gbp().valueMinor `should equal` 120L
    }

    @Test
    fun `value minor gbp 2 dp`() {
        2.21.gbp().valueMinor `should equal` 221L
    }

    @Test
    fun `value minor yen`() {
        543.jpy().valueMinor `should equal` 543L
    }
}
