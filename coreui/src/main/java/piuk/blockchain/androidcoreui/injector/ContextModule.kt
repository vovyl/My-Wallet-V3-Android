package piuk.blockchain.androidcoreui.injector

import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class ContextModule(private val appContext: Context) {
    @Provides
    fun appContext(): Context = appContext;
}