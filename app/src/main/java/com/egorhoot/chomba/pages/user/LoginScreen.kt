package com.egorhoot.chomba.pages.user

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.egorhoot.chomba.GameViewModel
import com.egorhoot.chomba.R
import com.egorhoot.chomba.ui.theme.composable.IconButton
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient

@OptIn(ExperimentalMaterial3Api::class)
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
                    viewModel.profileVM.signInWithGoogleToken(token)
                }
            } catch (e: Exception) {
                Log.e("SignResult", "OneTapUI request failed; $e")
            }
        }
    )

    val onGoogleSignInClick = {
        val apiKey = "356192759763-ft8atdev0oif0ld83cq0pdp8b55aqp31.apps.googleusercontent.com"
        val request = viewModel.profileVM.userRepo.getSignInRequest(apiKey)
        oneTapClient.beginSignIn(request)
            .addOnSuccessListener {
                try {
                    launcher.launch(
                        IntentSenderRequest.Builder(
                            it.pendingIntent.intentSender
                        ).build()
                    )
                    viewModel.profileVM.loadGames()
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
        Spacer(modifier = Modifier.fillMaxHeight(0.3f))
//        OutlinedTextField(
//            value = email.value,
//            onValueChange = { newValue ->
//                email.value = newValue
//            },
//            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
//            textStyle = LocalTextStyle.current.copy(color = Color.Black),
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(8.dp),
//            singleLine = true,
//            placeholder = { Text(stringResource(R.string.email)) },
//            colors = TextFieldDefaults.outlinedTextFieldColors(
//                containerColor = MaterialTheme.colorScheme.background,
//            ),
//        )
//
//        TextButton(
//            onClick = {
//                // Вызов метода для регистрации через email
//                onSignInEmail(email.value)
//            },
//        ) {
//            Text(text = stringResource(R.string.sign_in_with_email))
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                // Вызов метода для регистрации через Google
                onGoogleSignInClick()
            },
        ) {
            Text(text = stringResource(R.string.sign_in_with_google))
        }

        Spacer(modifier = Modifier.fillMaxHeight(0.7f))

        IconButton(icon = R.drawable.baseline_home_24,
            modifier = Modifier
                .padding(8.dp)
                .height(48.dp),
            action = { viewModel.setCurrentPage(0) })
    }
}