package com.blockchain.remoteconfig

import io.reactivex.Single

interface FeatureFlag {

    val enabled: Single<Boolean>
}
