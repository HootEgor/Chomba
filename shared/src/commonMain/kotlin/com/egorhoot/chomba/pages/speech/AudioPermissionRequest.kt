package com.egorhoot.chomba.pages.speech

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.egorhoot.chomba.theme.composable.AlertOk

// CommonMain
interface PermissionHandler {
    val isGranted: Boolean
    val shouldShowRationale: Boolean
    fun requestPermission()
}

@Composable
fun AudioPermissionRequest(
    modifier: Modifier = Modifier,
    permissionDenied: Boolean,
    permissionHandler: PermissionHandler,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    if (permissionHandler.isGranted) {
        onPermissionGranted()
    } else if (permissionHandler.shouldShowRationale) {
        if (permissionDenied) {
            AlertOk(
                titleKey = "request_permission_audio_title",
                messageKey = "request_permission_audio_message",
                action = { permissionHandler.requestPermission() }
            )
        } else {
            onPermissionDenied()
        }
    }
}
