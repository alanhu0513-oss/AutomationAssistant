package dev.aegis.shield.data

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UpdateChecker(private val feedUrl: String = ReleaseFeed.LATEST_RELEASE_URL) {

    suspend fun check(currentVersion: String): UpdateCheckResult =
        withContext(Dispatchers.IO) {
            runCatching { fetchLatestRelease(currentVersion) }
                .getOrElse { UpdateCheckResult.Failed(it.message ?: "network error") }
        }

    private fun fetchLatestRelease(currentVersion: String): UpdateCheckResult {
        val connection = URL(feedUrl).openConnection() as HttpURLConnection
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        connection.setRequestProperty("User-Agent", USER_AGENT)
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return UpdateCheckResult.Failed("HTTP ${connection.responseCode}")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            return ReleaseFeed.parseLatest(body, currentVersion)
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        private const val TIMEOUT_MS = 5_000
        private const val USER_AGENT = "Aegis"
    }
}
