package com.blockchain.ui

import android.app.Activity
import org.amshove.kluent.`should be`
import org.amshove.kluent.mock
import org.junit.Test

class CurrentContextAccessTest {

    @Test
    fun `context is null at first`() {
        CurrentContextAccess().context `should be` null
    }

    @Test
    fun `on resume, the context is stored`() {
        val access = CurrentContextAccess()
        val activity = mock<Activity>()
        access.createCallBacks().onActivityResumed(activity)
        access.context `should be` activity
    }

    @Test
    fun `on pause, the context is released`() {
        val access = CurrentContextAccess()
        val activity = mock<Activity>()
        access.createCallBacks().apply {
            onActivityResumed(activity)
            onActivityPaused(activity)
        }
        access.context `should be` null
    }

    @Test
    fun `on pause of a different activity, the context is not touched`() {
        val access = CurrentContextAccess()
        val activity1 = mock<Activity>()
        val activity2 = mock<Activity>()
        access.createCallBacks().apply {
            onActivityResumed(activity1)
            onActivityPaused(activity2)
        }
        access.context `should be` activity1
    }
}
