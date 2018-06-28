package piuk.blockchain.androidcoreui.injector

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ContextModule(private val appContext: Context) {

    @Singleton
    @Provides
    fun appContext(): Context = appContext
}