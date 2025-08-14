package com.egorhoot.chomba.ai
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import com.egorhoot.chomba.ChombaViewModel
import com.egorhoot.chomba.R
import com.egorhoot.chomba.pages.speech.AudioUiState
import com.egorhoot.chomba.pages.user.ProfileScreenUiState
import com.egorhoot.chomba.utils.IdConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VoiceRecognitionViewModel @Inject constructor(
    private val idConverter: IdConverter,
    val profileUi: MutableState<ProfileScreenUiState>,
    private val context: Context,
) : ChombaViewModel() {

    private val sharedPref = context.getSharedPreferences(
        "com.egorhoot.chomba",
        Context.MODE_PRIVATE
    )

    val uiState = mutableStateOf(AudioUiState())

    val recognizedText = mutableStateOf("")
    val score = mutableStateOf(0)
    val stop = mutableStateOf(false)
    var speechRecognizerLauncher: ActivityResultLauncher<Intent>? = null

    fun setSpeechRL(launcher: ActivityResultLauncher<Intent>) {
        speechRecognizerLauncher = launcher
    }

    fun startSpeechRecognition() {

        if (audioPermissionDenied()) {
            uiState.value = uiState.value.copy(
                audioPermissionDenied = true,
                audioPermissionGranted = false
            )
            Toast.makeText(context, R.string.please_enable_microphone_permission, Toast.LENGTH_SHORT).show()
            return
        }

        if(hasAudioPermission()) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, idConverter.getString(profileUi.value.selectedLanguage.languageTag))
                putExtra(RecognizerIntent.EXTRA_PROMPT, R.string.speech_prompt)
            }
            try {
                speechRecognizerLauncher?.launch(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context,
                    context.getString(R.string.your_device_does_not_support_speech_recognition), Toast.LENGTH_SHORT).show()
                startSpeechRecognition()
            }
        }
        else {
            uiState.value = uiState.value.copy(
                audioPermissionDenied = true,
                audioPermissionGranted = false
            )
            Toast.makeText(context, R.string.please_enable_microphone_permission, Toast.LENGTH_SHORT).show()
        }

    }

    fun processRecognizedText(recognizedText: String): Pair<Int, Boolean> {
        val score = recognizedText.filter { it.isDigit() }.toIntOrNull() ?: 0

        val stopWord = idConverter.getString(R.string.stop_recognition)
        val stop = recognizedText.contains(stopWord, ignoreCase = true)

        val five = idConverter.getString(R.string.five)
        if (score == 0 && recognizedText.contains(five, ignoreCase = true)) {
            return Pair(5, stop)
        }
        return Pair(score, stop)
    }

    private fun hasAudioPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun audioPermissionDenied(): Boolean {
        if (hasAudioPermission()) {
            with(sharedPref.edit()) {
                putBoolean("audioPermissionDenied", false)

                uiState.value = uiState.value.copy(
                    audioPermissionDenied = false
                )
                apply()
            }
        }
        return sharedPref.getBoolean("audioPermissionDenied", false)
    }

    fun onPermissionDenied() {
        uiState.value = uiState.value.copy(
            audioPermissionDenied = true
        )
        if (audioPermissionDenied()) return
        with(sharedPref.edit()) {
            putBoolean("audioPermissionDenied", true)
            apply()
        }
    }
}

