package com.example.chomba.data

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

data class Player(
    var visible: Boolean = true,
    var name: String = "",
    var color: Color = Color.Magenta,
)
