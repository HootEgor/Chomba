package com.egorhoot.chomba.ui.theme.composable

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.egorhoot.chomba.pages.user.ProfileScreenUiState
import com.egorhoot.chomba.util.StringProvider // Import StringProvider

@Composable
fun ShowAlert(
    uiState: ProfileScreenUiState
) {
    if (uiState.showAlert) {
        val context = LocalContext.current
        // StringProvider is still needed for button texts
        val stringProvider = remember { StringProvider(context) }

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
                        text = stringProvider.getString("ok_button"), // Key for OK
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
                        text = stringProvider.getString("cancel_button"), // Key for Cancel
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }
}
