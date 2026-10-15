package com.alquran.offline.ui.screens.amalan

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Green = androidx.compose.ui.graphics.Color(0xFF145B45)
private val Gold = androidx.compose.ui.graphics.Color(0xFFD6B36A)
private val items = listOf(
    "Sholat Subuh", "Dzikir pagi", "Membaca Al-Qur'an", "Sholat Zuhur",
    "Sholat Asar", "Dzikir petang", "Sholat Magrib", "Sholat Isya", "Sholat Witir", "Sedekah / berbuat baik"
)

@Composable
fun AmalanScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val day = remember { SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date()) }
    val prefs = remember(day) { context.getSharedPreferences("amalan_tracker", Context.MODE_PRIVATE) }
    var selectedTab by remember { mutableStateOf("Semua") }
    var count by remember { mutableIntStateOf(prefs.getInt("tasbih_count", 0)) }
    var target by remember { mutableIntStateOf(prefs.getInt("tasbih_target", 33)) }
    var pagesRead by remember { mutableIntStateOf(prefs.getInt("khatam_pages_read", 0).coerceIn(0, 604)) }
    var dailyPageGoal by remember { mutableIntStateOf(prefs.getInt("khatam_daily_goal", 20).coerceIn(1, 604)) }
    var studyNotes by remember { androidx.compose.runtime.mutableStateOf(prefs.getString("study_notes", "") ?: "") }
    var memorizedSurahs by remember { mutableIntStateOf(prefs.getInt("memorized_surahs", 0).coerceIn(0, 114)) }
    var fastingDays by remember { mutableIntStateOf(prefs.getInt("fasting_days", 0).coerceAtLeast(0)) }
    var sadaqahCount by remember { mutableIntStateOf(prefs.getInt("sadaqah_count", 0).coerceAtLeast(0)) }
    var gratitudeNote by remember { androidx.compose.runtime.mutableStateOf(prefs.getString("gratitude_note_$day", "") ?: "") }
    var revisionCount by remember { mutableIntStateOf(prefs.getInt("quran_revision_count", 0).coerceAtLeast(0)) }
    var selectedDhikr by remember { androidx.compose.runtime.mutableStateOf(prefs.getString("selected_dhikr", "Subhanallah") ?: "Subhanallah") }
    val checked = remember(day) { mutableStateMapOf<String, Boolean>().apply { items.forEach { put(it, prefs.getBoolean("$day:$it", false)) } } }
    val done = checked.values.count { it }
    val weeklyProgress = remember(day, done) {
        (0..6).sumOf { offset ->
            val calendar = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -offset) }
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(calendar.time)
            items.count { prefs.getBoolean("$dateKey:$it", false) }
        }
    }
    val streak = remember(day, done) {
        var current = 0
        val calendar = java.util.Calendar.getInstance()
        // Jika hari ini belum ada amalan, hitung streak mulai dari kemarin.
        if (items.none { prefs.getBoolean("$day:$it", false) }) calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        repeat(366) {
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(calendar.time)
            if (items.any { prefs.getBoolean("$dateKey:$it", false) }) {
                current++
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
            } else return@remember current
        }
        current
    }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali") }
            Column(Modifier.weight(1f)) {
                Text("Amalan & Tasbih", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Langkah kecil, istiqamah setiap hari", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 18.dp, end = 18.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Semua", "Tasbih", "Amalan", "Progres").forEach { tab ->
                        AssistChip(onClick = { selectedTab = tab }, label = { Text(tab) }, colors = AssistChipDefaults.assistChipColors(containerColor = if (selectedTab == tab) Green else MaterialTheme.colorScheme.surfaceVariant, labelColor = if (selectedTab == tab) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                }
            }
            if (selectedTab == "Semua" || selectedTab == "Tasbih") {
            item {
                Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Green), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("تَسْبِيحٌ", color = Gold, fontSize = 24.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Tasbih Digital", color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Subhanallah", "Alhamdulillah", "Allahu Akbar", "Istighfar").forEach { dhikr ->
                                AssistChip(
                                    onClick = { selectedDhikr = dhikr; prefs.edit().putString("selected_dhikr", dhikr).apply() },
                                    label = { Text(dhikr, fontSize = 10.sp) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (selectedDhikr == dhikr) Gold else androidx.compose.ui.graphics.Color.White.copy(alpha = .08f),
                                        labelColor = if (selectedDhikr == dhikr) Green else androidx.compose.ui.graphics.Color.White
                                    )
                                )
                            }
                        }
                        Text("Dzikir dipilih: $selectedDhikr", color = Gold, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(14.dp))
                        Surface(shape = CircleShape, color = androidx.compose.ui.graphics.Color.White.copy(alpha = .09f), border = BorderStroke(1.dp, Gold.copy(alpha = .7f)), modifier = Modifier.size(150.dp).clickable {
                            count = if (count >= 9999) 0 else count + 1
                            prefs.edit().putInt("tasbih_count", count).apply()
                        }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text(count.toString(), color = androidx.compose.ui.graphics.Color.White, fontSize = 42.sp, fontWeight = FontWeight.Bold)
                                Text("ketuk untuk dzikir", color = Gold, fontSize = 11.sp)
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        LinearProgressIndicator(progress = (count.toFloat() / target).coerceIn(0f, 1f), modifier = Modifier.fillMaxWidth(.78f), color = Gold, trackColor = androidx.compose.ui.graphics.Color.White.copy(alpha = .18f))
                        Spacer(Modifier.height(6.dp))
                        Text("Target $target · ${if (count >= target) "Target tercapai, alhamdulillah" else "${(target-count).coerceAtLeast(0)} lagi menuju target"}", color = androidx.compose.ui.graphics.Color.White.copy(alpha = .9f), style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            listOf(33, 99, 100).forEach { goal ->
                                OutlinedButton(onClick = { target = goal; prefs.edit().putInt("tasbih_target", goal).apply() }, border = BorderStroke(1.dp, if (target == goal) Gold else androidx.compose.ui.graphics.Color.White.copy(alpha = .45f)), colors = ButtonDefaults.outlinedButtonColors(contentColor = androidx.compose.ui.graphics.Color.White), shape = RoundedCornerShape(14.dp)) { Text("$goal") }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = { count = (count - 1).coerceAtLeast(0); prefs.edit().putInt("tasbih_count", count).apply() }, border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = .5f)), colors = ButtonDefaults.outlinedButtonColors(contentColor = androidx.compose.ui.graphics.Color.White)) { Icon(Icons.Default.Remove, contentDescription = "Kurangi") }
                            Button(onClick = { count = 0; prefs.edit().putInt("tasbih_count", 0).apply() }, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Green)) { Icon(Icons.Default.Refresh, contentDescription = null); Text("Reset") }
                            Button(onClick = { count += 1; prefs.edit().putInt("tasbih_count", count).apply() }, colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.White, contentColor = Green)) { Icon(Icons.Default.Add, contentDescription = null); Text("Dzikir") }
                        }
                    }
                }
            }

            }
            if (selectedTab == "Semua" || selectedTab == "Amalan") {
            item {
                Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Konsistensi Ibadah", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column(Modifier.weight(1f).background(Green.copy(alpha = .08f), RoundedCornerShape(16.dp)).padding(14.dp)) {
                                Text("Istiqamah", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$streak hari", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Green)
                                Text("beruntun tercatat", style = MaterialTheme.typography.labelSmall)
                            }
                            Column(Modifier.weight(1f).background(Gold.copy(alpha = .15f), RoundedCornerShape(16.dp)).padding(14.dp)) {
                                Text("7 hari terakhir", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$weeklyProgress", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Green)
                                Text("amalan dicentang", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Text("Statistik ini hanya membantu memantau kebiasaan pribadi, bukan ukuran nilai ibadah.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            }
            if (selectedTab == "Semua" || selectedTab == "Amalan") {
            item {
                Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Column(Modifier.padding(18.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Amalan hari ini", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Centang yang sudah dikerjakan. Daftar diperbarui setiap hari.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("$done/${items.size}", color = Green, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(progress = done.toFloat() / items.size, modifier = Modifier.fillMaxWidth(), color = Green)
                        Spacer(Modifier.height(8.dp))
                        items.forEach { label ->
                            Row(Modifier.fillMaxWidth().clickable {
                                val next = !(checked[label] ?: false); checked[label] = next; prefs.edit().putBoolean("$day:$label", next).apply()
                            }.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = checked[label] ?: false, onCheckedChange = { next -> checked[label] = next; prefs.edit().putBoolean("$day:$label", next).apply() })
                                Text(label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                                if (checked[label] == true) Icon(Icons.Default.CheckCircle, contentDescription = "Selesai", tint = Green, modifier = Modifier.size(19.dp))
                            }
                        }
                    }
                }
            }

            }
            if (selectedTab == "Semua" || selectedTab == "Progres") {
            item {
                Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Target Khatam Al-Qur'an", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Pantau progres bacaan secara pribadi. Mushaf cetak bisa berbeda jumlah halamannya.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("$pagesRead", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Green)
                            Text(" / 604 halaman", modifier = Modifier.padding(start = 6.dp, bottom = 5.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        LinearProgressIndicator(progress = pagesRead / 604f, modifier = Modifier.fillMaxWidth(), color = Green)
                        Text("${(pagesRead * 100 / 604)}% selesai · perkiraan ${kotlin.math.ceil((604 - pagesRead).coerceAtLeast(0).toDouble() / dailyPageGoal).toInt()} hari lagi dengan target saat ini", style = MaterialTheme.typography.bodySmall)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { dailyPageGoal = (dailyPageGoal - 5).coerceAtLeast(1); prefs.edit().putInt("khatam_daily_goal", dailyPageGoal).apply() }, enabled = dailyPageGoal > 1) { Text("−5") }
                            Text("Target $dailyPageGoal halaman/hari", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                            OutlinedButton(onClick = { dailyPageGoal = (dailyPageGoal + 5).coerceAtMost(604); prefs.edit().putInt("khatam_daily_goal", dailyPageGoal).apply() }, enabled = dailyPageGoal < 604) { Text("+5") }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { pagesRead = (pagesRead - 1).coerceAtLeast(0); prefs.edit().putInt("khatam_pages_read", pagesRead).apply() }, enabled = pagesRead > 0, modifier = Modifier.weight(1f)) { Text("−1 halaman") }
                            Button(onClick = { pagesRead = (pagesRead + 1).coerceAtMost(604); prefs.edit().putInt("khatam_pages_read", pagesRead).apply() }, enabled = pagesRead < 604, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Green)) { Text("+1 halaman") }
                        }
                        OutlinedButton(onClick = { pagesRead = 0; prefs.edit().putInt("khatam_pages_read", 0).apply() }, modifier = Modifier.fillMaxWidth()) { Text("Mulai target dari awal") }
                    }
                }
            }

            }
            if (selectedTab == "Semua" || selectedTab == "Progres") {
            item {
                Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Catatan Kajian Pribadi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Simpan poin kajian, pertanyaan, atau ayat yang ingin dipelajari lagi. Catatan tersimpan di perangkat ini.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(value = studyNotes, onValueChange = { studyNotes = it; prefs.edit().putString("study_notes", it).apply() }, modifier = Modifier.fillMaxWidth(), minLines = 4, placeholder = { Text("Tulis catatan di sini…") }, label = { Text("Catatan") })
                        Text("${studyNotes.length} karakter · tersimpan otomatis", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedButton(onClick = { studyNotes = ""; prefs.edit().putString("study_notes", "").apply() }, enabled = studyNotes.isNotEmpty(), modifier = Modifier.fillMaxWidth()) { Text("Hapus catatan") }
                    }
                }
            }

            }
            if (selectedTab == "Semua" || selectedTab == "Progres") {
            item {
                Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Hafalan & Murajaah", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Catat progres hafalan secara pribadi. Jumlah surah adalah target buatan sendiri, bukan penilaian kemampuan.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Surah dihafal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$memorizedSurahs / 114", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Green)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { memorizedSurahs = (memorizedSurahs - 1).coerceAtLeast(0); prefs.edit().putInt("memorized_surahs", memorizedSurahs).apply() }, enabled = memorizedSurahs > 0) { Icon(Icons.Default.Remove, contentDescription = "Kurangi hafalan") }
                                IconButton(onClick = { memorizedSurahs = (memorizedSurahs + 1).coerceAtMost(114); prefs.edit().putInt("memorized_surahs", memorizedSurahs).apply() }, enabled = memorizedSurahs < 114) { Icon(Icons.Default.Add, contentDescription = "Tambah hafalan") }
                            }
                        }
                        LinearProgressIndicator(progress = memorizedSurahs / 114f, modifier = Modifier.fillMaxWidth(), color = Green)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Sesi murajaah", fontWeight = FontWeight.Medium)
                                Text("$revisionCount sesi tercatat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            OutlinedButton(onClick = { revisionCount = (revisionCount - 1).coerceAtLeast(0); prefs.edit().putInt("quran_revision_count", revisionCount).apply() }, enabled = revisionCount > 0) { Text("−") }
                            Button(onClick = { revisionCount++; prefs.edit().putInt("quran_revision_count", revisionCount).apply() }, colors = ButtonDefaults.buttonColors(containerColor = Green)) { Text("+ Murajaah") }
                        }
                    }
                }
            }

            }
            if (selectedTab == "Semua" || selectedTab == "Progres") {
            item {
                Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Tracker Puasa Sunnah", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Catat jumlah hari puasa yang telah dijalani. Ini hanya catatan pribadi; sesuaikan dengan kondisi dan tuntunan yang kamu ikuti.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Hari tercatat", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$fastingDays hari", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Green)
                            }
                            OutlinedButton(onClick = { fastingDays = (fastingDays - 1).coerceAtLeast(0); prefs.edit().putInt("fasting_days", fastingDays).apply() }, enabled = fastingDays > 0) { Text("−") }
                            Button(onClick = { fastingDays++; prefs.edit().putInt("fasting_days", fastingDays).apply() }, colors = ButtonDefaults.buttonColors(containerColor = Green)) { Text("+ Catat") }
                        }
                        Text("Tracker ini tidak menentukan hari yang dianjurkan; periksa kalender dan panduan agama tepercaya.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            }
            if (selectedTab == "Semua" || selectedTab == "Progres") {
            item {
                Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Catatan Kebaikan & Sedekah", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Catat tindakan baik atau sedekah yang sudah dilakukan tanpa harus mencantumkan nominal.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Kebaikan tercatat", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$sadaqahCount kali", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Green)
                            }
                            OutlinedButton(onClick = { sadaqahCount = (sadaqahCount - 1).coerceAtLeast(0); prefs.edit().putInt("sadaqah_count", sadaqahCount).apply() }, enabled = sadaqahCount > 0) { Text("−") }
                            Button(onClick = { sadaqahCount++; prefs.edit().putInt("sadaqah_count", sadaqahCount).apply() }, colors = ButtonDefaults.buttonColors(containerColor = Green)) { Text("+ Catat") }
                        }
                    }
                }
            }

            }
            if (selectedTab == "Semua" || selectedTab == "Progres") {
            item {
                Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Jurnal Syukur Hari Ini", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Tuliskan hal-hal yang kamu syukuri hari ini sebagai refleksi pribadi.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(value = gratitudeNote, onValueChange = { gratitudeNote = it; prefs.edit().putString("gratitude_note_$day", it).apply() }, modifier = Modifier.fillMaxWidth(), minLines = 3, placeholder = { Text("Hari ini aku bersyukur karena…") }, label = { Text("Jurnal syukur") })
                        Text("Tersimpan lokal untuk tanggal $day", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            }
            if (selectedTab == "Semua" || selectedTab == "Progres") {
            item {
                Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Ekspor Ringkasan Pribadi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Bagikan atau simpan ringkasan progres, target khatam, hafalan, murajaah, catatan kajian, dan jurnal syukur.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = {
                            val summary = buildString {
                                appendLine("RINGKASAN AMALAN PRIBADI")
                                appendLine("Tanggal: $day")
                                appendLine("Amalan hari ini: $done/${items.size}")
                                appendLine("Konsistensi tercatat: $streak hari")
                                appendLine("Progres khatam: $pagesRead/604 halaman")
                                appendLine("Target bacaan: $dailyPageGoal halaman/hari")
                                appendLine("Surah dihafal (target pribadi): $memorizedSurahs/114")
                                appendLine("Sesi murajaah: $revisionCount")
                                appendLine("Hari puasa tercatat: $fastingDays")
                                appendLine("Kebaikan/sedekah tercatat: $sadaqahCount")
                                appendLine("Dzikir terpilih: $selectedDhikr; hitungan saat ini: $count")
                                appendLine("\nCATATAN KAJIAN\n$studyNotes")
                                appendLine("\nJURNAL SYUKUR ($day)\n$gratitudeNote")
                                appendLine("\nDibuat dari aplikasi Al-Qur'an Offline. Data ini bersifat pribadi.")
                            }
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Ringkasan Amalan - $day")
                                putExtra(Intent.EXTRA_TEXT, summary)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Ekspor ringkasan"))
                        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Green)) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(Modifier.size(8.dp))
                            Text("Bagikan / Ekspor Ringkasan")
                        }
                    }
                }
            }

            }
            if (true) {
            item {
                Text("Catatan: daftar ini adalah pengingat pribadi, bukan penilaian kesalehan. Target halaman adalah perkiraan untuk mushaf 604 halaman dan dapat disesuaikan.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp))
            }
            }
        }
    }
}
