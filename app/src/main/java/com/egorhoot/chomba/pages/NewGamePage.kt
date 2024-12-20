package com.egorhoot.chomba.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.egorhoot.chomba.GameViewModel
import com.egorhoot.chomba.R
import com.egorhoot.chomba.data.Player
import com.egorhoot.chomba.ui.theme.composable.BasicIconButton
import com.egorhoot.chomba.ui.theme.composable.BasicTextButton
import com.egorhoot.chomba.ui.theme.composable.CircleLoader
import com.egorhoot.chomba.ui.theme.composable.IconButton
import com.egorhoot.chomba.ui.theme.composable.TopBar
import com.egorhoot.chomba.ui.theme.ext.basicButton
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import kotlinx.coroutines.channels.Channel
import kotlin.math.roundToInt

@Composable
fun NewGamePage(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel
) {
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
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ){
            Column(modifier = modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Top) {
                DraggablePlayerList(
                    playerList = playerList,
                    onMove = { fromIndex, toIndex -> viewModel.movePlayer(fromIndex, toIndex) },
                    onPlayerAction = { player -> viewModel.removePlayer(player) },
                    onSave = { player, name, color -> viewModel.updatePlayer(player ,name, color) }
                )

            }
        }

        Column(
            modifier = modifier
                .weight(1f)
                .padding(horizontal = 64.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircleLoader(visible = uiState.inProgress)
            AnimatedVisibility(viewModel.getNumberOfVisiblePlayers() < 3) {
                BasicIconButton(
                    text = R.string.add_player,
                    icon = R.drawable.baseline_add_24,
                    modifier = modifier.basicButton(),
                    action = { viewModel.addPlayer() }
                )
            }
            if (viewModel.isAuthorized()){
                BasicTextButton(
                    text = R.string.get_players_from_last_game,
                    modifier = modifier
                        .basicButton()
                        .padding(bottom = 4.dp),
                    action = { viewModel.getPlayersFromLastGame() }
                )
            }
            AnimatedVisibility(viewModel.getNumberOfVisiblePlayers() == 3) {
                BasicTextButton(
                    text = R.string.start,
                    modifier = modifier
                        .basicButton()
                        .padding(bottom = 16.dp),
                    action = { viewModel.startGame() }
                )
            }
        }
    }
}

@Composable
fun DraggablePlayerList(
    playerList: List<Player>,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    onPlayerAction: (Player) -> Unit,
    onSave: (Player, String, String) -> Unit,
    height: Int = 64
) {
    val draggedIndex = remember { mutableStateOf(-1) }
    val offsetY = remember { mutableStateOf(0f) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = false
    ) {
        itemsIndexed(playerList) { index, player ->
            val isDragging = draggedIndex.value == index
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height.dp)
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                draggedIndex.value = index
                            },
                            onDragEnd = {
                                draggedIndex.value = -1
                                offsetY.value = 0f
                            },
                            onDragCancel = {
                                draggedIndex.value = -1
                                offsetY.value = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offsetY.value += dragAmount.y*0.5f

                                val newIndex = calculateNewIndex(
                                    draggedIndex.value,
                                    offsetY.value,
                                    playerList.size,
                                    height.toFloat()
                                )
                                if (newIndex != draggedIndex.value) {
                                    onMove(draggedIndex.value, newIndex)
                                    draggedIndex.value = newIndex
                                    offsetY.value = 0f
                                }
                            }
                        )
                    }
                    .offset {
                        if (isDragging) {
                            IntOffset(0, offsetY.value.roundToInt())
                        } else {
                            IntOffset(0, 0)
                        }
                    }
            ) {
                PlayerItem(
                    modifier = Modifier.fillMaxSize(),
                    player = player,
                    onDelete = { onPlayerAction(player) },
                    onSave = { name, color ->
                        onSave(player, name, color)
                    },
                    isLast = index == playerList.size - 1,
                    isDragging = isDragging
                )

            }
        }
    }
}

fun calculateNewIndex(currentIndex: Int, offsetY: Float, listSize: Int, height: Float): Int {
    val newIndex = (currentIndex + (offsetY / height).toInt()).coerceIn(0, listSize - 1)
    return newIndex
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerItem(
    modifier: Modifier = Modifier,
    player: Player,
    onDelete: () -> Unit,
    onSave: (String, String) -> Unit,
    isLast: Boolean = false,
    isDragging: Boolean = false
){
    val focusManager = LocalFocusManager.current
    val userName = remember { mutableStateOf(player.name) }
    userName.value = player.name
    val color = remember { mutableStateOf(player.color) }
    color.value = player.color

    Row(
        modifier = modifier.fillMaxWidth()
            .border(2.dp, if (isDragging) MaterialTheme.colorScheme.tertiary else Color.Transparent,
                MaterialTheme.shapes.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.baseline_drag_handle_24),
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
        OutlinedTextField(
            value = userName.value,
            onValueChange = { newValue ->
                userName.value = newValue
                onSave(userName.value, color.value)
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = if(isLast) ImeAction.Done else ImeAction.Next,
                keyboardType = KeyboardType.Text,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .fillMaxHeight()
                .padding(vertical = 4.dp),
            singleLine = true,
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
            trailingIcon = {
                ColorPickerButton(
                    startColor = Color(color.value.toULong()),
                    onColorSelected = {
                        color.value = it.value.toString()},
                    saveColor = { onSave(userName.value, color.value)}
                )
            },
            placeholder = { Text(stringResource(R.string.player)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
            )
        )

        IconButton(icon = R.drawable.baseline_delete_24,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
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