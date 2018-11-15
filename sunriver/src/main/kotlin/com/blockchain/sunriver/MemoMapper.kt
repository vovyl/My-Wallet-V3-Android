package com.blockchain.sunriver

import org.stellar.sdk.Memo

internal class MemoMapper {

    fun mapMemo(memo: com.blockchain.transactions.Memo?): Memo =
        when {
            memo == null || memo.isEmpty() -> Memo.none()
            memo.type == "id" -> Memo.id(memo.value.toLong())
            memo.type == "hash" -> Memo.hash(memo.value)
            memo.type == null || memo.type == "text" -> Memo.text(memo.value)
            else -> throw IllegalArgumentException("Only null, text, hash and id are supported, not ${memo.type}")
        }
}
