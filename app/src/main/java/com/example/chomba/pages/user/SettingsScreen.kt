package com.example.chomba.pages.user

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chomba.R
import com.example.chomba.ui.theme.Shapes

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = Shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, MaterialTheme.colorScheme.onSurfaceVariant, Shapes.medium)
    ) {
        
    }

}