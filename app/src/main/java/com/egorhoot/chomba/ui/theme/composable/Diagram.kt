package com.egorhoot.chomba.ui.theme.composable

import android.text.Editable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseInBack
import androidx.compose.animation.core.EaseInBounce
import androidx.compose.animation.core.EaseInExpo
import androidx.compose.animation.core.EaseInOutCirc
import androidx.compose.animation.core.EaseInOutExpo
import androidx.compose.animation.core.TargetBasedAnimation
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.egorhoot.chomba.R
import com.egorhoot.chomba.data.CardSuit
import com.egorhoot.chomba.data.Game
import com.egorhoot.chomba.data.Player
import com.egorhoot.chomba.data.getChombaNum
import com.egorhoot.chomba.data.getMissBarrel
import com.egorhoot.chomba.data.getTotalGain
import com.egorhoot.chomba.data.getTotalLoss
import com.egorhoot.chomba.data.getTotalScore
import com.egorhoot.chomba.data.totalRound
import com.egorhoot.chomba.ui.theme.Shapes
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CircularChart(
    modifier: Modifier,
    pressModifier: Modifier,
    numberOfSides: Int = 0,
    rotationAngle: Float = 0f,
    value: Int,
    is555: Boolean = false,
    maxValue: Int = 1000,
    color: Color,
    zeroNum: Int = 0,
    backgroundCircleColor: Color = Color.LightGray.copy(alpha = 0.3f),
    thicknessFraction: Float = 0.2f,
    blind: Boolean,
    fontSize: Int = 32
) {
    val animatedValue = remember { mutableFloatStateOf(0f) }
    val playTime = remember { mutableLongStateOf(0L) }

    // Round final displayed value to nearest multiple of 5
    val roundVal = if (value < 0) -4 else 4

    LaunchedEffect(value, is555) {
        if (is555 || (value > 555 && value <= 650)) {
            // Step 1: Animate from 0 → 555
            runAnimation(
                TargetBasedAnimation(
                    animationSpec = tween(2000),
                    typeConverter = Float.VectorConverter,
                    initialValue = 0f,
                    targetValue = 555f
                ),
                playTime,
                animatedValue,
                555f
            )

            // Step 2: Animate from 555 → final value
            if (is555){
                runAnimation(
                    TargetBasedAnimation(
                        animationSpec = tween(1000, 500, EaseInBack),
                        typeConverter = Float.VectorConverter,
                        initialValue = 555f,
                        targetValue = 650f
                    ),
                    playTime,
                    animatedValue,
                    650f
                )
                runAnimation(
                    TargetBasedAnimation(
                        animationSpec = tween(1000),
                        typeConverter = Float.VectorConverter,
                        initialValue = 650f,
                        targetValue = value.toFloat()
                    ),
                    playTime,
                    animatedValue,
                    value.toFloat()
                )
            }else{
                runAnimation(
                    TargetBasedAnimation(
                        animationSpec = tween(1000, 500, EaseInBack),
                        typeConverter = Float.VectorConverter,
                        initialValue = 555f,
                        targetValue = value.toFloat()
                    ),
                    playTime,
                    animatedValue,
                    value.toFloat()
                )
            }


        } else {
            runAnimation(
                TargetBasedAnimation(
                    animationSpec = tween(2000),
                    typeConverter = Float.VectorConverter,
                    initialValue = 0f,
                    targetValue = value.toFloat()
                ),
                playTime,
                animatedValue,
                value.toFloat()
            )
            // Normal animation: 0 → value
//            var startTime = withFrameNanos { it }
//            val toValue = TargetBasedAnimation(
//                animationSpec = tween(2000),
//                typeConverter = Float.VectorConverter,
//                initialValue = 0f,
//                targetValue = value.toFloat()
//            )
//            do {
//                playTime.longValue = withFrameNanos { it } - startTime
//                animatedValue.floatValue = toValue.getValueFromNanos(playTime.longValue)
//            } while (animatedValue.floatValue < value.toFloat())
        }

        // Snap to nearest multiple of 5
        animatedValue.floatValue = ((animatedValue.floatValue.toInt() + roundVal) / 5) * 5f
    }

    var sweepAngle = (animatedValue.floatValue / maxValue.toFloat() * 360f).coerceAtLeast(0f)
    if (sweepAngle < 0) sweepAngle = 0f

    val backGroundColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (numberOfSides > 2) {
            PolygonProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                numberOfSides = numberOfSides,
                rotationAngle = rotationAngle,
                animatedValue = animatedValue.floatValue,
                maxValue = 1000f,
                thicknessFraction = thicknessFraction,
                color = color,
                backgroundColor = backgroundCircleColor,
                backGroundColor = backGroundColor
            )
        }else{
            Canvas(modifier = Modifier.fillMaxSize()) {
                val size = size.width.coerceAtMost(size.height)
                val arcRadius = size / 2
                val adjustedThickness = arcRadius * thicknessFraction

                // Background arc (clear base)
                drawArc(
                    color = backGroundColor,
                    startAngle = 90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = adjustedThickness, cap = StrokeCap.Round),
                    size = Size(arcRadius * 2, arcRadius * 2),
                    topLeft = Offset((size - arcRadius * 2) / 2, (size - arcRadius * 2) / 2)
                )
                drawArc(
                    color = backGroundColor,
                    startAngle = 90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    size = Size(arcRadius * 2, arcRadius * 2),
                    topLeft = Offset((size - arcRadius * 2) / 2, (size - arcRadius * 2) / 2)
                )
                // Light background arc
                drawArc(
                    color = backgroundCircleColor,
                    startAngle = 90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = adjustedThickness, cap = StrokeCap.Round),
                    size = Size(arcRadius * 2, arcRadius * 2),
                    topLeft = Offset((size - arcRadius * 2) / 2, (size - arcRadius * 2) / 2)
                )
                // Foreground animated arc
                drawArc(
                    color = color,
                    startAngle = 90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = adjustedThickness, cap = StrokeCap.Round),
                    size = Size(arcRadius * 2, arcRadius * 2),
                    topLeft = Offset((size - arcRadius * 2) / 2, (size - arcRadius * 2) / 2)
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = if (numberOfSides > 2) PolygonShape(numberOfSides, rotationAngle) else CircleShape,
            color = Color.Transparent,
        ) {
            Column(
                modifier = pressModifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = animatedValue.floatValue.toInt().toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontSize = fontSize.sp
                )
                if (blind && zeroNum == 0) {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_visibility_off_24),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                }
                LazyRow(
                    modifier = Modifier.fillMaxWidth(0.5f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    items(zeroNum) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_1200952),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

class PolygonShape(
    private val sides: Int,
    private val rotationAngle: Float = 0f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val radius = min(size.width, size.height) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        for (i in 0 until sides) {
            val angle = Math.toRadians((360.0 / sides) * i - 90.0 + rotationAngle)
            val x = center.x + (cos(angle) * radius).toFloat()
            val y = center.y + (sin(angle) * radius).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }
}



@Composable
fun PolygonProgressIndicator(
    modifier: Modifier = Modifier,
    numberOfSides: Int = 5, // variable that controls shape
    animatedValue: Float,
    maxValue: Float,
    thicknessFraction: Float = 0.1f,
    rotationAngle: Float = 0f,
    color: Color = Color.Green,
    backgroundColor: Color = Color.LightGray,
    backGroundColor: Color = Color.DarkGray
) {
    val sweepFraction = (animatedValue / maxValue).coerceIn(0f, 1f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val minSize = size.minDimension
        val radius = minSize / 2.0f
        val center = Offset(size.width / 2, size.height / 2)
        val strokeWidth = radius * thicknessFraction

        // 🔹 Helper function to build polygon path with "progress"
        fun buildPolygonPath(
            fraction: Float,
            startFromBottom: Boolean = false
        ): Path {
            val path = Path()

            // 1️⃣ Compute all polygon vertices with rotation
            val vertices = (0 until numberOfSides).map { i ->
                val angle = Math.toRadians((360.0 / numberOfSides) * i - 90.0 + rotationAngle)
                Offset(
                    center.x + (cos(angle) * radius).toFloat(),
                    center.y + (sin(angle) * radius).toFloat()
                )
            }

            // 2️⃣ Choose start point
            val orderedVertices = if (startFromBottom) {
                // Always start at bottom-center of shape
                val bottomPoint = Offset(center.x, center.y + radius) // bottom of bounding circle
                // Find closest polygon edge to bottom point
                val closestIndex = vertices.indices.minByOrNull { idx ->
                    (vertices[idx].y - bottomPoint.y).absoluteValue +
                            (vertices[idx].x - bottomPoint.x).absoluteValue
                } ?: 0

                // Reorder vertices so progress starts at bottom
                vertices.drop(closestIndex) + vertices.take(closestIndex)
            } else {
                vertices
            }

            // 3️⃣ Compute edges and progress
            val totalEdges = numberOfSides
            val totalProgress = fraction * totalEdges
            val fullEdges = totalProgress.toInt()
            val partialEdgeFraction = totalProgress % 1f

            // Move to first vertex
            path.moveTo(orderedVertices[0].x, orderedVertices[0].y)

            // Draw full edges
            for (i in 0 until fullEdges) {
                val nextIndex = (i + 1) % numberOfSides
                path.lineTo(orderedVertices[nextIndex].x, orderedVertices[nextIndex].y)
            }

            // Draw partial edge
            if (partialEdgeFraction > 0) {
                val startIndex = fullEdges % numberOfSides
                val endIndex = (startIndex + 1) % numberOfSides
                val start = orderedVertices[startIndex]
                val end = orderedVertices[endIndex]
                val interpX = start.x + (end.x - start.x) * partialEdgeFraction
                val interpY = start.y + (end.y - start.y) * partialEdgeFraction
                path.lineTo(interpX, interpY)
            }

            return path
        }





        // 🔹 Background polygon outline (full)
        val fullPath = buildPolygonPath(1f)
        drawPath(
            path = fullPath,
            color = backGroundColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = fullPath,
            color = backGroundColor,
        )

        // 🔹 Light background polygon
        drawPath(
            path = fullPath,
            color = backgroundColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 🔹 Foreground progress polygon
        val progressPath = buildPolygonPath(sweepFraction,true)
        drawPath(
            path = progressPath,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}


suspend fun runAnimation(
    anim: TargetBasedAnimation<Float, AnimationVector1D>,
    playTime: MutableLongState,
    animatedValue: MutableFloatState,
    target: Float
) {
    var startTime = withFrameNanos { it }
    do {
        playTime.longValue = withFrameNanos { it } - startTime
        animatedValue.floatValue = anim.getValueFromNanos(playTime.longValue)
    } while (
        (anim.initialValue < target && animatedValue.floatValue < target) ||
        (anim.initialValue > target && animatedValue.floatValue > target)
    )
}

@Composable
fun GameCard(
    modifier: Modifier = Modifier,
    game: Game,
    onSelect: () -> Unit,
    selected: Boolean = false,
    finished: Boolean = false,
    onDelete: () -> Unit,
    onEdit: () -> Unit
){

    val editable = game.editable

    Surface (
        shape = Shapes.large,
        color = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = if (selected) MaterialTheme.colorScheme.onTertiaryContainer
                else MaterialTheme.colorScheme.secondaryContainer,
                shape = Shapes.large
            )
            .clickable(onClick = onSelect)
    ){
        Column(
            modifier = modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                for(player in game.playerList){
                    PlayerGameCard(
                        modifier = modifier
                            .weight(1f),
                        player = player
                    )
                }
            }

            AnimatedVisibility(visible = selected) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Divider(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                        )
                        Text(
                            text = game.totalRound().toString(),
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                        )
                        Divider(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(3.dp, 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (suit in CardSuit.values()){
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(1.dp, 0.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(id = suitIcon(suit.ordinal)),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (suit.ordinal > 1 && suit != CardSuit.ACE) Color.Red
                                    else MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = game.getChombaNum(suit).toString(), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .padding(0.dp, 4.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(0.dp, 4.dp)
                                .wrapContentSize()
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_trending_up_24),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = game.getTotalGain().toString(), style = MaterialTheme.typography.bodyMedium)
                        }

                        Row(
                            modifier = Modifier
                                .padding(0.dp, 4.dp)
                                .wrapContentSize()
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_trending_down_24),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = game.getTotalLoss().toString(), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Chart(modifier = Modifier
                        .padding(4.dp, 0.dp, 4.dp, 4.dp)
                        .aspectRatio(2.2f),
                        playerList = game.playerList,
                        drawSpeed = 1000)
                }
            }

            AnimatedVisibility(visible = selected && editable) {
                Column {
                    IconButton(icon = R.drawable.baseline_border_color_24,
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(8.dp, 0.dp, 8.dp, 0.dp),
                        action = onEdit)
                    IconButton(icon = R.drawable.baseline_delete_24,
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(8.dp, 0.dp, 8.dp, 4.dp),
                        action = onDelete)
                }
            }


        }

    }
}

@Composable
fun PlayerGameCard(
    modifier: Modifier = Modifier,
    player: Player,
){
    Column (
        modifier = modifier
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        ResizableText(
            text = player.name,
            style = MaterialTheme.typography.titleMedium
        )
//        Text(
//            text = player.name,
//            style = MaterialTheme.typography.titleMedium,
//            fontSize = TextUnit.Unspecified
//        )
        Box(modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            CircularChart(
                modifier = Modifier
                    .fillMaxSize(0.75f),
                pressModifier = Modifier,
                numberOfSides = player.numberOfEdges,
                rotationAngle = player.rotationAngle,
                value = player.getTotalScore(),
                maxValue = 1000,
                color = Color(player.color.toULong()),
                zeroNum = if(player.getTotalScore() == 880) player.getMissBarrel()
                else 0,
                blind = false,
                fontSize = 24
            )
        }
    }

}