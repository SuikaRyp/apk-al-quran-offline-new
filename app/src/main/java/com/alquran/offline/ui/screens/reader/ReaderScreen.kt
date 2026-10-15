package com.alquran.offline.ui.screens.reader

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alquran.offline.model.Ayah
import com.alquran.offline.ui.components.AppTopBar
import com.alquran.offline.ui.components.AyahCard
import com.alquran.offline.ui.components.BasmalahHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit = {},
    onNavigateToSurah: (Int) -> Unit = {},
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val recitationPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
    var downloadingMurottal by remember { mutableStateOf(false) }
    var murottalProgress by remember { mutableStateOf(0f) }
    var downloadedAyahs by remember { mutableStateOf(0) }
    var totalMurottalAyahs by remember { mutableStateOf(MurottalOfflineStore.totalAyahs()) }
    var showDownloadConfirmation by remember { mutableStateOf(false) }
    var showReciterPicker by remember { mutableStateOf(false) }
    var selectedReciterId by remember {
        mutableStateOf(context.getSharedPreferences("suika_reciter_prefs", Context.MODE_PRIVATE).getString("reciter_id", "husary") ?: "husary")
    }
    val selectedReciter = remember(selectedReciterId) { QuranReciters.byId(selectedReciterId) }
    DisposableEffect(Unit) {
        onDispose {
            recitationPlayer.value?.let { player ->
                try { if (player.isPlaying) player.stop() } catch (_: Throwable) { }
                player.release()
            }
            recitationPlayer.value = null
        }
    }

    val surah by viewModel.surah.collectAsState()
    val ayahs by viewModel.ayahs.collectAsState()
    val arabicFontSize by viewModel.arabicFontSize.collectAsState()
    val translationFontSize by viewModel.translationFontSize.collectAsState()
    val showTranslation by viewModel.showTranslation.collectAsState()

    val listState = rememberLazyListState()

    // Scroll to target verse when ayahs load
    LaunchedEffect(ayahs.isNotEmpty()) {
        if (ayahs.isNotEmpty() && viewModel.initialTargetVerse > 1) {
            val targetIndex = (viewModel.initialTargetVerse - 1).coerceIn(0, ayahs.size - 1)
            // Account for Surah Info Banner (index 0) + optional Basmalah (index 1)
            val hasBasmalah = viewModel.surahId != 1 && viewModel.surahId != 9
            val headerOffset = 1 + if (hasBasmalah) 1 else 0
            val maxIndex = (ayahs.size + headerOffset - 1).coerceAtLeast(0)
            val safeIndex = (targetIndex + headerOffset).coerceIn(0, maxIndex)
            try {
                listState.animateScrollToItem(safeIndex)
            } catch (e: Throwable) {
                try {
                    listState.scrollToItem(safeIndex)
                } catch (ignored: Throwable) {
                }
            }
        }
    }

    // Auto-update Last Read as user scrolls
    val firstVisibleIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex }
    }
    LaunchedEffect(firstVisibleIndex) {
        if (ayahs.isNotEmpty()) {
            val hasBasmalah = viewModel.surahId != 1 && viewModel.surahId != 9
            val headerOffset = 1 + if (hasBasmalah) 1 else 0
            val ayahIndex = (firstVisibleIndex - headerOffset).coerceIn(0, ayahs.size - 1)
            val currentAyah = ayahs.getOrNull(ayahIndex)
            if (currentAyah != null) {
                viewModel.updateLastRead(currentAyah.verseId)
            }
        }
    }

    fun copyAyahToClipboard(ayah: Ayah) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val text = buildString {
                appendLine(ayah.textAr)
                if (ayah.transliteration.isNotBlank()) appendLine(ayah.transliteration)
                if (ayah.textId.isNotBlank()) appendLine(ayah.textId)
                appendLine("(${surah?.nameLatin ?: "Surah ${ayah.surahId}"}: ${ayah.verseId})")
            }
            val clip = ClipData.newPlainText("Ayat Al-Qur'an", text)
            clipboard?.setPrimaryClip(clip)
            scope.launch {
                snackbarHostState.showSnackbar("Ayat berhasil disalin ke papan klip")
            }
        } catch (e: Throwable) {
            scope.launch {
                snackbarHostState.showSnackbar("Gagal menyalin ayat ke papan klip")
            }
        }
    }

    fun shareAyah(ayah: Ayah) {
        try {
            val shareText = buildString {
                appendLine(ayah.textAr)
                appendLine()
                if (ayah.transliteration.isNotBlank()) {
                    appendLine(ayah.transliteration)
                    appendLine()
                }
                if (ayah.textId.isNotBlank()) {
                    appendLine("\"${ayah.textId}\"")
                    appendLine()
                }
                appendLine("— QS. ${surah?.nameLatin ?: "Surah ${ayah.surahId}"} [${ayah.surahId}:${ayah.verseId}]")
                appendLine("Dibaca via Al-Qur'an Offline")
            }
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Bagikan Ayat").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(shareIntent)
        } catch (e: ActivityNotFoundException) {
            scope.launch {
                snackbarHostState.showSnackbar("Tidak ada aplikasi yang dapat menerima konten ini.")
            }
        } catch (e: Throwable) {
            scope.launch {
                snackbarHostState.showSnackbar("Tidak dapat membagikan ayat saat ini.")
            }
        }
    }

    if (showReciterPicker) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showReciterPicker = false },
            title = { Text("Pilih qari murottal") },
            text = {
                Column(Modifier.heightIn(max = 520.dp)) {
                    Text("Pilih suara. Foto profil ditampilkan dari sumber online jika tersedia; pemilihan audio tetap berfungsi tanpa foto.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    androidx.compose.foundation.lazy.LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(QuranReciters.all.size) { index ->
                            val reciter = QuranReciters.all[index]
                            Card(
                                onClick = {
                                    selectedReciterId = reciter.id
                                    context.getSharedPreferences("suika_reciter_prefs", Context.MODE_PRIVATE).edit().putString("reciter_id", reciter.id).apply()
                                    showReciterPicker = false
                                },
                                colors = CardDefaults.cardColors(containerColor = if (reciter.id == selectedReciterId) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    ReciterPortrait(reciter = reciter, modifier = Modifier.size(64.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(reciter.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                        Text(reciter.style, style = MaterialTheme.typography.bodySmall)
                                        Text(reciter.portraitCredit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (reciter.id == selectedReciterId) Icon(Icons.Default.CheckCircle, contentDescription = "Dipilih", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { OutlinedButton(onClick = { showReciterPicker = false }) { Text("Selesai") } }
        )
    }

    if (showDownloadConfirmation) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDownloadConfirmation = false },
            title = { Text("Unduh murottal lengkap?") },
            text = { Text("Aplikasi akan mengunduh 6.236 rekaman ayat ${selectedReciter.name} ke penyimpanan internal aplikasi. Perlu koneksi internet stabil, waktu cukup lama, dan ruang kosong sekitar 1 GB atau lebih. Setelah selesai, audio bisa diputar tanpa internet.") },
            confirmButton = {
                Button(onClick = {
                    showDownloadConfirmation = false
                    downloadingMurottal = true
                    murottalProgress = 0f
                    downloadedAyahs = 0
                    scope.launch {
                        try {
                            withContext(Dispatchers.IO) {
                                MurottalOfflineStore.downloadAll(context, selectedReciter.id) { done, total ->
                                    downloadedAyahs = done
                                    totalMurottalAyahs = total
                                    murottalProgress = done.toFloat() / total.coerceAtLeast(1)
                                }
                            }
                            snackbarHostState.showSnackbar("Murottal lengkap tersimpan. Sekarang bisa diputar offline.")
                        } catch (error: Throwable) {
                            snackbarHostState.showSnackbar("Unduhan terhenti: ${error.message ?: "koneksi atau penyimpanan bermasalah"}. Tekan unduh lagi untuk melanjutkan.")
                        } finally {
                            downloadingMurottal = false
                        }
                    }
                }) { Text("Mulai unduh") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDownloadConfirmation = false }) { Text("Batal") }
            }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = surah?.nameLatin ?: "Membaca Surah",
                subtitle = surah?.let { "${it.totalVerses} ayat · ${it.type}" },
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = onHomeClick) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Beranda"
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan Tampilan"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (ayahs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Surah Banner Info Header (inspired by com.andi.alquran.id)
                item {
                    surah?.let { s ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = s.nameAr,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 32.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = s.nameLatin,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = s.translationId,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "${s.type} • ${s.totalVerses} Ayat",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                        OutlinedButton(
                            onClick = { showReciterPicker = true },
                            enabled = !downloadingMurottal,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Qari: ${selectedReciter.name}")
                        }
                        Text(selectedReciter.style, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(
                            onClick = { showDownloadConfirmation = true },
                            enabled = !downloadingMurottal,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (downloadingMurottal) "Mengunduh murottal…" else "Unduh suara ${selectedReciter.name.substringBeforeLast(' ')} agar offline")
                        }
                        Text(
                            text = if (downloadingMurottal) "${downloadedAyahs} / ${totalMurottalAyahs} ayat tersimpan" else "Rekaman asli · riwayat Hafs · setiap qari disimpan terpisah",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
                        )
                        if (downloadingMurottal) {
                            LinearProgressIndicator(progress = murottalProgress, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }

                // Basmalah Banner for surahs other than Surah 1 (Al-Fatihah) and Surah 9 (At-Tawbah)
                if (viewModel.surahId != 1 && viewModel.surahId != 9) {
                    item {
                        BasmalahHeader()
                    }
                }

                itemsIndexed(
                    items = ayahs,
                    key = { _, ayah -> ayah.id }
                ) { _, ayah ->
                    AyahCard(
                        ayah = ayah,
                        arabicFontSize = arabicFontSize,
                        translationFontSize = translationFontSize,
                        showTranslation = showTranslation,
                        onBookmarkClick = {
                            viewModel.toggleBookmark(ayah)
                            scope.launch {
                                val msg = if (ayah.isBookmarked) "Bookmark dihapus" else "Ayat ditambahkan ke bookmark"
                                snackbarHostState.showSnackbar(msg)
                            }
                        },
                        onCopyClick = { copyAyahToClipboard(ayah) },
                        onShareClick = { shareAyah(ayah) },
                        onSpeakTranslationClick = {
                            // Original recorded Qur'an recitation (Al-Husary, murattal), not synthetic TTS.
                            try {
                                recitationPlayer.value?.let { oldPlayer ->
                                    try { if (oldPlayer.isPlaying) oldPlayer.stop() } catch (_: Throwable) { }
                                    oldPlayer.release()
                                }
                                val localAudio = MurottalOfflineStore.ayahFile(context, ayah.surahId, ayah.verseId, selectedReciter.id)
                                val player = MediaPlayer()
                                recitationPlayer.value = player
                                if (localAudio.exists() && localAudio.length() > 0L) {
                                    player.setDataSource(localAudio.absolutePath)
                                } else {
                                    val audioUrl = "https://everyayah.com/data/${selectedReciter.folder}/%03d%03d.mp3".format(
                                        ayah.surahId, ayah.verseId
                                    )
                                    player.setDataSource(audioUrl)
                                }
                                player.setOnPreparedListener { prepared -> prepared.start() }
                                player.setOnCompletionListener { completed ->
                                    completed.release()
                                    if (recitationPlayer.value === completed) recitationPlayer.value = null
                                }
                                player.setOnErrorListener { failed, _, _ ->
                                    failed.release()
                                    if (recitationPlayer.value === failed) recitationPlayer.value = null
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Audio belum tersimpan offline. Unduh murottal saat internet tersedia.")
                                    }
                                    true
                                }
                                player.prepareAsync()
                            } catch (_: Throwable) {
                                recitationPlayer.value?.let { player ->
                                    try { player.release() } catch (_: Throwable) { }
                                }
                                recitationPlayer.value = null
                                scope.launch {
                                    snackbarHostState.showSnackbar("Gagal memuat audio. Jika belum diunduh, periksa koneksi internet.")
                                }
                            }
                        }
                    )
                }

                // Surah Navigation: Previous & Next Surah Buttons (com.andi.alquran.id reference)
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (viewModel.surahId > 1) {
                            OutlinedButton(
                                onClick = { onNavigateToSurah(viewModel.surahId - 1) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Surah Sebelumnya")
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        if (viewModel.surahId < 114) {
                            Button(
                                onClick = { onNavigateToSurah(viewModel.surahId + 1) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Surah Berikutnya")
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}


@Composable
private fun ReciterPortrait(reciter: QuranReciter, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = reciter.portraitUrl) {
        value = withContext(Dispatchers.IO) {
            val cacheDir = java.io.File(context.filesDir, "reciter_photos")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val cacheFile = java.io.File(cacheDir, "${reciter.id}.img")
            try {
                if (cacheFile.exists() && cacheFile.length() > 100L) {
                    BitmapFactory.decodeFile(cacheFile.absolutePath)
                } else {
                    val connection = (URL(reciter.portraitUrl).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 3500
                        readTimeout = 3500
                        setRequestProperty("User-Agent", "SuikaAlQuranOffline/2.0")
                    }
                    try {
                        connection.inputStream.use { input ->
                            val bytes = input.readBytes()
                            if (bytes.size > 100) cacheFile.writeBytes(bytes)
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        }
                    } finally { connection.disconnect() }
                }
            } catch (_: Throwable) { null }
        }
    }
    if (bitmap != null) {
        Image(bitmap = bitmap!!.asImageBitmap(), contentDescription = "Foto ${reciter.name}", modifier = modifier, contentScale = androidx.compose.ui.layout.ContentScale.Crop)
    } else {
        Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer) {
            Box(contentAlignment = Alignment.Center) {
                Text(reciter.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}
