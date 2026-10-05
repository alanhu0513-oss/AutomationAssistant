package com.example.automationassistant

import android.app.Application
import com.example.automationassistant.automation.ShieldLog
import com.example.automationassistant.automation.SharedPreferencesStore
import com.example.automationassistant.automation.TargetStore

/**
 * Single hydration point for every persisted store. The activity, the
 * accessibility service and the quick-settings tile can each be the first
 * component of a fresh process — the service is even bound at boot without
 * ever launching an activity — so hydration must happen above all of them.
 */
class ShieldApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val store = SharedPreferencesStore(this)
        TargetStore.hydrate(store)
        ShieldLog.hydrate(store)
    }
}
