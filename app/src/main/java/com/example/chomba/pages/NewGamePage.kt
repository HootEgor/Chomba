package com.example.chomba.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chomba.GameViewModel
import com.example.chomba.R
import com.example.chomba.data.Player
import com.example.chomba.ui.theme.composable.BasicIconButton
import com.example.chomba.ui.theme.composable.BasicTextButton
import com.example.chomba.ui.theme.composable.IconButton
import com.example.chomba.ui.theme.composable.TopBar
import com.example.chomba.ui.theme.ext.basicButton
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NewGamePage(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel
){
    val uiState by viewModel.uiState
    val playerList by viewModel.playerList
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TopBar(
            title = stringResource(R.string.new_game),
            onFirstActionClick = { viewModel.setCurrentPage(0) }
        )
        Spacer(modifier = modifier.height(32.dp))
        Box(
            modifier = modifier
                .weight(2f)
                .padding(horizontal = 64.dp),
            contentAlignment = Alignment.Center
        ){
            Column(modifier = modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Top) {
                LazyColumn {
                    items(playerList) { item ->
                        AnimatedVisibility(
                            visible = item.visible
                        ) {
                            PlayerItem(
                                player = item,
                                onDelete = { viewModel.removePlayer(item)},
                                onSave = { name, color ->
                                    viewModel.updatePlayer(item, name, color)
                                }
                            )
                        }
                    }
                }

                AnimatedVisibility(viewModel.getNumberOfVisiblePlayers() < 4){
                BasicIconButton(text = R.string.add_player,
                    icon = R.drawable.baseline_add_24,
                    modifier = modifier.basicButton(),
                    action ={ viewModel.addPlayer() })
                }
            }
        }

        Box(
            modifier = modifier
                .weight(1f)
                .padding(horizontal = 64.dp),
            contentAlignment = Alignment.Center
        ){
            this@Column.AnimatedVisibility(viewModel.getNumberOfVisiblePlayers() > 1){
                BasicTextButton(text = R.string.start,
                    modifier = modifier
                        .basicButton()
                        .padding(bottom = 16.dp),
                    action = {viewModel.startGame()})
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerItem(
    modifier: Modifier = Modifier,
    player: Player,
    onDelete: () -> Unit,
    onSave: (String, Color) -> Unit
){
    val userName = remember { mutableStateOf(player.name) }
    userName.value = player.name
    val color = remember { mutableStateOf(player.color) }
    color.value = player.color

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = userName.value,
            onValueChange = { newValue ->
                userName.value = newValue
                onSave(userName.value, color.value)
            },
            label = { Text(stringResource(R.string.player))},
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            ),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .fillMaxHeight()
                .padding(bottom = 8.dp),
            singleLine = true,
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
            trailingIcon = {
                ColorPickerButton(
                    startColor = color.value,
                    onColorSelected = {
                        color.value = it},
                    saveColor = { onSave(userName.value, color.value)}
                )
            }
        )

        IconButton(icon = R.drawable.baseline_delete_24,
            modifier = Modifier.fillMaxSize(),
            action = { onDelete() })
    }
}

@Composable
fun ColorPickerButton(
    startColor: Color,
    onColorSelected: (Color) -> Unit,
    saveColor: () -> Unit
) {
    val selectedColor = remember { mutableStateOf(startColor) }
    selectedColor.value = startColor
    val showDialog = remember { mutableStateOf(false) }
    val controller = rememberColorPickerController()

    Column {
        IconButton(
            icon = R.drawable.baseline_square_24,
            modifier = Modifier.size(24.dp),
            action = {showDialog.value = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = selectedColor.value
            ),
            noIcon = true
        )

        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    showDialog.value = false
                },
                title = { },
                text = {
                    HsvColorPicker(
                        modifier = Modifier.size(300.dp),
                        controller = controller,
                        onColorChanged = { colorEnvelope: ColorEnvelope ->
                            selectedColor.value = colorEnvelope.color
                            onColorSelected(selectedColor.value)
                        },
                        initialColor = startColor
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            saveColor()
                            showDialog.value = false
                        }
                    ) {
                        Text(text = stringResource(R.string.ok))
                    }

                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDialog.value = false
                        }
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            )

        }
    }
}