package com.egorhoot.chomba.pages.user.editgame

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.egorhoot.chomba.R
import com.egorhoot.chomba.ui.theme.composable.IconButton
import com.egorhoot.chomba.ui.theme.ext.smallButton

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.egorhoot.chomba.data.Player
import com.egorhoot.chomba.data.Score
import com.egorhoot.chomba.data.getChombas
import com.egorhoot.chomba.pages.ColorPickerButton
import com.egorhoot.chomba.pages.ScoreCard
import com.egorhoot.chomba.pages.suitIcon
import com.egorhoot.chomba.pages.typeIcon
import com.egorhoot.chomba.ui.theme.Shapes

@Composable
fun EditGameScreen(
    modifier: Modifier,
    editViewModel: EditGameViewModel = hiltViewModel(),
    onBack: () -> Unit
){
    val uiState by editViewModel.uiState
    val profileUiState by editViewModel.profileUi
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            for(player in profileUiState.currentGame?.playerList!!){
                EditPlayerCard(
                    modifier = modifier,
                    player = player,
                    viewModel = editViewModel
                )
            }
        }
        EditBottomBar(
            modifier = modifier,
            editViewModel = editViewModel,
            onBack = onBack
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlayerCard(
    modifier: Modifier,
    player: Player,
    viewModel: EditGameViewModel,
){
    val expanded = remember {mutableStateOf(false)}
    Surface (
        shape = Shapes.large,
        color = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = Shapes.large
            )
    ){
        Column(
            modifier = modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .clickable { expanded.value = !expanded.value },
            ) {
                Text(
                    modifier = modifier.padding(16.dp),
                    text = player.name,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            AnimatedVisibility(visible = expanded.value) {
                Column {
                    OutlinedTextField(
                        value = player.name,
                        onValueChange = { newValue ->
                            viewModel.editPlayerName(player, newValue)
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Text,
                        ),
                        modifier = Modifier
                            .padding(4.dp, 16.dp),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium,
                        placeholder = { Text(stringResource(R.string.score)) },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = MaterialTheme.colorScheme.background,
                        ),
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp, 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        items(player.scoreList) { score ->
                            EditScoreCard(score = score,
                                onEditScoreValue = { value ->
                                    viewModel.editScoreValue(player, score, value)
                                },
                                onEditScoreRound = { round ->
                                    viewModel.editScoreRound(player, score, round)
                                })
                        }
                    }
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScoreCard(
    score: Score,
    onEditScoreValue: (Int) -> Unit = {},
    onEditScoreRound: (Int) -> Unit = {}
){
    val scoreValue = remember { mutableIntStateOf(score.value) }
    scoreValue.intValue = score.value

    val scoreRound = remember { mutableIntStateOf(score.round) }
    scoreRound.intValue = score.round

    val prefix = if(score.type == -1 || score.type == -4 ) "-"
    else ""
    Row(
        verticalAlignment = Alignment.CenterVertically
    ){
        OutlinedTextField(
            value = scoreRound.intValue.toString(),
            onValueChange = { newValue ->
                scoreRound.intValue = if(newValue.isEmpty()) 0 else newValue.toInt()
                onEditScoreRound(scoreRound.intValue)
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Decimal,
            ),
            modifier = Modifier
                .padding(4.dp)
                .weight(0.7f),
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium,
            placeholder = { Text(stringResource(R.string.score)) },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(4.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(text = prefix,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier,
                textAlign = TextAlign.Center)
            OutlinedTextField(
                value = scoreValue.intValue.toString(),
                onValueChange = { newValue ->
                    scoreValue.intValue = if(newValue.isEmpty()) 0 else newValue.toInt()
                    onEditScoreValue(scoreValue.intValue)
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Decimal,
                ),
                modifier = Modifier
                    .padding(0.dp, 4.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium,
                placeholder = { Text(stringResource(R.string.score)) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        }

        Row(
            modifier = Modifier.weight(1f),
        ) {
            for(chomba in score.getChombas()){
                Icon(painter = painterResource(id = suitIcon(chomba.ordinal)),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .weight(1f))
            }
        }

        Icon(painter = painterResource(id = typeIcon(score.type)),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .weight(1f))
    }
}


@Composable
fun EditBottomBar(
    modifier: Modifier,
    editViewModel: EditGameViewModel,
    onBack: () -> Unit
){

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(
            icon = R.drawable.baseline_arrow_back_ios_24,
            modifier = modifier
                .smallButton()
                .weight(1f),
            action = onBack
        )
        IconButton(
            icon = R.drawable.baseline_save_24,
            modifier = modifier
                .smallButton()
                .weight(1f),
            action = {editViewModel.saveGame()
                onBack()}
        )
        IconButton(
            icon = R.drawable.baseline_undo_24,
            modifier = modifier
                .smallButton()
                .weight(1f),
            action = {editViewModel.undoGame() }
        )

    }
}