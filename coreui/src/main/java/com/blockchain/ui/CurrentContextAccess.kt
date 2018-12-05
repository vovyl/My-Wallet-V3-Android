package com.blockchain.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle

class CurrentContextAccess {

    var context: Context? = null

    fun createCallBacks(): Application.ActivityLifecycleCallbacks =
        object : Application.ActivityLifecycleCallbacks {

            override fun onActivityResumed(activity: Activity) {
                context = activity
            }

            override fun onActivityPaused(activity: Activity) {
                if (context == activity) {
                    context = null
                }
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {}

            override fun onActivityDestroyed(activity: Activity) {}
        }
}
