package piuk.blockchain.androidcore.utils.extensions

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.internal.functions.Functions

/**
 * Subscribes to a [Maybe] and silently consumes any emitted values. Any exceptions thrown won't
 * cascade into a [OnErrorNotImplementedException], but will be signalled to the RxJava plugin
 * error handler.
 *
 * @return A [Disposable] object.
 */
fun <T> Maybe<T>.emptySubscribe() =
        subscribe(Functions.emptyConsumer(), Functions.ERROR_CONSUMER)

/**
 * Subscribes to a [Maybe] and silently consumes any emitted values. Any exceptions thrown won't
 * cascade into a [OnErrorNotImplementedException], but will be signalled to the RxJava plugin
 * error handler.
 *
 * @return A [Disposable] object.
 */
fun <T> Single<T>.emptySubscribe() =
        subscribe(Functions.emptyConsumer(), Functions.ERROR_CONSUMER)

/**
 * Subscribes to a [Flowable] and silently consumes any emitted values. Any exceptions thrown won't
 * cascade into a [OnErrorNotImplementedException], but will be signalled to the RxJava plugin
 * error handler.
 *
 * @return A [Disposable] object.
 */
fun <T> Flowable<T>.emptySubscribe() =
        subscribe(Functions.emptyConsumer(), Functions.ERROR_CONSUMER)

/**
 * Subscribes to a [Observable] and silently consumes any emitted values. Any exceptions thrown won't
 * cascade into a [OnErrorNotImplementedException], but will be signalled to the RxJava plugin
 * error handler.
 *
 * @return A [Disposable] object.
 */
fun <T> Observable<T>.emptySubscribe() =
        subscribe(Functions.emptyConsumer(), Functions.ERROR_CONSUMER)

/**
 * Subscribes to a [Completable] and silently completes, if applicable. Any exceptions thrown won't
 * cascade into a [OnErrorNotImplementedException], but will be signalled to the RxJava plugin
 * error handler.
 *
 * @return A [Disposable] object.
 */
fun Completable.emptySubscribe() =
        subscribe(Functions.EMPTY_ACTION, Functions.ERROR_CONSUMER)