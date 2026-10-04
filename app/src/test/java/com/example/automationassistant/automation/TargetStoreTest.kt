package com.example.automationassistant.automation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class TargetStoreTest {

    @Before
    fun reset() {
        TargetStore.hydrate(InMemoryKeyValueStore())
    }

    @Test
    fun `both keys are unset by default`() {
        assertNull(TargetStore.targetProtectedApp.value)
        assertNull(TargetStore.interrupterPackageName.value)
    }

    @Test
    fun `writes update the flows and the backing store`() {
        val store = InMemoryKeyValueStore()
        TargetStore.hydrate(store)

        TargetStore.setTargetProtectedApp("com.example.protected")
        TargetStore.setInterrupterPackageName("com.example.interrupter")

        assertEquals("com.example.protected", TargetStore.targetProtectedApp.value)
        assertEquals("com.example.interrupter", TargetStore.interrupterPackageName.value)
        assertEquals("com.example.protected", store.read(TargetStore.KEY_TARGET_PROTECTED_APP))
        assertEquals("com.example.interrupter", store.read(TargetStore.KEY_INTERRUPTER_PACKAGE_NAME))
    }

    @Test
    fun `hydrate reloads persisted values`() {
        val store = InMemoryKeyValueStore(
            mapOf(
                TargetStore.KEY_TARGET_PROTECTED_APP to "com.example.protected",
                TargetStore.KEY_INTERRUPTER_PACKAGE_NAME to "com.example.interrupter",
            ),
        )
        TargetStore.hydrate(store)
        assertEquals("com.example.protected", TargetStore.targetProtectedApp.value)
        assertEquals("com.example.interrupter", TargetStore.interrupterPackageName.value)
    }

    @Test
    fun `clearing a value removes the key`() {
        val store = InMemoryKeyValueStore(
            mapOf(TargetStore.KEY_TARGET_PROTECTED_APP to "com.example.protected"),
        )
        TargetStore.hydrate(store)
        TargetStore.setTargetProtectedApp(null)
        assertNull(TargetStore.targetProtectedApp.value)
        assertNull(store.read(TargetStore.KEY_TARGET_PROTECTED_APP))
    }
}
