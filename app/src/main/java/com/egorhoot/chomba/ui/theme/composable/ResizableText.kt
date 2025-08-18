package com.egorhoot.chomba.ui.theme.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun ResizableText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    contentAlignment: Alignment = Alignment.Center
) {
    var scale by remember { mutableStateOf(1f) }
    var containerWidth by remember { mutableStateOf(0) }
    var textWidth by remember { mutableStateOf(0) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .onGloballyPositioned { coordinates ->
                containerWidth = coordinates.size.width
            }
    ) {
        Text(
            text = text,
            style = style,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(contentAlignment)
                .onGloballyPositioned { coordinates ->
                    textWidth = coordinates.size.width
                    scale = if (containerWidth > 0 && textWidth > containerWidth) {
                        containerWidth.toFloat() / textWidth.toFloat()
                    } else {
                        1f
                    }
                }
                .scale(scale)
        )
    }
}
