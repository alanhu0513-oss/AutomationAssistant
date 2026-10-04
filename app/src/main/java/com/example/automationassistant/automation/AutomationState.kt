package com.example.automationassistant.automation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * What the dashboard needs to know, and nothing more: is the accessibility
 * service running, how many popups it has blocked this session, and the last
 * recoverable error worth surfacing as a snackbar.
 */
object AutomationState {

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _blockedCount = MutableStateFlow(0)
    val blockedCount: StateFlow<Int> = _blockedCount.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    internal fun setRunning(value: Boolean) {
        _isRunning.value = value
    }

    internal fun recordBlocked() {
        _blockedCount.value += 1
    }

    /** Surfaces a friendly message on the dashboard (never crashes the service). */
    internal fun publishError(message: String) {
        _lastError.value = message
    }

    internal fun clearError() {
        _lastError.value = null
    }

    /** Test hook: resets this singleton between test cases. */
    internal fun reset() {
        _isRunning.value = false
        _blockedCount.value = 0
        _lastError.value = null
    }
}
