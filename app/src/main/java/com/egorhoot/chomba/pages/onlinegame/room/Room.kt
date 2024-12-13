package com.egorhoot.chomba.pages.onlinegame.room

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.egorhoot.chomba.R
import com.egorhoot.chomba.data.User
import com.egorhoot.chomba.pages.onlinegame.OnLineGame
import com.egorhoot.chomba.pages.onlinegame.OnLineGameUiState
import com.egorhoot.chomba.pages.user.ProfileScreenUiState
import com.egorhoot.chomba.ui.theme.Shapes
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
                if(viewModel.isAllReady()){
                    OnLineGame(
                        modifier = modifier.fillMaxSize()
                    )
                } else if(onLineGameUiState.game.room.id.isNotEmpty()){
                    PreGameRoom(modifier = modifier.fillMaxSize(),
                        viewModel = viewModel)
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
fun PreGameRoom(
    modifier: Modifier = Modifier,
    viewModel: RoomViewModel = hiltViewModel(),
){
    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopBar(
                title = viewModel.roomCode,
                onFirstActionClick = { viewModel.homePage()},
                secondButtonIcon = R.drawable.baseline_content_copy_24,
                onSecondActionClick = { viewModel.copyRoomCodeToClipboard()},
                secondIconEnabled = true
            )
            Column(
                modifier = modifier.fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (user in viewModel.game.userList) {
                    UserPreview(
                        modifier = Modifier.fillMaxWidth().padding(2.dp),
                        user = user)
                }

            }
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(icon = R.drawable.baseline_arrow_back_ios_24,
                    modifier = Modifier.weight(1f).padding(2.dp, 0.dp),
                    action = { viewModel.leaveGame()},
                    shape = Shapes.extraLarge)
                BasicTextButton(text = R.string.leave,
                    modifier = Modifier.weight(1f).padding(2.dp, 0.dp),
                    action = { viewModel.setRoomPage(0)})
                BasicTextButton(text = if(viewModel.isOwner()) R.string.start else R.string.ready,
                    modifier = Modifier.weight(1f).padding(2.dp, 0.dp),
                    action = { viewModel.readyToPlay()},
                    isEnabled = if(viewModel.isOwner()) viewModel.isNonOwnerReady() else true)
            }
        }

    }
}

@Composable
fun UserPreview(
    modifier: Modifier = Modifier,
    user: User
) {
    Surface(
        shape = Shapes.medium,
        //shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = modifier.height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (user.userPicture != "") {
                AsyncImage(
                    model = user.userPicture.let { Uri.parse(it) },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = user.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                modifier = modifier.weight(1f)
            )
            //ready text with icon at the end of row
            if (user.ready) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_check_24),
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp)
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

