package com.blockchain.logging

import io.reactivex.Completable

interface LastTxUpdater {

    fun updateLastTxTime(): Completable
}