package com.egorhoot.chomba.pages.user.leaderboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.materialcore.toRadians
import com.egorhoot.chomba.R
import com.egorhoot.chomba.data.CardSuit
import com.egorhoot.chomba.data.LeaderBoardPlayer
import com.egorhoot.chomba.data.getChombaNum
import com.egorhoot.chomba.data.getScoreText
import com.egorhoot.chomba.data.getTotalGain
import com.egorhoot.chomba.data.getTotalLoss
import com.egorhoot.chomba.ui.theme.Shapes
import com.egorhoot.chomba.ui.theme.composable.FullIconButton
import com.egorhoot.chomba.ui.theme.composable.IconButton
import com.egorhoot.chomba.ui.theme.composable.ResizableText
import com.egorhoot.chomba.ui.theme.composable.animatedBorder
import com.egorhoot.chomba.ui.theme.composable.suitIcon
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun LeaderBoard(
    modifier: Modifier = Modifier,
    viewModel: LeaderBoardViewModel,
    ) {
    val uiState by viewModel.uiState
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .height(42.dp),
        ) {
            Surface(
                shape = Shapes.medium,
                color = MaterialTheme.colorScheme.tertiaryContainer,
            ) {
                Row(
                    modifier = modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(modifier = modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_numbers_24),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(8.dp, 0.dp, 0.dp, 0.dp)
                                .size(24.dp)
                        )
                        Spacer(modifier = modifier.fillMaxWidth())
                    }
                    
                    Row(
                        modifier = modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        FullIconButton(
                            icon = R.drawable.first,
                            modifier = modifier.weight(1f),
                            action = { viewModel.sortPlayersByWins() },
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                        FullIconButton(
                            icon = R.drawable.card_suits,
                            modifier = modifier.weight(1f),
                            action = { viewModel.sortPlayersByTotalChombas() },
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                        FullIconButton(
                            icon = R.drawable.baseline_score_24,
                            modifier = modifier.weight(1f),
                            action = { viewModel.sortPlayersByTotalScore() },
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                }
            }
        }

        LazyColumn{
            items(uiState.players) {
                    item ->
                LeaderBoardItem(
                player = item,
                position = uiState.players.indexOf(item) + 1
            )
            }
        }
    }
}

@Composable
fun LeaderBoardItem(
    modifier: Modifier = Modifier,
    player: LeaderBoardPlayer,
    position: Int
    ) {
    val expanded = remember {mutableStateOf(false)}

    val currentColor = remember { mutableStateOf(player.colors[0]) }

    val targetAlpha = remember { mutableStateOf(1f) }

    LaunchedEffect(player) {
        while (true) {
            targetAlpha.value = if (targetAlpha.value == 1f) 0.3f else 1f
            delay(1000L)
        }
    }

    val animatedColor = animateColorAsState(
        targetValue = currentColor.value.copy(alpha = targetAlpha.value),
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "animatedColor"
    )

    LaunchedEffect(player) {
        while (true) {
            currentColor.value = player.colors.random().copy(alpha = targetAlpha.value)
            delay(1000L)
        }
    }


    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp, 1.dp)
            .border(
                width = if (position == 1) 3.dp else 1.dp,
                color = if (position == 1) animatedColor.value else MaterialTheme.colorScheme.tertiaryContainer,
                shape = Shapes.medium
            )
            .clickable(onClick = {
                expanded.value = !expanded.value
            }),
        shape = Shapes.medium,
    ) {
        Column {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .height(42.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(modifier = modifier.weight(1f),
                    horizontalArrangement = Arrangement.Start
                ){
                    Text(
                        text = position.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        modifier = modifier.padding(16.dp, 0.dp, 16.dp, 0.dp)
                    )
                    ResizableText(
                        text = player.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    )
                }

                AnimatedVisibility(visible = !expanded.value,
                    modifier = modifier.weight(1f),
                    enter = expandVertically(
                        expandFrom = Alignment.Top
                    ),
                    exit = shrinkVertically(
                        shrinkTowards = Alignment.Top
                    )) {
                    Row(
                        modifier = modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = player.wins.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            modifier = modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = player.totalChombas.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            modifier = modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = player.getScoreText(),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            modifier = modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

            }

            AnimatedVisibility(visible = expanded.value) {
                Column {


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(3.dp, 0.dp),
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
                            Text(text = player.getChombaNum(suit).toString(), style = MaterialTheme.typography.bodyMedium)
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
                        Text(text = player.getTotalGain().toString(), style = MaterialTheme.typography.bodyMedium)
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
                        Text(text = player.getTotalLoss().toString(), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                }
            }
        }
    }
}

@Preview
@Composable
fun LeaderBoardPreview() {
    val player = LeaderBoardPlayer(
        name = "Player",
        wins = 1,
        totalScore = 1000,
        winStreak = 1,
        totalChombas = 1,
        soreList = emptyList(),
        colors = listOf(Color.Red, Color.Blue, Color.Green)
    )
    LeaderBoardItem(player = player, position = 2)
}