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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.clip
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
import com.egorhoot.chomba.data.User
import com.egorhoot.chomba.pages.user.camera.CameraPermissionRequest
import com.egorhoot.chomba.pages.user.camera.CameraScreen
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
    val profileUi by viewModel.profileUi
    val playerList by viewModel.playerList

    val playerName = remember { mutableStateOf("") }

    if(profileUi.scanQrCode){
        if (!profileUi.cameraPermissionGranted) {
            CameraPermissionRequest(
                modifier = Modifier.padding(bottom = 16.dp),
                permissionDenied = profileUi.cameraPermissionDenied,
                onPermissionGranted = { viewModel.requestCamera() },
                onPermissionDenied = { viewModel.onPermissionDenied() }
            )
        }
        else{
            CameraScreen (
                onGetIds = { userUid ->
                    viewModel.onRecognizeId(userUid, playerName.value)},
                onError = viewModel::onCameraError,
                onBack = viewModel::stopScanner)
        }

        return
    }

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
                    viewModel = viewModel,
                    onMove = { fromIndex, toIndex -> viewModel.movePlayer(fromIndex, toIndex) },
                    onPlayerAction = { player -> viewModel.removePlayer(player) },
                    onSave = { player, name, color -> viewModel.updatePlayer(player ,name, color) },
                    onCameraAction = { name ->
                        playerName.value = name
                        viewModel.startScanner()
                    },
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
//            AnimatedVisibility(viewModel.getNumberOfVisiblePlayers() < 3) {
//                BasicIconButton(
//                    text = R.string.add_player,
//                    icon = R.drawable.baseline_add_24,
//                    modifier = modifier.basicButton(),
//                    action = { viewModel.addPlayer() }
//                )
//            }
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
    viewModel: GameViewModel,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    onPlayerAction: (Player) -> Unit,
    onSave: (Player, String, String) -> Unit,
    height: Int = 64,
    onCameraAction: (String) -> Unit = {}
) {
    val draggedIndex = remember { mutableStateOf(-1) }
    val offsetY = remember { mutableStateOf(0f) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = false
    ) {
        itemsIndexed(playerList, key = { _, player -> player.name }) { index, player ->
            val isDragging = draggedIndex.value == playerList.indexOf(player) //index
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
                    viewModel = viewModel,
                    player = player,
                    onDelete = { onPlayerAction(player) },
                    onSave = { name, color ->
                        onSave(player, name, color)
                    },
                    isLast = index == playerList.size - 1,
                    isDragging = isDragging,
                    onCameraAction = { playerName ->
                        onCameraAction(playerName)
                    }
                )

            }
        }
    }
}

fun calculateNewIndex(currentIndex: Int, offsetY: Float, listSize: Int, height: Float): Int {
    val newIndex = (currentIndex + (offsetY / height).toInt()).coerceIn(0, listSize - 1)
    return newIndex
}




@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerItem(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel,
    player: Player,
    onDelete: () -> Unit,
    onSave: (String, String) -> Unit,
    isLast: Boolean = false,
    isDragging: Boolean = false,
    onCameraAction: (String) -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val userName = remember { mutableStateOf(player.name) }
    val color = remember { mutableStateOf(player.color) }
    val isEditing = remember { mutableStateOf(false) }
    val expanded = remember { mutableStateOf(false) }
    val availableUsers = viewModel.getAvailableUsersToSelect(player.userId ?: "") + User()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(
                2.dp,
                if (isDragging) MaterialTheme.colorScheme.tertiary else Color.Transparent,
                MaterialTheme.shapes.small
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.baseline_drag_handle_24),
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(vertical = 4.dp)
        ) {
            if (isEditing.value) {
                // Editable TextField when long-clicked
                OutlinedTextField(
                    value = userName.value,
                    onValueChange = {
                        userName.value = it
                        onSave(userName.value, color.value)
                    },
                    singleLine = true,
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            isEditing.value = false
                            focusManager.clearFocus()
                        }
                    ),
                    trailingIcon = {
                        ColorPickerButton(
                            startColor = Color(color.value.toULong()),
                            onColorSelected = { color.value = it.value.toString() },
                            saveColor = { onSave(userName.value, color.value) }
                        )
                    },
                    placeholder = { Text(stringResource(R.string.player)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    )
                )
            } else {
                // Clickable Text with Dropdown
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .clickable { expanded.value = true }
                        .combinedClickable(
                            onClick = { expanded.value = true },
                            onLongClick = {
                                if (player.userId.isBlank()){
                                    isEditing.value = true
                                }
                            }
                        )
                        .background(MaterialTheme.colorScheme.background)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = userName.value.ifBlank { stringResource(R.string.player) },
                        style = TextStyle(color = MaterialTheme.colorScheme.onBackground)
                    )
                    ColorPickerButton(
                        startColor = Color(color.value.toULong()),
                        onColorSelected = { color.value = it.value.toString() },
                        saveColor = { onSave(userName.value, color.value) }
                    )
                }

                DropdownMenu(
                    modifier = Modifier.background(MaterialTheme.colorScheme.background),
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false }
                ) {
                    availableUsers.forEach { user ->
                        DropdownMenuItem(
                            modifier = Modifier.background(if (user.id.isBlank()) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.background),
                            text = { Text(user.nickname) },
                            onClick = {
                                viewModel.setPlayerFromUser(user, player.name)
                                userName.value = user.nickname
                                expanded.value = false
                                onSave(user.nickname, color.value)
                            }
                        )
                    }
                }
            }
        }

        IconButton(icon = R.drawable.baseline_camera_24,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            action = {onCameraAction(player.name)})
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