package com.egorhoot.chomba.pages.onlinegame

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.egorhoot.chomba.R
import com.egorhoot.chomba.data.Player
import com.egorhoot.chomba.data.getTotalScore
import com.egorhoot.chomba.ui.theme.Shapes
import com.egorhoot.chomba.ui.theme.composable.BasicTextButton
import com.egorhoot.chomba.ui.theme.composable.CardView
import com.egorhoot.chomba.ui.theme.composable.ScoreBar
import com.egorhoot.chomba.ui.theme.composable.TopBar
import com.egorhoot.chomba.util.StringProvider
import kotlinx.coroutines.delay

@Composable
fun OnLineGame(
    modifier: Modifier = Modifier,
    viewModel: OnLineGameViewModel = hiltViewModel(),
) {
    val uiState = viewModel.onLineGameUiState.value
    val stringProvider = StringProvider(LocalContext.current)
    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopBar(
                title = uiState.topBarText,
                onFirstActionClick = { viewModel.homePage()},
                secondButtonIcon = R.drawable.baseline_content_copy_24,
                onSecondActionClick = {},
                secondIconEnabled = true
            )
            Column(
                modifier = modifier.fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (viewModel.isPlayerListNotEmpty()){
                    Row(){
                        for (player in viewModel.getOtherPlayers()){
                            OpponentCard(
                                modifier = Modifier
                                    .weight(1f),
                                player = player,
                                isAction = player.name == uiState.game.currentActionPlayer)
                        }
                    }

                    if(uiState.game.game.uiState.declarer != null){
                        Text(
                            text = uiState.game.game.uiState.declarer!!.name + ": " + uiState.game.game.uiState.declarer!!.declaration,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(0.dp, 2.dp),
                        )
                    }

                    Row (
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.33f)
                    ) {
                        for(card in uiState.game.pricup){
                            CardView(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(1.dp),
                                card = if(uiState.game.isBindingEnd) card else null,
                                onClick = {
                                }
                            )
                        }
                    }
                    ScoreBar(
                        score = uiState.game.game.playerList.first { it.name == viewModel.thatUserName }.getTotalScore(),
                        modifier = Modifier.fillMaxWidth(0.9f).padding(0.dp, 4.dp)
                    )
                    //lazy table for cards 4 cards per row
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                    ) {
                        itemsIndexed(uiState.game.game.playerList.first { it.name == viewModel.thatUserName }.hand.toList()) { index, card ->
                            CardView(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(1.dp),
                                card = card,
                                onClick = {
                                }
                            )
                        }
                    }

                    if(uiState.game.isBindingEnd){
                        BasicTextButton(
                            text = stringProvider.getString("submit"),
                            action = {
                                //viewModel.submit()
                            },
                            modifier = Modifier.fillMaxWidth(0.6f).padding(0.dp, 4.dp).weight(1f)
                        )
                    }else{
                        Row{
                            BasicTextButton(
                                text = stringProvider.getString("pass"),
                                action = {
                                    viewModel.pass()
                                },
                                modifier = Modifier.fillMaxWidth(0.4f).padding(0.dp, 4.dp).weight(1f),
                                isEnabled = uiState.game.currentActionPlayer == viewModel.thatUserName
                            )
                            BasicTextButton(
                                text = stringProvider.getString("_5"),
                                action = {
                                    viewModel.binding()
                                },
                                modifier = Modifier.fillMaxWidth(0.4f).padding(0.dp, 4.dp).weight(1f),
                                isEnabled = uiState.game.currentActionPlayer == viewModel.thatUserName
                            )
                        }
                    }


                }

            }
        }

    }
}

@Composable
fun OpponentCard(
    modifier: Modifier = Modifier,
    player: Player,
    isAction: Boolean = false,
){
    val currentColor = remember { mutableStateOf(Color(player.color.toULong())) }

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

    val picture = player.userPicture
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, Shapes.medium)
            .aspectRatio(1f)
            . border(
                width = if (isAction) 3.dp else 0.dp,
                color = if (isAction) animatedColor.value else Color.Transparent,
                shape = Shapes.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (picture != "") {
            AsyncImage(
                model = picture,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight(0.5f)
                    .aspectRatio(1f).background(MaterialTheme.colorScheme.surface, Shapes.medium)
                    .clip(Shapes.medium)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight(0.5f)
                    .aspectRatio(1f).background(MaterialTheme.colorScheme.surface, Shapes.medium)
                    .clip(Shapes.medium)
            )
        }
        Text(
            text = player.name,
            style = MaterialTheme.typography.titleSmall
        )
        ScoreBar(
            score = player.getTotalScore(),
            modifier = Modifier.fillMaxWidth(0.9f)
        )
    }
}

