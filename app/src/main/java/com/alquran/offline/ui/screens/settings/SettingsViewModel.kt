package com.alquran.offline.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alquran.offline.azan.AzanAlarmScheduler
import com.alquran.offline.azan.AzanPreferencesRepository
import com.alquran.offline.azan.LocationHelper
import com.alquran.offline.azan.PrayerTimesCalculator
import com.alquran.offline.azan.PrayerTimesResult
import com.alquran.offline.data.repository.QuranRepository
import com.alquran.offline.model.ThemeMode
import com.alquran.offline.notification.NotificationScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class SettingsViewModel(
    private val repository: QuranRepository,
    private val appContext: Context
) : ViewModel() {

    val arabicFontSize: StateFlow<Float> = repository.arabicFontSize.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 28f
    )

    val translationFontSize: StateFlow<Float> = repository.translationFontSize.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 15f
    )

    val showTranslation: StateFlow<Boolean> = repository.showTranslation.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val themeMode: StateFlow<ThemeMode> = repository.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeMode.SYSTEM
    )

    val notificationEnabled: StateFlow<Boolean> = repository.notificationEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val notificationHour: StateFlow<Int> = repository.notificationHour.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 7
    )

    val notificationMinute: StateFlow<Int> = repository.notificationMinute.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun setArabicFontSize(size: Float) {
        viewModelScope.launch { repository.setArabicFontSize(size) }
    }

    fun setTranslationFontSize(size: Float) {
        viewModelScope.launch { repository.setTranslationFontSize(size) }
    }

    fun setShowTranslation(show: Boolean) {
        viewModelScope.launch { repository.setShowTranslation(show) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun resetLastRead() {
        viewModelScope.launch { repository.resetLastRead() }
    }

    fun setNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setNotificationEnabled(enabled)
            if (enabled) {
                val h = notificationHour.value
                val m = notificationMinute.value
                NotificationScheduler.scheduleDailyNotification(appContext, h, m)
            } else {
                NotificationScheduler.cancelNotification(appContext)
            }
        }
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            repository.setNotificationTime(hour, minute)
            if (notificationEnabled.value) {
                NotificationScheduler.scheduleDailyNotification(appContext, hour, minute)
            }
        }
    }

    // ---------------- Notifikasi Waktu Sholat (Azan) ----------------

    private val azanPreferencesRepository = AzanPreferencesRepository(appContext)

    val azanEnabled: StateFlow<Boolean> = azanPreferencesRepository.enabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val azanLocationLabel: StateFlow<String> = azanPreferencesRepository.locationLabel.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Jakarta (default)"
    )

    val azanTone: StateFlow<String?> = azanPreferencesRepository.tone.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun setAzanTone(id: String) {
        viewModelScope.launch {
            azanPreferencesRepository.setTone(id)
        }
    }

    private val _todayPrayerTimes = MutableStateFlow<PrayerTimesResult?>(null)
    val todayPrayerTimes: StateFlow<PrayerTimesResult?> = _todayPrayerTimes.asStateFlow()

    private val _isLocating = MutableStateFlow(false)
    val isLocating: StateFlow<Boolean> = _isLocating.asStateFlow()

    init {
        viewModelScope.launch {
            val lat = azanPreferencesRepository.latitudeNow()
            val lon = azanPreferencesRepository.longitudeNow()
            recomputeTodayTimes(lat, lon)
        }
    }

    private fun recomputeTodayTimes(lat: Double, lon: Double) {
        try {
            _todayPrayerTimes.value = PrayerTimesCalculator.calculate(
                Calendar.getInstance(),
                lat,
                lon,
                PrayerTimesCalculator.currentTimeZoneOffsetHours()
            )
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun setAzanEnabled(enabled: Boolean) {
        viewModelScope.launch {
            azanPreferencesRepository.setEnabled(enabled)
            if (enabled) {
                val lat = azanPreferencesRepository.latitudeNow()
                val lon = azanPreferencesRepository.longitudeNow()
                recomputeTodayTimes(lat, lon)
                AzanAlarmScheduler.scheduleAll(appContext, lat, lon)
            } else {
                AzanAlarmScheduler.cancelAll(appContext)
            }
        }
    }

    /** Ambil lokasi GPS terkini (offline) lalu jadwalkan ulang 5 alarm sholat. */
    fun useCurrentLocationForAzan() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLocating.value = true
            try {
                val location = LocationHelper.getLastKnownLocation(appContext)
                    ?: LocationHelper.requestFreshLocation(appContext)
                if (location != null) {
                    val label = "Lat ${"%.3f".format(location.latitude)}, " +
                        "Lon ${"%.3f".format(location.longitude)}"
                    azanPreferencesRepository.setLocation(location.latitude, location.longitude, label)
                    recomputeTodayTimes(location.latitude, location.longitude)
                    if (azanPreferencesRepository.isEnabledNow()) {
                        AzanAlarmScheduler.scheduleAll(appContext, location.latitude, location.longitude)
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                _isLocating.value = false
            }
        }
    }

    class Factory(
        private val repository: QuranRepository,
        private val appContext: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository, appContext) as T
        }
    }
}
