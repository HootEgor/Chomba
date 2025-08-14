package com.egorhoot.chomba

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import com.egorhoot.chomba.ui.theme.primaryContainerDark
import com.egorhoot.chomba.ui.theme.primaryContainerLight
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContent {
            val isDarkTheme = isSystemInDarkTheme()

            val primaryContainerLight = primaryContainerLight
            val primaryContainerDark = primaryContainerDark

            enableEdgeToEdge(
                statusBarStyle = if (isDarkTheme) {
                    SystemBarStyle.dark(scrim = primaryContainerDark.toArgb())
                } else {
                    SystemBarStyle.light(
                        scrim = primaryContainerLight.toArgb(),
                        darkScrim = primaryContainerLight.toArgb())
                },
                navigationBarStyle = if (isDarkTheme) {
                    SystemBarStyle.dark(scrim = primaryContainerDark.toArgb())
                } else {
                    SystemBarStyle.light(
                        scrim = primaryContainerLight.toArgb(),
                        darkScrim = primaryContainerLight.toArgb())
                }
            )

            GameScreen()
        }
    }
}