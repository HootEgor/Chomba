package com.egorhoot.chomba

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.egorhoot.chomba.pages.HomePage
import com.egorhoot.chomba.pages.NewGamePage
import androidx.lifecycle.viewmodel.compose.viewModel
import com.egorhoot.chomba.pages.GamePage
import com.egorhoot.chomba.pages.user.UserPage
import com.egorhoot.chomba.pages.solo.SoloGamePage
import com.egorhoot.chomba.pages.solo.SoloViewModel
import com.egorhoot.chomba.ui.theme.composable.ShowAlert

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(),
    soloViewModel: SoloViewModel = viewModel()
) {
    val uiState by viewModel.uiState
    Image(
        painter = painterResource(id = R.drawable.home_back_queen),
        contentDescription = "background",
        alpha = 0.1f,
        modifier = Modifier
            .fillMaxSize(),
        contentScale = ContentScale.Crop
    )
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
    
    ShowAlert(uiState = viewModel.profileUi.value)
}
