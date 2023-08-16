package com.example.chomba.pages

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chomba.GameViewModel
import com.example.chomba.R
import com.example.chomba.data.Player
import com.example.chomba.data.Score
import com.example.chomba.data.getBarrel
import com.example.chomba.data.getDissolution
import com.example.chomba.data.getMissBarrel
import com.example.chomba.data.getTotalScore
import com.example.chomba.data.getZeroNum
import com.example.chomba.ui.theme.Shapes
import com.example.chomba.ui.theme.composable.BasicIconButton
import com.example.chomba.ui.theme.composable.BasicTextButton
import com.example.chomba.ui.theme.composable.IconButton
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
                        showScoreList = {viewModel.showScoreList(player, true)},
                        isDistributor = player.name == playerList[uiState.distributorIndex].name,
                        setScorePerRoundD = {viewModel.setScorePerRoundD(player)}
                    )
                }
            }
        }

        if(uiState.declarer != null){
            Row{
                if(uiState.playerOnBarrel == null){
                    val visible = remember { mutableStateOf(false) }
                    BasicIconButton(text = R.string.dissolution,
                        icon = R.drawable.baseline_border_color_24,
                        modifier = Modifier
                            .basicButton()
                            .weight(1f),
                        action = {visible.value = true})

                    if(visible.value){
                        AlertDialog(
                            onDismissRequest = {visible.value = false},
                            title = { Text(text = stringResource(R.string.make_dissolution), style = MaterialTheme.typography.headlineSmall) },
                            text = {
                                Text(text = stringResource(R.string.are_you_sure),
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center)
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = { visible.value= false
                                        viewModel.makeDissolution()}
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
                                    onClick = { visible.value= false}
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

                }

                BasicIconButton(text = R.string.next_round,
                    icon = R.drawable.baseline_calculate_24,
                    modifier = Modifier
                        .basicButton()
                        .weight(1f),
                    action = {nextRound.value = true})
            }

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

        val scores = remember { (100..420 step 5).map { it.toString() } }
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
                            startIndex = 0
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
                            startIndex = 0
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
            setVisible = {viewModel.showScoreList(null, false)},
            onMakePenalty = { viewModel.makePenalty(it) }) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerCard(
    modifier: Modifier,
    player: Player,
    onSave:(Int) -> Unit,
    declarer: Player?,
    showScoreList: () -> Unit,
    isDistributor: Boolean,
    setScorePerRoundD: () -> Unit
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
                       Icon(
                           painter = painterResource(id = R.drawable.baseline_hail_24),
                           contentDescription = null,
                            modifier = Modifier.padding(start = 16.dp),
                           tint = if(!isDistributor) Color.Transparent
                           else MaterialTheme.colorScheme.onTertiaryContainer,
                       )
                       Text(text = player.name,
                           style = MaterialTheme.typography.titleMedium,
                           modifier = Modifier.weight(2f),
                           textAlign = TextAlign.Center)
                       Text(text = if(player.name == declarer?.name) player.declaration.toString()
                       else "",
                           style = MaterialTheme.typography.titleLarge,
                           modifier = Modifier
                               .weight(1f)
                               .combinedClickable(
                                   onClick = {},
                                   onLongClick = { setScorePerRoundD() }
                               ),
                           textAlign = TextAlign.Center)
                   }
                   Box(modifier = Modifier
                           .weight(2f),
                       contentAlignment = Alignment.Center){
                       Row{
                           Box(modifier = Modifier.weight(1f)){
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
                                   Column(
                                       modifier = Modifier
                                           .fillMaxSize()
                                           .padding(start = 8.dp)
                                           .padding(vertical = 4.dp),
                                       verticalArrangement = Arrangement.SpaceEvenly,
                                       horizontalAlignment = Alignment.CenterHorizontally
                                   ) {
                                       Row (modifier = Modifier.weight(1f),
                                           horizontalArrangement = Arrangement.SpaceAround,
                                           verticalAlignment = Alignment.CenterVertically){
                                           Icon(
                                               painter = painterResource(
                                                   id = R.drawable.baseline_border_color_24
                                               ),
                                               contentDescription = null,
                                               modifier = Modifier.size(24.dp),
                                           )
                                           Text(text = player.getDissolution().toString(),
                                               style = MaterialTheme.typography.titleMedium,
                                               modifier = Modifier.weight(1f),
                                               textAlign = TextAlign.Center)

                                       }
                                       Row (modifier = Modifier.weight(1f),
                                           horizontalArrangement = Arrangement.SpaceAround,
                                           verticalAlignment = Alignment.CenterVertically){
                                           Icon(
                                               painter = painterResource(
                                                   id = R.drawable.baseline_horizontal_rule_24
                                               ),
                                               contentDescription = null,
                                               modifier = Modifier.size(24.dp),
                                           )
                                           Text(text = player.getZeroNum().toString(),
                                               style = MaterialTheme.typography.titleMedium,
                                               modifier = Modifier.weight(1f),
                                               textAlign = TextAlign.Center)

                                       }
                                       Row (modifier = Modifier.weight(1f),
                                           horizontalArrangement = Arrangement.SpaceAround,
                                           verticalAlignment = Alignment.CenterVertically){
                                           Image(
                                               painter = painterResource(
                                                   id = R.drawable.ic_1200952
                                               ),
                                               contentDescription = null,
                                               modifier = Modifier.size(24.dp),
                                           )
                                           Text(text = player.getBarrel().toString(),
                                               style = MaterialTheme.typography.titleMedium,
                                               modifier = Modifier.weight(1f),
                                               textAlign = TextAlign.Center)

                                       }
                                   }
                               }
                           }
                           Box(modifier = Modifier.weight(1f)){
                               Surface(
                                   modifier = Modifier
                                       .fillMaxSize()
                                       .padding(vertical = 4.dp)
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
                    else 0
               )
           }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScoreListAlert(
    modifier: Modifier = Modifier,
    player: Player,
    setVisible: (Boolean) -> Unit,
    onMakePenalty: () -> Unit
){
    val penalty = remember { mutableStateOf(false) }
    AlertDialog(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f),
        onDismissRequest = { setVisible(false)},
        title = {
            Text(text = stringResource(R.string.player_score_list) +" "+ player.name,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = {penalty.value = true}
                ))
                },
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
        },
        dismissButton = {
            AnimatedVisibility(penalty.value ){
                TextButton(
                    onClick = {onMakePenalty()
                        penalty.value = false
                        setVisible(false)}
                ) {
                    Text(
                        text = stringResource(R.string._120),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

        }
    )

}

@Composable
fun ScoreCard(
    score: Score
){
    val prefix = if(score.type == -1 || score.type == -4 ) "-"
    else ""
    Row {
        Text(text = prefix + score.value.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center)
        Icon(painter = painterResource(id = typeIcon(score.type)),
            contentDescription = null,
            modifier = Modifier.size(24.dp).weight(1f))
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
                        Image(
                            painter = painterResource(
                                id = R.drawable.ic_1200952
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
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
    val scores = remember { (0..420 step 5).map { it.toString() } }
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

@DrawableRes
fun typeIcon(type: Int): Int {
    return when (type) {
        -1 -> R.drawable.baseline_close_24
        0 -> R.drawable.baseline_horizontal_rule_24
        1 -> R.drawable.baseline_check_24
        2, -2, -4 -> R.drawable.ic_1200952
        3 -> R.drawable.ic_gift
        -3 -> R.drawable.baseline_border_color_24
        else -> R.drawable.baseline_square_24
    }
}

