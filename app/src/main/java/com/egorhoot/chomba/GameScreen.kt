package com.egorhoot.chomba

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.egorhoot.chomba.pages.GamePage
import com.egorhoot.chomba.pages.HomePage
import com.egorhoot.chomba.pages.NewGamePage
import com.egorhoot.chomba.pages.onlinegame.room.Room
import com.egorhoot.chomba.pages.user.ProfileViewModel
import com.egorhoot.chomba.pages.user.UserPage
import com.egorhoot.chomba.ui.theme.AppTheme
import com.egorhoot.chomba.ui.theme.composable.ShowAlert

@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {

    val pageState by viewModel.pageState
    AppTheme(
        dynamicColor = false
    ){


//        Box(
//            modifier = Modifier.fillMaxSize()
//                .background(MaterialTheme.colorScheme.primaryContainer)
//        )

        Surface(
            modifier = Modifier.fillMaxSize().systemBarsPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Image(
                painter = painterResource(id = R.drawable.home_back_queen),
                contentDescription = "background",
                alpha = 0.1f,
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            when(pageState.currentPage) {
                0 -> {
                    HomePage(viewModel = viewModel, profileViewModel = profileViewModel)
                }
                1 -> {
                    NewGamePage(viewModel = viewModel)
                }
                2 ->{
                    GamePage(viewModel = viewModel)
                }
                3 -> {
                    UserPage(viewModel = profileViewModel, gameViewModel = viewModel)
                }
                4 -> {
                    Room()
                }
                else -> {
                    HomePage(viewModel = viewModel, profileViewModel = profileViewModel)
                }
            }

            ShowAlert(uiState = viewModel.profileUi.value) }
    }

}
