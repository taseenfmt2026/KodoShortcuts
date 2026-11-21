package shortcuts.kodo.locations

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager

object AutomationManager {

    const val SILENT = "SILENT"
    const val DND = "DND"

    fun execute(context: Context, actions: List<String>) {

        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val audioManager =
            context.getSystemService(
                Context.AUDIO_SERVICE
            ) as AudioManager

        actions.forEach { action ->

            try {
                when (action) {

                    SILENT -> {
                        audioManager.ringerMode =
                            AudioManager.RINGER_MODE_SILENT
                    }

                    DND -> {
                        if (notificationManager
                                .isNotificationPolicyAccessGranted
                        ) {
                            notificationManager
                                .setInterruptionFilter(
                                    NotificationManager
                                        .INTERRUPTION_FILTER_NONE
                                )
                        }
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }
}