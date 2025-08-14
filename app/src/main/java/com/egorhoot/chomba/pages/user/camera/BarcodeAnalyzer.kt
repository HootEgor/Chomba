package com.egorhoot.chomba.pages.user.camera

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class BarcodeAnalyzer @Inject constructor(
    private val context: Context,
    private val onGetId: (String) -> Unit,
    private val onError: () -> Unit
) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    // Coroutine-related variables
    private var analysisJob: Job? = null
    private val analysisScope = CoroutineScope(Dispatchers.Default)
    private var done = false

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        analysisJob?.cancel()
        analysisJob = analysisScope.launch {
            delay(500)
            try {
                imageProxy.image?.let { image ->
                    scanner.process(
                        InputImage.fromMediaImage(
                            image, imageProxy.imageInfo.rotationDegrees
                        )
                    ).addOnSuccessListener { barcodes ->
                        barcodes?.takeIf { it.isNotEmpty() }
                            ?.mapNotNull { it.rawValue }
                            ?.forEach { uid ->
                                if(done) return@forEach
                                onGetId(uid)
                            }
                    }.addOnFailureListener { e ->
                        Log.e("QRAnalysis", "QR code scanning failed", e)
                        onError()
                    }.addOnCompleteListener {
                        imageProxy.close()
                    }
                } ?: run {
                    imageProxy.close()
                }
            } catch (e: Exception) {
                Log.e("QRAnalysis", "Error processing image", e)
                imageProxy.close()
            }
        }
    }


}