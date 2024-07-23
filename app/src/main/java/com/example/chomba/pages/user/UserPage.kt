package com.example.chomba.pages.user

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.chomba.GameViewModel
import com.example.chomba.R
import com.example.chomba.data.Game
import com.example.chomba.data.Player
import com.example.chomba.data.getMissBarrel
import com.example.chomba.data.getTotalScore
import com.example.chomba.ui.theme.Shapes
import com.example.chomba.ui.theme.composable.BasicIconButton
import com.example.chomba.ui.theme.composable.BasicTextButton
import com.example.chomba.ui.theme.composable.CircleLoader
import com.example.chomba.ui.theme.composable.CircularChart
import com.example.chomba.ui.theme.composable.IconButton
import com.example.chomba.ui.theme.composable.TopBar
import com.example.chomba.ui.theme.ext.basicButton
import com.example.chomba.ui.theme.ext.smallButton
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.example.chomba.ui.theme.composable.GameCard
import com.example.chomba.ui.theme.composable.ShowAlert
import com.example.chomba.ui.theme.composable.UserNameBar

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
            signOutAction = { viewModel.profileVM.signOut() }
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
                                onDelete = { viewModel.profileVM.deleteGame(uiState.gameList[index].id) }
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







