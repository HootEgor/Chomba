package com.example.chomba.pages

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.chargemap.compose.numberpicker.ListItemPicker
import com.example.chomba.GameViewModel
import com.example.chomba.R
import com.example.chomba.data.Player
import com.example.chomba.data.getTotalScore
import com.example.chomba.data.getZeroNum
import com.example.chomba.ui.theme.Shapes
import com.example.chomba.ui.theme.composable.BasicIconButton
import com.example.chomba.ui.theme.composable.TopBar
import com.example.chomba.ui.theme.ext.basicButton

@Composable
fun GamePage(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel
){
    val uiState by viewModel.uiState
    val playerList by viewModel.playerList
    val nextRound = remember { mutableStateOf(false) }
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TopBar(
            title = stringResource(R.string.round) + " " + viewModel.getCurrentRound().toString(),
            onFirstActionClick = { viewModel.setCurrentPage(0) }
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
                        onSave = {viewModel.saveScorePerRound(player, it)}
                    )
                }
            }
        }
        BasicIconButton(text = R.string.next_round,
            icon = R.drawable.baseline_calculate_24,
            modifier = Modifier.basicButton(),
            action = {nextRound.value = true})

    }


    if(nextRound.value){
        AlertDialog(
            onDismissRequest = { nextRound.value = false },
            title = { Text(text = stringResource(R.string.next_round), style = MaterialTheme.typography.headlineSmall) },
            text = {

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
}

@Composable
fun PlayerCard(
    modifier: Modifier,
    player: Player,
    onSave:(Int) -> Unit,
){

    Surface(
        shape = Shapes.large,
        color = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.secondaryContainer,
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
                       Row (modifier = Modifier.weight(1f)){
                           LazyRow(
                               modifier = Modifier.fillMaxSize(),
                               verticalAlignment = Alignment.CenterVertically,
                               horizontalArrangement = Arrangement.SpaceEvenly
                           ){
                               items(player.getZeroNum()){
                                   Box(
                                       modifier = Modifier
                                           .size(10.dp)
                                           .background(color = Color.Red, shape = CircleShape)
                                   )
                               }
                           }
                       }
                   }
                   Box(modifier = Modifier
                           .weight(2f)){
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
                           Box(
                               contentAlignment = Alignment.Center,
                           ){
                               ScorePicker(
                                   modifier = Modifier.fillMaxSize(),
                                   onSelect = { onSave(it)},
                                   startScore = player.scorePerRound
                               )
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
                   modifier = Modifier.fillMaxSize(0.75f),
                   value = player.getTotalScore(),
                   maxValue = 1000,
                   color = player.color
               )
           }
        }
    }
}

@Composable
fun CircularChart(
    modifier: Modifier,
    value: Int,
    maxValue: Int,
    color: Color,
    backgroundCircleColor: Color = Color.LightGray.copy(alpha = 0.3f),
    thicknessFraction: Float = 0.2f
) {
    val sweepAngle = value.toFloat()/ maxValue.toFloat() * 360f

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

        Text(
            text = value.toString(),
            style = MaterialTheme.typography.displaySmall
        )
    }
}

@Composable
fun ScorePicker(
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit,
    startScore: Int,
){
    val scores = (0..420)
    val pickerValue = remember { mutableStateOf(0) }

    NumberPicker(
        value = pickerValue.value,
        range = scores,
        onValueChange = {
            pickerValue.value = it
            onSelect(it)
        }
    )
}

@Composable
fun NumberPicker(
    modifier: Modifier = Modifier,
    label: (Int) -> String = {
        it.toString()
    },
    value: Int,
    onValueChange: (Int) -> Unit,
    dividersColor: Color = MaterialTheme.colorScheme.primary,
    range: Iterable<Int>,
    textStyle: TextStyle = LocalTextStyle.current,
) {
    ListItemPicker(
        modifier = modifier,
        label = label,
        value = value,
        onValueChange = onValueChange,
        dividersColor = dividersColor,
        list = range.toList(),
        textStyle = textStyle
    )
}
