package shortcuts.kodo.locations

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat

import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

object LocationManager {

    private fun client(context: Context) =
        LocationServices.getGeofencingClient(context)

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, GeofenceReceiver::class.java)

        return PendingIntent.getBroadcast(
            context,
            100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_MUTABLE
        )
    }

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasBackgroundPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT < 29 ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    fun refresh(context: Context) {

        if (!hasLocationPermission(context) ||
            !hasBackgroundPermission(context)
        ) return

        client(context).removeGeofences(
            pendingIntent(context)
        ).addOnCompleteListener {

            val rules = StorageManager.getRules(context)
                .filter { it.enabled }

            if (rules.isEmpty()) return@addOnCompleteListener

            val geofences = rules.map { rule ->

                Geofence.Builder()
                    .setRequestId(rule.id)
                    .setCircularRegion(
                        rule.latitude,
                        rule.longitude,
                        (rule.radius * 0.3048f).coerceAtLeast(1f)
                    )
                    .setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_ENTER
                    )
                    .setExpirationDuration(
                        Geofence.NEVER_EXPIRE
                    )
                    .build()
            }

            val request = GeofencingRequest.Builder()
                .setInitialTrigger(
                    GeofencingRequest.INITIAL_TRIGGER_ENTER
                )
                .addGeofences(geofences)
                .build()

            try {
                client(context).addGeofences(
                    request,
                    pendingIntent(context)
                ).addOnFailureListener {
                    it.printStackTrace()
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    fun remove(context: Context, id: String) {
        client(context).removeGeofences(listOf(id))
    }
}