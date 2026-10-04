package com.example.automationassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.automationassistant.automation.TargetStore
import com.example.automationassistant.data.AppRepository
import com.example.automationassistant.ui.AssistantScreen
import com.example.automationassistant.ui.SettingsActions
import com.example.automationassistant.ui.theme.AutomationAssistantTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        TargetStore.hydrate(this)
        val repository = AppRepository(packageManager = packageManager, selfPackage = packageName)
        setContent {
            AutomationAssistantTheme {
                AssistantScreen(
                    repository = repository,
                    onOpenAccessibilitySettings = {
                        SettingsActions.openAccessibilitySettings(this)
                    },
                    onOpenBatterySettings = {
                        SettingsActions.openBatteryOptimizationSettings(this)
                    },
                    onOpenUrl = { url ->
                        SettingsActions.openUrl(this, url)
                    },
                )
            }
        }
    }
}
