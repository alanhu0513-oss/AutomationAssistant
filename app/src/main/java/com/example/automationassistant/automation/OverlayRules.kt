package com.example.automationassistant.automation

object OverlayRules {

    val targetPackages: Set<String> = emptySet()
    val targetClasses: Set<String> = emptySet()

    val targetCount: Int
        get() = targetPackages.size + targetClasses.size

    fun isTarget(pkg: String, cls: String?, selfPackage: String): Boolean {
        if (pkg == selfPackage) return false
        if (pkg in targetPackages) return true
        return cls != null && cls in targetClasses
    }

    fun shouldDispatch(now: Long, lastDispatchAt: Long, windowMs: Long): Boolean =
        now - lastDispatchAt >= windowMs
}
