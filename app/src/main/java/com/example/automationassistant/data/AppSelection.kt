package com.example.automationassistant.data

/**
 * Raw launcher-app row before selection: whatever `PackageManager` reported,
 * with system status resolved. Kept as a plain data holder so every rule in
 * [AppSelection] is pure and unit-testable on the JVM.
 */
data class CandidateApp(
    val packageName: String,
    val label: String,
    val isSystem: Boolean,
)

/**
 * The selection rules behind the visual game picker:
 *
 * 1. never show this app itself,
 * 2. never show preinstalled system apps (bloatware, settings, dials),
 * 3. blank labels fall back to the package name,
 * 4. one row per package, alphabetized case-insensitively.
 */
object AppSelection {

    fun select(candidates: List<CandidateApp>, selfPackage: String): List<CandidateApp> =
        candidates
            .filterNot { it.packageName == selfPackage || it.isSystem }
            .map { candidate ->
                if (candidate.label.isBlank()) candidate.copy(label = candidate.packageName)
                else candidate
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
}
