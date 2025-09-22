package com.egorhoot.chomba.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.egorhoot.chomba.GameViewModel
import com.egorhoot.chomba.R
import com.egorhoot.chomba.pages.user.ProfileViewModel
import com.egorhoot.chomba.ui.theme.composable.BasicTextButton
import com.egorhoot.chomba.ui.theme.composable.TopBar
import com.egorhoot.chomba.ui.theme.ext.smallButton
import com.egorhoot.chomba.util.StringProvider

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel,
    profileViewModel: ProfileViewModel
) {
    val stringProvider = StringProvider(LocalContext.current)
    Column {
        TopBar(
            title = stringProvider.getString("app_name"),
            firstButtonIcon = R.drawable.baseline_account_circle_24,
            onFirstActionClick = {
                viewModel.setCurrentPage(3)
                //profileViewModel.loadGames()
                                 },
        )
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
//            if(viewModel.playerList.value.isNotEmpty()) {
//                BasicTextButton(text = stringProvider.getString(.continue_game,
//                    modifier = modifier.smallButton(),
//                    action = {viewModel.setCurrentPage(1)})
//            }

//            BasicTextButton(text = stringProvider.getString(.solo_game,
//                modifier = modifier.smallButton(),
//                action = {viewModel.setCurrentPage(4)
//                    soloViewModel.newGame()})

//            BasicTextButton(text = stringProvider.getString(.online_game,
//                modifier = modifier.smallButton(),
//                action = {viewModel.onlineGame()})

            BasicTextButton(text = stringProvider.getString("new_game"),
                modifier = modifier.smallButton(),
                action = {viewModel.newGame()})

            BasicTextButton(text = stringProvider.getString("game_list"),
                modifier = modifier.smallButton(),
                action = {
                    viewModel.setCurrentPage(3)
                    //profileViewModel.loadGames()
                })
        }
    }


}