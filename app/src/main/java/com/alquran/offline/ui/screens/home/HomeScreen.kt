package com.alquran.offline.ui.screens.home

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.alquran.offline.ui.components.DailyMotivationCard
import com.alquran.offline.ui.components.HeroMosqueArt
import com.alquran.offline.ui.components.HexBookLogo
import com.alquran.offline.ui.components.QuranBookArt
import com.alquran.offline.ui.theme.HeroNightTop
import com.alquran.offline.ui.theme.IslamicCream
import com.alquran.offline.ui.theme.IslamicDeepGreen
import com.alquran.offline.ui.theme.IslamicGold
import com.alquran.offline.ui.theme.IslamicGreen

private val ScreenPadding = 20.dp

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSurahList: () -> Unit,
    onNavigateToJuzList: () -> Unit,
    onNavigateToHadith: () -> Unit,
    onNavigateToHadithDetail: (Int) -> Unit,
    onNavigateToPrayerList: () -> Unit,
    onNavigateToDailyPrayer: () -> Unit = {},
    onNavigateToAsmaulHusna: () -> Unit = {},
    onNavigateToAmalan: () -> Unit = {},
    onNavigateToMedia: () -> Unit = {},
    onNavigateToBookmark: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToReader: (Int, Int) -> Unit
) {
    val lastRead by viewModel.lastRead.collectAsState()
    val todayHadith by viewModel.todayHadith.collectAsState()
    val dailyMotivation by viewModel.dailyMotivation.collectAsState()

    // Status bar menyatu dengan hero malam; dikembalikan saat keluar dari beranda.
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val prevColor = window?.statusBarColor
        val prevLight = controller?.isAppearanceLightStatusBars
        window?.statusBarColor = HeroNightTop.toArgb()
        controller?.isAppearanceLightStatusBars = false
        onDispose {
            if (window != null && prevColor != null) window.statusBarColor = prevColor
            if (controller != null && prevLight != null) controller.isAppearanceLightStatusBars = prevLight
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(durationMillis = 420)) +
                slideInVertically(
                    animationSpec = tween(durationMillis = 420),
                    initialOffsetY = { fullHeight -> fullHeight / 24 }
                ),
            modifier = Modifier.fillMaxSize()
        ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Hero: logo, judul, tombol cari & pengaturan, ilustrasi masjid
            item {
                val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                val heroHeight = 250.dp + topInset
                Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(heroHeight)
                        .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                ) {
                    HeroMosqueArt(modifier = Modifier.matchParentSize())

                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = topInset + 12.dp, end = ScreenPadding),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HeroIconButton(Icons.Default.Search, "Pencarian", onNavigateToSearch)
                        HeroIconButton(Icons.Default.Settings, "Pengaturan", onNavigateToSettings)
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = ScreenPadding, bottom = 66.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HexBookLogo(boxSize = 72.dp)
                        Column {
                            Text(
                                text = "Al-Qur'an",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                color = IslamicCream
                            )
                            Text(
                                text = "Bersama Al-Qur'an,\nmenuju hidup yang lebih baik",
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = IslamicCream.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = heroHeight - 40.dp)
                            .padding(horizontal = ScreenPadding)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToReader(lastRead.surahId, lastRead.verseId) },
                            shape = RoundedCornerShape(24.dp),
                            color = IslamicDeepGreen,
                            shadowElevation = 8.dp
                        ) {
                            Box(
                                modifier = Modifier.background(
                                    Brush.linearGradient(listOf(IslamicDeepGreen, IslamicGreen))
                                )
                            ) {
                                QuranBookArt(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 8.dp)
                                )
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                            contentDescription = null,
                                            tint = IslamicGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Lanjutan Membaca",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = IslamicCream
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = lastRead.surahNameLatin,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 26.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Surah ke-${lastRead.surahId} · Ayat ${lastRead.verseId}",
                                        fontSize = 13.sp,
                                        color = IslamicCream.copy(alpha = 0.9f)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFF2E8B6E),
                                        modifier = Modifier.clickable {
                                            onNavigateToReader(lastRead.surahId, lastRead.verseId)
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "Buka Sekarang",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }

            // Menu Utama
            item {
                Column(
                    modifier = Modifier.padding(horizontal = ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Menu Utama",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(1.5.dp)
                                .background(IslamicGold)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Row(
                            modifier = Modifier.clickable(onClick = onNavigateToSurahList),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Lihat Semua",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    MenuRow(
                        left = MenuItem("Al-Qur'an", "114 Surah", Icons.AutoMirrored.Filled.MenuBook, onNavigateToSurahList),
                        right = MenuItem("Koleksi Hadits", "38.100+ Hadits", Icons.Default.FormatQuote, onNavigateToHadith)
                    )
                    MenuRow(
                        left = MenuItem("Bacaan Sholat", "Panduan Sahih", Icons.Default.Mosque, onNavigateToPrayerList, gold = true),
                        right = MenuItem("Doa Harian", "30+ Doa Shahih", Icons.Default.AutoStories, onNavigateToDailyPrayer)
                    )
                    MenuRow(
                        left = MenuItem("Asmaul Husna", "99 Nama Allah", Icons.Default.Star, onNavigateToAsmaulHusna),
                        right = MenuItem("Al-Qur'an 30 Juz", "Navigasi Juz", Icons.Default.Explore, onNavigateToJuzList)
                    )
                    MenuRow(
                        left = MenuItem("Tasbih Digital", "Dzikir & target", Icons.Default.Explore, onNavigateToAmalan, gold = true),
                        right = MenuItem("Amalan Harian", "Pantau ibadah", Icons.Default.CalendarMonth, onNavigateToAmalan)
                    )
                    MenuRow(
                        left = MenuItem("Audio Islami", "Azan & bacaan offline", Icons.Default.AutoStories, onNavigateToMedia),
                        right = MenuItem("Video Islami", "Koleksi pilihan", Icons.Default.Mosque, onNavigateToMedia, gold = true)
                    )
                }
            }

            // Hadits Hari Ini
            todayHadith?.let { hadith ->
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = ScreenPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Hadits Hari Ini",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = IslamicDeepGreen
                            ) {
                                Text(
                                    text = "${hadith.kitab} #${hadith.nomor}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = IslamicCream,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = IslamicDeepGreen,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToHadithDetail(hadith.id) }
                        ) {
                            Box(
                                modifier = Modifier.background(
                                    Brush.linearGradient(listOf(IslamicDeepGreen, Color(0xFF1A5C49)))
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FormatQuote,
                                        contentDescription = null,
                                        tint = IslamicGold,
                                        modifier = Modifier.size(30.dp)
                                    )
                                    Text(
                                        text = hadith.teksId,
                                        fontSize = 15.sp,
                                        lineHeight = 23.sp,
                                        color = IslamicCream,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                                contentDescription = null,
                                                tint = IslamicGold,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = hadith.sumber,
                                                fontSize = 12.sp,
                                                color = IslamicCream.copy(alpha = 0.85f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Baca Selengkapnya",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Motivasi Harian
            dailyMotivation?.let { motivation ->
                item {
                    Box(modifier = Modifier.padding(horizontal = ScreenPadding)) {
                        DailyMotivationCard(motivation)
                    }
                }
            }

            // Surah Pilihan
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Surah Pilihan",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = ScreenPadding)
                    )

                    val popularSurahs = listOf(
                        Triple(18, "Al-Kahf", "110 Ayat"),
                        Triple(36, "Yasin", "83 Ayat"),
                        Triple(67, "Al-Mulk", "30 Ayat"),
                        Triple(55, "Ar-Rahman", "78 Ayat"),
                        Triple(56, "Al-Waqi'ah", "96 Ayat")
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = 8.dp)
                    ) {
                        items(items = popularSurahs, key = { it.first }) { (id, name, verses) ->
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .width(132.dp)
                                    .clickable { onNavigateToReader(id, 1) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "$id",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = verses,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
        }
    }
}

@Composable
private fun HeroIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

private class MenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val gold: Boolean = false
)

@Composable
private fun MenuRow(left: MenuItem, right: MenuItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickMenuCard(left, Modifier.weight(1f))
        QuickMenuCard(right, Modifier.weight(1f))
    }
}

@Composable
private fun QuickMenuCard(item: MenuItem, modifier: Modifier = Modifier) {
    // Launcher-style widget: no bulky card, just a softly animated icon and clear label.
    // A tiny stagger keeps the movement organic rather than making every icon bounce together.
    val motion = rememberInfiniteTransition(label = "${item.title}WidgetMotion")
    val floatY by motion.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1900 + (item.title.length * 45),
                delayMillis = (item.title.length * 73) % 520
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "${item.title}Float"
    )
    val pulse by motion.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200 + (item.title.length * 35)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "${item.title}Pulse"
    )
    val iconTint = if (item.gold) Color(0xFF9A7A32) else IslamicGreen
    val iconBackground = if (item.gold) Color(0xFFF4EBD3) else Color(0xFFE3F0E9)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = item.onClick)
            .padding(horizontal = 4.dp, vertical = 9.dp)
            .animateContentSize(animationSpec = tween(220)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(y = floatY.dp)
                .size(54.dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            // Thin inset ring gives the icon a tactile, polished widget appearance.
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBackground.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(25.dp)
                )
            }
        }
        Text(
            text = item.title,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = item.subtitle,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
