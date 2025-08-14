package com.egorhoot.chomba.pages.user.camera

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
fun CameraPermissionRequest(
    modifier: Modifier = Modifier,
    permissionDenied: Boolean,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val permissionsState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    if (permissionsState.status.isGranted) {
        onPermissionGranted()
    }else if (permissionsState.status.shouldShowRationale) {
        if (permissionDenied) {
            Surface(
                shape = Shapes.large,
                shadowElevation = 2.dp,
                color = MaterialTheme.colorScheme.background,
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = Shapes.large
                    )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Spacer(modifier = modifier.fillMaxHeight(0.4f))
                    Text(text = stringResource(R.string.request_permission_camera_message))
                    BasicIconButton(
                        text = R.string.request_permission,
                        icon = R.drawable.baseline_error_outline_24,
                        modifier = modifier
                            .smallButton()
                            .padding(horizontal = 16.dp),
                        action = { permissionsState.run { launchPermissionRequest() }}
                    )
                }
            }

        }else{
            onPermissionDenied()
        }
    } else {
        if (permissionDenied) return
        AlertOk(
            title = R.string.request_permission_camera_title,
            message = R.string.request_permission_camera_message
        ) {
            permissionsState.run { launchPermissionRequest() }
        }
    }

}