package com.alquran.offline.ui.screens.settings

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alquran.offline.azan.AzanTones

/** Pemutar singkat untuk mencoba nada azan sebelum dipilih. */
private class AzanTonePreviewPlayer(private val context: Context) {
    private var player: MediaPlayer? = null

    fun play(toneId: String, onFinished: () -> Unit) {
        stop()
        try {
            player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, AzanTones.resolveUri(context, toneId))
                setOnCompletionListener {
                    this@AzanTonePreviewPlayer.stop()
                    onFinished()
                }
                prepare()
                start()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            stop()
            onFinished()
        }
    }

    fun stop() {
        try {
            player?.stop()
        } catch (ignored: Throwable) {
        }
        try {
            player?.release()
        } catch (ignored: Throwable) {
        }
        player = null
    }
}

@Composable
fun AzanToneDialog(
    currentToneId: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val tones = remember { AzanTones.availableTones(context) }
    var selectedId by remember { mutableStateOf(AzanTones.effectiveId(context, currentToneId)) }
    var playingId by remember { mutableStateOf<String?>(null) }
    val previewPlayer = remember { AzanTonePreviewPlayer(context.applicationContext) }

    DisposableEffect(Unit) {
        onDispose { previewPlayer.stop() }
    }

    AlertDialog(
        onDismissRequest = {
            previewPlayer.stop()
            onDismiss()
        },
        title = { Text("Nada Dering Azan") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                tones.forEach { tone ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedId = tone.id },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedId == tone.id,
                            onClick = { selectedId = tone.id }
                        )
                        Text(
                            text = tone.label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )
                        TextButton(
                            onClick = {
                                if (playingId == tone.id) {
                                    previewPlayer.stop()
                                    playingId = null
                                } else {
                                    playingId = tone.id
                                    previewPlayer.play(tone.id) { playingId = null }
                                }
                            }
                        ) {
                            Text(if (playingId == tone.id) "Stop" else "Putar")
                        }
                    }
                }
                if (tones.size == 1) {
                    Text(
                        text = "Belum ada file mp3 azan di aplikasi ini, jadi hanya nada alarm bawaan HP yang tersedia.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    previewPlayer.stop()
                    onSelect(selectedId)
                    onDismiss()
                }
            ) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    previewPlayer.stop()
                    onDismiss()
                }
            ) { Text("Batal") }
        }
    )
}
