package com.egorhoot.chomba.pages.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.egorhoot.chomba.GameViewModel
import com.egorhoot.chomba.R
import com.egorhoot.chomba.pages.user.editgame.EditGameScreen
import com.egorhoot.chomba.pages.user.editgame.EditGameViewModel
import com.egorhoot.chomba.pages.user.leaderboard.LeaderBoard
import com.egorhoot.chomba.pages.user.leaderboard.LeaderBoardViewModel
import com.egorhoot.chomba.ui.theme.Shapes
import com.egorhoot.chomba.ui.theme.composable.IconButton
import com.egorhoot.chomba.ui.theme.ext.smallButton
import com.egorhoot.chomba.ui.theme.composable.GameCard
import com.egorhoot.chomba.ui.theme.composable.UserNameBar

@Composable
fun UserPage(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel,
    viewModel: ProfileViewModel,
) {
    val uiState by viewModel.profileUi
    if(uiState.isAuthenticated){
        UserProfile(
            gameViewModel = gameViewModel,
            viewModel = viewModel)
    }else{
        LoginScreen(viewModel = gameViewModel,
            profileViewModel = viewModel,
            onSignInEmail = { email ->
                gameViewModel.userRepo.sendSignInLink(email)})
    }
}

@Composable
fun UserProfile(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel,
    viewModel: ProfileViewModel,
) {
    val uiState by viewModel.profileUi

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        UserNameBar(
            name = uiState.displayName,
            picture = uiState.userPicture,
            signOutAction = { viewModel.onSignOut() }
        )

        when(uiState.currentScreen){
            0 -> if(uiState.gameList.isNotEmpty()){
                Surface(
                    shape = Shapes.medium,
                    //shadowElevation = 4.dp,
                    modifier = modifier
                        .fillMaxWidth()
                        .weight(1f)

                ) {
                    LazyColumn(
                        modifier = modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        items(uiState.gameList.size) { index ->
                            GameCard(
                                modifier = modifier
                                    .fillMaxWidth(),
                                game = uiState.gameList[index],
                                onSelect = { viewModel.setCurrentGame(uiState.gameList[index].id) },
                                selected = uiState.gameList[index].id == uiState.currentGameIndex,
                                finished = viewModel.isCurrentGameFinished(),
                                onDelete = { viewModel.onDeleteGame(uiState.gameList[index].id) },
                                onEdit = { viewModel.toggleEditGame() }
                            )
                        }
                    }
                }
            }else{
                Text(
                    text = stringResource(uiState.saveMsg),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            1 -> SettingsScreen(
                modifier = modifier
                    .weight(1f),
                profileViewModel = viewModel
            )
            2 -> LeaderBoard(
                modifier = modifier
                    .weight(1f)
            )
            3 -> EditGameScreen(
                modifier = modifier,
                onBack = {viewModel.toggleEditGame()},
            )
        }

        if(uiState.currentScreen!=3){
            BottomBar(
                modifier = modifier.fillMaxWidth(),
                gameViewModel = gameViewModel,
                viewModel = viewModel,
                uiState = uiState)
        }
    }
}



@Composable
fun BottomBar(
    modifier: Modifier,
    gameViewModel: GameViewModel,
    viewModel: ProfileViewModel,
    uiState: ProfileScreenUiState,
){

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(
            icon = R.drawable.baseline_settings_24,
            modifier = modifier
                .smallButton()
                .weight(1f),
            action = {viewModel.toggleSettings()}
        )
        IconButton(
            icon = if(uiState.currentScreen == 2) R.drawable.baseline_videogame_asset_24 else R.drawable.baseline_leaderboard_24,
            modifier = modifier
                .smallButton()
                .weight(1f),
            action = {viewModel.toggleLeaderBoard() },
            isEnabled = uiState.currentScreen != 1 && uiState.relatedUserList.isNotEmpty()
        )
        IconButton(
            icon = R.drawable.baseline_home_24,
            modifier = modifier
                .smallButton()
                .weight(1f),
            action = {gameViewModel.setCurrentPage(0) },
            isEnabled = uiState.currentScreen != 1
        )
        IconButton(
            icon = R.drawable.baseline_arrow_forward_ios_24,
            modifier = modifier
                .smallButton()
                .weight(1f),
            action = {gameViewModel.continueGame()},
            isEnabled = uiState.currentGameIndex != null && uiState.currentScreen == 0 && !viewModel.isCurrentGameFinished() && viewModel.isUserOwner()
        )

    }
}







