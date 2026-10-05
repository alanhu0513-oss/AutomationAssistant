package com.example.automationassistant.data

/**
 * The battery-management family a device belongs to. Menu paths for keeping
 * a background service alive differ per family — not per app — so this is
 * the only branching the survival guide needs.
 */
enum class OemBrand {
    MIUI,
    COLOROS,
    ONEUI,
    FUNTOUCH,
    STOCK,
}

/**
 * Maps `Build.MANUFACTURER` to the battery-menu family the player should
 * follow. Pure and brand-agnostic: manufacturer strings are lowercased,
 * trimmed and stripped of spaces/dashes before matching, unknown values
 * (including blank) fall back to the stock Android path.
 */
object OemGuide {

    fun brandFor(manufacturer: String): OemBrand {
        val key = manufacturer.trim().lowercase().replace(" ", "").replace("-", "")
        return when (key) {
            "xiaomi", "redmi", "poco", "blackshark" -> OemBrand.MIUI
            "oppo", "realme", "oneplus" -> OemBrand.COLOROS
            "samsung" -> OemBrand.ONEUI
            "vivo", "iqoo" -> OemBrand.FUNTOUCH
            else -> OemBrand.STOCK
        }
    }
}
