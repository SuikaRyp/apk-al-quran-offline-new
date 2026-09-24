/*
 * Al-Qur'an Offline — Modul Notifikasi Waktu Sholat
 * © 2026 Kyuu / KyuuX444
 *
 * Kalkulator waktu sholat 100% OFFLINE berbasis rumus astronomi matahari standar
 * (posisi matahari & equation of time). Tidak memerlukan koneksi internet atau API,
 * hanya butuh lintang/bujur lokasi dan zona waktu perangkat.
 *
 * Sudut yang dipakai mengikuti konvensi umum Kemenag RI:
 *  - Subuh  : matahari 20° di bawah ufuk sebelum terbit
 *  - Isya   : matahari 18° di bawah ufuk setelah terbenam
 *  - Ashar  : mazhab Syafi'i (panjang bayangan = panjang benda + bayangan saat istiwa)
 *  - Ditambah ihtiyat (margin kehati-hatian) beberapa menit agar notifikasi tidak
 *    pernah muncul mendahului waktu sholat yang sesungguhnya.
 */
package com.alquran.offline.azan

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

enum class PrayerName(val label: String, val labelArab: String) {
    SUBUH("Subuh", "الفجر"),
    DZUHUR("Dzuhur", "الظهر"),
    ASHAR("Ashar", "العصر"),
    MAGHRIB("Maghrib", "المغرب"),
    ISYA("Isya", "العشاء")
}

data class PrayerTimesResult(
    val fajr: Calendar,
    val sunrise: Calendar,
    val dhuhr: Calendar,
    val asr: Calendar,
    val maghrib: Calendar,
    val isha: Calendar
) {
    fun timeFor(prayer: PrayerName): Calendar = when (prayer) {
        PrayerName.SUBUH -> fajr
        PrayerName.DZUHUR -> dhuhr
        PrayerName.ASHAR -> asr
        PrayerName.MAGHRIB -> maghrib
        PrayerName.ISYA -> isha
    }

    fun asOrderedList(): List<Pair<PrayerName, Calendar>> = listOf(
        PrayerName.SUBUH to fajr,
        PrayerName.DZUHUR to dhuhr,
        PrayerName.ASHAR to asr,
        PrayerName.MAGHRIB to maghrib,
        PrayerName.ISYA to isha
    )
}

object PrayerTimesCalculator {

    // Sudut standar Kemenag RI
    private const val FAJR_ANGLE = 20.0
    private const val ISHA_ANGLE = 18.0
    private const val SUNSET_ANGLE = 0.833 // refraksi + jari-jari matahari
    private const val ASR_SHADOW_FACTOR = 1.0 // mazhab Syafi'i/Maliki/Hanbali

    // Ihtiyat (margin pengaman) dalam menit: lebih baik notifikasi terlambat
    // beberapa menit daripada mendahului waktu sholat yang sebenarnya.
    private const val IHTIYAT_MINUTES = 3.0

    /**
     * Menghitung 5 waktu sholat + terbit matahari untuk sebuah tanggal & lokasi.
     * [timeZoneOffsetHours] adalah offset UTC lokasi tsb dalam jam (mis. WIB = 7.0).
     */
    fun calculate(
        date: Calendar,
        latitude: Double,
        longitude: Double,
        timeZoneOffsetHours: Double
    ): PrayerTimesResult {
        val jd = julianDay(
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH) + 1,
            date.get(Calendar.DAY_OF_MONTH)
        )
        val (declination, eqTimeHours) = sunPosition(jd)

        val dhuhrHour = 12.0 + timeZoneOffsetHours - (longitude / 15.0) - eqTimeHours

        fun hourAngle(angleDeg: Double): Double {
            val num = -sind(angleDeg) - sind(latitude) * sind(declination)
            val den = cosd(latitude) * cosd(declination)
            val ratio = (num / den).coerceIn(-1.0, 1.0)
            return acosd(ratio) / 15.0
        }

        fun asrHourAngle(): Double {
            val shadowLat = abs(latitude - declination)
            val altitude = atand(1.0 / (ASR_SHADOW_FACTOR + tand(shadowLat)))
            val num = sind(altitude) - sind(latitude) * sind(declination)
            val den = cosd(latitude) * cosd(declination)
            val ratio = (num / den).coerceIn(-1.0, 1.0)
            return acosd(ratio) / 15.0
        }

        val fajrHour = dhuhrHour - hourAngle(FAJR_ANGLE) - IHTIYAT_MINUTES / 60.0
        val sunriseHour = dhuhrHour - hourAngle(SUNSET_ANGLE)
        val dhuhrFinal = dhuhrHour + IHTIYAT_MINUTES / 60.0
        val asrHour = dhuhrHour + asrHourAngle() + IHTIYAT_MINUTES / 60.0
        val maghribHour = dhuhrHour + hourAngle(SUNSET_ANGLE) + IHTIYAT_MINUTES / 60.0
        val ishaHour = dhuhrHour + hourAngle(ISHA_ANGLE) + IHTIYAT_MINUTES / 60.0

        return PrayerTimesResult(
            fajr = calendarAt(date, fajrHour),
            sunrise = calendarAt(date, sunriseHour),
            dhuhr = calendarAt(date, dhuhrFinal),
            asr = calendarAt(date, asrHour),
            maghrib = calendarAt(date, maghribHour),
            isha = calendarAt(date, ishaHour)
        )
    }

    /** Offset UTC (jam, termasuk pecahan) dari zona waktu perangkat saat ini. */
    fun currentTimeZoneOffsetHours(): Double =
        TimeZone.getDefault().rawOffset / 3600000.0

    private fun calendarAt(date: Calendar, hourOfDayDecimal: Double): Calendar {
        var h = hourOfDayDecimal
        val dayShift = floor(h / 24.0).toInt()
        h -= dayShift * 24.0
        val totalMinutes = (h * 60.0).let { if (it < 0) it + 24 * 60 else it }
        val hh = (totalMinutes / 60.0).toInt().coerceIn(0, 23)
        val mm = (totalMinutes % 60.0).toInt().coerceIn(0, 59)
        return (date.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, dayShift)
            set(Calendar.HOUR_OF_DAY, hh)
            set(Calendar.MINUTE, mm)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    /** Mengembalikan Pair(deklinasi matahari (derajat), equation of time (jam)) */
    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = norm360(357.529 + 0.98560028 * d) // anomali rata-rata
        val q = norm360(280.459 + 0.98564736 * d) // bujur rata-rata matahari
        val l = norm360(q + 1.915 * sind(g) + 0.020 * sind(2 * g)) // bujur ekliptika
        val e = 23.439 - 0.00000036 * d // obliquity ekliptika
        val ra = norm360(atan2d(cosd(e) * sind(l), cosd(l))) / 15.0 // asensio rekta (jam)
        val declination = asind(sind(e) * sind(l))
        var eqTime = q / 15.0 - ra
        if (eqTime > 12) eqTime -= 24
        if (eqTime < -12) eqTime += 24
        return declination to eqTime
    }

    private fun norm360(x: Double): Double {
        var v = x % 360.0
        if (v < 0) v += 360.0
        return v
    }

    private fun sind(deg: Double) = sin(deg * PI / 180.0)
    private fun cosd(deg: Double) = cos(deg * PI / 180.0)
    private fun tand(deg: Double) = tan(deg * PI / 180.0)
    private fun acosd(x: Double) = acos(x.coerceIn(-1.0, 1.0)) * 180.0 / PI
    private fun asind(x: Double) = asin(x.coerceIn(-1.0, 1.0)) * 180.0 / PI
    private fun atand(x: Double) = atan(x) * 180.0 / PI
    private fun atan2d(y: Double, x: Double) = atan2(y, x) * 180.0 / PI
}
