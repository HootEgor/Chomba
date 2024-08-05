package com.egorhoot.chomba.ai
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.egorhoot.chomba.ChombaViewModel
import com.egorhoot.chomba.R
import com.egorhoot.chomba.data.Language
import com.egorhoot.chomba.pages.user.ProfileScreenUiState
import com.egorhoot.chomba.utils.IdConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class VoiceRecognitionViewModel @Inject constructor(
    private val idConverter: IdConverter,
    val profileUi: MutableState<ProfileScreenUiState>
) : ChombaViewModel() {
    val recognizedText = mutableStateOf("")
    val score = mutableStateOf(0)
    val stop = mutableStateOf(false)
    var speechRecognizerLauncher: ActivityResultLauncher<Intent>? = null

    fun setSpeechRL(launcher: ActivityResultLauncher<Intent>) {
        speechRecognizerLauncher = launcher
    }

    fun startSpeechRecognition(context: Context) {

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, idConverter.getString(profileUi.value.selectedLanguage.languageTag))
            putExtra(RecognizerIntent.EXTRA_PROMPT, R.string.speech_prompt)
        }
        try {
            speechRecognizerLauncher?.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Your device does not support speech recognition", Toast.LENGTH_SHORT).show()
            startSpeechRecognition(context)
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
}

