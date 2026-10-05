package dev.aegis.shield.automation

import android.app.PendingIntent
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import dev.aegis.shield.MainActivity
import dev.aegis.shield.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Quick Settings tile: mirrors whether the shield engine is actually running
 * and jumps to the place that fixes it. The platform does not let a tile
 * toggle Accessibility itself, so the semantics are honest:
 *
 * - **Inactive** (grey) → the service is off; tapping opens Accessibility
 *   settings so the player can switch it on.
 * - **Active** (green) → the service is connected; tapping opens the app.
 */
class ShieldTileService : TileService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var stateJob: Job? = null

    override fun onStartListening() {
        super.onStartListening()
        render(AutomationState.isRunning.value)
        stateJob = scope.launch {
            AutomationState.isRunning.collect { running -> render(running) }
        }
    }

    override fun onStopListening() {
        stateJob?.cancel()
        stateJob = null
        super.onStopListening()
    }

    override fun onClick() {
        val intent = if (AutomationState.isRunning.value) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        collapseTo(intent)
    }

    private fun render(running: Boolean) {
        val tile = qsTile ?: return
        tile.label = getString(R.string.app_name)
        tile.icon = Icon.createWithResource(this, R.mipmap.ic_launcher)
        tile.state = if (running) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }

    // The PendingIntent overload only exists on API 34+; the Intent overload is
    // the correct call below that. Lint cannot see the runtime guard, hence the
    // suppression — this is the pattern Android's own docs recommend.
    @SuppressLint("StartActivityAndCollapseDeprecated")
    private fun collapseTo(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE),
            )
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
