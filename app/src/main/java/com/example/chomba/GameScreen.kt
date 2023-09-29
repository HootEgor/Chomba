package com.example.chomba

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.example.chomba.pages.HomePage
import com.example.chomba.pages.NewGamePage
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chomba.pages.GamePage
import com.example.chomba.pages.user.UserPage
import com.example.chomba.pages.solo.SoloGamePage
import com.example.chomba.pages.solo.SoloViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(),
    soloViewModel: SoloViewModel = viewModel()
) {
    val uiState by viewModel.uiState

    when(uiState.currentPage) {
        0 -> {
            HomePage(viewModel = viewModel,
                soloViewModel = soloViewModel)
        }
        1 -> {
            NewGamePage(viewModel = viewModel)
        }
        2 ->{
            GamePage(viewModel = viewModel)
        }
        3 -> {
            UserPage(viewModel = viewModel)
        }
        4 -> {
            SoloGamePage(viewModel = viewModel,
                soloViewModel = soloViewModel)
        }
        else -> {
            HomePage(viewModel = viewModel,
                soloViewModel = soloViewModel)
        }
    }
}
