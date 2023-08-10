package com.example.chomba.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.chomba.GameViewModel
import com.example.chomba.R
import com.example.chomba.data.User
import com.example.chomba.ui.theme.composable.BasicIconButton
import com.example.chomba.ui.theme.composable.BasicTextButton
import com.example.chomba.ui.theme.composable.TopBar
import com.example.chomba.ui.theme.ext.basicButton

@Composable
fun NewGamePage(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel
){
    val uiState by viewModel.uiState
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TopBar(
            title = stringResource(R.string.new_game),
            onFirstActionClick = { viewModel.setCurrentPage(0) }
        )
        Box(
            modifier = modifier
                .weight(2f)
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ){
            LazyColumn(
                modifier = modifier.fillMaxHeight()
            ) {
                items(uiState.userList) {
                        item -> UserItem(user = item)
                }
            }

            BasicIconButton(text = R.string.add_player,
                icon = R.drawable.baseline_add_24,
                modifier = modifier.basicButton(),
                action ={ viewModel.addUser() })

        }

        Box(
            modifier = modifier.weight(1f),
            contentAlignment = Alignment.BottomCenter
        ){
            BasicTextButton(text = R.string.start,
                modifier = modifier.basicButton().padding(bottom = 16.dp),
                action = {viewModel.setCurrentPage(2)})

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserItem(
    modifier: Modifier = Modifier,
    user: User
){
    val userName = remember { mutableStateOf("") }
    userName.value = user.name

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = TextFieldValue(text = user.name, selection = TextRange(user.name.length)),
            onValueChange = { newValue ->
                userName.value = newValue.text
            },
            label = { Text(stringResource(R.string.player_name)) },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true,
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
            trailingIcon = {
            }
        )
    }
}