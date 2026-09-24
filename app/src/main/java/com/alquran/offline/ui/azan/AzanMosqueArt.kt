package com.alquran.offline.ui.azan

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.random.Random

/**
 * Ornamen dekoratif "malam menuju masjid": langit gelap berbintang, bulan sabit,
 * siluet masjid dengan kubah & dua menara, serta siluet orang berjalan menuju
 * pintu masjid. Semua digambar manual lewat Canvas (bukan gambar/foto) supaya
 * tetap ringan & 100% offline.
 */
@Composable
fun AzanMosqueArt(
    modifier: Modifier = Modifier,
    silhouetteColor: Color = Color(0xFF04140F),
    skyGlowColor: Color = Color(0xFFE5BF62)
) {
    val infinite = rememberInfiniteTransition(label = "mosque_art")

    val twinkle by infinite.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )

    val walkProgress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "walk"
    )

    val legSwing by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "leg_swing"
    )

    val stars = rememberStars()

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // --- Bintang berkelip ---
        stars.forEach { star ->
            drawCircle(
                color = Color.White.copy(alpha = (star.baseAlpha * twinkle).coerceIn(0.15f, 1f)),
                radius = star.radius,
                center = Offset(star.xFraction * w, star.yFraction * h * 0.55f)
            )
        }

        // --- Bulan sabit ---
        val moonCenter = Offset(w * 0.82f, h * 0.14f)
        val moonRadius = w * 0.07f
        drawCircle(color = skyGlowColor.copy(alpha = 0.95f), radius = moonRadius, center = moonCenter)
        drawCircle(
            color = Color(0xFF0D1411),
            radius = moonRadius,
            center = moonCenter.plus(Offset(moonRadius * 0.42f, -moonRadius * 0.12f))
        )

        val groundY = h * 0.86f

        // --- Tanah / jalan setapak ---
        drawLine(
            color = silhouetteColor,
            start = Offset(0f, groundY),
            end = Offset(w, groundY),
            strokeWidth = 6f
        )

        // --- Siluet Masjid (kubah besar + 2 menara) ---
        drawMosqueSilhouette(
            baseX = w * 0.62f,
            groundY = groundY,
            width = w * 0.5f,
            color = silhouetteColor
        )

        // --- Siluet orang berjalan menuju pintu masjid ---
        val startX = w * 0.06f
        val endX = w * 0.52f
        val personX = startX + (endX - startX) * walkProgress
        drawWalkingPerson(
            center = Offset(personX, groundY),
            scale = w * 0.028f,
            legSwing = legSwing,
            color = silhouetteColor
        )
    }
}

private data class StarSpec(val xFraction: Float, val yFraction: Float, val radius: Float, val baseAlpha: Float)

@Composable
private fun rememberStars(): List<StarSpec> {
    return androidx.compose.runtime.remember {
        val rnd = Random(42)
        List(28) {
            StarSpec(
                xFraction = rnd.nextFloat(),
                yFraction = rnd.nextFloat(),
                radius = rnd.nextFloat() * 3.2f + 1f,
                baseAlpha = rnd.nextFloat() * 0.6f + 0.3f
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMosqueSilhouette(
    baseX: Float,
    groundY: Float,
    width: Float,
    color: Color
) {
    val domeRadius = width * 0.22f
    val domeCenterY = groundY - width * 0.55f
    val bodyTop = domeCenterY + domeRadius * 0.35f
    val bodyHeight = groundY - bodyTop
    val bodyWidth = width * 0.42f

    // Badan bangunan utama
    drawRect(
        color = color,
        topLeft = Offset(baseX - bodyWidth / 2f, bodyTop),
        size = androidx.compose.ui.geometry.Size(bodyWidth, bodyHeight)
    )

    // Kubah utama
    val domePath = Path().apply {
        moveTo(baseX - domeRadius, bodyTop)
        cubicTo(
            baseX - domeRadius, domeCenterY - domeRadius * 1.15f,
            baseX + domeRadius, domeCenterY - domeRadius * 1.15f,
            baseX + domeRadius, bodyTop
        )
        close()
    }
    drawPath(domePath, color = color, style = Fill)

    // Puncak kubah (alif kecil di atas kubah)
    drawLine(
        color = color,
        start = Offset(baseX, domeCenterY - domeRadius * 1.15f),
        end = Offset(baseX, domeCenterY - domeRadius * 1.55f),
        strokeWidth = width * 0.012f
    )
    drawCircle(color = color, radius = width * 0.016f, center = Offset(baseX, domeCenterY - domeRadius * 1.62f))

    // Pintu lengkung di tengah badan
    val doorWidth = bodyWidth * 0.28f
    val doorHeight = bodyHeight * 0.42f
    val doorTop = groundY - doorHeight
    val skyHole = Color(0xFF0B1512)
    val doorPath = Path().apply {
        moveTo(baseX - doorWidth / 2f, groundY)
        lineTo(baseX - doorWidth / 2f, doorTop + doorWidth / 2f)
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                left = baseX - doorWidth / 2f,
                top = doorTop,
                right = baseX + doorWidth / 2f,
                bottom = doorTop + doorWidth
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )
        lineTo(baseX + doorWidth / 2f, groundY)
        close()
    }
    drawPath(doorPath, color = skyHole, style = Fill)

    // Dua menara di kiri & kanan
    val minaretWidth = width * 0.06f
    val minaretHeight = bodyHeight * 1.55f
    listOf(-1f, 1f).forEach { side ->
        val mx = baseX + side * (bodyWidth / 2f + width * 0.06f)
        val mTop = groundY - minaretHeight
        drawRect(
            color = color,
            topLeft = Offset(mx - minaretWidth / 2f, mTop),
            size = androidx.compose.ui.geometry.Size(minaretWidth, minaretHeight)
        )
        // Kubah kecil menara
        val smallDomePath = Path().apply {
            val r = minaretWidth * 0.75f
            moveTo(mx - r, mTop)
            cubicTo(mx - r, mTop - r * 1.3f, mx + r, mTop - r * 1.3f, mx + r, mTop)
            close()
        }
        drawPath(smallDomePath, color = color, style = Fill)
        drawLine(
            color = color,
            start = Offset(mx, mTop - minaretWidth * 0.75f * 1.3f),
            end = Offset(mx, mTop - minaretWidth * 0.75f * 1.3f - width * 0.05f),
            strokeWidth = width * 0.01f
        )
    }

    // Jendela-jendela kecil hiasan
    val windowY = bodyTop + bodyHeight * 0.28f
    listOf(-0.55f, 0.55f).forEach { frac ->
        drawCircle(
            color = skyHole,
            radius = width * 0.02f,
            center = Offset(baseX + frac * bodyWidth / 2f, windowY)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWalkingPerson(
    center: Offset,
    scale: Float,
    legSwing: Float,
    color: Color
) {
    val headRadius = scale * 0.5f
    val headCenter = Offset(center.x, center.y - scale * 4.6f)
    drawCircle(color = color, radius = headRadius, center = headCenter)

    // Sedikit kain/peci di kepala
    drawArc(
        color = color,
        startAngle = 200f,
        sweepAngle = 140f,
        useCenter = true,
        topLeft = Offset(headCenter.x - headRadius, headCenter.y - headRadius * 1.05f),
        size = androidx.compose.ui.geometry.Size(headRadius * 2f, headRadius * 2f)
    )

    // Badan (baju koko sederhana, sedikit melebar ke bawah)
    val bodyTop = Offset(center.x, headCenter.y + headRadius * 0.9f)
    val bodyBottom = Offset(center.x, center.y - scale * 1.1f)
    val bodyPath = Path().apply {
        moveTo(bodyTop.x - scale * 0.55f, bodyTop.y)
        lineTo(bodyTop.x + scale * 0.55f, bodyTop.y)
        lineTo(bodyBottom.x + scale * 0.95f, bodyBottom.y)
        lineTo(bodyBottom.x - scale * 0.95f, bodyBottom.y)
        close()
    }
    drawPath(bodyPath, color = color, style = Fill)

    // Dua kaki melangkah (animasi ayun sederhana)
    val swing = (legSwing - 0.5f) * scale * 1.6f
    drawLine(
        color = color,
        start = Offset(center.x - scale * 0.35f, bodyBottom.y),
        end = Offset(center.x - scale * 0.35f + swing, center.y),
        strokeWidth = scale * 0.38f
    )
    drawLine(
        color = color,
        start = Offset(center.x + scale * 0.35f, bodyBottom.y),
        end = Offset(center.x + scale * 0.35f - swing, center.y),
        strokeWidth = scale * 0.38f
    )
}

private fun Offset.plus(other: Offset) = Offset(this.x + other.x, this.y + other.y)
