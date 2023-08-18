package com.example.chomba

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.example.chomba.pages.HomePage
import com.example.chomba.pages.NewGamePage
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chomba.pages.GamePage
import com.example.chomba.pages.user.UserPage
import androidx.compose.runtime.livedata.observeAsState

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState

    when(uiState.currentPage) {
        0 -> {
            HomePage(viewModel = viewModel)
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
        else -> {
            HomePage(viewModel = viewModel)
        }
    }
}
