package com.egorhoot.chomba.theme.composable

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.Bitmap
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import com.egorhoot.chomba.theme.Shapes

@OptIn(ExperimentalCoilApi::class)
@Composable
fun QRCodeImage(
    modifier: Modifier,
    qrCode: Bitmap
){
    Surface(
        shape = Shapes.large,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = Shapes.large
            ),
    ) {
        AsyncImage(
            model = qrCode,
            contentDescription = "QR Code",
            contentScale = ContentScale.Fit,
            modifier = modifier
        )
    }

}