package com.example.chomba.pages
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.chomba.R
import com.example.chomba.ai.VoiceRecognitionViewModel
import com.example.chomba.ui.theme.composable.IconButton

@Composable
fun VoiceRecognitionButton(
    modifier: Modifier = Modifier,
    onRecognized: (Int) -> Unit,
    viewModel: VoiceRecognitionViewModel
) {
    val context = LocalContext.current

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (!matches.isNullOrEmpty()) {
                    val recognizedText = matches[0]
                    val (score, stop) = viewModel.processRecognizedText(recognizedText)
                    if (score > 0) {
                        onRecognized(score)
                    }
                    if (!stop) {
                        viewModel.startSpeechRecognition(context)
                    }
                }
            } else {
                Toast.makeText(context, "Speech recognition failed", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.setSpeechRL(speechRecognizerLauncher)
    }

    IconButton(icon = R.drawable.baseline_keyboard_voice_24,
        modifier = modifier,
        action = {
                viewModel.startSpeechRecognition(context)
        }
    )
}
