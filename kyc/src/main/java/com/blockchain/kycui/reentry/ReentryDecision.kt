package com.blockchain.kycui.reentry

import com.blockchain.kyc.models.nabu.NabuUser

interface ReentryDecision {

    fun findReentryPoint(user: NabuUser): ReentryPoint
}
