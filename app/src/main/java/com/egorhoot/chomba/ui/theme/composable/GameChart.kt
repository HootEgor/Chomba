package com.egorhoot.chomba.ui.theme.composable

import android.text.TextPaint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TargetBasedAnimation
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.egorhoot.chomba.data.Player
import com.egorhoot.chomba.data.getMaxRound
import com.egorhoot.chomba.data.getScoreSum
import com.egorhoot.chomba.data.getTotalScore
import com.egorhoot.chomba.ui.theme.Shapes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt

@Composable
fun Chart(
    modifier: Modifier = Modifier,
    playerList: List<Player>,
    drawSpeed: Int = 2000
) {
    Surface(
        shape = Shapes.large,
        color = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = Shapes.large
            ),
    ) {
        CurvedLineChart(
            modifier = Modifier
                .fillMaxWidth(),
            data = playerList,
            lineWidth = 8f,
            gradientFromAlpha = 0.1f,
            drawSpeed = drawSpeed
            )
    }
}

@Composable
fun CurvedLineChart(
    modifier: Modifier = Modifier,
    data: List<Player>,
    lineWidth: Float = 5f,
    gradientFromAlpha: Float = 0.5f,
    gradientToAlpha: Float = 0f,
    gridLineColor: Color = Color.Gray,
    gridLineThickness: Float = 1f,
    drawSpeed: Int = 2000,
) {

    val drawPathAnimation = remember {
        Animatable(0f)
    }

    val targetRound = data.first().getMaxRound()

    LaunchedEffect(targetRound) {
        drawPathAnimation.animateTo(
            1f, tween(sqrt(targetRound.toDouble()).toInt()*drawSpeed, 0, LinearEasing)
        )
    }

    Canvas(modifier = modifier) {
        var min = -200
        for (player in data) {
            for (i in 1 .. player.getMaxRound()) {
                if (player.getTotalScore(i) < min) {
                    min = player.getTotalScore(i)
                }
            }
        }

        min-=50

        for (i in 800 downTo min+50 step 200) {
            drawScoreLines(gridLineColor, i, gridLineThickness, min)
        }

        drawMinusZone(listOf(Color.Red.copy(alpha = 0.25f), Color.Red.copy(alpha = 0f)), min)

        val sortedPlayers = data.sortedBy { it.getScoreSum() }
        for (player in sortedPlayers.reversed()) {
            val color = Color(player.color.toULong())
            val gradientColors = listOf(color.copy(alpha = gradientFromAlpha), color.copy(alpha = gradientToAlpha))
            drawCurvedLine(player, color, lineWidth, min, drawPathAnimation.value)
            drawGradientUnderCurve(player, gradientColors, min, drawPathAnimation.value)
        }
    }
}

private fun DrawScope.drawMinusZone(gradientColors: List<Color>, min: Int) {
    val zeroY = size.height - (normalizeData(0, min) * size.height)
    val path = Path().apply {
        moveTo(0f, zeroY)
        lineTo(size.width, zeroY)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }

    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = gradientColors,
            startY = zeroY,
            endY = size.height
        )
    )
}

private fun DrawScope.drawScoreLines(lineColor: Color, value: Int, thickness: Float, min: Int) {
    val y = size.height - (normalizeData(value, min) * size.height)

    drawLine(
        color = lineColor,
        start = Offset(0f, y),
        end = Offset(size.width/2 - 30f, y),
        strokeWidth = thickness,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    )

    drawLine(
        color = lineColor,
        start = Offset(size.width/2 + 30f, y),
        end = Offset(size.width, y),
        strokeWidth = thickness,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    )

    val textPaint = TextPaint().apply {
        color = lineColor.toArgb()
        textSize = 8.sp.toPx()
        textAlign = android.graphics.Paint.Align.CENTER
    }

    val centerX = size.width / 2

    drawContext.canvas.nativeCanvas.drawText(
        value.toString(),
        centerX,
        y - (textPaint.descent() + textPaint.ascent()) / 2,
        textPaint
    )
}

private fun DrawScope.drawCurvedLine(player: Player, lineColor: Color, lineWidth: Float, min: Int, progress: Float) {
    if (player.scoreList.isEmpty()) return

    val path = Path().apply {
        val stepX = size.width / if(player.getMaxRound() > 20) player.getMaxRound() else 20
        var currentX = 0f

        moveTo(currentX, size.height - (normalizeData(player.getTotalScore(0), min) * size.height))
        for (i in 1 .. player.getMaxRound()) {
            val nextX = currentX + stepX
            val data = normalizeData(player.getTotalScore(i), min)
            val controlPoint1 = Offset(currentX + stepX / 2, size.height - (normalizeData(player.getTotalScore(i-1), min) * size.height))
            val controlPoint2 = Offset(currentX + stepX / 2, size.height - (data * size.height))
            cubicTo(
                controlPoint1.x, controlPoint1.y,
                controlPoint2.x, controlPoint2.y,
                nextX, size.height - (data * size.height)
            )
            currentX = nextX
        }
    }

    val pathMeasure = PathMeasure()
    val animatedPath = derivedStateOf {
        val newPath = Path()
        pathMeasure.setPath(path, false)
        pathMeasure.getSegment(
            0f,
            progress * pathMeasure.length, newPath
        )
        newPath
    }

    drawPath(
        path = animatedPath.value,
        color = lineColor,
        style = Stroke(
            width = lineWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
    )
}

private fun DrawScope.drawGradientUnderCurve(player: Player, gradientColors: List<Color>, min: Int, progress: Float) {
    if (player.scoreList.isEmpty()) return

    var currentX = 0f
    val path = Path().apply {
        val stepX = size.width / if(player.getMaxRound() > 20) player.getMaxRound()else 20

        moveTo(currentX, size.height)
        moveTo(currentX, size.height - (normalizeData(player.getTotalScore(0), min) * size.height))
        for (i in 1 .. player.getMaxRound()) {
            val nextX = currentX + stepX
            val data = normalizeData(player.getTotalScore(i), min)
            val controlPoint1 = Offset(currentX + stepX / 2, size.height - (normalizeData(player.getTotalScore(i-1), min) * size.height))
            val controlPoint2 = Offset(currentX + stepX / 2, size.height - (data * size.height))
            cubicTo(
                controlPoint1.x, controlPoint1.y,
                controlPoint2.x, controlPoint2.y,
                nextX, size.height - (data * size.height)
            )
            currentX = nextX
        }
    }

    val pathMeasure = PathMeasure()
    val animatedPath = derivedStateOf {
        val newPath = Path()
        pathMeasure.setPath(path, false)
        pathMeasure.getSegment(
            0f,
            progress * pathMeasure.length, newPath
        )
        newPath.apply {
            lineTo(currentX, size.height)
            close()
        }
    }

    drawPath(
        path = animatedPath.value,
        brush = Brush.verticalGradient(gradientColors)
    )
}

private fun normalizeData(data: Int, min: Int): Float {
    val max = 1000
    val normalized = (data - min) / (max - min).toFloat()
    return normalized
}