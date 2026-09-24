package com.alquran.offline.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alquran.offline.QuranApplication
import com.alquran.offline.azan.AzanAlarmScheduler
import com.alquran.offline.azan.AzanPreferencesRepository
import com.alquran.offline.notification.MotivationalNotificationScheduler
import com.alquran.offline.notification.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val app = context.applicationContext as? QuranApplication ?: return
            val repository = app.repository
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    MotivationalNotificationScheduler.scheduleAll(context)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }

                try {
                    val enabled = repository.notificationEnabled.first()
                    val hour = repository.notificationHour.first()
                    val minute = repository.notificationMinute.first()

                    if (enabled) {
                        NotificationScheduler.scheduleDailyNotification(context, hour, minute)
                    }

                    val azanPrefs = AzanPreferencesRepository(context)
                    if (azanPrefs.isEnabledNow()) {
                        AzanAlarmScheduler.scheduleAll(
                            context,
                            azanPrefs.latitudeNow(),
                            azanPrefs.longitudeNow()
                        )
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                } finally {
                    try {
                        pendingResult.finish()
                    } catch (ignored: Throwable) {
                    }
                }
            }
        }
    }
}
