package com.egorhoot.chomba.pages.user.leaderboard

import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.egorhoot.chomba.R
import com.egorhoot.chomba.data.LeaderBoardPlayer
import com.egorhoot.chomba.data.getScoreText
import com.egorhoot.chomba.pages.user.ProfileViewModel
import com.egorhoot.chomba.ui.theme.Shapes
import com.egorhoot.chomba.ui.theme.composable.BasicTextButton
import com.egorhoot.chomba.ui.theme.composable.FullIconButton
import com.egorhoot.chomba.ui.theme.composable.IconButton
import com.egorhoot.chomba.ui.theme.composable.ResizableText
import com.egorhoot.chomba.ui.theme.ext.smallButton

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
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp, 1.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = Shapes.medium
            ),
        shape = Shapes.medium,
    ) {
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
}