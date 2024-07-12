package com.example.chomba.ai
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.chomba.R
import java.util.Locale

class VoiceRecognitionViewModel(application: Application) : AndroidViewModel(application) {
    val recognizedText = mutableStateOf("")
    val score = mutableStateOf(0)
    val stop = mutableStateOf(false)
    private val context = application
    var speechRecognizerLauncher: ActivityResultLauncher<Intent>? = null

    fun setSpeechRL(launcher: ActivityResultLauncher<Intent>) {
        speechRecognizerLauncher = launcher
    }

    fun startSpeechRecognition(context: Context) {
        val systemLocale = Locale.getDefault()
        val languageTag = systemLocale.toLanguageTag()

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU") //TODO: change to languageTag
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

        val stopWord = context.getString(R.string.stop_recognition)
        val stop = recognizedText.contains(stopWord, ignoreCase = true)

        val five = context.getString(R.string.five)
        if (score == 0 && recognizedText.contains(five, ignoreCase = true)) {
            return Pair(5, stop)
        }
        return Pair(score, stop)
    }
}

