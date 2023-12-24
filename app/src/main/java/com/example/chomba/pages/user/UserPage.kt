package com.example.chomba.pages.user

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
                viewModel.sendSignInLink(email)})
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
            signOutAction = { viewModel.signOut() }
        )

        if(uiState.gameList.isNotEmpty()){
            LazyColumn(
                modifier = modifier
                    .weight(1f)
            ) {
                items(uiState.gameList.size) { index ->
                    GameCard(
                        game = uiState.gameList[index],
                        onSelect = { viewModel.setCurrentGame(uiState.gameList[index].id) },
                        selected = uiState.gameList[index].id == uiState.currentGameIndex
                    )
                }
            }
        }else{
            Text(
                text = stringResource(uiState.saveMsg),
                style = MaterialTheme.typography.titleMedium
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BasicTextButton(
                    text = R.string.load_games,
                    modifier = modifier
                        .basicButton()
                        .weight(1f),
                    action = { viewModel.loadGames() }
                )
                BasicTextButton(
                    text = R.string.home,
                    modifier = modifier
                        .basicButton()
                        .weight(1f),
                    action = { viewModel.setCurrentPage(0) }
                )

            }
            if (uiState.currentGameIndex != null) {
                BasicTextButton(
                    text = R.string.continue_game,
                    modifier = modifier.basicButton(),
                    action = { viewModel.continueGame() }
                )
            }
        }

    }
}

@Composable
fun UserNameBar(
    name: String,
    picture: Uri?,
    signOutAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = Shapes.medium,
        //shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = modifier.height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (picture != null) {
                AsyncImage(
                    model = picture,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                modifier = modifier.weight(1f)
            )
            IconButton(
                icon = R.drawable.baseline_logout_24,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(2.dp),
                action = signOutAction,
            )

        }
    }
}



@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel,
    onSignInEmail: (String) -> Unit,
) {
    val email = remember { mutableStateOf("") }
    val oneTapClient: SignInClient = Identity.getSignInClient(LocalContext.current)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            try {
                val credentials = oneTapClient.getSignInCredentialFromIntent(result.data)
                credentials.googleIdToken?.let { token ->
                    viewModel.signInWithGoogleToken(token)
                }
            } catch (e: Exception) {
                Log.e("SignResult", "OneTapUI request failed; $e")
            }
        }
    )

    val onGoogleSignInClick = {
        val apiKey = "356192759763-ft8atdev0oif0ld83cq0pdp8b55aqp31.apps.googleusercontent.com"
        val request = viewModel.getSignInRequest(apiKey)
        oneTapClient.beginSignIn(request)
            .addOnSuccessListener {
                try {
                    launcher.launch(
                        IntentSenderRequest.Builder(
                            it.pendingIntent.intentSender
                        ).build()
                    )
                } catch (e: Exception) {
//                    SnackbarManager.showMessage(e.toSnackbarMessage())
                    Log.e("PRG", "OneTapUI start failed; $e")
                }
            }
            .addOnFailureListener {
//                SnackbarManager.showMessage(it.toSnackbarMessage())
                Log.w("PRG", "Google SignIn cancelled", it)
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = email.value,
            onValueChange = { newValue ->
                email.value = newValue
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            textStyle = LocalTextStyle.current.copy(color = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            singleLine = true,
            label = { Text(stringResource(R.string.email))},
        )

        TextButton(
            onClick = {
                // Вызов метода для регистрации через email
                onSignInEmail(email.value)
            },
        ) {
            Text(text = stringResource(R.string.sign_in_with_email))
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                // Вызов метода для регистрации через Google
                onGoogleSignInClick()
            },
        ) {
            Text(text = stringResource(R.string.sign_in_with_google))
        }
    }
}


