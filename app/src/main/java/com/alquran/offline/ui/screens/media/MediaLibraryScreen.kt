package com.alquran.offline.ui.screens.media

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alquran.offline.R

private val Green = Color(0xFF145B45)
private val Gold = Color(0xFFD6B36A)
private data class OnlineMedia(val title: String, val detail: String, val url: String, val license: String)

@Composable
fun MediaLibraryScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    var track by remember { mutableIntStateOf(0) }
    var playing by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }
    val player = remember { MediaPlayer() }
    DisposableEffect(Unit) {
        onDispose { try { player.stop() } catch (_: Exception) {}; player.release() }
    }
    fun startTrack(index: Int) {
        try {
            player.reset()
            val descriptor = context.resources.openRawResourceFd(if (index == 0) R.raw.azan else R.raw.azan_1)
            descriptor.use { player.setDataSource(it.fileDescriptor, it.startOffset, it.length) }
            player.setOnCompletionListener { playing = false; loaded = false }
            player.prepare()
            player.start()
            track = index
            loaded = true
            playing = true
        } catch (_: Exception) { playing = false }
    }
    fun openUrl(url: String) {
        try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (_: Exception) { }
    }
    val audioSources = listOf(
        OnlineMedia("Al-Fatihah — audio", "Rekaman tersedia di Wikimedia Commons", "https://commons.wikimedia.org/wiki/File:001Fatiha.ogg", "CC0 · cek halaman sumber"),
        OnlineMedia("Al-Ikhlas — Mishary Alafasy", "Bacaan surah pendek", "https://commons.wikimedia.org/wiki/File:112.AlIkhlas-MisharyRashedAlafasy.ogg", "CC0 · atribusi tersedia di halaman"),
        OnlineMedia("Adhan — rekaman CC0", "Azan pendek untuk referensi", "https://commons.wikimedia.org/wiki/File:Adhan.ogg", "CC0")
    )
    val videos = listOf(
        OnlineMedia("Adhan in Istanbul", "Video azan · Wikimedia Commons", "https://commons.wikimedia.org/wiki/File:Adhan_in_Istanbul.webm", "CC0 · oleh Viceskeeni2"),
        OnlineMedia("Adhan, Great Mosque of Mecca", "Video azan · Makkah", "https://commons.wikimedia.org/wiki/File:Adhan,_Great_Mosque_of_Mecca_-_Jan_21,_2013.webm", "CC BY 3.0 · atribusi Seyfula Islam"),
        OnlineMedia("Adhan in Shalqar Mosque", "Video azan di masjid", "https://commons.wikimedia.org/wiki/File:Adhan_in_Shalqar_mosque.webm", "Lihat ketentuan lisensi di halaman sumber"),
        OnlineMedia("Quran MP4", "Video Al-Qur'an dengan teks dan bacaan", "https://quranmp4.com/resources", "Baca kredit dan lisensi sebelum menggunakan"),
        OnlineMedia("Anak sedang sholat — ilustrasi referensi", "Foto CC0 dari Wikimedia Commons; bukan animasi", "https://commons.wikimedia.org/wiki/File:Muslim_Kid_Praying.jpg", "CC0 · chidioc · Wikimedia Commons")
    )
    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali") }
            Column(Modifier.weight(1f)) {
                Text("Media Islami", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Audio lokal dan koleksi video", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 18.dp, top = 10.dp, end = 18.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Column(Modifier.fillMaxWidth().background(Green, RoundedCornerShape(24.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("مكتبة الصوت", color = Gold, fontSize = 24.sp)
                    Text("Audio bawaan", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Dua rekaman azan yang sudah tersedia di project dapat diputar tanpa internet.", color = Color.White.copy(alpha = .85f), style = MaterialTheme.typography.bodySmall)
                    Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = .10f), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(shape = CircleShape, color = Gold, modifier = Modifier.size(44.dp)) { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Green, modifier = Modifier.padding(8.dp)) }
                            Column(Modifier.weight(1f)) {
                                Text(if (track == 0) "Azan bawaan 1" else "Azan bawaan 2", color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text(if (playing) "Sedang diputar" else "Siap diputar offline", color = Color.White.copy(alpha = .75f), style = MaterialTheme.typography.labelSmall)
                            }
                            IconButton(onClick = { if (playing) { try { player.pause() } catch (_: Exception) {}; playing = false } else if (loaded) { try { player.start(); playing = true } catch (_: Exception) { startTrack(track) } } else startTrack(track) }) {
                                Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = if (playing) "Jeda" else "Putar", tint = Color.White)
                            }
                            IconButton(onClick = { try { player.stop(); player.reset() } catch (_: Exception) {}; playing = false; loaded = false }) { Icon(Icons.Default.Stop, contentDescription = "Berhenti", tint = Color.White) }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { startTrack(0) }, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Green), modifier = Modifier.weight(1f)) { Text("Azan 1") }
                        Button(onClick = { startTrack(1) }, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Green), modifier = Modifier.weight(1f)) { Text("Azan 2") }
                    }
                }
            }
            item { Text("Audio pilihan dari web", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(audioSources.size) { index ->
                val item = audioSources[index]
                MediaLinkRow(item.title, item.detail, item.license) { openUrl(item.url) }
            }
            item { Spacer(Modifier.height(4.dp)); Text("Video Islami", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            item {
                Text("Video dibuka dari sumber aslinya dan membutuhkan internet. File MP4 belum dibundel ke APK, supaya ukuran aplikasi tetap ringan dan lisensi sumber tetap jelas.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(videos.size) { index ->
                val item = videos[index]
                MediaLinkRow(item.title, item.detail, item.license) { openUrl(item.url) }
            }
            item {
                Text("Catatan lisensi: periksa halaman sumber sebelum menyalin atau mendistribusikan ulang media. Sebagian konten mensyaratkan atribusi; tautan ini membuka halaman sumber resmi.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MediaLinkRow(title: String, detail: String, license: String, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(12.dp), color = Green.copy(alpha = .10f), modifier = Modifier.size(42.dp)) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Green, modifier = Modifier.padding(10.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(license, style = MaterialTheme.typography.labelSmall, color = Green)
            }
        }
    }
}
