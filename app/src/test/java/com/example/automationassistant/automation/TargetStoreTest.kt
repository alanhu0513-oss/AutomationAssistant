package com.example.automationassistant.automation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TargetStoreTest {

    @Before
    fun reset() {
        TargetStore.hydrate(InMemoryKeyValueStore())
    }

    @Test
    fun `defaults are empty protected set, pending first launch, unprompted notifications`() {
        assertEquals(emptySet<String>(), TargetStore.protectedApps.value)
        assertTrue(TargetStore.isFirstLaunch.value)
        assertFalse(TargetStore.notificationsPrompted.value)
    }

    @Test
    fun `toggling a game updates the flow and the backing store`() {
        val store = InMemoryKeyValueStore()
        TargetStore.hydrate(store)

        TargetStore.setAppProtected("com.example.game", true)
        TargetStore.setAppProtected("com.example.other", true)
        assertEquals(setOf("com.example.game", "com.example.other"), TargetStore.protectedApps.value)
        assertEquals(
            setOf("com.example.game", "com.example.other"),
            store.readStringSet(TargetStore.KEY_PROTECTED_APPS),
        )

        TargetStore.setAppProtected("com.example.game", false)
        assertEquals(setOf("com.example.other"), TargetStore.protectedApps.value)
        assertEquals(
            setOf("com.example.other"),
            store.readStringSet(TargetStore.KEY_PROTECTED_APPS),
        )
    }

    @Test
    fun `hydrate reloads persisted values`() {
        val store = InMemoryKeyValueStore()
        store.writeStringSet(TargetStore.KEY_PROTECTED_APPS, setOf("com.example.game"))
        store.writeBoolean(TargetStore.KEY_IS_FIRST_LAUNCH, false)
        store.writeBoolean(TargetStore.KEY_NOTIFICATIONS_PROMPTED, true)

        TargetStore.hydrate(store)

        assertEquals(setOf("com.example.game"), TargetStore.protectedApps.value)
        assertFalse(TargetStore.isFirstLaunch.value)
        assertTrue(TargetStore.notificationsPrompted.value)
    }

    @Test
    fun `completing first launch persists the flag`() {
        val store = InMemoryKeyValueStore()
        TargetStore.hydrate(store)
        assertTrue(TargetStore.isFirstLaunch.value)

        TargetStore.completeFirstLaunch()

        assertFalse(TargetStore.isFirstLaunch.value)
        assertFalse(store.readBoolean(TargetStore.KEY_IS_FIRST_LAUNCH, default = true))
    }

    @Test
    fun `notifications prompt is recorded so the dialog shows only once`() {
        val store = InMemoryKeyValueStore()
        TargetStore.hydrate(store)

        TargetStore.markNotificationsPrompted()

        assertTrue(TargetStore.notificationsPrompted.value)
        assertTrue(store.readBoolean(TargetStore.KEY_NOTIFICATIONS_PROMPTED, default = false))

        // Survives a re-hydrate (fresh process).
        TargetStore.hydrate(store)
        assertTrue(TargetStore.notificationsPrompted.value)
    }
}
