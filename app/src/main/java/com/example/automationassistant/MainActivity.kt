package com.example.automationassistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.automationassistant.automation.TargetStore
import com.example.automationassistant.data.AppRepository
import com.example.automationassistant.ui.DashboardScreen
import com.example.automationassistant.ui.OnboardingScreen
import com.example.automationassistant.ui.SettingsActions
import com.example.automationassistant.ui.theme.AutomationAssistantTheme

class MainActivity : ComponentActivity() {

    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Android 13+ runtime notification permission for the persistent
        // shield notification. Registered before the first frame so the
        // onboarding/dashboard flow can trigger it at the right moment.
        notificationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (!granted) {
                    Log.i(TAG, "Notification permission declined — shield relies on the engine anyway")
                }
            }

        val repository = AppRepository(packageManager = packageManager, selfPackage = packageName)
        val activity = this
        setContent {
            AutomationAssistantTheme {
                val isFirstLaunch by TargetStore.isFirstLaunch.collectAsState()
                if (isFirstLaunch) {
                    OnboardingScreen(
                        onOpenAccessibilitySettings = {
                            SettingsActions.openAccessibilitySettings(activity)
                        },
                        onFinished = { TargetStore.completeFirstLaunch() },
                    )
                } else {
                    DashboardScreen(
                        repository = repository,
                        onOpenAccessibilitySettings = {
                            SettingsActions.openAccessibilitySettings(activity)
                        },
                        onOpenBatterySettings = {
                            SettingsActions.openBatteryOptimizationSettings(activity)
                        },
                        onOpenUrl = { url -> SettingsActions.openUrl(activity, url) },
                        onRequestNotificationPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                                PackageManager.PERMISSION_GRANTED
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                    )
                }
            }
        }
    }

    private companion object {
        private const val TAG = "GamingShield"
    }
}
