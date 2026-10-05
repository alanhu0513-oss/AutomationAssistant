package dev.aegis.shield.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.aegis.shield.R
import dev.aegis.shield.automation.Strictness
import dev.aegis.shield.data.AppEntry
import dev.aegis.shield.data.AppRepository
import dev.aegis.shield.data.AppSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loaded + filtered view of the game picker. Owned by the caller (created
 * with [rememberGamesState]) and rendered via [gamesItems] so the outer
 * LazyColumn keeps virtualizing even very long app lists.
 */
class GamesState internal constructor(
    val apps: List<AppEntry>,
    val loading: Boolean,
    val query: String,
    val onQueryChange: (String) -> Unit,
)

/**
 * Loads the launchable-app list once and applies the live search filter.
 * Load failures are handed to [onError] (a snackbar in the dashboard) —
 * never thrown.
 */
@Composable
fun rememberGamesState(
    repository: AppRepository,
    onError: (String) -> Unit,
): GamesState {
    val errorText = stringResource(R.string.error_apps_load)
    var allApps by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var query by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        loading = true
        val loaded = withContext(Dispatchers.IO) { repository.loadUserApps() }
        loaded.fold(
            onSuccess = {
                allApps = it
                loading = false
            },
            onFailure = {
                loading = false
                // Load failures are already logged inside the repository.
                onError(errorText)
            },
        )
    }

    val filtered = remember(allApps, query) { AppSearch.filter(allApps, query) }
    return GamesState(
        apps = filtered,
        loading = loading,
        query = query,
        onQueryChange = { query = it },
    )
}

/** The "Choose Your Games" block, contributed to a LazyColumn. */
fun LazyListScope.gamesItems(
    state: GamesState,
    protectedApps: Set<String>,
    strictnessLevels: Map<String, Strictness>,
    onToggleProtected: (String, Boolean) -> Unit,
    onCycleStrictness: (String) -> Unit,
) {
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
            query = state.query,
            onQueryChange = state.onQueryChange,
        )
    }

    when {
        state.loading -> item(key = "loading") {
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

        state.apps.isEmpty() -> item(key = "empty") {
            Text(
                text = stringResource(
                    if (state.query.isBlank()) R.string.games_empty
                    else R.string.games_no_results,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        else -> items(state.apps, key = { it.packageName }) { entry ->
            GameRow(
                entry = entry,
                isProtected = entry.packageName in protectedApps,
                strictness = strictnessLevels[entry.packageName] ?: Strictness.NORMAL,
                onClick = {
                    onToggleProtected(entry.packageName, entry.packageName !in protectedApps)
                },
                onCycleStrictness = { onCycleStrictness(entry.packageName) },
            )
        }
    }
}
