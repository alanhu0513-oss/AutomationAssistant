package com.example.automationassistant.data

data class AppEntry(
    val packageName: String,
    val label: String,
    val isSelf: Boolean = false,
)
