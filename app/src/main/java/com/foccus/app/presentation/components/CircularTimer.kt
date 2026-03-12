package com.foccus.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foccus.app.presentation.theme.*

@Composable
fun CircularTimer(
    progress: Float,
    timeText: String,
    labelText: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 280.dp,
    strokeWidth: Dp = 6.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500, easing = EaseInOutCubic),
        label = "timer_progress"
    )

    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulse by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 1.02f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val trackColor = SurfaceVariantDark
    val progressColor = if (isActive) Accent else OnSurfaceVariant
    val glowColor = if (isActive) Accent.copy(alpha = 0.12f) else Color.Transparent

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(size * pulse)
                .fillMaxSize()
        ) {
            val canvasSize = this.size.minDimension
            val sw = strokeWidth.toPx()
            val arcSize = Size(canvasSize - sw, canvasSize - sw)
            val topLeft = Offset(sw / 2f, sw / 2f)

            if (isActive) {
                drawArc(
                    color = glowColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = Offset(0f, 0f),
                    size = Size(canvasSize, canvasSize),
                    style = Stroke(width = sw * 2.5f, cap = StrokeCap.Butt)
                )
            }

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = sw, cap = StrokeCap.Butt)
            )

            if (animatedProgress > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = sw, cap = StrokeCap.Butt)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Thin,
                    fontSize = 52.sp,
                    color = if (isActive) Accent else OnSurface,
                    letterSpacing = 2.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = labelText,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = OnSurfaceVariant,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Light
                )
            )
        }
    }
}
