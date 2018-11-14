package com.blockchain.sunriver

import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not be`
import org.amshove.kluent.`should throw the Exception`
import org.amshove.kluent.`with message`
import org.junit.Test
import org.stellar.sdk.Memo
import org.stellar.sdk.MemoHash
import org.stellar.sdk.MemoId
import org.stellar.sdk.MemoText

class MemoMapperTest {

    @Test
    fun `null memo`() {
        MemoMapper().mapMemo(null) `should equal` Memo.none()
    }

    @Test
    fun `with specified type -text- should be a MemoText`() {
        val memo = createMemo("Hello, test memo", type = "text")
        memo `should not be` null
        memo `should be instance of` MemoText::class.java
        (memo as MemoText).text `should equal` "Hello, test memo"
    }

    @Test
    fun `with no specified type -null- should be a MemoText`() {
        val memo = createMemo("Hello, test memo, with null")
        memo `should not be` null
        memo `should be instance of` MemoText::class.java
        (memo as MemoText).text `should equal` "Hello, test memo, with null"
    }

    @Test
    fun `with specified type -id- should be a MemoId`() {
        val memo = createMemo("9871230892735", type = "id")
        memo `should not be` null
        memo `should be instance of` MemoId::class.java
        (memo as MemoId).id `should equal` 9871230892735L
    }

    @Test
    fun `with specified type -hash- should be a MemoHash`() {
        val memo = createMemo("0102030405060707020212351a8e0d9fffff0f8f7f6f5f5f24f5f67f2f2f63fa", type = "hash")
        memo `should not be` null
        memo `should be instance of` MemoHash::class.java
        (memo as MemoHash).hexValue `should equal` "0102030405060707020212351a8e0d9fffff0f8f7f6f5f5f24f5f67f2f2f63fa"
    }

    @Test
    fun `with unknown specified type should throw`() {
        {
            MemoMapper().mapMemo(
                com.blockchain.transactions.Memo(
                    value = "Hello, test memo",
                    type = "unknown"
                )
            )!!
        } `should throw the Exception` IllegalArgumentException::class `with message`
            "Only null, text, hash and id are supported, not unknown"
    }

    @Test
    fun `Map none`() {
        MemoMapper().mapMemo(com.blockchain.transactions.Memo.None) `should equal` Memo.none()
    }

    @Test
    fun `Map blank text`() {
        MemoMapper().mapMemo(com.blockchain.transactions.Memo(value = "   ", type = "text")) `should equal` Memo.none()
    }

    @Test
    fun `Map blank id`() {
        MemoMapper().mapMemo(com.blockchain.transactions.Memo(value = "   ", type = "id")) `should equal` Memo.none()
    }

    private fun createMemo(value: String, type: String? = null) = MemoMapper().mapMemo(
        com.blockchain.transactions.Memo(
            value = value,
            type = type
        )
    )
}
