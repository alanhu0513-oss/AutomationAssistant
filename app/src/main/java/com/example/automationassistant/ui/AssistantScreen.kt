package com.example.automationassistant.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.automationassistant.BuildConfig
import com.example.automationassistant.R
import com.example.automationassistant.automation.AutomationState
import com.example.automationassistant.automation.TargetStore
import com.example.automationassistant.data.AppEntry
import com.example.automationassistant.data.AppRepository
import com.example.automationassistant.data.AppSearch
import com.example.automationassistant.data.UpdateCheckResult
import com.example.automationassistant.data.UpdateChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AssistantScreen(
    repository: AppRepository,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    val isRunning by AutomationState.isRunning.collectAsState()
    val enabled by AutomationState.enabled.collectAsState()
    val lastDismissed by AutomationState.lastDismissed.collectAsState()
    val lastWindow by AutomationState.lastWindow.collectAsState()
    val foregroundPackage by AutomationState.foregroundPackage.collectAsState()
    val targetProtectedApp by TargetStore.targetProtectedApp.collectAsState()
    val interrupterPackage by TargetStore.interrupterPackageName.collectAsState()

    var apps by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    var appsLoading by remember { mutableStateOf(true) }
    var query by rememberSaveable { mutableStateOf("") }
    var selectorOpen by rememberSaveable { mutableStateOf(true) }
    var activePickerName by rememberSaveable { mutableStateOf(PickerTarget.PROTECTED.name) }
    val activePicker = PickerTarget.valueOf(activePickerName)

    var updateState by remember { mutableStateOf<UpdateState>(UpdateState.Checking) }
    val scope = rememberCoroutineScope()

    fun checkForUpdates() {
        updateState = UpdateState.Checking
        scope.launch {
            updateState = when (val result = UpdateChecker().check(BuildConfig.VERSION_NAME)) {
                is UpdateCheckResult.Available -> UpdateState.Available(result.info)
                UpdateCheckResult.UpToDate -> UpdateState.UpToDate
                is UpdateCheckResult.Failed -> UpdateState.Failed
            }
        }
    }

    LaunchedEffect(Unit) { checkForUpdates() }

    LaunchedEffect(repository) {
        apps = withContext(Dispatchers.IO) { repository.loadLauncherApps() }
        appsLoading = false
    }

    fun labelOf(packageName: String?): String? {
        if (packageName == null) return null
        return apps.firstOrNull { it.packageName == packageName }?.label ?: packageName
    }

    fun selectApp(entry: AppEntry) {
        if (entry.isSelf) return
        when (activePicker) {
            PickerTarget.PROTECTED -> {
                if (TargetStore.interrupterPackageName.value == entry.packageName) {
                    TargetStore.setInterrupterPackageName(null)
                }
                TargetStore.setTargetProtectedApp(entry.packageName)
            }

            PickerTarget.INTERRUPTER -> {
                if (TargetStore.targetProtectedApp.value == entry.packageName) {
                    TargetStore.setTargetProtectedApp(null)
                }
                TargetStore.setInterrupterPackageName(entry.packageName)
            }
        }
    }

    val filteredApps = remember(apps, query) { AppSearch.filter(apps, query) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = 40.dp,
                bottom = 40.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item(key = "header") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.screen_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = stringResource(R.string.screen_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item(key = "status") {
                StatusCard(
                    isRunning = isRunning,
                    enabled = enabled,
                    watchingLabel = labelOf(interrupterPackage),
                    foregroundPackage = foregroundPackage,
                    lastWindow = lastWindow,
                    lastDismissed = lastDismissed?.let { labelOf(it) },
                    onToggle = AutomationState::setEnabled,
                )
            }

            item(key = "config") {
                ConfigSection(
                    targetProtectedApp = targetProtectedApp,
                    protectedLabel = labelOf(targetProtectedApp),
                    interrupterPackage = interrupterPackage,
                    interrupterLabel = labelOf(interrupterPackage),
                    activePicker = activePicker,
                    onChooseProtected = {
                        activePickerName = PickerTarget.PROTECTED.name
                        selectorOpen = true
                    },
                    onChooseInterrupter = {
                        activePickerName = PickerTarget.INTERRUPTER.name
                        selectorOpen = true
                    },
                    onClearProtected = { TargetStore.setTargetProtectedApp(null) },
                    onClearInterrupter = { TargetStore.setInterrupterPackageName(null) },
                )
            }

            if (selectorOpen) {
                stickyHeader(key = "search") {
                    SearchField(
                        query = query,
                        onQueryChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(bottom = 12.dp),
                    )
                }

                if (appsLoading) {
                    item(key = "loading") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Text(
                                text = stringResource(R.string.selector_loading),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else if (filteredApps.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            text = stringResource(R.string.selector_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(filteredApps, key = { it.packageName }) { entry ->
                        AppRow(
                            entry = entry,
                            isProtected = entry.packageName == targetProtectedApp,
                            isInterrupter = entry.packageName == interrupterPackage,
                            onClick = { selectApp(entry) },
                        )
                    }
                }

                item(key = "selector_done") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { selectorOpen = false }) {
                            Text(stringResource(R.string.selector_done))
                        }
                    }
                }
            }

            item(key = "battery") {
                FilledTonalButton(
                    onClick = onOpenBatterySettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.button_battery_settings),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            item(key = "update") {
                UpdateCard(
                    state = updateState,
                    versionName = BuildConfig.VERSION_NAME,
                    onOpenRelease = onOpenUrl,
                    onRetry = { checkForUpdates() },
                )
            }

            item(key = "accessibility") {
                FilledTonalButton(
                    onClick = onOpenAccessibilitySettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.button_open_accessibility),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}
