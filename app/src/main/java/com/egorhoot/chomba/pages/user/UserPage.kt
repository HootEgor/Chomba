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
import com.egorhoot.chomba.ui.theme.Shapes
import com.egorhoot.chomba.ui.theme.composable.IconButton
import com.egorhoot.chomba.ui.theme.ext.smallButton
import com.egorhoot.chomba.ui.theme.composable.GameCard
import com.egorhoot.chomba.ui.theme.composable.UserNameBar

@Composable
fun UserPage(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel
) {
    val uiState by viewModel.profileUi
    if(uiState.isAuthenticated){
        UserProfile(viewModel = viewModel)
    }else{
        LoginScreen(viewModel = viewModel,
            onSignInEmail = { email ->
                viewModel.profileVM.userRepo.sendSignInLink(email)})
    }
}

@Composable
fun UserProfile(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel,

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
            signOutAction = { viewModel.profileVM.onSignOut() }
        )

        if(uiState.isSettings){
            SettingsScreen(
                modifier = modifier
                    .weight(1f),
                profileViewModel = viewModel.profileVM
            )
        }else{
            if(uiState.gameList.isNotEmpty()){
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
                                onSelect = { viewModel.profileVM.setCurrentGame(uiState.gameList[index].id) },
                                selected = uiState.gameList[index].id == uiState.currentGameIndex,
                                finished = viewModel.profileVM.isCurrentGameFinished(),
                                onDelete = { viewModel.profileVM.onDeleteGame(uiState.gameList[index].id) }
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
        }

        BottomBar(
            modifier = modifier.fillMaxWidth(),
            viewModel = viewModel,
            uiState = uiState)

    }
}



@Composable
fun BottomBar(
    modifier: Modifier,
    viewModel: GameViewModel,
    uiState: ProfileScreenUiState
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
            action = {viewModel.profileVM.toggleSettings()}
        )
        IconButton(
            icon = R.drawable.baseline_home_24,
            modifier = modifier
                .smallButton()
                .weight(1f),
            action = {viewModel.setCurrentPage(0) },
            isEnabled = !uiState.isSettings
        )
        IconButton(
            icon = R.drawable.baseline_arrow_forward_ios_24,
            modifier = modifier
                .smallButton()
                .weight(1f),
            action = {viewModel.continueGame()},
            isEnabled = uiState.currentGameIndex != null && !uiState.isSettings && !viewModel.profileVM.isCurrentGameFinished()
        )

    }
}







