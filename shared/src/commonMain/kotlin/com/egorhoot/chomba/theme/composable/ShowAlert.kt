package com.egorhoot.chomba.theme.composable

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import chomba.shared.generated.resources.Res
import chomba.shared.generated.resources.cancel
import chomba.shared.generated.resources.ok
import com.egorhoot.chomba.pages.user.ProfileScreenUiState
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShowAlert(
    uiState: ProfileScreenUiState
) {
    if (uiState.showAlert) {
        AlertDialog(
            onDismissRequest = uiState.alertDismiss,
            title = {
                Text(
                    // Use the pre-resolved title from uiState
                    text = uiState.resolvedAlertTitle,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    // Use the pre-resolved message from uiState
                    text = uiState.resolvedAlertMessage,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(
                    onClick = uiState.alertAction
                ) {
                    Text(
                        text = stringResource(Res.string.ok), // Key for OK
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
                        text = stringResource(Res.string.cancel), // Key for Cancel
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }
}
