package com.egorhoot.chomba.pages.speech

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.egorhoot.chomba.R
import com.egorhoot.chomba.ui.theme.Shapes
import com.egorhoot.chomba.ui.theme.composable.AlertOk
import com.egorhoot.chomba.ui.theme.composable.BasicIconButton
import com.egorhoot.chomba.ui.theme.ext.smallButton
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
                title = R.string.request_permission_audio_title,
                message = R.string.request_permission_audio_message,
                action = { permissionsState.launchPermissionRequest() }
            )
        }else{
            onPermissionDenied()
        }
    }

}