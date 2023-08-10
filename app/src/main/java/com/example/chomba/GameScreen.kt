package com.example.chomba

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.example.chomba.pages.HomePage
import com.example.chomba.pages.NewGamePage

@Composable
fun GameScreen(
    viewModel: GameViewModel = GameViewModel()
) {
    val uiState by viewModel.uiState
    when(uiState.currentPage) {
        0 -> {
            HomePage(viewModel = viewModel)
        }
        1 -> {
            NewGamePage(viewModel = viewModel)
        }
    }

    Log.d("EEE", "${uiState.currentPage}" )
}