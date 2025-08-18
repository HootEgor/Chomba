package com.egorhoot.chomba.pages.win

import android.os.Build.VERSION.SDK_INT
import androidx.annotation.RawRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.egorhoot.chomba.R
import com.egorhoot.chomba.data.Player
import com.egorhoot.chomba.data.getMaxRound
import com.egorhoot.chomba.data.getTotalScore
import com.egorhoot.chomba.ui.theme.composable.ResizableText
import com.egorhoot.chomba.ui.theme.composable.suitIcon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt

@Composable
fun WinPage(
    modifier: Modifier = Modifier,
    players: List<Player>,
    drawSpeed: Long = 2000L
) {
    val maxRounds = players.first().getMaxRound()
    var min = 0
    for (player in players) {
        for (i in 1 .. player.getMaxRound()) {
            if (player.getTotalScore(i) < min) {
                min = player.getTotalScore(i)
            }
        }
    }

    val winner = remember { mutableStateOf("")  }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AnimatedVisibility(visible = winner.value != "",
                modifier = Modifier,
                enter = expandVertically(
                    expandFrom = Alignment.Top
                ),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top
                )) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Icon(
                        painter = painterResource(id = R.drawable.outline_chess_queen_24),
                        contentDescription = "Winner Icon",
                        modifier = Modifier
                            .size(56.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = winner.value + " " + stringResource(id = R.string.win),
                        style = TextStyle(
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                players.forEachIndexed { index, player ->
                    PlayerColumn(
                        modifier = Modifier.weight(1f),
                        player = player,
                        index = index,
                        maxRounds = maxRounds,
                        drawSpeed = drawSpeed,
                        minScore = min,
                        onWin = {
                            winner.value = player.name
                        }
                    )
                }
            }
        }


        if (winner.value != "") {
            Gif(
                gifRes = R.raw.usa_confetti,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }

}

@Composable
fun PlayerColumn(
    modifier: Modifier = Modifier,
    player: Player,
    index: Int,
    maxRounds: Int,
    minScore: Int = 0,
    drawSpeed: Long,
    onWin: () -> Unit = {}
) {
    val maxHeight = 1100
    val visibleState = remember { mutableStateOf(false) }
    val animatedHeight = remember { Animatable(0f) }

    val duration = (sqrt((maxRounds).toDouble()) * drawSpeed/maxRounds).toInt()

    data class FloatingIcon(val id: Int, val x: Float, val animY: Animatable<Float, AnimationVector1D>, val resId: Int)
    val floatingIcons = remember { mutableStateListOf<FloatingIcon>() }
    val iconScope = rememberCoroutineScope()
    var iconCounter = remember { mutableIntStateOf(0) }

    LaunchedEffect(player) {
        // Show player name with stagger
        delay(1000L + index * 500L)
        visibleState.value = true
        delay((3-index) * 500L) // Adjust delay for stagger effect

        player.scoreList.forEachIndexed { roundIndex, targetScore ->

            // Add icons for taken chombas
            targetScore.takenChombas.forEach { c ->
                val animY = Animatable(0f)
                val icon = FloatingIcon(
                    id = iconCounter.intValue++,
                    x = (-50..50).random().toFloat(), // random x position, will scale later
                    animY = animY,
                    resId = suitIcon(c.ordinal)
                )
                floatingIcons.add(icon)

                iconScope.launch {
                    animY.animateTo(
                        targetValue = -25f, // move up 50 dp
                        animationSpec = tween(750, easing = LinearOutSlowInEasing)
                    )
                    floatingIcons.remove(icon)
                }
            }

            // Animate column height smoothly
            animatedHeight.animateTo(
                player.getTotalScore(roundIndex+1).toFloat(),
                animationSpec = tween(durationMillis = duration, easing = LinearOutSlowInEasing)
            )
        }

        if (player.getTotalScore() == 1000){
            onWin()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = modifier
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .fillMaxHeight()
    ) {
        val topHeight = normalize(maxHeight, minScore, maxHeight)
        Column(
            modifier = Modifier
                .fillMaxHeight(topHeight),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            // Animated column for score
            if (visibleState.value) {

                Box(
                    modifier = Modifier.aspectRatio(1.75f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {

//                    Text(
//                        text = player.name,
//                        fontWeight = FontWeight.Bold,
//                        )
                    ResizableText(
                        text = player.name,
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        modifier = modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    )

                    floatingIcons.forEach { icon ->
                        Image(
                            painter = painterResource(id = icon.resId),
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .offset(x = icon.x.dp, y = icon.animY.value.dp)
                        )
                    }
                }
            }

            if (animatedHeight.value > 0){
                ScoreBar(
                    modifier = Modifier,
                    score = animatedHeight.value.toInt(),
                    height = normalize(animatedHeight.value.toInt(), 0, maxHeight),
                    color = Color(player.color.toULong()),
                    backColor = MaterialTheme.colorScheme.background,
                    textStyle = TextStyle(
                        fontSize = 45.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black.copy(alpha = 0.5f)
                    )
                )
            }

        }

        if (animatedHeight.value < 0){
            ScoreBar(
                modifier = Modifier,
                score = animatedHeight.value.toInt(),
                height = normalize(animatedHeight.value.toInt(), 0, minScore),
                color = Color(player.color.toULong()),
                backColor = MaterialTheme.colorScheme.background,
                textStyle = TextStyle(
                    fontSize = 45.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black.copy(alpha = 0.5f)
                )
            )
        }

    }
}

@Composable
fun ScoreBar(
    modifier: Modifier = Modifier,
    score: Int,
    height: Float,
    color: Color,
    backColor: Color = MaterialTheme.colorScheme.background,
    textStyle: TextStyle = LocalTextStyle.current
) {
    Surface(
        color = Color.Transparent,
        modifier = modifier
            .fillMaxHeight(height)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.small,

        ){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color)
//                .drawBehind() {
//                    val cutoutWidth= size.width * 0.8f
//                    val cutoutHeight= if (size.height * 0.8f < size.width * 0.4f)
//                        size.height * 0.8f else size.width * 0.4f
//                    val cutoutLeft = (size.width - cutoutWidth) / 2
//                    val cutoutTop = (size.height - cutoutHeight) / 2
//
//                    drawRoundRect(
//                        color = backColor,
//                        topLeft = Offset(cutoutLeft, cutoutTop),
//                        size = Size(cutoutWidth, cutoutHeight),
//                        cornerRadius = CornerRadius(20.dp.toPx())
//                    )
//                }
            ,
            contentAlignment = Alignment.Center
        ){
            Text(
                text = score.toString(),
                fontWeight = FontWeight.Bold,
                style = textStyle,
            )
        }
    }
}

@Composable
fun Gif(
    @RawRes gifRes: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(gifRes)
            .build(),
        imageLoader = imageLoader
    )

    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.FillHeight,
        modifier = modifier
    )
}


fun normalize(value: Int, min: Int, max: Int): Float {
    return if (max - min == 0) 0f else value.toFloat() / (max - min)
}
