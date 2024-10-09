package com.egorhoot.chomba.pages.onlinegame

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun OnLineGame(
    modifier: Modifier = Modifier,
    viewModel: OnLineGameViewModel = hiltViewModel()
) {
    val uiState = viewModel.onLineGameUiState.value
    Surface(
        modifier = modifier.fillMaxSize()
    ) {

        Column {
            Text(text = uiState.game.roomCode)
            for (user in uiState.game.userList) {
                Text(text = user.id)
            }
        }
    }
}