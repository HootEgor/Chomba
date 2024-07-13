package com.example.chomba.pages.user

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.chomba.GameViewModel
import com.example.chomba.R
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient

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
            label = { Text(stringResource(R.string.email)) },
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