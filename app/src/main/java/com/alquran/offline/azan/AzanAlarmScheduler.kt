package com.alquran.offline.azan

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.alquran.offline.ui.MainActivity
import java.util.Calendar

object AzanAlarmScheduler {

    const val CHANNEL_ID = "azan_prayer_channel_v2"
    private const val LEGACY_CHANNEL_ID = "azan_prayer_channel"
    const val ACTION_AZAN_TRIGGER = "com.alquran.offline.ACTION_AZAN_TRIGGER"
    const val ACTION_AZAN_RESCHEDULE = "com.alquran.offline.ACTION_AZAN_RESCHEDULE"
    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_PRAYER_TIME_MILLIS = "extra_prayer_time_millis"

    private const val REQUEST_CODE_BASE = 5100
    private const val RESCHEDULE_REQUEST_CODE = 5199

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return
            try {
                manager.deleteNotificationChannel(LEGACY_CHANNEL_ID)
            } catch (ignored: Throwable) {
            }
            val existing = manager.getNotificationChannel(CHANNEL_ID)
            if (existing != null) return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Waktu Sholat (Azan)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pengingat layar penuh saat masuk waktu sholat"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 800, 400, 800, 400, 800)
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                // Channel sengaja senyap: suara azan (nada pilihan pengguna, looping sampai
                // dimatikan) diputar oleh AzanFullScreenActivity. Kalau channel ikut bunyi,
                // nada alarm bawaan HP akan bertumpuk dengan mp3 azan.
                setSound(null, null)
            }
            manager.createNotificationChannel(channel)
        }
    }

    /** Menghitung ulang & menjadwalkan 5 alarm sholat untuk hari ini (sisa) + besok. */
    fun scheduleAll(context: Context, latitude: Double, longitude: Double) {
        try {
            createNotificationChannel(context)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val tz = PrayerTimesCalculator.currentTimeZoneOffsetHours()
            val now = System.currentTimeMillis()

            val today = Calendar.getInstance()
            val tomorrow = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }

            val todayTimes = PrayerTimesCalculator.calculate(today, latitude, longitude, tz)
            val tomorrowTimes = PrayerTimesCalculator.calculate(tomorrow, latitude, longitude, tz)

            // Untuk tiap sholat, pakai jadwal hari ini jika belum lewat, kalau sudah lewat pakai besok.
            todayTimes.asOrderedList().forEachIndexed { index, (prayer, cal) ->
                val triggerCal = if (cal.timeInMillis > now) cal else
                    tomorrowTimes.asOrderedList()[index].second
                scheduleOne(context, alarmManager, prayer, triggerCal.timeInMillis, index)
            }

            scheduleReschedule(context, alarmManager)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun scheduleOne(
        context: Context,
        alarmManager: AlarmManager,
        prayer: PrayerName,
        triggerAtMillis: Long,
        index: Int
    ) {
        val intent = Intent(context, AzanAlarmReceiver::class.java).apply {
            action = ACTION_AZAN_TRIGGER
            putExtra(EXTRA_PRAYER_NAME, prayer.name)
            putExtra(EXTRA_PRAYER_TIME_MILLIS, triggerAtMillis)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_CODE_BASE + index, intent, flags
        )

        // Intent "buka aplikasi" yang ditampilkan sistem di status bar saat alarm-clock terjadwal.
        val showIntent = Intent(context, MainActivity::class.java)
        val showFlags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        val showPendingIntent = PendingIntent.getActivity(
            context, REQUEST_CODE_BASE + index + 500, showIntent, showFlags
        )

        try {
            // setAlarmClock() memberi prioritas tertinggi ala alarm bawaan HP: alarm ini boleh
            // menyalakan layar & (lewat notifikasi fullScreenIntent) tampil di atas aplikasi lain,
            // termasuk saat sedang bermain game — persis seperti alarm biasa berbunyi.
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent),
                pendingIntent
            )
        } catch (security: SecurityException) {
            fallbackSchedule(alarmManager, triggerAtMillis, pendingIntent)
        } catch (e: Throwable) {
            fallbackSchedule(alarmManager, triggerAtMillis, pendingIntent)
        }
    }

    private fun fallbackSchedule(alarmManager: AlarmManager, triggerAtMillis: Long, pendingIntent: PendingIntent) {
        try {
            androidx.core.app.AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager, AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
            )
        } catch (e: Throwable) {
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } catch (ignored: Throwable) {
            }
        }
    }

    /** Alarm harian jam 00:05 untuk menghitung ulang jadwal (waktu sholat bergeser tiap hari). */
    private fun scheduleReschedule(context: Context, alarmManager: AlarmManager) {
        val intent = Intent(context, AzanAlarmReceiver::class.java).apply {
            action = ACTION_AZAN_RESCHEDULE
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        val pendingIntent = PendingIntent.getBroadcast(context, RESCHEDULE_REQUEST_CODE, intent, flags)

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 5)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        fallbackSchedule(alarmManager, cal.timeInMillis, pendingIntent)
    }

    fun cancelAll(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            PrayerName.values().forEachIndexed { index, _ ->
                val intent = Intent(context, AzanAlarmReceiver::class.java).apply { action = ACTION_AZAN_TRIGGER }
                val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
                val pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_BASE + index, intent, flags)
                alarmManager.cancel(pendingIntent)
            }
            val rescheduleIntent = Intent(context, AzanAlarmReceiver::class.java).apply {
                action = ACTION_AZAN_RESCHEDULE
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            alarmManager.cancel(
                PendingIntent.getBroadcast(context, RESCHEDULE_REQUEST_CODE, rescheduleIntent, flags)
            )
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
