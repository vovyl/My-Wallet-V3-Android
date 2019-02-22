package com.blockchain.kyc

import com.blockchain.remoteconfig.RemoteConfiguration
import com.blockchain.remoteconfig.featureFlag

fun sunriverAirdropRemoteConfig(remoteConfiguration: RemoteConfiguration) =
    remoteConfiguration.featureFlag("android_sunriver_airdrop_enabled")

fun smsVerificationRemoteConfig(remoteConfiguration: RemoteConfiguration) =
    remoteConfiguration.featureFlag("android_sms_verification")
