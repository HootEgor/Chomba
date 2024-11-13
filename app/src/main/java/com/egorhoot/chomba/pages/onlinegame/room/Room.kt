package com.egorhoot.chomba.pages.onlinegame.room

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.egorhoot.chomba.R
import com.egorhoot.chomba.pages.onlinegame.OnLineGame
import com.egorhoot.chomba.pages.onlinegame.OnLineGameUiState
import com.egorhoot.chomba.pages.user.ProfileScreenUiState
import com.egorhoot.chomba.ui.theme.composable.BasicTextButton
import com.egorhoot.chomba.ui.theme.composable.IconButton
import com.egorhoot.chomba.ui.theme.composable.TopBar

@Composable
fun Room(
    modifier: Modifier = Modifier,
    viewModel: RoomViewModel = hiltViewModel()
) {
    val uiState = viewModel.roomUiState.value
    val onLineGameUiState = viewModel.onLineGameUiState.value
    val profileUiState = viewModel.profileUi.value
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if(uiState.page!=1){
            TopBar(
                title = stringResource(R.string.online_game),
                onFirstActionClick = { viewModel.homePage()}
            )
        }
        when(uiState.page){
            0 -> {
                RoomMainPage(
                    modifier = modifier.fillMaxSize().weight(1f),
                    viewModel = viewModel,
                    uiState = uiState,
                    onLineGameUiState = onLineGameUiState,
                    profileUiState = profileUiState
                )
            }
            1 -> {
                if(onLineGameUiState.game.room.id.isNotEmpty()){
                    OnLineGame(modifier = modifier.fillMaxSize(),
                        leaveGame = { viewModel.leaveGame() },
                        back = { viewModel.setRoomPage(0) })
                }
                else{
                    Text(stringResource(R.string.in_progress))
                }
            }
            else -> {
                RoomMainPage(
                    modifier = modifier.fillMaxSize().weight(1f),
                    viewModel = viewModel,
                    uiState = uiState,
                    onLineGameUiState = onLineGameUiState,
                    profileUiState = profileUiState
                )
            }
        }
    }

}

@Composable
fun RoomMainPage(
    modifier: Modifier = Modifier,
    viewModel: RoomViewModel,
    uiState: RoomUiState,
    onLineGameUiState: OnLineGameUiState,
    profileUiState: ProfileScreenUiState
){
    BrowseRooms (
        modifier = modifier,
        rooms = onLineGameUiState.rooms,
        inProgress = profileUiState.inProgress,
        onSelect = { code -> viewModel.onRoomCodeChanged(code) }
    )
    JoinRoom (
        modifier = Modifier.padding(0.dp, 16.dp),
        onChange = { code -> viewModel.onRoomCodeChanged(code) },
        roomCode = viewModel.roomUiState.value.roomCode
    )
    Choose (
        join = { viewModel.joinRoom() },
        create = { viewModel.createRoom() },
        browse = { viewModel.browseRooms()},
        isCodeValid = uiState.roomCode.length == 6
    )
}

@Composable
fun Choose(
    modifier: Modifier = Modifier,
    join: () -> Unit,
    create:()->Unit,
    browse:()->Unit = {},
    isCodeValid: Boolean = false
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            icon = R.drawable.baseline_refresh_24,
            modifier = modifier.weight(1f).padding(2.dp, 0.dp),
            action = { browse()},
            shape = MaterialTheme.shapes.extraLarge)
        BasicTextButton(text = R.string.create_room,
            modifier = modifier.weight(1f).padding(2.dp, 0.dp),
            action = { create() })
        BasicTextButton(text = R.string.join,
            modifier = modifier.weight(1f).padding(2.dp, 0.dp),
            action = { join() },
            isEnabled = isCodeValid)
    }
}

@Composable
fun JoinRoom(
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit = {},
    roomCode: String = ""
) {
    val focusManager = LocalFocusManager.current
    val code = remember {
        mutableStateOf(roomCode)
    }
    val isValid = remember {
        mutableStateOf(code.value.length == 6)
    }
    LaunchedEffect(roomCode){
        code.value = roomCode
        isValid.value = roomCode.length == 6
    }
    OutlinedTextField(
        value = code.value,
        onValueChange = { newValue ->
            isValid.value = newValue.length == 6
            code.value = newValue
            onChange(newValue)
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Number,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
            }
        ),
        modifier = modifier
            .fillMaxWidth(0.7f)
            .height(50.dp),
        singleLine = true,
        textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
        isError = !isValid.value
    )
}

@Composable
fun BrowseRooms(
    modifier: Modifier = Modifier,
    rooms: List<String>,
    inProgress: Boolean = false,
    onSelect: (String) -> Unit = {}
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        for(room in rooms){
            RoomCard(
                modifier = Modifier,
                room = room,
                onSelect = onSelect
            )
        }
        if(inProgress){
            CircularProgressIndicator()
        }
    }
}

@Composable
fun RoomCard(
    modifier: Modifier = Modifier,
    room: String,
    onSelect: (String) -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(2.dp),
        onClick = { onSelect(room) },
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(room,
                color = MaterialTheme.colorScheme.onTertiaryContainer)
        }
    }
}

