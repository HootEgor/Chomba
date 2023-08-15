package com.example.chomba.pages

import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chomba.GameViewModel
import com.example.chomba.R
import com.example.chomba.data.Player
import com.example.chomba.data.Score
import com.example.chomba.data.getMissBarrel
import com.example.chomba.data.getTotalScore
import com.example.chomba.data.getZeroNum
import com.example.chomba.ui.theme.Shapes
import com.example.chomba.ui.theme.composable.BasicIconButton
import com.example.chomba.ui.theme.composable.BasicTextButton
import com.example.chomba.ui.theme.composable.Picker
import com.example.chomba.ui.theme.composable.TopBar
import com.example.chomba.ui.theme.composable.rememberPickerState
import com.example.chomba.ui.theme.ext.basicButton
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GamePage(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel
){
    val uiState by viewModel.uiState
    val playerList by viewModel.playerList
    val nextRound = remember { mutableStateOf(false) }
    val setDeclarer = remember { mutableStateOf(false) }
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TopBar(
            title = stringResource(R.string.round) + " " + viewModel.getCurrentRound().toString(),
            onFirstActionClick = { viewModel.setCurrentPage(0) },
            modifier = Modifier.combinedClickable(
                onClick = {},
                onLongClick = {setDeclarer.value = true}
            )
        )
        Surface(
            modifier = modifier
                .fillMaxSize(0.9f),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
            ) {
                items(playerList) { player ->
                    PlayerCard(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 4.dp)
                            .aspectRatio(2.2f),
                        player = player,
                        onSave = {viewModel.saveScorePerRound(player, it)},
                        declarer = uiState.declarer,
                        showScoreList = {viewModel.showScoreList(player, true)}
                    )
                }
            }
        }

        if(uiState.declarer != null){
            BasicIconButton(text = R.string.next_round,
                icon = R.drawable.baseline_calculate_24,
                modifier = Modifier.basicButton(),
                action = {nextRound.value = true})
        }
        else{
            BasicIconButton(text = R.string.set_declarer,
                icon = R.drawable.baseline_blind_24,
                modifier = Modifier.basicButton(),
                action = {setDeclarer.value = true})
        }


    }

    if(uiState.winner != null){
        AlertDialog(
            onDismissRequest = {viewModel.setWinner(null)},
            title = { Text(text = stringResource(R.string.winner), style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(text = uiState.winner?.name ?: stringResource(R.string.error),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center)
            },
            confirmButton = {
            },
            dismissButton = {
            }
        )
    }

    if(setDeclarer.value){

        val players = remember { viewModel.getPlayersName() }
        val playersValue = rememberPickerState()

        val scores = remember { (100..420).map { it.toString() } }
        val scoresValue = rememberPickerState()

        AlertDialog(
            onDismissRequest = { setDeclarer.value = false },
            title = { Text(text = stringResource(R.string.set_declarer), style = MaterialTheme.typography.headlineSmall) },
            text = {
                Surface(shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(vertical = 2.dp),

                    ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Picker(
                            state = playersValue,
                            items = players,
                            visibleItemsCount = 3,
                            modifier = modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.Center)
                                .weight(1f),
                            textModifier = Modifier.padding(4.dp),
                            textStyle = TextStyle(fontSize = 16.sp),
                            startIndex = players.indexOf(uiState.declarer?.name)
                        )

                        Picker(
                            state = scoresValue,
                            items = scores,
                            visibleItemsCount = 3,
                            modifier = modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.Center)
                                .weight(1f),
                            textModifier = Modifier.padding(4.dp),
                            textStyle = TextStyle(fontSize = 16.sp),
                            startIndex = scores.indexOf(uiState.declarer?.declaration.toString())
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { setDeclarer.value = false
                        viewModel.setDeclarer(playersValue.selectedItem,
                            scoresValue.selectedItem.toIntOrNull() ?: 0)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.ok),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { setDeclarer.value = false}
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }


    if(nextRound.value){
        AlertDialog(
            onDismissRequest = { nextRound.value = false },
            title = { Text(text = stringResource(R.string.next_round), style = MaterialTheme.typography.headlineSmall) },
            text = {
                Column {
                    for(player in playerList){
                        Text(text = player.name + " : " + player.scorePerRound.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { nextRound.value = false
                        viewModel.nextRound()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.ok),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { nextRound.value = false}
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }

    if(uiState.showScoreList){
        uiState.showPlayer?.let { ScoreListAlert(player = it,
            setVisible = {viewModel.showScoreList(null, false)}) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerCard(
    modifier: Modifier,
    player: Player,
    onSave:(Int) -> Unit,
    declarer: Player?,
    showScoreList: () -> Unit
){

    Surface(
        shape = Shapes.large,
        color = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = if (player.name == declarer?.name) MaterialTheme.colorScheme.onTertiaryContainer
                else MaterialTheme.colorScheme.secondaryContainer,
                shape = Shapes.large
            ),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
           Box(modifier = Modifier.weight(1f)) {
               Column(modifier = Modifier.fillMaxSize(),
                   verticalArrangement = Arrangement.SpaceBetween,
                   horizontalAlignment = Alignment.CenterHorizontally) {
                   Row(modifier = Modifier.weight(1f),
                       verticalAlignment = Alignment.CenterVertically,
                       horizontalArrangement = Arrangement.SpaceEvenly) {
                       Text(text = player.name,
                           style = MaterialTheme.typography.titleMedium,
                           modifier = Modifier.weight(2f),
                           textAlign = TextAlign.Center)
                       Text(text = if(player.name == declarer?.name) player.declaration.toString()
                       else "",
                           style = MaterialTheme.typography.titleLarge,
                           modifier = Modifier.weight(1f),
                           textAlign = TextAlign.Center)
                   }
                   Box(modifier = Modifier
                           .weight(2f),
                       contentAlignment = Alignment.Center){
                       Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = Shapes.large
                                ),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.background,
                       ) {
                           ScorePicker(
                               modifier = Modifier
                                   .fillMaxSize()
                                   .padding(horizontal = 4.dp),
                               onSelect = { onSave(it)},
                               startScore = player.scorePerRound
                           )
                       }
                   }
               }
           }
           Box(modifier = Modifier
               .weight(1f)
               .aspectRatio(1f),
               contentAlignment = Alignment.Center) {
               CircularChart(
                   modifier = Modifier
                          .fillMaxSize(0.75f),
                   pressModifier = Modifier
                       .combinedClickable(
                           onClick = {},
                           onLongClick = {
                               showScoreList()
                           }),
                   value = player.getTotalScore(),
                   maxValue = 1000,
                   color = player.color,
                     zeroNum = if(player.getTotalScore() == 880) player.getMissBarrel()
                    else player.getZeroNum()
               )
           }
        }
    }
}

@Composable
fun ScoreListAlert(
    modifier: Modifier = Modifier,
    player: Player,
    setVisible: (Boolean) -> Unit,
){
    AlertDialog(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f),
        onDismissRequest = { setVisible(false)},
        title = { Text(text = stringResource(R.string.player_score_list) +" "+ player.name, style = MaterialTheme.typography.headlineSmall) },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
            ) {
                items(player.scoreList) { score ->
                    ScoreCard(score = score)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {setVisible(false)}
            ) {
                Text(
                    text = stringResource(R.string.ok),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )

}

@Composable
fun ScoreCard(
    score: Score
){
    Row {
        Text(text = score.value.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center)
        Text(text = score.type.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center)
    }
}

@Composable
fun CircularChart(
    modifier: Modifier,
    pressModifier: Modifier,
    value: Int,
    maxValue: Int,
    color: Color,
    zeroNum: Int,
    backgroundCircleColor: Color = Color.LightGray.copy(alpha = 0.3f),
    thicknessFraction: Float = 0.2f
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
            content = {Column (
                modifier = pressModifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.displaySmall
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(0.5f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ){
                    items(zeroNum){
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(color = Color.Red, shape = CircleShape)
                        )
                    }
                }


            }}
        )


    }
}

@Composable
fun ScorePicker(
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit,
    startScore: Int,
) {
    val scores = remember { (0..420).map { it.toString() } }
    val pickerValue = rememberPickerState()

    LaunchedEffect(pickerValue.selectedItem) {
        onSelect(pickerValue.selectedItem.toIntOrNull() ?: startScore)
    }

    Picker(
        state = pickerValue,
        items = scores,
        visibleItemsCount = 3,
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center),
        textModifier = Modifier.padding(4.dp),
        textStyle = TextStyle(fontSize = 16.sp),
        startIndex = scores.indexOf(startScore.toString())
    )
}

