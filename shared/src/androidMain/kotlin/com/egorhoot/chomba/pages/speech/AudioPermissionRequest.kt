package com.egorhoot.chomba.pages.speech


// CommonMain
// AndroidMain
import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

class AndroidAudioPermissionHandler @OptIn(ExperimentalPermissionsApi::class) constructor(
    private val permissionState: androidx.compose.runtime.State<PermissionState>
) : PermissionHandler {
    @OptIn(ExperimentalPermissionsApi::class)
    override val isGranted: Boolean get() = permissionState.value.status.isGranted
    @OptIn(ExperimentalPermissionsApi::class)
    override val shouldShowRationale: Boolean get() = permissionState.value.status.shouldShowRationale
    @OptIn(ExperimentalPermissionsApi::class)
    override fun requestPermission() {
        permissionState.value.launchPermissionRequest()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberAndroidAudioPermissionHandler(): PermissionHandler {
    val permissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    return AndroidAudioPermissionHandler(permissionState as State<PermissionState>)
}

