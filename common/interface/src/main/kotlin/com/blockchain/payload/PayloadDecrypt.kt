package com.blockchain.payload

interface PayloadDecrypt {

    val isDoubleEncrypted: Boolean

    fun decryptHDWallet(validatedSecondPassword: String)

    fun decryptWatchOnlyWallet()
}
