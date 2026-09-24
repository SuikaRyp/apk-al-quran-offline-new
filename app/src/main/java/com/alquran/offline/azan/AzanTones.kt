package com.alquran.offline.azan

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri

/**
 * Katalog nada dering azan.
 *
 * ID nada = nama file mp3 di `app/src/main/res/raw/` (tanpa ekstensi).
 *
 * CARA MENAMBAH MP3:
 *  1. Taruh file di `app/src/main/res/raw/` dengan nama persis seperti ID di daftar [slots],
 *     contoh `azan_1.mp3`, `azan_2.mp3`, dst. (huruf kecil, angka, underscore; tanpa spasi).
 *  2. (Opsional) ganti label tampilannya di [slots].
 *  3. Mau slot lebih dari 8? Tambah baris baru di [slots] dengan ID berawalan `azan`
 *     (pola `azan*` sudah dijaga di res/raw/keep.xml agar tidak dibuang saat shrink resource).
 *
 * Slot yang file mp3-nya belum ada otomatis tidak muncul di menu, jadi aman
 * dibuild walau belum ada satu pun mp3. Pilihan "Nada alarm bawaan HP" selalu ada.
 */
object AzanTones {

    const val DEFAULT_ID = "default"

    data class Tone(val id: String, val label: String)

    private val slots = listOf(
        Tone("azan", "Baraka Allahu Lakuma"),
        Tone("azan_1", "Adzan 1"),
        Tone("azan_2", "Adzan 2"),
        Tone("azan_3", "Adzan 3"),
        Tone("azan_4", "Adzan 4"),
        Tone("azan_5", "Adzan 5"),
        Tone("azan_6", "Adzan 6"),
        Tone("azan_7", "Adzan 7"),
        Tone("azan_8", "Adzan 8")
    )

    private val defaultTone = Tone(DEFAULT_ID, "Nada alarm bawaan HP")

    private fun rawResId(context: Context, id: String): Int =
        context.resources.getIdentifier(id, "raw", context.packageName)

    /** Nada bawaan HP + semua slot yang file mp3-nya sudah ada di res/raw. */
    fun availableTones(context: Context): List<Tone> =
        listOf(defaultTone) + slots.filter { rawResId(context, it.id) != 0 }

    /**
     * ID nada yang benar-benar dipakai.
     * - Belum pernah memilih: pakai `azan.mp3` bila ada (perilaku lama), kalau tidak nada bawaan HP.
     * - Nada yang dipilih sudah dihapus dari res/raw: jatuh ke nada bawaan HP.
     */
    fun effectiveId(context: Context, savedId: String?): String {
        if (savedId == null) {
            return if (rawResId(context, "azan") != 0) "azan" else DEFAULT_ID
        }
        if (savedId == DEFAULT_ID) return DEFAULT_ID
        return if (rawResId(context, savedId) != 0) savedId else DEFAULT_ID
    }

    fun labelFor(context: Context, savedId: String?): String {
        val id = effectiveId(context, savedId)
        return if (id == DEFAULT_ID) defaultTone.label
        else slots.firstOrNull { it.id == id }?.label ?: id
    }

    fun resolveUri(context: Context, savedId: String?): Uri {
        val id = effectiveId(context, savedId)
        if (id != DEFAULT_ID) {
            val rawId = rawResId(context, id)
            if (rawId != 0) return Uri.parse("android.resource://${context.packageName}/$rawId")
        }
        return RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    }
}
