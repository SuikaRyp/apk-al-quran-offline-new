package com.alquran.offline

import android.app.Application
import com.alquran.offline.data.local.AppDatabase
import com.alquran.offline.data.preferences.UserPreferencesRepository
import com.alquran.offline.data.repository.AsmaulHusnaRepository
import com.alquran.offline.data.repository.AsmaulHusnaRepositoryImpl
import com.alquran.offline.data.repository.DailyPrayerRepository
import com.alquran.offline.data.repository.DailyPrayerRepositoryImpl
import com.alquran.offline.data.repository.PrayerRepository
import com.alquran.offline.data.repository.PrayerRepositoryImpl
import com.alquran.offline.data.repository.QuranRepository
import com.alquran.offline.data.repository.QuranRepositoryImpl
import com.alquran.offline.notification.MotivationalNotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuranApplication : Application() {

    lateinit var repository: QuranRepository
        private set

    lateinit var prayerRepository: PrayerRepository
        private set

    lateinit var dailyPrayerRepository: DailyPrayerRepository
        private set

    lateinit var asmaulHusnaRepository: AsmaulHusnaRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val preferencesRepository = UserPreferencesRepository(this)
        val database = AppDatabase.getInstance(this)
        repository = QuranRepositoryImpl(database, preferencesRepository)
        prayerRepository = PrayerRepositoryImpl(this)
        dailyPrayerRepository = DailyPrayerRepositoryImpl(this)
        asmaulHusnaRepository = AsmaulHusnaRepositoryImpl(this)

        // Motivasi harian (07/12/17), pengingat tidur (21:00), pengingat wudhu (21/22/00).
        // Aman dipanggil berulang: PendingIntent memakai FLAG_UPDATE_CURRENT.
        try {
            MotivationalNotificationScheduler.createNotificationChannels(this)
            MotivationalNotificationScheduler.scheduleAll(this)
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        // Asynchronously pre-warm the database on Dispatchers.IO.
        // Room extracts and prepares the asset database in the background
        // without stalling the UI thread during cold start.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                database.openHelper.writableDatabase
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}
