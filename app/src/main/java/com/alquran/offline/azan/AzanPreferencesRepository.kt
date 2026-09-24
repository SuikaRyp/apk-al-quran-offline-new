package com.alquran.offline.azan

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.azanDataStore by preferencesDataStore(name = "azan_preferences")

/**
 * Lokasi default: Jakarta. Dipakai hanya sebagai titik awal sebelum pengguna
 * memilih "Gunakan Lokasi Saat Ini" di halaman Pengaturan.
 */
class AzanPreferencesRepository(private val context: Context) {

    private object Keys {
        val ENABLED = booleanPreferencesKey("azan_enabled")
        val LATITUDE = floatPreferencesKey("azan_latitude")
        val LONGITUDE = floatPreferencesKey("azan_longitude")
        val LOCATION_LABEL = stringPreferencesKey("azan_location_label")
        val TONE = stringPreferencesKey("azan_tone")
    }

    private val safeData: Flow<androidx.datastore.preferences.core.Preferences> =
        context.azanDataStore.data.catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }

    val enabled: Flow<Boolean> = safeData.map { it[Keys.ENABLED] ?: false }
    val latitude: Flow<Float> = safeData.map { it[Keys.LATITUDE] ?: -6.2088f }
    val longitude: Flow<Float> = safeData.map { it[Keys.LONGITUDE] ?: 106.8456f }
    val locationLabel: Flow<String> = safeData.map { it[Keys.LOCATION_LABEL] ?: "Jakarta (default)" }

    /** ID nada dering azan (lihat [AzanTones]); null = belum pernah memilih. */
    val tone: Flow<String?> = safeData.map { it[Keys.TONE] }

    suspend fun isEnabledNow(): Boolean = enabled.first()
    suspend fun latitudeNow(): Double = latitude.first().toDouble()
    suspend fun longitudeNow(): Double = longitude.first().toDouble()

    suspend fun setEnabled(value: Boolean) {
        safeEdit { it[Keys.ENABLED] = value }
    }

    suspend fun toneNow(): String? = tone.first()

    suspend fun setTone(id: String) {
        safeEdit { it[Keys.TONE] = id }
    }

    suspend fun setLocation(lat: Double, lon: Double, label: String) {
        safeEdit {
            it[Keys.LATITUDE] = lat.toFloat()
            it[Keys.LONGITUDE] = lon.toFloat()
            it[Keys.LOCATION_LABEL] = label
        }
    }

    private suspend fun safeEdit(
        action: suspend (androidx.datastore.preferences.core.MutablePreferences) -> Unit
    ) {
        try {
            context.azanDataStore.edit(action)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
