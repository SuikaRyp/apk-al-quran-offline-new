package com.alquran.offline.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.json.JSONObject
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val PREFS_NAME = "amalan_tracker"
private const val BACKUP_VERSION = 1

@Composable
fun BackupRestoreScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            val result = runCatching { writeBackup(context, uri) }
            scope.launch { snackbar.showSnackbar(if (result.isSuccess) "Backup berhasil disimpan" else "Backup gagal disimpan. Coba lokasi lain.") }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val result = runCatching { restoreBackup(context, uri) }
            scope.launch {
                snackbar.showSnackbar(
                    if (result.isSuccess) "Data backup berhasil dipulihkan" else "File tidak valid atau tidak dapat dibaca"
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali") }
                Column {
                    Text("Backup & Restore", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Kelola salinan data ibadah", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Data yang dicadangkan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("• Hitungan tasbih dan target dzikir\n• Target serta progres khatam\n• Catatan kajian dan jurnal syukur\n• Target hafalan, murajaah, puasa, dan sedekah\n• Checklist amalan harian", style = MaterialTheme.typography.bodyMedium)
                    Text("File disimpan di lokasi yang kamu pilih. Backup tidak dikirim ke server.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Button(
                onClick = {
                    val name = "suika-amalan-${SimpleDateFormat("yyyyMMdd-HHmm", Locale.ROOT).format(Date())}.json"
                    exportLauncher.launch(name)
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text("Ekspor Backup (.json)")
            }
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json", "text/*", "application/octet-stream")) },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.CloudDownload, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text("Pulihkan dari File")
            }
            Text("Penting: pemulihan akan mengganti nilai yang memiliki kunci sama. Simpan backup terbaru sebelum memulihkan file lama.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun writeBackup(context: Context, uri: Uri) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val data = JSONObject()
    prefs.all.forEach { (key, value) ->
        when (value) {
            is String -> data.put(key, JSONObject().put("type", "string").put("value", value))
            is Boolean -> data.put(key, JSONObject().put("type", "boolean").put("value", value))
            is Int -> data.put(key, JSONObject().put("type", "int").put("value", value))
            is Long -> data.put(key, JSONObject().put("type", "long").put("value", value))
            is Float -> data.put(key, JSONObject().put("type", "float").put("value", value.toDouble()))
        }
    }
    val root = JSONObject().put("app", "Suika Al-Qur'an").put("backupVersion", BACKUP_VERSION).put("createdAt", System.currentTimeMillis()).put("preferences", data)
    context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(root.toString(2)) }
        ?: error("Tidak bisa membuka file tujuan")
}

private fun restoreBackup(context: Context, uri: Uri) {
    val text = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
        ?: error("Tidak bisa membaca file")
    val root = JSONObject(text)
    require(root.optString("app") == "Suika Al-Qur'an" && root.optInt("backupVersion", -1) == BACKUP_VERSION) { "Format backup tidak didukung" }
    val data = root.getJSONObject("preferences")
    val editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
    val keys = data.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        val item = data.getJSONObject(key)
        when (item.getString("type")) {
            "string" -> editor.putString(key, item.getString("value"))
            "boolean" -> editor.putBoolean(key, item.getBoolean("value"))
            "int" -> editor.putInt(key, item.getInt("value"))
            "long" -> editor.putLong(key, item.getLong("value"))
            "float" -> editor.putFloat(key, item.getDouble("value").toFloat())
            else -> Unit
        }
    }
    editor.apply()
}
