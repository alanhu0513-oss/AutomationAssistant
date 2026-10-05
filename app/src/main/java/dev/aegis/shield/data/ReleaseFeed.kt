package dev.aegis.shield.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

object ReleaseFeed {

    const val LATEST_RELEASE_URL =
        "https://api.github.com/repos/alanhu0513-oss/aegis/releases/latest"

    fun parseLatest(body: String, currentVersion: String): UpdateCheckResult {
        val root = runCatching { Json.parseToJsonElement(body) }.getOrNull()
        val release = root as? JsonObject
            ?: return UpdateCheckResult.Failed("feed is not a release object")
        val tagName = release.string("tag_name")
            ?: return UpdateCheckResult.Failed("feed has no tag_name")
        val releaseUrl = release.string("html_url")
            ?: return UpdateCheckResult.Failed("feed has no html_url")
        if (!UpdateRules.isNewer(tagName, currentVersion)) return UpdateCheckResult.UpToDate
        return UpdateCheckResult.Available(
            UpdateInfo(
                tagName = tagName,
                versionName = UpdateRules.versionNameOf(tagName),
                releaseUrl = releaseUrl,
            ),
        )
    }

    private fun JsonObject.string(key: String): String? =
        (this[key] as? JsonPrimitive)?.contentOrNull
}
