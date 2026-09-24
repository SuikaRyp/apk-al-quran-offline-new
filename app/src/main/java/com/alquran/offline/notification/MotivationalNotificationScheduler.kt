package com.alquran.offline.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.alquran.offline.R
import com.alquran.offline.receiver.MotivationalNotificationReceiver
import com.alquran.offline.receiver.NotificationAutoCancelReceiver
import com.alquran.offline.receiver.SleepRemovalNotificationReceiver
import com.alquran.offline.receiver.WudhuRemovalNotificationReceiver
import com.alquran.offline.ui.MainActivity
import java.util.Calendar

object MotivationalNotificationScheduler {

    // Channel IDs
    const val MOTIVATIONAL_CHANNEL_ID = "motivational_daily_channel_v2"
    const val SLEEP_REMINDER_CHANNEL_ID = "sleep_reminder_channel_v2"
    const val WUDHU_REMINDER_CHANNEL_ID = "wudhu_reminder_channel_v2"

    // Channel lama (importance lebih rendah) dihapus supaya tidak menumpuk di pengaturan HP.
    private val LEGACY_CHANNEL_IDS = listOf(
        "motivational_daily_channel",
        "sleep_reminder_channel",
        "wudhu_reminder_channel"
    )

    // Notification IDs (1001 = hadits harian, 6000+ = azan)
    const val MOTIVATIONAL_NOTIFICATION_ID = 1002
    const val SLEEP_NOTIFICATION_ID = 1003
    const val WUDHU_9_NOTIFICATION_ID = 1004
    const val WUDHU_10_NOTIFICATION_ID = 1005
    const val WUDHU_12_NOTIFICATION_ID = 1006

    // Request Codes (2001 = hadits harian, 5100+ = azan).
    // Tiap alarm HARUS punya request code sendiri, kalau sama alarm yang satu akan menimpa yang lain.
    const val MOTIVATIONAL_REQUEST_CODE = 2002          // motivasi pagi 07:00
    const val SLEEP_REQUEST_CODE = 2003
    const val WUDHU_9_REQUEST_CODE = 2004
    const val WUDHU_10_REQUEST_CODE = 2005
    const val WUDHU_12_REQUEST_CODE = 2006
    const val MOTIVATIONAL_NOON_REQUEST_CODE = 2007     // motivasi siang 12:00
    const val MOTIVATIONAL_EVENING_REQUEST_CODE = 2008  // motivasi sore 17:00

    private const val AUTO_CANCEL_REQUEST_BASE = 3000   // + notification id
    private const val CONTENT_REQUEST_BASE = 4000       // + notification id

    const val EXTRA_HOUR = "extra_hour"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

    /** Notifikasi tidur/wudhu tidak bisa dihapus selama 5 menit, lalu hilang sendiri. */
    private const val REMINDER_VISIBLE_MS = 5 * 60 * 1000L

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val motivationalChannel = NotificationChannel(
                MOTIVATIONAL_CHANNEL_ID,
                "Motivasi Harian",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi motivasi inspiratif setiap pagi, siang, dan sore"
                enableVibration(true)
            }

            val sleepChannel = NotificationChannel(
                SLEEP_REMINDER_CHANNEL_ID,
                "Pengingat Tidur",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pengingat untuk istirahat malam"
                enableVibration(true)
            }

            val wudhuChannel = NotificationChannel(
                WUDHU_REMINDER_CHANNEL_ID,
                "Pengingat Wudhu",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pengingat untuk berwudhu sebelum tidur"
                enableVibration(true)
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.apply {
                LEGACY_CHANNEL_IDS.forEach { legacyId ->
                    try {
                        deleteNotificationChannel(legacyId)
                    } catch (ignored: Throwable) {
                    }
                }
                createNotificationChannel(motivationalChannel)
                createNotificationChannel(sleepChannel)
                createNotificationChannel(wudhuChannel)
            }
        }
    }

    // Motivasi 3x sehari: 07:00, 12:00, 17:00
    fun scheduleMotivationalNotifications(context: Context) {
        createNotificationChannels(context)
        val slots = listOf(
            7 to MOTIVATIONAL_REQUEST_CODE,
            12 to MOTIVATIONAL_NOON_REQUEST_CODE,
            17 to MOTIVATIONAL_EVENING_REQUEST_CODE
        )
        slots.forEach { (hour, requestCode) ->
            scheduleNotificationAt(
                context, hour, 0, requestCode, MotivationalNotificationReceiver::class.java
            )
        }
    }

    // Pengingat tidur 21:00
    fun scheduleSleepReminder(context: Context) {
        createNotificationChannels(context)
        scheduleNotificationAt(
            context, 21, 0, SLEEP_REQUEST_CODE, SleepRemovalNotificationReceiver::class.java
        )
    }

    // Pengingat wudhu 21:00, 22:00, 00:00
    fun scheduleWudhuReminders(context: Context) {
        createNotificationChannels(context)
        scheduleNotificationAt(
            context, 21, 0, WUDHU_9_REQUEST_CODE, WudhuRemovalNotificationReceiver::class.java
        )
        scheduleNotificationAt(
            context, 22, 0, WUDHU_10_REQUEST_CODE, WudhuRemovalNotificationReceiver::class.java
        )
        scheduleNotificationAt(
            context, 0, 0, WUDHU_12_REQUEST_CODE, WudhuRemovalNotificationReceiver::class.java
        )
    }

    /** Jadwalkan ulang semuanya (dipakai saat app start dan setelah reboot / update). */
    fun scheduleAll(context: Context) {
        scheduleMotivationalNotifications(context)
        scheduleSleepReminder(context)
        scheduleWudhuReminders(context)
    }

    private fun pendingFlags(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

    private fun canScheduleExact(alarmManager: AlarmManager): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                alarmManager.canScheduleExactAlarms()
            } catch (e: Throwable) {
                false
            }
        } else {
            true
        }

    private fun setAlarm(alarmManager: AlarmManager, triggerAtMillis: Long, pendingIntent: PendingIntent) {
        try {
            if (canScheduleExact(alarmManager)) {
                AlarmManagerCompat.setExactAndAllowWhileIdle(
                    alarmManager, AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
                )
            } else {
                AlarmManagerCompat.setAndAllowWhileIdle(
                    alarmManager, AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
                )
            }
        } catch (e: SecurityException) {
            AlarmManagerCompat.setAndAllowWhileIdle(
                alarmManager, AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
            )
        }
    }

    private fun scheduleNotificationAt(
        context: Context,
        hour: Int,
        minute: Int,
        requestCode: Int,
        receiverClass: Class<*>
    ) {
        try {
            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, receiverClass).apply {
                putExtra(EXTRA_HOUR, hour)
            }
            val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, pendingFlags())

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            setAlarm(alarmManager, calendar.timeInMillis, pendingIntent)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun cancelAllMotivationalNotifications(context: Context) {
        try {
            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            cancelAlarm(context, alarmManager, MOTIVATIONAL_REQUEST_CODE, MotivationalNotificationReceiver::class.java)
            cancelAlarm(context, alarmManager, MOTIVATIONAL_NOON_REQUEST_CODE, MotivationalNotificationReceiver::class.java)
            cancelAlarm(context, alarmManager, MOTIVATIONAL_EVENING_REQUEST_CODE, MotivationalNotificationReceiver::class.java)
            cancelAlarm(context, alarmManager, SLEEP_REQUEST_CODE, SleepRemovalNotificationReceiver::class.java)
            cancelAlarm(context, alarmManager, WUDHU_9_REQUEST_CODE, WudhuRemovalNotificationReceiver::class.java)
            cancelAlarm(context, alarmManager, WUDHU_10_REQUEST_CODE, WudhuRemovalNotificationReceiver::class.java)
            cancelAlarm(context, alarmManager, WUDHU_12_REQUEST_CODE, WudhuRemovalNotificationReceiver::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelAlarm(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        receiverClass: Class<*>
    ) {
        val intent = Intent(context, receiverClass)
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, pendingFlags())
        alarmManager.cancel(pendingIntent)
    }

    // ---------------------------------------------------------------------------------------------
    // Menampilkan notifikasi
    // ---------------------------------------------------------------------------------------------

    private fun canPostNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun openAppPendingIntent(context: Context, notificationId: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context, CONTENT_REQUEST_BASE + notificationId, intent, pendingFlags()
        )
    }

    private fun showNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        text: String,
        bigText: String,
        priority: Int,
        lockedForFiveMinutes: Boolean
    ) {
        try {
            if (!canPostNotifications(context)) return
            createNotificationChannels(context)

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_quran)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(openAppPendingIntent(context, notificationId))

            if (lockedForFiveMinutes) {
                builder.setAutoCancel(false)
                    .setOngoing(true)
                    .setTimeoutAfter(REMINDER_VISIBLE_MS) // Android 8+: sistem menghapus otomatis
            } else {
                builder.setAutoCancel(true)
            }

            NotificationManagerCompat.from(context).notify(notificationId, builder.build())

            if (lockedForFiveMinutes) {
                scheduleAutoCancel(context, notificationId)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * Cadangan untuk Android 5-7 (setTimeoutAfter baru jalan di Android 8+), sekaligus
     * memastikan notifikasi "ongoing" tidak nyangkut kalau proses app dimatikan sistem.
     * Sengaja tidak pakai Thread.sleep di dalam receiver.
     */
    private fun scheduleAutoCancel(context: Context, notificationId: Int) {
        try {
            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, NotificationAutoCancelReceiver::class.java).apply {
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, AUTO_CANCEL_REQUEST_BASE + notificationId, intent, pendingFlags()
            )
            setAlarm(alarmManager, System.currentTimeMillis() + REMINDER_VISIBLE_MS, pendingIntent)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun sendMotivationalNotification(context: Context, title: String, message: String) {
        showNotification(
            context = context,
            channelId = MOTIVATIONAL_CHANNEL_ID,
            notificationId = MOTIVATIONAL_NOTIFICATION_ID,
            title = title,
            text = message,
            bigText = message,
            priority = NotificationCompat.PRIORITY_MAX,
            lockedForFiveMinutes = false
        )
    }

    fun sendSleepReminder(context: Context) {
        showNotification(
            context = context,
            channelId = SLEEP_REMINDER_CHANNEL_ID,
            notificationId = SLEEP_NOTIFICATION_ID,
            title = "Waktu Istirahat",
            text = "Sudah jam 9 malam, persiapkan diri untuk tidur. Jangan lupa baca doa tidur!",
            bigText = "Istirahat yang cukup adalah bagian dari menjaga kesehatan. " +
                "Persiapkan diri untuk tidur dengan membaca doa.",
            priority = NotificationCompat.PRIORITY_MAX,
            lockedForFiveMinutes = true
        )
    }

    fun sendWudhuReminder(context: Context, hour: Int) {
        val timeText = when (hour) {
            21 -> "9 malam"
            22 -> "10 malam"
            0 -> "tengah malam"
            else -> "$hour malam"
        }

        val notificationId = when (hour) {
            21 -> WUDHU_9_NOTIFICATION_ID
            22 -> WUDHU_10_NOTIFICATION_ID
            else -> WUDHU_12_NOTIFICATION_ID
        }

        showNotification(
            context = context,
            channelId = WUDHU_REMINDER_CHANNEL_ID,
            notificationId = notificationId,
            title = "Pengingat Wudhu",
            text = "Sudah jam $timeText. Berwudhu terlebih dahulu sebelum tidur.",
            bigText = "Berwudhu sebelum tidur adalah Sunnah Rasulullah. " +
                "Lalu baca doa tidur dan istirahat dengan nyenyak.",
            priority = NotificationCompat.PRIORITY_MAX,
            lockedForFiveMinutes = true
        )
    }
}
