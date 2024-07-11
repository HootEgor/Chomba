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
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Говорите...")
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
        val stop = recognizedText.contains("стоп", ignoreCase = true)
        if (score == 0 && recognizedText.contains("пять", ignoreCase = true)) {
            return Pair(5, stop)
        }
        return Pair(score, stop)
    }
}

