package com.example.automationassistant.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.automationassistant.BuildConfig
import com.example.automationassistant.R
import com.example.automationassistant.automation.AutomationState
import com.example.automationassistant.automation.TargetStore
import com.example.automationassistant.data.AppEntry
import com.example.automationassistant.data.AppRepository
import com.example.automationassistant.data.AppSearch
import com.example.automationassistant.data.UpdateCheckResult
import com.example.automationassistant.data.UpdateChecker
import com.example.automationassistant.data.UpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The premium dashboard: glass status card → sliding shield toggle → the
 * visual game picker → (optional) update notice. Every failure path lands in
 * a styled snackbar instead of a crash.
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

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val appsErrorText = stringResource(R.string.error_apps_load)

    var apps by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    var appsLoading by remember { mutableStateOf(true) }
    var query by rememberSaveable { mutableStateOf("") }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var dialogHandled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        appsLoading = true
        val loaded = withContext(Dispatchers.IO) { repository.loadUserApps() }
        loaded.fold(
            onSuccess = {
                apps = it
                appsLoading = false
            },
            onFailure = {
                appsLoading = false
                // Load failures are already logged inside the repository.
                snackbarHostState.showSnackbar(appsErrorText)
            },
        )
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

    val filteredApps = remember(apps, query) { AppSearch.filter(apps, query) }

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

            item(key = "games_header") {
                Text(
                    text = stringResource(R.string.games_section_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            item(key = "search") {
                SearchField(
                    query = query,
                    onQueryChange = { query = it },
                )
            }

            when {
                appsLoading -> item(key = "loading") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Neon.Green,
                        )
                        Text(
                            text = stringResource(R.string.games_loading),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                filteredApps.isEmpty() -> item(key = "empty") {
                    Text(
                        text = stringResource(
                            if (query.isBlank()) R.string.games_empty
                            else R.string.games_no_results,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                else -> items(filteredApps, key = { it.packageName }) { entry ->
                    GameRow(
                        entry = entry,
                        isProtected = entry.packageName in protectedApps,
                        onClick = {
                            TargetStore.setAppProtected(
                                entry.packageName,
                                entry.packageName !in protectedApps,
                            )
                        },
                    )
                }
            }

            item(key = "capabilities") {
                SystemCapabilitiesCard()
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
