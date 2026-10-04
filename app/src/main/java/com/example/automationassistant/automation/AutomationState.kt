package com.example.automationassistant.automation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AutomationState {

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _enabled = MutableStateFlow(true)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _lastDismissed = MutableStateFlow<String?>(null)
    val lastDismissed: StateFlow<String?> = _lastDismissed.asStateFlow()

    fun setEnabled(value: Boolean) {
        _enabled.value = value
    }

    internal fun setRunning(value: Boolean) {
        _isRunning.value = value
    }

    internal fun setLastDismissed(value: String) {
        _lastDismissed.value = value
    }

    internal fun clearLastDismissed() {
        _lastDismissed.value = null
    }
}
