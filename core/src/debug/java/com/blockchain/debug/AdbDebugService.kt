package com.blockchain.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.blockchain.metadata.MetadataRepository
import com.blockchain.serialization.JsonSerializable
import info.blockchain.wallet.payload.PayloadManagerWiper
import io.reactivex.rxkotlin.subscribeBy
import org.koin.KoinContext
import org.koin.standalone.StandAloneContext
import piuk.blockchain.androidcore.data.access.AccessState
import timber.log.Timber

class AdbDebugService : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("AdbDebugService Intent = $intent")
        intent.extras?.run {
            for (e in keySet()) {
                Timber.d(" $e = ${get(e)}")
            }
        }
        when (intent.action) {
        // Usage:
        // adb shell am broadcast
        // -n "piuk.blockchain.android/com.blockchain.debug.AdbDebugService"
        // -a "com.blockchain.intent.CLEAR_META_DATA"
        // --es type 11
            "com.blockchain.intent.CLEAR_META_DATA" -> clearMetadata(context, intent)
        // adb shell am broadcast
        // -n "piuk.blockchain.android/com.blockchain.debug.AdbDebugService"
        // -a "com.blockchain.intent.LOGOUT"
            "com.blockchain.intent.LOGOUT" -> releasePayloadAndLogout(context)
        }
    }

    private fun clearMetadata(context: Context, intent: Intent) {
        val metadataType = intent.extras?.get("type").toString().toIntOrNull()
        if (metadataType == null) {
            toast(context, "Specify type to clear with --es type 11")
            return
        }
        val repository = get<MetadataRepository>()
        repository.saveMetadata(Empty(), Empty::class.java, metadataType)
            .subscribeBy(onError = { Timber.e("Failed to clear meta data $it") }) {
                toast(context, "Written empty object over meta data no. $metadataType")
                releasePayloadAndLogout(context)
            }
    }

    private fun releasePayloadAndLogout(context: Context) {
        get<PayloadManagerWiper>().wipe()
        Timber.d("Released payload")
        get<AccessState>().logout(context)
        toast(context, "Logged out")
    }

    private fun toast(context: Context, message: String) {
        Timber.d(message)
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private class Empty : JsonSerializable

    private inline fun <reified T> get() = (StandAloneContext.koinContext as KoinContext).get<T>()
}
