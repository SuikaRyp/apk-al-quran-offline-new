package com.alquran.offline.ui.azan

import android.app.KeyguardManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alquran.offline.azan.AzanAlarmScheduler
import com.alquran.offline.azan.AzanPreferencesRepository
import com.alquran.offline.azan.AzanTones
import com.alquran.offline.azan.PrayerName
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AzanFullScreenActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindowToShowOverEverything()
        acquireWakeLockSafely()

        val prayerNameStr = intent.getStringExtra(AzanAlarmScheduler.EXTRA_PRAYER_NAME)
        val prayer = try {
            prayerNameStr?.let { PrayerName.valueOf(it) }
        } catch (e: Throwable) {
            null
        } ?: PrayerName.DZUHUR
        val triggerMillis = intent.getLongExtra(
            AzanAlarmScheduler.EXTRA_PRAYER_TIME_MILLIS, System.currentTimeMillis()
        )
        val timeText = android.text.format.DateFormat.format("HH:mm", triggerMillis).toString()

        startVibration()
        lifecycleScope.launch {
            val savedTone = try {
                AzanPreferencesRepository(applicationContext).toneNow()
            } catch (e: Throwable) {
                null
            }
            if (!isFinishing && mediaPlayer == null) startSound(savedTone)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                dismissAndFinish()
            }
        })

        setContent {
            AzanScreen(
                prayer = prayer,
                timeText = timeText,
                onDismiss = { dismissAndFinish() },
                onSnooze = { snoozeAndFinish() }
            )
        }
    }

    private fun setupWindowToShowOverEverything() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun acquireWakeLockSafely() {
        try {
            val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
            wakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "alquran:azan_wakelock"
            ).apply { acquire(90_000L) }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun startVibration() {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            val pattern = longArrayOf(0, 700, 400, 700, 400, 700, 800)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * Memutar nada azan pilihan pengguna (Pengaturan -> Nada Dering Azan). Daftar nada &
     * cara menambah mp3 ada di [AzanTones]. Bila tidak ada mp3, jatuh ke nada alarm bawaan HP.
     */
    private fun startSound(savedTone: String?) {
        try {
            val soundUri = AzanTones.resolveUri(this, savedTone)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(this@AzanFullScreenActivity, soundUri)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun stopEverything() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (ignored: Throwable) {
        }
        mediaPlayer = null
        try {
            vibrator?.cancel()
        } catch (ignored: Throwable) {
        }
        try {
            if (wakeLock?.isHeld == true) wakeLock?.release()
        } catch (ignored: Throwable) {
        }
    }

    private fun dismissAndFinish() {
        stopEverything()
        finish()
    }

    private fun snoozeAndFinish() {
        stopEverything()
        try {
            val am = getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
            if (am != null) {
                val triggerAt = System.currentTimeMillis() + 10 * 60 * 1000L
                val prayerNameStr = intent.getStringExtra(AzanAlarmScheduler.EXTRA_PRAYER_NAME)
                val snoozeIntent = android.content.Intent(
                    this, com.alquran.offline.azan.AzanAlarmReceiver::class.java
                ).apply {
                    action = AzanAlarmScheduler.ACTION_AZAN_TRIGGER
                    putExtra(AzanAlarmScheduler.EXTRA_PRAYER_NAME, prayerNameStr)
                    putExtra(AzanAlarmScheduler.EXTRA_PRAYER_TIME_MILLIS, triggerAt)
                }
                val flags = android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) android.app.PendingIntent.FLAG_IMMUTABLE else 0)
                val pi = android.app.PendingIntent.getBroadcast(this, 6500, snoozeIntent, flags)
                try {
                    am.setAlarmClock(android.app.AlarmManager.AlarmClockInfo(triggerAt, pi), pi)
                } catch (security: SecurityException) {
                    androidx.core.app.AlarmManagerCompat.setExactAndAllowWhileIdle(
                        am, android.app.AlarmManager.RTC_WAKEUP, triggerAt, pi
                    )
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        finish()
    }

    override fun onDestroy() {
        stopEverything()
        super.onDestroy()
    }
}

private val reminderTexts = listOf(
    "Yuk, luangkan waktu sebentar untuk sholat, ya 🤍",
    "Sholat dulu, urusan dunia bisa menunggu sebentar kok 😊",
    "Jangan lupa sholat — istirahatkan hati sejenak bersama-Nya 🌙",
    "Sayangi dirimu, sayangi waktumu untuk sholat tepat waktu 🌸",
    "Sebentar saja, temui Rabb-mu dulu, ya 🕊️",
    "Semoga sholatmu jadi penenang hari ini 🤲"
)

@Composable
private fun AzanScreen(
    prayer: PrayerName,
    timeText: String,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "azan_screen")
    val pulse by infinite.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val marquee by infinite.animateFloat(
        initialValue = -18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Reverse),
        label = "marquee"
    )

    var reminderIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3600)
            reminderIndex = (reminderIndex + 1) % reminderTexts.size
        }
    }

    val nightTop = Color(0xFF071913)
    val nightMid = Color(0xFF0F6E4D)
    val duskBottom = Color(0xFFC09228)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(nightTop, nightMid, duskBottom))
            )
    ) {
        AzanMosqueArt(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 28.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "🕌",
                    fontSize = 40.sp,
                    modifier = Modifier.scale(pulse)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "WAKTU SUDAH MENUNJUKKAN PUKUL $timeText",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(x = marquee.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "SAATNYA MELAKSANAKAN SHOLAT",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = prayer.label.uppercase(),
                    color = Color(0xFFFFE9B0),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(pulse)
                )
                Text(
                    text = prayer.labelArab,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnimatedContent(
                targetState = reminderIndex,
                label = "reminder_text",
                transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) }
            ) { idx ->
                Text(
                    text = reminderTexts[idx],
                    color = Color.White,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .background(Color.Black.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "\"Peliharalah sholatmu, terutama sholat wustha\" — QS. Al-Baqarah",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onSnooze,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("🔕 Tunda 10 menit", fontSize = 13.sp, textAlign = TextAlign.Center)
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFC09228),
                            contentColor = Color(0xFF211602)
                        )
                    ) {
                        Text("✅ Aku Segera Sholat", fontSize = 13.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

