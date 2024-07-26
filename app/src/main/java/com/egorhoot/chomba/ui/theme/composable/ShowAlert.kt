package com.egorhoot.chomba.ui.theme.composable

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.egorhoot.chomba.R
import com.egorhoot.chomba.pages.user.ProfileScreenUiState

@Composable
fun ShowAlert(
    uiState: ProfileScreenUiState
) {
    if(uiState.showAlert){
        AlertDialog(
            onDismissRequest = uiState.alertDismiss,
            title = { Text(text = stringResource(uiState.alertTitle), style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(text = uiState.alertMsg,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center)
            },
            confirmButton = {
                TextButton(
                    onClick =  uiState.alertAction
                ) {
                    Text(
                        text = stringResource(R.string.ok),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = uiState.alertDismiss
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }
}