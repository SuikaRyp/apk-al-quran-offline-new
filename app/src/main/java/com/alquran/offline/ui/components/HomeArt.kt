package com.alquran.offline.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alquran.offline.ui.theme.HeroNightMid
import com.alquran.offline.ui.theme.HeroNightTop
import com.alquran.offline.ui.theme.HeroSilhouette
import com.alquran.offline.ui.theme.IslamicCream
import com.alquran.offline.ui.theme.IslamicDeepGreen
import com.alquran.offline.ui.theme.IslamicGold
import com.alquran.offline.ui.theme.IslamicGreen
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val starSpots = listOf(
    Triple(0.08f, 0.10f, 1.4f), Triple(0.20f, 0.22f, 1.0f), Triple(0.33f, 0.08f, 1.2f),
    Triple(0.44f, 0.18f, 0.9f), Triple(0.62f, 0.07f, 1.3f), Triple(0.74f, 0.16f, 1.0f),
    Triple(0.88f, 0.10f, 1.5f), Triple(0.94f, 0.26f, 0.9f), Triple(0.14f, 0.38f, 0.9f),
    Triple(0.50f, 0.34f, 0.8f)
)

/** Siluet masjid malam + bulan sabit, digambar lewat Canvas (100% offline, ringan). */
@Composable
fun HeroMosqueArt(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(HeroNightTop, HeroNightMid, Color(0xFF2B4B3F))
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(IslamicGold.copy(alpha = 0.45f), Color.Transparent),
                center = Offset(w * 0.80f, h * 0.78f),
                radius = w * 0.55f
            )
        )

        starSpots.forEach { (x, y, r) ->
            drawCircle(Color.White.copy(alpha = 0.7f), radius = r.dp.toPx(), center = Offset(x * w, y * h))
        }

        // Bulan sabit
        val c = Offset(w * 0.56f, h * 0.27f)
        val r = w * 0.045f
        val moon = Path.combine(
            PathOperation.Difference,
            Path().apply { addOval(Rect(center = c, radius = r)) },
            Path().apply { addOval(Rect(center = c + Offset(r * 0.55f, -r * 0.2f), radius = r * 0.9f)) }
        )
        drawPath(moon, Color(0xFFF3E3B8))

        // Bukit jauh
        val far = Path().apply {
            moveTo(0f, h); lineTo(0f, h * 0.80f)
            cubicTo(w * 0.15f, h * 0.70f, w * 0.30f, h * 0.82f, w * 0.50f, h * 0.76f)
            lineTo(w, h * 0.72f); lineTo(w, h); close()
        }
        drawPath(far, Color(0xFF123229))

        // Masjid
        val sil = HeroSilhouette
        val win = IslamicGold.copy(alpha = 0.85f)
        drawRect(sil, Offset(w * 0.55f, h * 0.80f), Size(w * 0.45f, h * 0.20f))
        minaret(w * 0.60f, h * 0.84f, h * 0.30f, w * 0.03f, sil, win)
        minaret(w * 0.97f, h * 0.84f, h * 0.44f, w * 0.026f, sil, win)
        dome(w * 0.90f, h * 0.80f, w * 0.035f, sil)
        dome(w * 0.68f, h * 0.80f, w * 0.03f, sil)
        drawRect(sil, Offset(w * 0.80f - w * 0.098f, h * 0.72f), Size(w * 0.196f, h * 0.28f))
        dome(w * 0.80f, h * 0.72f, w * 0.075f, sil)
        for (i in -1..1) {
            drawRoundRect(
                win,
                Offset(w * 0.80f + i * w * 0.05f - w * 0.008f, h * 0.76f),
                Size(w * 0.016f, h * 0.06f),
                CornerRadius(w * 0.008f)
            )
        }

        // Tanah depan
        val near = Path().apply {
            moveTo(0f, h); lineTo(0f, h * 0.90f)
            cubicTo(w * 0.25f, h * 0.84f, w * 0.45f, h * 0.94f, w * 0.7f, h * 0.88f)
            lineTo(w, h * 0.90f); lineTo(w, h); close()
        }
        drawPath(near, sil)
    }
}

private fun DrawScope.minaret(cx: Float, baseY: Float, topY: Float, width: Float, color: Color, windowColor: Color) {
    val capBase = topY + width * 2.2f
    drawRect(color, Offset(cx - width / 2, capBase), Size(width, baseY - capBase))
    drawRect(color, Offset(cx - width * 0.85f, capBase), Size(width * 1.7f, width * 0.35f))
    val cap = Path().apply {
        moveTo(cx - width * 0.8f, capBase); lineTo(cx, topY); lineTo(cx + width * 0.8f, capBase); close()
    }
    drawPath(cap, color)
    drawRect(windowColor, Offset(cx - width * 0.1f, capBase + width * 0.9f), Size(width * 0.2f, width * 0.9f))
}

private fun DrawScope.dome(cx: Float, baseY: Float, r: Float, color: Color) {
    val p = Path().apply {
        moveTo(cx - r, baseY)
        cubicTo(cx - r, baseY - r * 1.5f, cx - r * 0.3f, baseY - r * 1.6f, cx, baseY - r * 2.1f)
        cubicTo(cx + r * 0.3f, baseY - r * 1.6f, cx + r, baseY - r * 1.5f, cx + r, baseY)
        close()
    }
    drawPath(p, color)
    drawLine(color, Offset(cx, baseY - r * 2.1f), Offset(cx, baseY - r * 2.5f), strokeWidth = 2.dp.toPx())
}

/** Ilustrasi mushaf bertema hijau-emas untuk kartu "Lanjutan Membaca". */
@Composable
fun QuranBookArt(modifier: Modifier = Modifier, boxSize: Dp = 130.dp) {
    Canvas(modifier = modifier.size(boxSize)) {
        rotate(degrees = -18f, pivot = center) {
            val bw = size.width * 0.66f
            val bh = size.height * 0.86f
            val tl = Offset((size.width - bw) / 2f, (size.height - bh) / 2f)
            val corner = CornerRadius(6.dp.toPx())
            drawRoundRect(IslamicCream, tl + Offset(7.dp.toPx(), 5.dp.toPx()), Size(bw, bh), corner)
            drawRoundRect(
                Brush.linearGradient(listOf(Color(0xFF0F4A3C), Color(0xFF07281F))),
                tl, Size(bw, bh), corner
            )
            drawRoundRect(
                IslamicGold, tl + Offset(7.dp.toPx(), 7.dp.toPx()),
                Size(bw - 14.dp.toPx(), bh - 14.dp.toPx()), CornerRadius(4.dp.toPx()),
                style = Stroke(1.5.dp.toPx())
            )
            val mid = tl + Offset(bw / 2f, bh / 2f)
            val d = bw * 0.26f
            val diamond = Path().apply {
                moveTo(mid.x, mid.y - d); lineTo(mid.x + d * 0.8f, mid.y)
                lineTo(mid.x, mid.y + d); lineTo(mid.x - d * 0.8f, mid.y); close()
            }
            drawPath(diamond, IslamicGold.copy(alpha = 0.25f))
            drawPath(diamond, IslamicGold, style = Stroke(1.5.dp.toPx()))
            drawCircle(IslamicGold, radius = d * 0.22f, center = mid)
        }
    }
}

/** Logo heksagon bergaris emas dengan ikon buku di tengah. */
@Composable
fun HexBookLogo(modifier: Modifier = Modifier, boxSize: Dp = 76.dp) {
    Box(modifier = modifier.size(boxSize), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(boxSize)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val rad = size.minDimension / 2f - 3.dp.toPx()
            val hex = Path()
            for (i in 0 until 6) {
                val a = (-90.0 + 60.0 * i) * PI / 180.0
                val x = cx + rad * cos(a).toFloat()
                val y = cy + rad * sin(a).toFloat()
                if (i == 0) hex.moveTo(x, y) else hex.lineTo(x, y)
            }
            hex.close()
            drawPath(hex, Brush.verticalGradient(listOf(IslamicGreen, IslamicDeepGreen)))
            drawPath(hex, IslamicGold, style = Stroke(3.dp.toPx()))
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.MenuBook,
            contentDescription = null,
            tint = IslamicCream,
            modifier = Modifier.size(boxSize * 0.42f)
        )
    }
}
