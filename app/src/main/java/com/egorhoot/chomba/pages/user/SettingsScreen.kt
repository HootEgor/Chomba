package com.egorhoot.chomba.pages.user

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.egorhoot.chomba.R
import com.egorhoot.chomba.data.Language
import com.egorhoot.chomba.pages.ColorPickerButton
import com.egorhoot.chomba.pages.user.camera.CameraPermissionRequest
import com.egorhoot.chomba.pages.user.camera.CameraScreen
import com.egorhoot.chomba.pages.user.leaderboard.ShowQrCodeAlert
import com.egorhoot.chomba.ui.theme.Shapes
import com.egorhoot.chomba.ui.theme.composable.BasicIconButton
import com.egorhoot.chomba.ui.theme.composable.BasicTextButton
import com.egorhoot.chomba.ui.theme.composable.IconButton
import com.egorhoot.chomba.ui.theme.composable.NameHorizontalDivider

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {

    val uiState by profileViewModel.profileUi

    val showQrCode = remember {mutableStateOf(false)}

    Surface(
        shape = Shapes.medium,
        modifier = modifier
            .fillMaxWidth()
    ) {
        if (uiState.scanQrCode){
            if (!uiState.cameraPermissionGranted) {
                CameraPermissionRequest(
                    modifier = Modifier.padding(bottom = 16.dp),
                    permissionDenied = uiState.cameraPermissionDenied,
                    onPermissionGranted = { profileViewModel.requestCamera() },
                    onPermissionDenied = { profileViewModel.onPermissionDenied() }
                )
            }
            else {
                CameraScreen(
                    onGetIds = { userUid ->
                        profileViewModel.onRecognizeId(userUid)
                    },
                    onError = profileViewModel::onCameraError,
                    onBack = profileViewModel::stopScanner
                )
            }

            return@Surface
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            NameHorizontalDivider(
                text = stringResource(id = R.string.speech_rec_idioma),
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleMedium
            )
//            Text(
//                text = stringResource(id = R.string.speech_rec_idioma),
//                style = MaterialTheme.typography.titleMedium
//            )
            RadioButtonSample(
                modifier = Modifier.padding(top = 16.dp),
                onSelected = { language ->
                    profileViewModel.selectSpeechRecLanguage(profileViewModel.profileUi, language)
                },
                selectedOption = profileViewModel.profileUi.value.selectedLanguage
            )

            NameHorizontalDivider(
                text = stringResource(id = R.string.player_settings),
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleMedium
            )

            PlayerSettings(
                modifier = Modifier.fillMaxWidth(),
                profileViewModel = profileViewModel,
                showQrCode = { showQrCode.value = true },
                mergePlayer = {
                    profileViewModel.startScanner()
                }
            )
        }
    }

    if (showQrCode.value){
        ShowQrCodeAlert(
            action = {showQrCode.value = false},
            qrCode = profileViewModel.getUserQRCode(
                MaterialTheme.colorScheme.onTertiaryContainer,
                MaterialTheme.colorScheme.tertiaryContainer
            )
        )
    }



}

@Composable
fun RadioButtonSample(
    modifier: Modifier = Modifier,
    onSelected: (Language) -> Unit = { },
    selectedOption: Language,
) {
    val radioOptions = listOf(
        Language(R.drawable.flag_ua, R.string.tag_ua),
        Language(R.drawable.flag_uk, R.string.tag_uk),
        Language(R.drawable.orc, R.string.tag_ru)
    )
    Column {
        radioOptions.chunked(3).forEach { chunk ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                chunk.forEach { language ->
                    Row(
                        Modifier
                            .selectable(
                                selected = (language == selectedOption),
                                onClick = {onSelected(language) }
                            )
                            .weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = (language == selectedOption),
                            onClick = { onSelected(language) }
                        )
                        Image(
                            painter = painterResource(id = language.icon.toInt()),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                // Fill the remaining space if the chunk has less than 3 items
                repeat(3 - chunk.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun PlayerSettings(
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel,
    showQrCode: () -> Unit = {},
    mergePlayer: () -> Unit = {},
) {
    val uiState by profileViewModel.profileUi

    val focusManager = LocalFocusManager.current
    val userName = remember { mutableStateOf(uiState.nickname) }

    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(0.7f),
                value = userName.value,
                label = {
                    Text(
                        text = stringResource(id = R.string.nickname)
                    )
                },
                onValueChange = {
                    userName.value = it
                },
                singleLine = true,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (uiState.nickname != userName.value){
                            profileViewModel.saveUserNickName(userName.value)
                        }
                        focusManager.clearFocus()
                    }
                ),
                trailingIcon = {
                    val icon = if (uiState.nickname == userName.value) R.drawable.baseline_check_24 else R.drawable.baseline_save_24
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).clickable {
                            if (uiState.nickname != userName.value){
                                profileViewModel.saveUserNickName(userName.value)
                                focusManager.clearFocus()
                            }
                        }
                    )
                },
                placeholder = { Text(stringResource(R.string.nickname)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                )
            )

            Image(
                painter = painterResource(R.drawable.outline_qr_code_scanner_24),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().clickable {
                    showQrCode()
                },
            )
        }

        BasicIconButton(
            text = R.string.merge_player,
            icon = R.drawable.outline_call_merge_24,
            modifier = Modifier.fillMaxWidth(0.6f),
            action = {
                mergePlayer()
            }
        )
    }

}