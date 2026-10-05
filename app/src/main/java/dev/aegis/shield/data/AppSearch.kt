package dev.aegis.shield.data

object AppSearch {

    fun normalize(query: String): String = query.trim().lowercase()

    fun matches(entry: AppEntry, normalizedQuery: String): Boolean {
        if (normalizedQuery.isEmpty()) return true
        if (entry.label.lowercase().startsWith(normalizedQuery)) return true
        if (entry.packageName.lowercase().contains(normalizedQuery)) return true
        return entry.label.lowercase().contains(normalizedQuery)
    }

    fun filter(apps: List<AppEntry>, query: String): List<AppEntry> {
        val normalized = normalize(query)
        if (normalized.isEmpty()) return apps
        val labelPrefix = ArrayList<AppEntry>()
        val remainder = ArrayList<AppEntry>()
        for (app in apps) {
            when {
                app.label.lowercase().startsWith(normalized) -> labelPrefix.add(app)
                matches(app, normalized) -> remainder.add(app)
            }
        }
        return labelPrefix + remainder
    }
}
