package piuk.blockchain.androidcore.data.settings

import io.reactivex.Single

interface PhoneVerificationQuery {

    fun needsPhoneVerification(): Single<Boolean>
}
