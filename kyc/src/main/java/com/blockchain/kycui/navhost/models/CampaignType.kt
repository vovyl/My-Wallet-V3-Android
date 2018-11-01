package com.blockchain.kycui.navhost.models

sealed class CampaignType {

    object NativeBuySell : CampaignType()
    object Sunriver : CampaignType()
}