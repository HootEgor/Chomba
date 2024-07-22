package com.example.chomba.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.chomba.GameViewModel
import com.example.chomba.R
import com.example.chomba.pages.solo.SoloViewModel
import com.example.chomba.ui.theme.composable.BasicTextButton
import com.example.chomba.ui.theme.composable.TopBar
import com.example.chomba.ui.theme.ext.smallButton

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel,
    soloViewModel: SoloViewModel
) {
    Column {
        TopBar(
            title = stringResource(R.string.app_name),
            firstButtonIcon = R.drawable.baseline_account_circle_24,
            onFirstActionClick = { viewModel.setCurrentPage(3)
                viewModel.profileVM.loadGames()},
        )
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if(viewModel.playerList.value.isNotEmpty()) {
                BasicTextButton(text = R.string.continue_game,
                    modifier = modifier.smallButton(),
                    action = {viewModel.setCurrentPage(2)})
            }

            BasicTextButton(text = R.string.solo_game,
                modifier = modifier.smallButton(),
                action = {viewModel.setCurrentPage(4)
                    soloViewModel.newGame()})

            BasicTextButton(text = R.string.new_game,
                modifier = modifier.smallButton(),
                action = {viewModel.newGame()})

            BasicTextButton(text = R.string.load_games,
                modifier = modifier.smallButton(),
                action = {viewModel.setCurrentPage(3)
                    viewModel.profileVM.loadGames()})
        }
    }

}