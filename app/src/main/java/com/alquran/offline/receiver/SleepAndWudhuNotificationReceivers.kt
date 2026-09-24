package com.alquran.offline.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alquran.offline.notification.MotivationalNotificationScheduler

class SleepRemovalNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            MotivationalNotificationScheduler.sendSleepReminder(context)
            // Reschedule for next day
            MotivationalNotificationScheduler.scheduleSleepReminder(context)
        }
    }
}

class WudhuRemovalNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            // Jam alarm dikirim lewat extra oleh scheduler (21, 22, atau 0).
            // Kalau tidak ada, tebak dari jam sekarang.
            val scheduledHour = intent?.getIntExtra(MotivationalNotificationScheduler.EXTRA_HOUR, -1) ?: -1
            val reminderHour = if (scheduledHour in setOf(21, 22, 0)) {
                scheduledHour
            } else {
                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                when {
                    hour in 21..22 -> hour
                    hour == 0 || hour < 6 -> 0 // midnight reminder
                    else -> 21 // default to 9pm
                }
            }

            MotivationalNotificationScheduler.sendWudhuReminder(context, reminderHour)
            // Reschedule for next day
            MotivationalNotificationScheduler.scheduleWudhuReminders(context)
        }
    }
}

/** Menghapus notifikasi pengingat tidur/wudhu setelah 5 menit. */
class NotificationAutoCancelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val id = intent?.getIntExtra(MotivationalNotificationScheduler.EXTRA_NOTIFICATION_ID, -1) ?: -1
        if (context != null && id != -1) {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.cancel(id)
        }
    }
}
