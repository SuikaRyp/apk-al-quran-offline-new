package com.alquran.offline.ui.components

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.alquran.offline.ui.theme.IslamicCream
import com.alquran.offline.ui.theme.IslamicDeepGreen
import com.alquran.offline.ui.theme.IslamicGold
import com.alquran.offline.ui.theme.IslamicGreen

sealed class BottomTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomTab("home", "Beranda", Icons.Filled.Home, Icons.Outlined.Home)
    object Quran : BottomTab("surah_list", "Al-Qur'an", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook)
    object Hadith : BottomTab("hadith_list?initialId=0", "Hadits", Icons.Filled.FormatQuote, Icons.Outlined.FormatQuote)
    object Bookmark : BottomTab("bookmark", "Bookmark", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder)

    companion object {
        val tabs = listOf(Home, Quran, Hadith, Bookmark)
    }
}

@Composable
fun AppBottomBar(
    currentRoute: String?,
    onTabSelected: (String) -> Unit
) {
    // Area navigasi sistem ikut hijau tua selama bottom bar tampil.
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val prevColor = window?.navigationBarColor
        val prevLight = controller?.isAppearanceLightNavigationBars
        window?.navigationBarColor = IslamicDeepGreen.toArgb()
        controller?.isAppearanceLightNavigationBars = false
        onDispose {
            if (window != null && prevColor != null) window.navigationBarColor = prevColor
            if (controller != null && prevLight != null) controller.isAppearanceLightNavigationBars = prevLight
        }
    }

    Surface(
        color = IslamicDeepGreen,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 12.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            BottomTab.tabs.forEach { tab ->
                val isSelected = when (tab) {
                    is BottomTab.Home -> currentRoute == "home"
                    is BottomTab.Quran -> currentRoute == "surah_list" || currentRoute == "juz_list"
                    is BottomTab.Hadith -> currentRoute?.startsWith("hadith_list") == true
                    is BottomTab.Bookmark -> currentRoute == "bookmark"
                }

                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (!isSelected || (tab is BottomTab.Quran && currentRoute == "juz_list")) {
                            onTabSelected(tab.route)
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.label
                        )
                    },
                    label = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = IslamicGreen,
                        unselectedIconColor = IslamicCream.copy(alpha = 0.7f),
                        unselectedTextColor = IslamicCream.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}
