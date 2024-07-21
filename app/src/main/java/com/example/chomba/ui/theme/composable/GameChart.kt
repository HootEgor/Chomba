package com.example.chomba.ui.theme.composable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.chomba.data.Player
import com.example.chomba.data.getTotalScore
import com.example.chomba.ui.theme.Shapes

@Composable
fun Chart(
    modifier: Modifier = Modifier,
    playerList: List<Player>
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
            gradientFromAlpha = 0.5f,

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
    gridLineSpacing: Float = 50f
) {
    Canvas(modifier = modifier) {
        drawGridLines(gridLineColor, gridLineThickness, gridLineSpacing)
        for (player in data) {
            val color = Color(player.color.toULong())
            val gradientColors = listOf(color.copy(alpha = gradientFromAlpha), color.copy(alpha = gradientToAlpha))
            drawCurvedLine(player, color, lineWidth)
//            drawGradientUnderCurve(player, gradientColors)
        }
    }
}

private fun DrawScope.drawGridLines(gridLineColor: Color, gridLineThickness: Float, gridLineSpacing: Float) {
    val horizontalLinesCount = (size.height / gridLineSpacing).toInt()
    for (i in 0..horizontalLinesCount) {
        val y = i * gridLineSpacing
        drawLine(
            color = gridLineColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = gridLineThickness,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    }
}

private fun DrawScope.drawCurvedLine(player: Player, lineColor: Color, lineWidth: Float) {
    if (player.scoreList.isEmpty()) return

    val path = Path().apply {
        val stepX = size.width / if(player.scoreList.size > 20) player.scoreList.size else 20
        var currentX = 0f
        var min = -200

        moveTo(currentX, size.height - (normalizeData(player.getTotalScore(0), min) * size.height))
        for (i in 1 .. player.scoreList.size) {
            val nextX = currentX + stepX
            if (player.getTotalScore(i) < min) {
                min = player.getTotalScore(i)-50
            }
            val data = normalizeData(player.getTotalScore(i), min)
            val controlPoint1 = Offset(currentX + stepX / 2, size.height - (data * size.height))
            val controlPoint2 = Offset(currentX + stepX / 2, size.height - (data * size.height))
            cubicTo(
                controlPoint1.x, controlPoint1.y,
                controlPoint2.x, controlPoint2.y,
                nextX, size.height - (data * size.height)
            )
            currentX = nextX
        }
    }

    drawPath(
        path = path,
        color = lineColor,
        style = Stroke(
            width = lineWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

private fun DrawScope.drawGradientUnderCurve(player: Player, gradientColors: List<Color>) {
    if (player.scoreList.isEmpty()) return

    val path = Path().apply {
        val stepX = size.width / if(player.scoreList.size > 20) player.scoreList.size else 20
        var currentX = 0f
        var min = -200

        moveTo(currentX, size.height)
        moveTo(currentX, size.height - (normalizeData(player.getTotalScore(0), min) * size.height))
        for (i in 1 .. player.scoreList.size) {
            val nextX = currentX + stepX
            if (player.getTotalScore(i) < min) {
                min = player.getTotalScore(i)
            }
            val data = normalizeData(player.getTotalScore(i), min)
            val controlPoint1 = Offset(currentX + stepX / 2, size.height - (data * size.height))
            val controlPoint2 = Offset(currentX + stepX / 2, size.height - (data * size.height))
            cubicTo(
                controlPoint1.x, controlPoint1.y,
                controlPoint2.x, controlPoint2.y,
                nextX, size.height - (data * size.height)
            )
            currentX = nextX
        }
        lineTo(currentX, size.height)
        close()
    }

    drawPath(
        path = path,
        brush = Brush.verticalGradient(gradientColors)
    )
}

private fun normalizeData(data: Int, min: Int): Float {
    val max = 1000
    val normalized = (data - min) / (max - min).toFloat()
    return normalized
}