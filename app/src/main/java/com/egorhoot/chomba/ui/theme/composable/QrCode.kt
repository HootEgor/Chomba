package com.egorhoot.chomba.ui.theme.composable

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.egorhoot.chomba.ui.theme.Shapes

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
        Image(
            bitmap = qrCode.asImageBitmap(),
            contentDescription = "QR Code",
            modifier = modifier
        )
    }

}