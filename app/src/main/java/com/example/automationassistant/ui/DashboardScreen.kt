package com.example.automationassistant.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.automationassistant.BuildConfig
import com.example.automationassistant.R
import com.example.automationassistant.automation.AutomationState
import com.example.automationassistant.automation.ShieldLog
import com.example.automationassistant.automation.TargetStore
import com.example.automationassistant.data.AppRepository
import com.example.automationassistant.data.UpdateCheckResult
import com.example.automationassistant.data.UpdateChecker
import com.example.automationassistant.data.UpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The premium dashboard: glass status card → sliding shield toggle → device
 * health → preview mode → game picker → transparency → shield log →
 * (optional) update notice. Every failure path lands in a styled snackbar
 * instead of a crash.
 */
@Composable
fun DashboardScreen(
    repository: AppRepository,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onRequestNotificationPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isRunning by AutomationState.isRunning.collectAsState()
    val blockedCount by AutomationState.blockedCount.collectAsState()
    val engineError by AutomationState.lastError.collectAsState()
    val protectedApps by TargetStore.protectedApps.collectAsState()
    val notificationsPrompted by TargetStore.notificationsPrompted.collectAsState()
    val serviceEverEnabled by TargetStore.serviceEverEnabled.collectAsState()
    val shieldLogEntries by ShieldLog.entries.collectAsState()
    val strictnessLevels by TargetStore.strictnessLevels.collectAsState()
    val previewEnabled by TargetStore.previewMode.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var dialogHandled by rememberSaveable { mutableStateOf(false) }

    val gamesState = rememberGamesState(repository) { message ->
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    LaunchedEffect(Unit) {
        val result = withContext(Dispatchers.IO) { UpdateChecker().check(BuildConfig.VERSION_NAME) }
        if (result is UpdateCheckResult.Available) {
            updateInfo = result.info
        }
    }

    // Errors published by the service surface as a friendly snackbar.
    LaunchedEffect(engineError) {
        engineError?.let {
            snackbarHostState.showSnackbar(it)
            AutomationState.clearError()
        }
    }

    val notificationsGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
    val showNotifDialog = !notificationsGranted && !notificationsPrompted && !dialogHandled

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                ) {
                    Text(data.visuals.message, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = 40.dp,
                bottom = 48.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(key = "header") {
                Text(
                    text = stringResource(R.string.dashboard_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            if (serviceEverEnabled && !isRunning) {
                item(key = "offline") {
                    OfflineBanner(onReenable = onOpenAccessibilitySettings)
                }
            }

            item(key = "status") {
                StatusCard(
                    isRunning = isRunning,
                    selectedGameCount = protectedApps.size,
                    blockedCount = blockedCount,
                    onKeepRunning = onOpenBatterySettings,
                )
            }

            item(key = "toggle") {
                ShieldToggle(
                    running = isRunning,
                    onOpenSettings = onOpenAccessibilitySettings,
                )
            }

            item(key = "health") {
                DeviceHealthCard(onOpenBatterySettings = onOpenBatterySettings)
            }

            item(key = "preview") {
                PreviewToggleCard(
                    enabled = previewEnabled,
                    onToggle = { TargetStore.setPreviewMode(it) },
                )
            }

            gamesItems(
                state = gamesState,
                protectedApps = protectedApps,
                strictnessLevels = strictnessLevels,
                onToggleProtected = { packageName, protected ->
                    TargetStore.setAppProtected(packageName, protected)
                },
                onCycleStrictness = { packageName ->
                    val current = TargetStore.strictnessFor(packageName)
                    TargetStore.setStrictness(packageName, current.next())
                },
            )

            item(key = "capabilities") {
                SystemCapabilitiesCard()
            }

            item(key = "log") {
                ShieldLogCard(
                    entries = shieldLogEntries,
                    onClear = { ShieldLog.clear() },
                )
            }

            updateInfo?.let { info ->
                item(key = "update") {
                    UpdateCard(info = info, onOpenRelease = onOpenUrl)
                }
            }
        }
    }

    if (showNotifDialog) {
        NotificationPermissionDialog(
            onAllow = {
                dialogHandled = true
                TargetStore.markNotificationsPrompted()
                onRequestNotificationPermission()
            },
            onDismiss = {
                dialogHandled = true
                TargetStore.markNotificationsPrompted()
            },
        )
    }
}
