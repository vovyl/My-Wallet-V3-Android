package com.blockchain.kyc.remoteconfig

import com.blockchain.remoteconfig.RemoteConfiguration
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import org.amshove.kluent.`it returns`

fun RemoteConfiguration.givenEnabled(key: String) {
    whenever(getIfFeatureEnabled(key)) `it returns` Single.just(true)
}

fun RemoteConfiguration.givenDisabled(key: String) {
    whenever(getIfFeatureEnabled(key)) `it returns` Single.just(false)
}
