package com.egorhoot.chomba.pages
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.egorhoot.chomba.R
import com.egorhoot.chomba.ai.VoiceRecognitionViewModel
import com.egorhoot.chomba.ui.theme.composable.IconButton

@Composable
fun VoiceRecognitionButton(
    modifier: Modifier = Modifier,
    onRecognized: (Int) -> Unit,
    viewModel: VoiceRecognitionViewModel = hiltViewModel()
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
