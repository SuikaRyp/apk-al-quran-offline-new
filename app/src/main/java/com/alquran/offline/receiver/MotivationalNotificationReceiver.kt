package com.alquran.offline.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alquran.offline.model.DailyMotivationalMessages
import com.alquran.offline.notification.MotivationalNotificationScheduler

class MotivationalNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            val dailyMessage = DailyMotivationalMessages.getDailyMotivation()
            MotivationalNotificationScheduler.sendMotivationalNotification(
                context,
                "💡 Motivasi Harian",
                dailyMessage.text
            )

            // Reschedule for next day
            MotivationalNotificationScheduler.scheduleMotivationalNotifications(context)
        }
    }
}
