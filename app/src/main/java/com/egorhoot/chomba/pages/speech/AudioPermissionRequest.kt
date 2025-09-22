package com.egorhoot.chomba.pages.speech

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.egorhoot.chomba.ui.theme.composable.AlertOk
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AudioPermissionRequest(
    modifier: Modifier = Modifier,
    permissionDenied: Boolean,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val permissionsState = rememberPermissionState(
        android.Manifest.permission.RECORD_AUDIO
    )

    if (permissionsState.status.isGranted) {
        onPermissionGranted()
    }else if (permissionsState.status.shouldShowRationale) {
        if (permissionDenied) {
            AlertOk(
                titleKey = "request_permission_audio_title",
                messageKey = "request_permission_audio_message",
                action = { permissionsState.launchPermissionRequest() }
            )
        }else{
            onPermissionDenied()
        }
    }

}