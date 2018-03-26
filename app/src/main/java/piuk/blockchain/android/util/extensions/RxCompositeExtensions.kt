@file:JvmName("RxCompositeExtensions")

package piuk.blockchain.android.util.extensions

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import piuk.blockchain.androidcoreui.ui.base.BasePresenter

/**
 * Adds the subscription to the upstream [Observable] to the [CompositeDisposable]
 * supplied by a class extending [BasePresenter]. This allows the subscription to be
 * cancelled automatically by the Presenter on Android lifecycle events.
 *
 * @param presenter A class extending [BasePresenter]
 * @param <T>       The type of the upstream [Observable]
 */
fun <T> Observable<T>.addToCompositeDisposable(presenter: BasePresenter<*>): Observable<T> =
        this.doOnSubscribe { presenter.compositeDisposable.add(it) }

/**
 * Adds the subscription to the upstream [Completable] to the [CompositeDisposable] supplied by a
 * class extending [BasePresenter]. This allows the subscription to be cancelled automatically by
 * the Presenter on Android lifecycle events.
 *
 * @param presenter A class extending [BasePresenter]
 */
fun Completable.addToCompositeDisposable(presenter: BasePresenter<*>): Completable =
        this.doOnSubscribe { presenter.compositeDisposable.add(it) }

/**
 * Adds the subscription to the upstream [Single] to the [CompositeDisposable]
 * supplied by a class extending [BasePresenter]. This allows the subscription to be
 * cancelled automatically by the Presenter on Android lifecycle events.
 *
 * @param presenter A class extending [BasePresenter]
 * @param <T>       The type of the upstream [Single]
</T> */
fun <T> Single<T>.addToCompositeDisposable(presenter: BasePresenter<*>): Single<T> =
        this.doOnSubscribe { presenter.compositeDisposable.add(it) }