package com.egorhoot.chomba.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.egorhoot.chomba.GameViewModel
import com.egorhoot.chomba.R
import com.egorhoot.chomba.pages.user.ProfileViewModel
import com.egorhoot.chomba.ui.theme.composable.BasicTextButton
import com.egorhoot.chomba.ui.theme.composable.TopBar
import com.egorhoot.chomba.ui.theme.ext.smallButton

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel,
    profileViewModel: ProfileViewModel
) {

    Column {
        TopBar(
            title = stringResource(R.string.app_name),
            firstButtonIcon = R.drawable.baseline_account_circle_24,
            onFirstActionClick = { viewModel.setCurrentPage(3)
                profileViewModel.loadGames()},
        )
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
//            if(viewModel.playerList.value.isNotEmpty()) {
//                BasicTextButton(text = R.string.continue_game,
//                    modifier = modifier.smallButton(),
//                    action = {viewModel.setCurrentPage(1)})
//            }

//            BasicTextButton(text = R.string.solo_game,
//                modifier = modifier.smallButton(),
//                action = {viewModel.setCurrentPage(4)
//                    soloViewModel.newGame()})

            BasicTextButton(text = R.string.online_game,
                modifier = modifier.smallButton(),
                action = {viewModel.onlineGame()})

            BasicTextButton(text = R.string.new_game,
                modifier = modifier.smallButton(),
                action = {viewModel.newGame()})

            BasicTextButton(text = R.string.load_games,
                modifier = modifier.smallButton(),
                action = {viewModel.setCurrentPage(3)
                    profileViewModel.loadGames()})
        }
    }


}