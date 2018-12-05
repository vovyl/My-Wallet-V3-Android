package com.blockchain.ui.password

import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject

interface SecondPasswordHandler {

    val hasSecondPasswordSet: Boolean

    interface ResultListener {

        fun onNoSecondPassword()

        fun onSecondPasswordValidated(validatedSecondPassword: String)
    }

    interface ResultListenerEx : ResultListener {

        fun onCancelled()
    }

    fun validateExtended(listener: ResultListenerEx)

    fun validate(listener: SecondPasswordHandler.ResultListener) {
        validate(object : SecondPasswordHandler.ResultListenerEx, SecondPasswordHandler.ResultListener by listener {
            override fun onCancelled() {}
        })
    }
}

fun SecondPasswordHandler.secondPassword(): Maybe<String> {
    val password = PublishSubject.create<String>()

    return Maybe.defer {
        validateExtended(
            object : SecondPasswordHandler.ResultListenerEx {
                override fun onCancelled() {
                    password.onComplete()
                }

                override fun onNoSecondPassword() {
                    password.onComplete()
                }

                override fun onSecondPasswordValidated(validatedSecondPassword: String) {
                    password.onNext(validatedSecondPassword)
                }
            }
        )
        password.firstElement()
    }.subscribeOn(AndroidSchedulers.mainThread())
}
