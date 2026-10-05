package dev.aegis.shield.data

object UpdateRules {

    fun versionNameOf(tagName: String): String =
        tagName.trim().removePrefix("v").removePrefix("V")

    fun parseVersion(raw: String): List<Int> =
        versionNameOf(raw)
            .split('.')
            .map { part -> part.takeWhile { it.isDigit() }.toIntOrNull() ?: 0 }

    fun isNewer(candidate: String, current: String): Boolean {
        val left = parseVersion(candidate)
        val right = parseVersion(current)
        val size = maxOf(left.size, right.size)
        for (index in 0 until size) {
            val candidatePart = left.getOrElse(index) { 0 }
            val currentPart = right.getOrElse(index) { 0 }
            if (candidatePart != currentPart) return candidatePart > currentPart
        }
        return false
    }
}
