package shortcuts.kodo.locations

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val event = GeofencingEvent.fromIntent(intent) ?: return

        if (event.hasError()) return

        if (event.geofenceTransition ==
            Geofence.GEOFENCE_TRANSITION_ENTER
        ) {

            val ids = event.triggeringGeofences
                ?.map { it.requestId }
                ?: return

            val rules = StorageManager.getRules(context)

            ids.forEach { id ->

                val rule = rules.firstOrNull {
                    it.id == id && it.enabled
                } ?: return@forEach

                AutomationManager.execute(
                    context,
                    rule.actions
                )
            }
        }
    }

    class BootReceiver : BroadcastReceiver() {

        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                LocationManager.refresh(context)
            }
        }
    }
}