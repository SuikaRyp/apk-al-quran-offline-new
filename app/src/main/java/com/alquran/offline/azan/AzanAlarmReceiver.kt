package com.alquran.offline.azan

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.alquran.offline.R
import com.alquran.offline.ui.azan.AzanFullScreenActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AzanAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        val prefs = AzanPreferencesRepository(appContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    AzanAlarmScheduler.ACTION_AZAN_TRIGGER -> {
                        val prayerNameStr = intent.getStringExtra(AzanAlarmScheduler.EXTRA_PRAYER_NAME)
                        val triggerMillis = intent.getLongExtra(AzanAlarmScheduler.EXTRA_PRAYER_TIME_MILLIS, System.currentTimeMillis())
                        val prayer = try {
                            prayerNameStr?.let { PrayerName.valueOf(it) }
                        } catch (e: Throwable) {
                            null
                        } ?: PrayerName.DZUHUR

                        if (prefs.isEnabledNow()) {
                            launchFullScreenAlert(appContext, prayer, triggerMillis)
                        }

                        // Alarm ini sudah dipakai; jadwalkan ulang seluruh siklus agar besok tetap jalan.
                        if (prefs.isEnabledNow()) {
                            AzanAlarmScheduler.scheduleAll(appContext, prefs.latitudeNow(), prefs.longitudeNow())
                        }
                    }

                    AzanAlarmScheduler.ACTION_AZAN_RESCHEDULE, Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                        if (prefs.isEnabledNow()) {
                            AzanAlarmScheduler.scheduleAll(appContext, prefs.latitudeNow(), prefs.longitudeNow())
                        }
                    }
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

    private fun launchFullScreenAlert(context: Context, prayer: PrayerName, triggerMillis: Long) {
        AzanAlarmScheduler.createNotificationChannel(context)

        val fullScreenIntent = Intent(context, AzanFullScreenActivity::class.java).apply {
            putExtra(AzanAlarmScheduler.EXTRA_PRAYER_NAME, prayer.name)
            putExtra(AzanAlarmScheduler.EXTRA_PRAYER_TIME_MILLIS, triggerMillis)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }

        // 1) Coba langsung buka activity-nya. Karena alarm ini dijadwalkan lewat
        //    AlarmManager.setAlarmClock(), sistem Android mengizinkan start-activity
        //    dari background untuk kejadian ini — persis seperti aplikasi alarm biasa
        //    yang bisa "membangunkan" & menampilkan layar dering di atas app lain.
        try {
            context.startActivity(fullScreenIntent)
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        // 2) Selalu pasang juga sebagai notifikasi fullScreenIntent, sebagai jaring pengaman
        //    di perangkat/OEM yang membatasi start-activity langsung (mis. HP dengan
        //    battery-saver agresif), dan supaya tetap tampil sebagai heads-up bila
        //    activity gagal tampil di atas app yang sedang aktif.
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, 6000 + prayer.ordinal, fullScreenIntent, flags
        )

        val timeText = android.text.format.DateFormat.format("HH:mm", triggerMillis).toString()

        val notification = NotificationCompat.Builder(context, AzanAlarmScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_quran)
            .setContentTitle("🕌 Waktu Sholat ${prayer.label} — $timeText")
            .setContentText("Yuk, tunaikan sholat ${prayer.label}. Ketuk untuk membuka pengingat.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setAutoCancel(true)
            .setOngoing(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(
                        context, android.Manifest.permission.POST_NOTIFICATIONS
                    ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
            }
            val manager = NotificationManagerCompat.from(context)
            if (manager.areNotificationsEnabled()) {
                manager.notify(6000 + prayer.ordinal, notification)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
