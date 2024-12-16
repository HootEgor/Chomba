package com.egorhoot.chomba.ui.theme.composable

import android.text.TextPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import com.egorhoot.chomba.ui.theme.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScoreBar(
    modifier: Modifier = Modifier,
    score: Int,
    height: Dp = 16.dp,
    barColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = Color.White,
    borderColor: Color = Color.Black,
    borderWidth: Dp = 2.dp,
) {
    val checkPoints: List<Int> = listOf(555, 880)

    Canvas(modifier = modifier.padding(0.dp, 16.dp, 0.dp, 0.dp).height(height).fillMaxWidth()) {
        val barWidth = size.width
        val barHeight = size.height
        // Draw background bar
        drawRoundRect(
            color = backgroundColor,
            topLeft = Offset(0f, 0f),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)
        )

        // Draw score bar (progress)
        val progressWidth = (score / 1000f) * barWidth
        if(score in 50..999){
            //draw bar with round start and flat end
            drawRoundRect(
                color = barColor,
                topLeft = Offset(0f, 0f),
                size = Size(progressWidth, barHeight),
                cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)
            )
            drawRoundRect(
                color = barColor,
                topLeft = Offset(progressWidth/2, 0f),
                size = Size(progressWidth/2, barHeight),
                cornerRadius = CornerRadius(barHeight / 2, 0f)
            )
            drawLine(
                color = Color.Black,
                start = Offset(progressWidth, -15f),
                end = Offset(progressWidth, barHeight),
                strokeWidth = borderWidth.toPx()
            )
            drawVerticalScore(Color.Black, progressWidth, -20f, score.toString())
        }else{
            drawRoundRect(
                color = barColor,
                topLeft = Offset(0f, 0f),
                size = Size(progressWidth, barHeight),
                cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)
            )
        }


        // Draw border
        drawRoundRect(
            color = borderColor,
            topLeft = Offset(0f, 0f),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(barHeight / 2, barHeight / 2),
            style = Stroke(width = borderWidth.toPx())
        )

        for (checkPoint in checkPoints) {
            val checkPointWidth = (checkPoint / 1000f) * barWidth
            drawLine(
                color = Color.Black,
                start = Offset(checkPointWidth, 0f),
                end = Offset(checkPointWidth, barHeight),
                strokeWidth = borderWidth.toPx()
            )
            if(checkPoint in score-49..score+49) continue
            drawLine(
                color = Color.Black,
                start = Offset(checkPointWidth, -15f),
                end = Offset(checkPointWidth, 0f),
                strokeWidth = borderWidth.toPx()
            )
            drawVerticalScore(Color.Black, checkPointWidth, -20f, checkPoint.toString())
        }
    }
}

private fun DrawScope.drawVerticalScore(lineColor: Color, x: Float, y:Float, text: String) {
    val textPaint = TextPaint().apply {
        color = lineColor.toArgb()
        textSize = 8.sp.toPx()
        textAlign = android.graphics.Paint.Align.CENTER
    }
    drawContext.canvas.nativeCanvas.drawText(text, x, y, textPaint)
}

@Preview
@Composable
fun ScoreBarPreview() {
    Column (
        modifier = Modifier.width(400.dp).background(Color.White),
        verticalArrangement = Arrangement.SpaceEvenly,
    ){
        for (s in 0..1000 step 50){
            ScoreBar(score = s)
        }
    }

}