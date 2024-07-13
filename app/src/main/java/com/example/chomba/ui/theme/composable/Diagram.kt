package com.example.chomba.ui.theme.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chomba.R
import com.example.chomba.data.Game
import com.example.chomba.data.Player
import com.example.chomba.data.getMissBarrel
import com.example.chomba.data.getTotalScore
import com.example.chomba.ui.theme.Shapes

@Composable
fun CircularChart(
    modifier: Modifier,
    pressModifier: Modifier,
    value: Int,
    maxValue: Int,
    color: Color,
    zeroNum: Int = 0,
    backgroundCircleColor: Color = Color.LightGray.copy(alpha = 0.3f),
    thicknessFraction: Float = 0.2f,
    blind: Boolean,
    fontSize: Int = 32
) {
    var sweepAngle = value.toFloat()/ maxValue.toFloat() * 360f

    if (sweepAngle<0)
        sweepAngle = 0f

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val size = size.width.coerceAtMost(size.height)
            val arcRadius = size / 2

            val adjustedThickness = arcRadius * thicknessFraction
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

        Surface(
            modifier = Modifier
                .fillMaxSize(),
            shape = CircleShape,
            color = Color.Transparent,
            content = {
                Column (
                modifier = pressModifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(
                    text = value.toString(),
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


    }
}

@Composable
fun GameCard(
    modifier: Modifier = Modifier,
    game: Game,
    onSelect: () -> Unit,
    selected: Boolean = false,
    onDelete: () -> Unit
){
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
                Divider(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp)
                )

                IconButton(icon = R.drawable.baseline_delete_24,
                    modifier = modifier.fillMaxWidth(),
                    action = onDelete)
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