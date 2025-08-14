package com.egorhoot.chomba.pages.user.camera

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.egorhoot.chomba.R
import com.egorhoot.chomba.ui.theme.composable.BasicIconButton

@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    onGetIds: (String) -> Unit,
    onError: () -> Unit,
    onBack: () -> Unit
) {
    val localContext = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(localContext) }
    val previewView = remember { PreviewView(localContext) }

    // This ensures that the camera is correctly setup when the composable is first composed
    LaunchedEffect(cameraProviderFuture) {
        val cameraProvider = cameraProviderFuture.get()

        // Unbind any existing use cases
        cameraProvider.unbindAll()

        val preview = Preview.Builder().build()
        val selector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview.surfaceProvider = previewView.surfaceProvider

        val resolutionSelector = ResolutionSelector.Builder()
            .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
            .build()
        val imageAnalysis = ImageAnalysis.Builder().setResolutionSelector(resolutionSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
        imageAnalysis.setAnalyzer(
            ContextCompat.getMainExecutor(localContext),
            BarcodeAnalyzer(localContext, onGetIds, onError)
        )

        // Bind the camera use cases to the lifecycle
        runCatching {
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                selector,
                preview,
                imageAnalysis
            )
        }.onFailure {
            Log.e("PRG", "Camera bind error ${it.localizedMessage}", it)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AndroidView(
            modifier = Modifier.matchParentSize().clipToBounds(),
            factory = { previewView }
        )

        val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()

                    val cutoutSize = size.width * 0.8f
                    val cutoutLeft = (size.width - cutoutSize) / 2
                    val cutoutTop = (size.height - cutoutSize) / 2

                    drawRect(
                        color = surfaceColor.copy(alpha = 0.8f)
                    )

                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = Offset(cutoutLeft, cutoutTop),
                        size = Size(cutoutSize, cutoutSize),
                        cornerRadius = CornerRadius(20.dp.toPx()),
                        blendMode = BlendMode.Clear
                    )
                }
        )

        BasicIconButton(
            text = R.string.back,
            icon = R.drawable.baseline_arrow_back_ios_24,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(16.dp),
            action = onBack
        )
    }
}