package com.egorhoot.chomba.ui.theme.composable

import android.text.Editable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CircularChart(
    modifier: Modifier,
    pressModifier: Modifier,
    value: Int,
    maxValue: Int = 1000,
    color: Color,
    zeroNum: Int = 0,
    backgroundCircleColor: Color = Color.LightGray.copy(alpha = 0.3f),
    thicknessFraction: Float = 0.2f,
    blind: Boolean,
    fontSize: Int = 32
) {
//    val targetSweepAngle = (value.toFloat() / maxValue.toFloat() * 360f).coerceAtLeast(0f)
    val animatedValue = remember {
        mutableFloatStateOf(0f)
    }

    val targetValue = if(value.toFloat() > 555f && value.toFloat() <= 650 && animatedValue.floatValue < 555f) 555f else value.toFloat()

    val anim = TargetBasedAnimation(
        animationSpec = tween(2000),
        typeConverter = Float.VectorConverter,
        initialValue = 0f,
        targetValue = targetValue
    )

    val fullAnim = TargetBasedAnimation(
        animationSpec = tween(1000, 500, EaseInBack),
        typeConverter = Float.VectorConverter,
        initialValue = targetValue,
        targetValue = value.toFloat()
    )

    val roundVal = if(value < 0) -4 else 4

    val playTime = remember { mutableLongStateOf(0L) }

    LaunchedEffect(value) {
        var startTime = withFrameNanos { it }

        do {
            playTime.longValue = withFrameNanos { it } - startTime
            animatedValue.floatValue = anim.getValueFromNanos(playTime.longValue)
        } while (animatedValue.floatValue < targetValue)

        startTime = withFrameNanos { it }
        do{
            playTime.longValue = withFrameNanos { it } - startTime
            animatedValue.floatValue = fullAnim.getValueFromNanos(playTime.longValue)
        } while (animatedValue.floatValue < value.toFloat())

        animatedValue.floatValue = ((animatedValue.floatValue.toInt()+roundVal) / 5) * 5f
    }

    var sweepAngle = (animatedValue.floatValue / maxValue.toFloat() * 360f).coerceAtLeast(0f)

    if (sweepAngle<0)
        sweepAngle = 0f

    val backGroundColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {


        Surface(
            modifier = Modifier
                .fillMaxSize(),
            shape = CircleShape,
            color = backGroundColor,
            content = {
                Column (
                modifier = pressModifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(
                    text = animatedValue.floatValue.toInt().toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontSize = fontSize.sp
                )
                    if(blind && zeroNum == 0){
                        Image(
                            painter = painterResource(
                                id = R.drawable.baseline_visibility_off_24
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    }

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ){

                        items(zeroNum){
                            Image(
                                painter = painterResource(
                                    id = R.drawable.ic_1200952
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }


            }
            }
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val size = size.width.coerceAtMost(size.height)
            val arcRadius = size / 2

            val adjustedThickness = arcRadius * thicknessFraction
            drawArc(
                color = backGroundColor,
                startAngle = 90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = adjustedThickness, cap = StrokeCap.Round),
                size = Size(arcRadius * 2, arcRadius * 2),
                topLeft = Offset(
                    x = (size - arcRadius * 2) / 2,
                    y = (size - arcRadius * 2) / 2
                )
            )
            drawArc(
                color = backgroundCircleColor,
                startAngle = 90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = adjustedThickness, cap = StrokeCap.Round),
                size = Size(arcRadius * 2, arcRadius * 2),
                topLeft = Offset(
                    x = (size - arcRadius * 2) / 2,
                    y = (size - arcRadius * 2) / 2
                )
            )
            drawArc(
                color = color,
                startAngle = 90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = adjustedThickness, cap = StrokeCap.Round),
                size = Size(arcRadius * 2, arcRadius * 2),
                topLeft = Offset(
                    x = (size - arcRadius * 2) / 2,
                    y = (size - arcRadius * 2) / 2
                )
            )
        }


    }
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
                            modifier = Modifier.padding(0.dp, 4.dp).wrapContentSize()
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
                            modifier = Modifier.padding(0.dp, 4.dp).wrapContentSize()
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