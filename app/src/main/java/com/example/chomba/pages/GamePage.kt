package com.example.chomba.pages

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chomba.GameViewModel
import com.example.chomba.R
import com.example.chomba.data.CardSuit
import com.example.chomba.data.Player
import com.example.chomba.data.Score
import com.example.chomba.data.getBarrel
import com.example.chomba.data.getDissolution
import com.example.chomba.data.getMissBarrel
import com.example.chomba.data.getTotalScore
import com.example.chomba.data.getZeroNum
import com.example.chomba.ui.theme.Shapes
import com.example.chomba.ui.theme.composable.BasicIconButton
import com.example.chomba.ui.theme.composable.Chart
import com.example.chomba.ui.theme.composable.CircularChart
import com.example.chomba.ui.theme.composable.IconButton
import com.example.chomba.ui.theme.composable.Picker
import com.example.chomba.ui.theme.composable.ResizableText
import com.example.chomba.ui.theme.composable.SaveGame
import com.example.chomba.ui.theme.composable.Tips
import com.example.chomba.ui.theme.composable.TopBar
import com.example.chomba.ui.theme.composable.rememberPickerState
import com.example.chomba.ui.theme.ext.basicButton

@Composable
fun GamePage(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel
){
    val uiState by viewModel.uiState
    val playerList by viewModel.playerList
    val nextRound = remember { mutableStateOf(false) }
    val setDeclarer = remember { mutableStateOf(false) }
    val showTip = remember { mutableStateOf(false) }
    val saveAlert = remember { mutableStateOf(false) }
    val isMenuExpanded = remember { mutableStateOf(false) }
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        TopBar(
            title = stringResource(R.string.round) + " " + viewModel.getCurrentRound().toString(),
            onFirstActionClick = { viewModel.setCurrentPage(0) },
            secondButtonIcon = R.drawable.baseline_menu_24,
            onSecondActionClick = {isMenuExpanded.value = true},
            secondIconEnabled = true,
            isMenuExpanded = true,
            menu = {
                val buttonsWithIcons = listOf(
                    R.drawable.baseline_lightbulb_24 to stringResource(R.string.tips),
                    R.drawable.baseline_save_24 to stringResource(R.string.save_game),
                    R.drawable.baseline_blind_24 to stringResource(R.string.set_declarer),
                )

                Dropdown(
                    buttonsWithIcons = buttonsWithIcons,
                    onItemClick = { selectedItem ->
                        when (selectedItem) {
                            buttonsWithIcons[0].second -> showTip.value = true
                            buttonsWithIcons[1].second -> {
                                viewModel.saveGame()
                                saveAlert.value = true
                            }
                            buttonsWithIcons[2].second -> setDeclarer.value = true
                        }
                    },
                    icon = R.drawable.baseline_more_vert_24
                )
            }
        )

        Surface(
            modifier = modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .weight(1f),
        ) {
            Column(
                modifier = Modifier.wrapContentSize(),
            ) {
                for(i in 0 until 3){
                    val player = playerList[i]
                    PlayerCard(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 4.dp)
                            .aspectRatio(if (uiState.declarer == null) 2.2f else 1.65f),
                        player = player,
                        onSave = {viewModel.saveScorePerRound(player, it)},
                        takeChomba = {viewModel.takeChomba(player, it)},
                        undoChomba = {viewModel.undoChomba(player, it)},
                        declarer = uiState.declarer,
                        showScoreList = {viewModel.showScoreList(player, true)},
                        isDistributor = player.name == playerList[uiState.distributorIndex].name,
                        setScorePerRoundD = {viewModel.setScorePerRoundD(player)},
                        setBlind = {viewModel.setBlind(player)}
                    )
                }
                if(uiState.declarer == null){
                    Chart(modifier = Modifier
                        .weight(1f)
                        .padding(top = 4.dp)
                        .aspectRatio(2.2f),
                        playerList = viewModel.playerList.value)
                }
            }
        }



        if(viewModel.isCurrentGameFinished()){
            BasicIconButton(text = R.string.save_and_exit,
                icon = R.drawable.baseline_home_24,
                modifier = Modifier.basicButton(),
                action = {viewModel.setCurrentPage(0)
                    viewModel.saveGame()})
        }
        else if(uiState.declarer != null){
            Row{
                if(uiState.playerOnBarrel?.name != uiState.declarer?.name){
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

    if(saveAlert.value){
        SaveGame(onDismissRequest = {saveAlert.value = false},
            msg = uiState.saveMsg)
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

    if(showTip.value){
        Tips(onDismissRequest = {showTip.value = false},
            msg = R.string.tips)
    }

    if(setDeclarer.value){

        val players = remember { viewModel.getPlayersName() }
        val playersValue = rememberPickerState()

        val scores = remember { (100..420 step 5).map { it.toString() } }
        val scoresValue = rememberPickerState()
        val startIndex = remember {mutableIntStateOf(0)}
        val currentScore = remember {mutableIntStateOf(100)}

        AlertDialog(
            onDismissRequest = { setDeclarer.value = false },
            title = { 
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.set_declarer),
                        style = MaterialTheme.typography.headlineSmall)
                    VoiceRecognitionButton(
                        modifier = Modifier.weight(1f),
                        onRecognized = { recognizedScore ->
                            if (recognizedScore < 100) {
                                val hundreds = (currentScore.intValue / 100) * 100
                                val newScore = hundreds + recognizedScore
                                val index = scores.indexOf(newScore.toString())
                                if (index != -1) {
                                    startIndex.intValue = index
                                }
                            } else {
                                currentScore.intValue = recognizedScore
                                val index = scores.indexOf(recognizedScore.toString())
                                if (index != -1) {
                                    startIndex.intValue = index
                                }
                            }
                        },
                        viewModel = viewModel.voiceRec)
                }},
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
                            startIndex = startIndex.intValue
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
                        Text(text = player.name + " : " + player.scorePerRound.toString() + if(player.blind) " x2" else "",
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

@Composable
fun CenterStripesText(
    text: String,
    stripeColor: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Divider(
            color = stripeColor,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Divider(
            color = stripeColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToggleUnderlineText(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    isUnderlined: Boolean = true,
) {
    Text(
        text = text,
        modifier = modifier
            .combinedClickable(
            onClick = { onClick() }
        ),
        style = if (!isUnderlined) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline
        ),
        textAlign = TextAlign.Center
    )
}

@Composable
fun ChombaCard(
    iconSize: Dp,
    painter: Painter,
    text: String,
    ){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .size(iconSize)
                .weight(1f),
        )
        Text(text = text,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center)
    }
}

@Composable
fun RepeatIcon(
    modifier: Modifier = Modifier,
    iconSize: Dp,
    painter: Painter,
    number: Int,
){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))
        for(i in 0 until number){
            Icon(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .size(iconSize)
                    .weight(1f),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerCard(
    modifier: Modifier,
    player: Player,
    onSave:(Int) -> Unit,
    takeChomba: (Int) -> Unit,
    undoChomba: (Int) -> Unit,
    declarer: Player?,
    showScoreList: () -> Unit,
    isDistributor: Boolean,
    setScorePerRoundD: () -> Unit,
    setBlind: () -> Unit
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
        Column{
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_hail_24),
                                contentDescription = null,
                                modifier = Modifier.padding(start = 16.dp),
                                tint = if (!isDistributor) Color.Transparent
                                else MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                            ResizableText(
                                text = player.name,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(2f),
                            )
                            Text(text = if (player.name == declarer?.name) player.declaration.toString()
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
                        Box(
                            modifier = Modifier
                                .weight(2f),
                            contentAlignment = Alignment.Center
                        ) {
                            Row {
                                Box(modifier = Modifier.weight(1f)) {
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
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                horizontalArrangement = Arrangement.SpaceAround,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    painter = painterResource(
                                                        id = R.drawable.baseline_border_color_24
                                                    ),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp),
                                                )
                                                Text(
                                                    text = player.getDissolution().toString(),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    modifier = Modifier.weight(1f),
                                                    textAlign = TextAlign.Center
                                                )

                                            }
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                horizontalArrangement = Arrangement.SpaceAround,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    painter = painterResource(
                                                        id = R.drawable.baseline_horizontal_rule_24
                                                    ),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp),
                                                )
                                                Text(
                                                    text = player.getZeroNum().toString(),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    modifier = Modifier.weight(1f),
                                                    textAlign = TextAlign.Center
                                                )

                                            }
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                horizontalArrangement = Arrangement.SpaceAround,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    painter = painterResource(
                                                        id = R.drawable.ic_1200952
                                                    ),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp),
                                                )
                                                Text(
                                                    text = player.getBarrel().toString(),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    modifier = Modifier.weight(1f),
                                                    textAlign = TextAlign.Center
                                                )

                                            }
                                        }
                                    }
                                }
                                Box(modifier = Modifier.weight(1f)) {
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
                                            onSelect = { onSave(it) },
                                            startScore = player.scorePerRound
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularChart(
                        modifier = Modifier
                            .fillMaxSize(0.75f),
                        pressModifier = Modifier
                            .combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    showScoreList()
                                },
                                onDoubleClick = { setBlind() }),
                        value = player.getTotalScore(),
                        maxValue = 1000,
                        color = Color(player.color.toULong()),
                        zeroNum = if (player.getTotalScore() == 880) player.getMissBarrel()
                        else 0,
                        blind = player.blind,
                    )
                }
            }

            AnimatedVisibility(visible = declarer != null) {
                Divider(modifier = Modifier.fillMaxWidth())

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(3.dp, 0.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (suit in CardSuit.values()){
                        val isTaken = player.takenChombas.contains(suit)
                        IconButton(
                            icon = suitIcon(suit.ordinal),
                            modifier = Modifier
                                .weight(1f)
                                .padding(1.dp, 0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTaken) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.background,
                                contentColor = if (suit.ordinal > 1) Color.Red
                                else MaterialTheme.colorScheme.onBackground
                            ),
                            action = {
                                if (!isTaken) takeChomba(suit.ordinal)
                                else undoChomba(suit.ordinal)
                            }
                        )
                    }
                }
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
            modifier = Modifier
                .size(24.dp)
                .weight(1f))
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

@Composable
fun Dropdown(
    buttonsWithIcons: List<Pair<Int, String>>,
    onItemClick: (String) -> Unit,
    icon: Int
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(
        icon = icon,
        modifier = Modifier
            .fillMaxHeight()
            .padding(2.dp),
        action = { expanded = true }
    )

    Box(
        contentAlignment = Alignment.TopEnd,
        modifier = Modifier
            .fillMaxHeight()
            .padding(end = 8.dp)
    ) {


        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier

                .background(MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            buttonsWithIcons.forEach { (buttonIcon, buttonText) ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            Icon(
                                painter = painterResource(id = buttonIcon),
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp),
                            )
                            Text(text = buttonText)
                        }
                    },
                    onClick = {
                        onItemClick(buttonText)
                        expanded = false
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                )
            }
        }
    }
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

@DrawableRes
fun suitIcon(suit: Int): Int {
    return when (suit) {
        0 -> R.drawable.ic_pica
        1 -> R.drawable.ic_trebol
        2 -> R.drawable.ic_diamante
        3 -> R.drawable.ic_corazon
        else -> R.drawable.baseline_square_24
    }
}

