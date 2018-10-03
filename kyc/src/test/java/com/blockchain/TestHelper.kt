package com.blockchain

import com.blockchain.kyc.models.nabu.KycState
import com.blockchain.kyc.models.nabu.NabuUser
import com.blockchain.kyc.models.nabu.UserState
import com.blockchain.nabu.metadata.NabuCredentialsMetadata

fun getBlankNabuUser(kycState: KycState = KycState.None): NabuUser = NabuUser(
    "",
    "",
    "",
    "",
    "",
    false,
    null,
    UserState.None,
    kycState,
    "",
    ""
)

val validOfflineToken get() = NabuCredentialsMetadata("userId", "lifetimeToken")