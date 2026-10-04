package com.example.automationassistant.data

data class UpdateInfo(
    val tagName: String,
    val versionName: String,
    val releaseUrl: String,
)

sealed interface UpdateCheckResult {
    data class Available(val info: UpdateInfo) : UpdateCheckResult
    data object UpToDate : UpdateCheckResult
    data class Failed(val reason: String) : UpdateCheckResult
}
