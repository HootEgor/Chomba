package com.egorhoot.chomba.pages.onlinegame.room

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.egorhoot.chomba.R
import com.egorhoot.chomba.pages.ColorPickerButton
import com.egorhoot.chomba.pages.onlinegame.OnLineGame
import com.egorhoot.chomba.ui.theme.composable.BasicTextButton
import com.egorhoot.chomba.ui.theme.composable.TopBar

@Composable
fun Room(
    modifier: Modifier = Modifier,
    viewModel: RoomViewModel = hiltViewModel()
) {
    val uiState = viewModel.roomUiState.value
    val onLineGameUiState = viewModel.onLineGameUiState.value
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TopBar(
            title = stringResource(R.string.online_game),
            onFirstActionClick = { viewModel.homePage()}
        )
        when(uiState.page){
            0 -> {
                Choose (
                    onSelect = { page ->
                        viewModel.setRoomPage(page)
                    },
                    create = { viewModel.createRoom() }
                )
            }
            1 -> {
                if(onLineGameUiState.game.roomCode.isNotEmpty()){
                    OnLineGame()
                }
                else{
                    Text(stringResource(R.string.in_progress))
                }
            }
            2 -> {
                JoinRoom (
                    onConfirm = { code ->
                        viewModel.joinRoom(code)
                    }
                )
            }
            else -> {
                Choose (
                    onSelect = { page ->
                        viewModel.setRoomPage(page)
                    },
                    create = { viewModel.createRoom() }
                )
            }
        }
    }

}

@Composable
fun Choose(
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit,
    create:()->Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BasicTextButton(text = R.string.create_room,
            modifier = modifier,
            action = { create() })
        BasicTextButton(text = R.string.join_room,
            modifier = modifier,
            action = { onSelect(2) })

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinRoom(
    modifier: Modifier = Modifier,
    onConfirm: (String) -> Unit
) {
    val isValid = remember {
        mutableStateOf(false)
    }
    val code = remember {
        mutableStateOf("")
    }
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = code.value,
            onValueChange = { newValue ->
                isValid.value = newValue.length == 6
                code.value = newValue
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Number,
            ),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(64.dp),
            singleLine = true,
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
            placeholder = { Text(stringResource(R.string.room_code)) },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
            ),
            isError = !isValid.value
        )
        Spacer(modifier = Modifier.padding(16.dp))
        BasicTextButton(text = R.string.join,
            modifier = modifier,
            action = { onConfirm(code.value) })
    }
}

