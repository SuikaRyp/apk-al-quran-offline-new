package com.alquran.offline.ui.screens.reader

import android.content.Context
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** Offline cache for original recorded Quran recitations from EveryAyah. */
object MurottalOfflineStore {
    private val ayahCounts = intArrayOf(
        7,286,200,176,120,165,206,75,129,109,123,111,43,52,99,128,111,110,98,135,
        112,78,118,64,77,227,93,88,69,60,34,30,73,54,45,83,182,88,75,85,
        54,53,89,59,37,35,38,29,18,45,60,49,62,55,78,96,29,22,24,13,
        14,11,11,18,12,12,30,52,52,44,28,28,20,56,40,31,50,40,46,42,
        29,19,36,25,22,17,19,26,30,20,15,21,11,8,8,19,5,8,8,11,
        11,8,3,9,5,4,7,3,6,3,5,4,5,6
    )

    fun surahDir(context: Context, surah: Int, reciterId: String = "husary"): File =
        File(File(context.filesDir, "murottal_${QuranReciters.byId(reciterId).id}"), surah.toString())

    fun ayahFile(context: Context, surah: Int, ayah: Int, reciterId: String = "husary"): File =
        File(surahDir(context, surah, reciterId), "%03d.mp3".format(ayah))

    fun isAyahDownloaded(context: Context, surah: Int, ayah: Int, reciterId: String = "husary"): Boolean =
        ayahFile(context, surah, ayah, reciterId).let { it.exists() && it.length() > 0L }

    fun isSurahDownloaded(context: Context, surah: Int, reciterId: String = "husary"): Boolean =
        surah in 1..114 && (1..ayahCounts[surah - 1]).all { isAyahDownloaded(context, surah, it, reciterId) }

    fun totalAyahs(): Int = ayahCounts.sum()

    fun downloadAll(context: Context, reciterId: String = "husary", onProgress: (done: Int, total: Int) -> Unit) {
        val reciter = QuranReciters.byId(reciterId)
        val total = totalAyahs()
        var done = 0
        for (surah in 1..114) {
            val dir = surahDir(context, surah, reciter.id)
            if (!dir.exists() && !dir.mkdirs()) throw IllegalStateException("Tidak bisa membuat folder audio")
            for (ayah in 1..ayahCounts[surah - 1]) {
                val target = ayahFile(context, surah, ayah, reciter.id)
                if (!target.exists() || target.length() == 0L) downloadOne(reciter, surah, ayah, target)
                done++
                onProgress(done, total)
            }
        }
    }

    private fun downloadOne(reciter: QuranReciter, surah: Int, ayah: Int, target: File) {
        val url = URL("https://everyayah.com/data/${reciter.folder}/%03d%03d.mp3".format(surah, ayah))
        val connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000
            readTimeout = 30000
            requestMethod = "GET"
            setRequestProperty("User-Agent", "SuikaAlQuranOffline/2.0")
        }
        val temp = File(target.parentFile, target.name + ".part")
        try {
            connection.connect()
            if (connection.responseCode !in 200..299) throw IllegalStateException("Audio ${reciter.name} HTTP ${connection.responseCode} pada $surah:$ayah")
            connection.inputStream.use { input -> temp.outputStream().use { output -> input.copyTo(output) } }
            if (temp.length() < 100L) throw IllegalStateException("File audio $surah:$ayah tidak valid")
            if (target.exists()) target.delete()
            if (!temp.renameTo(target)) throw IllegalStateException("Gagal menyimpan audio $surah:$ayah")
        } finally {
            temp.delete()
            connection.disconnect()
        }
    }
}
