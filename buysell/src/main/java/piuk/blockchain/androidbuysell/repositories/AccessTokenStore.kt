package piuk.blockchain.androidbuysell.repositories

import io.reactivex.Observable
import piuk.blockchain.androidbuysell.models.coinify.AuthResponse
import piuk.blockchain.androidcore.data.datastores.persistentstore.PersistentStore
import piuk.blockchain.androidcore.utils.Optional
import piuk.blockchain.androidcore.utils.annotations.Mockable
import javax.inject.Inject
import javax.inject.Singleton

@Mockable
@Singleton
class AccessTokenStore @Inject constructor() : TokenStore, PersistentStore<AuthResponse> {

    private var token: Optional<AuthResponse> = Optional.None

    override fun store(data: AuthResponse): Observable<AuthResponse> {
        token = Optional.Some(data)
        return Observable.just(getElement())
    }

    override fun getAccessToken(): Observable<Optional<AuthResponse>> = Observable.just(token)

    override fun invalidate() {
        token = Optional.None
    }

    private fun getElement(): AuthResponse = (token as Optional.Some).element

    fun requiresRefresh(): Boolean = when (token) {
        is Optional.None -> false
        else -> {
            val response = getElement()
            response.creationTime + response.expiresIn > System.currentTimeMillis() / 1000
        }
    }

}