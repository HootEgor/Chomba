package com.egorhoot.chomba.ui.theme.composable

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NameHorizontalDivider(
    modifier: Modifier = Modifier,
    text: String = "",
    thickness: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outline,
    textSize: Int = 14,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Row(
        modifier = modifier.padding(top = 8.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ){
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = thickness,
            color = color
        )
        Text(
            text = text,
            style = textStyle,
            modifier = Modifier.padding(horizontal = 8.dp),
            color = color,
            fontSize = textSize.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = thickness,
            color = color
        )
    }

}