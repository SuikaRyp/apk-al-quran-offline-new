package com.alquran.offline.ui.screens.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.hardware.GeomagneticField
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.icu.util.IslamicCalendar
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.core.content.ContextCompat
import com.alquran.offline.ui.components.AppTopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun IslamicToolsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    var azimuth by remember { mutableFloatStateOf(0f) }
    var location by remember { mutableStateOf<Location?>(null) }
    var locationMessage by remember { mutableStateOf("Izinkan lokasi untuk menghitung arah kiblat dari posisi kamu.") }
    var audioUri by remember { mutableStateOf<Uri?>(null) }
    var audioName by remember { mutableStateOf("Belum ada audio dipilih") }
    var isPlaying by remember { mutableStateOf(false) }
    var audioError by remember { mutableStateOf<String?>(null) }
    val player = remember { mutableStateOf<MediaPlayer?>(null) }

    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            audioUri = uri
            audioName = uri.lastPathSegment?.substringAfterLast('/')?.take(48) ?: "Audio murottal dipilih"
            audioError = null
            try { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) { }
            player.value?.release()
            player.value = null
            isPlaying = false
        }
    }
    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true || grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            location = findLastLocation(context)
            locationMessage = if (location == null) "Lokasi belum tersedia. Aktifkan GPS lalu coba lagi." else "Lokasi didapatkan dari perangkat."
        } else locationMessage = "Izin lokasi ditolak. Arah kiblat belum bisa dihitung."
    }

    DisposableEffect(context, location) {
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val matrix = FloatArray(9)
                val orientation = FloatArray(3)
                SensorManager.getRotationMatrixFromVector(matrix, event.values)
                SensorManager.getOrientation(matrix, orientation)
                val magneticHeading = (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f
                val currentLocation = location
                val declination = if (currentLocation != null) {
                    GeomagneticField(
                        currentLocation.latitude.toFloat(), currentLocation.longitude.toFloat(),
                        currentLocation.altitude.toFloat(), System.currentTimeMillis()
                    ).declination
                } else 0f
                val trueHeading = (magneticHeading + declination + 360f) % 360f
                val delta = ((trueHeading - azimuth + 540f) % 360f) - 180f
                azimuth = (azimuth + delta * 0.22f + 360f) % 360f
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        if (sensor != null) manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        onDispose {
            manager.unregisterListener(listener)
            player.value?.release()
            player.value = null
        }
    }

    val hijri = remember { getHijriDate() }
    val qiblaBearing = location?.let { calculateQiblaBearing(it.latitude, it.longitude) }
    val relativeQibla = qiblaBearing?.let { (it - azimuth + 360f) % 360f }

    Scaffold(topBar = { AppTopBar(title = "Fitur Islami", onBackClick = onBackClick) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Kalender Hijriah", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Text(hijri.first, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Text(hijri.second, style = MaterialTheme.typography.bodyMedium)
                    HorizontalDivider()
                    Text("Hari penting Islam (tanggal Hijriah)", fontWeight = FontWeight.SemiBold)
                    listOf("1 Muharram — Tahun Baru Hijriah", "10 Muharram — Hari Asyura", "12 Rabiulawal — Maulid Nabi (tanggal yang umum diperingati)", "1 Ramadan — Awal Ramadan", "1 Syawal — Idulfitri", "10 Zulhijah — Iduladha").forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                    Text("Catatan: tanggal Hijriah dapat berbeda menurut rukyat dan penetapan otoritas setempat.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Card(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Explore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Kompas Kiblat", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    val compassPrimary = MaterialTheme.colorScheme.primary
                    val compassSurfaceVariant = MaterialTheme.colorScheme.surfaceVariant
                    val compassOutline = MaterialTheme.colorScheme.outline
                    val compassOnSurface = MaterialTheme.colorScheme.onSurfaceVariant
                    val compassSurface = MaterialTheme.colorScheme.surface
                    Box(Modifier.size(220.dp), contentAlignment = Alignment.Center) {
                        Canvas(Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val radius = size.minDimension * 0.46f
                            drawCircle(color = compassSurfaceVariant, radius = radius, center = center)
                            drawCircle(color = compassOutline.copy(alpha = 0.65f), radius = radius, center = center, style = Stroke(width = 2.dp.toPx()))
                            for (tick in 0 until 36) {
                                rotate(tick * 10f, pivot = center) {
                                    val outer = Offset(center.x, center.y - radius + 8.dp.toPx())
                                    val inner = Offset(center.x, center.y - radius + if (tick % 3 == 0) 19.dp.toPx() else 14.dp.toPx())
                                    drawLine(compassOnSurface.copy(alpha = 0.7f), outer, inner, strokeWidth = if (tick % 3 == 0) 2.dp.toPx() else 1.dp.toPx())
                                }
                            }
                            if (relativeQibla != null) {
                                rotate(relativeQibla, pivot = center) {
                                    val tip = Offset(center.x, center.y - radius * 0.78f)
                                    val baseLeft = Offset(center.x - 8.dp.toPx(), center.y + 4.dp.toPx())
                                    val baseRight = Offset(center.x + 8.dp.toPx(), center.y + 4.dp.toPx())
                                    drawLine(compassPrimary, center, tip, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
                                    drawLine(compassPrimary, tip, baseLeft, strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                                    drawLine(compassPrimary, tip, baseRight, strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                                }
                            }
                            drawCircle(compassPrimary, radius = 5.dp.toPx(), center = center)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("N", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 112.dp))
                        }
                        if (relativeQibla != null) {
                            Surface(shape = RoundedCornerShape(12.dp), color = compassSurface, tonalElevation = 4.dp) {
                                Text("🕋 ${qiblaBearing?.toInt()}°", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text("Arah perangkat: ${azimuth.toInt()}° dari utara", style = MaterialTheme.typography.bodyMedium)
                    Text(if (qiblaBearing == null) locationMessage else "Arah kiblat: ${qiblaBearing.toInt()}° dari utara. Putar HP hingga penunjuk mengarah ke kiblat.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = {
                        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        if (fine || coarse) {
                            location = findLastLocation(context)
                            locationMessage = if (location == null) "Lokasi belum tersedia. Aktifkan GPS lalu coba lagi." else "Lokasi didapatkan dari perangkat."
                        } else locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.LocationOn, null); Spacer(Modifier.width(8.dp)); Text("Gunakan lokasi perangkat") }
                    Text("Kompas memakai sensor rotasi dan koreksi deklinasi magnetik jika lokasi tersedia. Jauhkan HP dari magnet/logam dan gerakkan HP membentuk angka 8 untuk kalibrasi. Akurasi tetap bergantung pada sensor perangkat.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Card(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AudioFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Murottal Offline", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Text("Pilih file audio yang sudah tersimpan di HP (MP3, M4A, atau format yang didukung perangkat).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(audioName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    OutlinedButton(onClick = { audioPicker.launch(arrayOf("audio/*")) }, modifier = Modifier.fillMaxWidth()) { Text("Pilih audio dari perangkat") }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(modifier = Modifier.weight(1f), enabled = audioUri != null, onClick = {
                            try {
                                if (player.value == null) {
                                    player.value = MediaPlayer().apply {
                                        setDataSource(context, audioUri!!)
                                        setOnCompletionListener { isPlaying = false }
                                        prepare()
                                    }
                                }
                                if (player.value?.isPlaying == true) { player.value?.pause(); isPlaying = false }
                                else { player.value?.start(); isPlaying = true }
                                audioError = null
                            } catch (e: Exception) { audioError = "Audio tidak dapat diputar. Coba pilih file audio lain."; isPlaying = false }
                        }) { Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null); Spacer(Modifier.width(4.dp)); Text(if (isPlaying) "Jeda" else "Putar") }
                        OutlinedButton(modifier = Modifier.weight(1f), enabled = player.value != null, onClick = {
                            try { player.value?.pause(); player.value?.seekTo(0); isPlaying = false } catch (_: Exception) { }
                        }) { Icon(Icons.Default.Stop, contentDescription = null); Spacer(Modifier.width(4.dp)); Text("Berhenti") }
                    }
                    player.value?.let { media ->
                        val duration = remember(media) { media.duration.coerceAtLeast(0) }
                        var position by remember(media) { mutableIntStateOf(0) }
                        LaunchedEffect(media, isPlaying) {
                            while (isPlaying) { position = try { media.currentPosition } catch (_: Exception) { 0 }; kotlinx.coroutines.delay(500) }
                        }
                        LinearProgressIndicator(progress = if (duration > 0) position.toFloat() / duration else 0f, modifier = Modifier.fillMaxWidth())
                        Text("${formatMs(position)} / ${formatMs(duration)}", style = MaterialTheme.typography.labelSmall)
                        Slider(value = position.toFloat().coerceIn(0f, duration.toFloat().coerceAtLeast(1f)), onValueChange = { position = it.toInt() }, onValueChangeFinished = { try { media.seekTo(position) } catch (_: Exception) { } }, valueRange = 0f..duration.toFloat().coerceAtLeast(1f))
                    }
                    audioError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

private fun getHijriDate(): Pair<String, String> {
    return try {
        if (Build.VERSION.SDK_INT >= 24) {
            val c = IslamicCalendar()
            val months = listOf("Muharram", "Safar", "Rabiulawal", "Rabiulakhir", "Jumadilawal", "Jumadilakhir", "Rajab", "Syakban", "Ramadan", "Syawal", "Zulkaidah", "Zulhijah")
            val month = c.get(android.icu.util.Calendar.MONTH).coerceIn(0, 11)
            val day = c.get(android.icu.util.Calendar.DAY_OF_MONTH)
            val year = c.get(android.icu.util.Calendar.YEAR)
            val date = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID")).format(Date())
            "$day ${months[month]} $year H" to date
        } else "Kalender Hijriah tersedia di Android 7+" to SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID")).format(Date())
    } catch (_: Exception) { "Tanggal Hijriah tidak tersedia" to "Periksa tanggal perangkat" }
}

private fun findLastLocation(context: Context): Location? {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!fine && !coarse) return null
    return try {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            .mapNotNull { provider -> try { manager.getLastKnownLocation(provider) } catch (_: SecurityException) { null } }
            .maxByOrNull { it.time }
    } catch (_: Exception) { null }
}

private fun calculateQiblaBearing(latitude: Double, longitude: Double): Float {
    val kaabaLat = Math.toRadians(21.4225)
    val kaabaLon = Math.toRadians(39.8262)
    val lat = Math.toRadians(latitude)
    val lon = Math.toRadians(longitude)
    val deltaLon = kaabaLon - lon
    val y = sin(deltaLon)
    val x = cos(lat) * kotlin.math.tan(kaabaLat) - sin(lat) * cos(deltaLon)
    return ((Math.toDegrees(atan2(y, x)) + 360.0) % 360.0).toFloat()
}

private fun formatMs(ms: Int): String = "%02d:%02d".format(ms / 60000, (ms / 1000) % 60)
