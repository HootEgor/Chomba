package com.example.chomba.data

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

data class Player(
    var visible: Boolean = true,
    var name: String = "",
    var color: Color = Color.Magenta,
    var scoreList: List<Score> = listOf(),
    var scorePerRound: Int = 0
)

fun Player.getTotalScore(): Int {
    var totalScore = 0
    for (score in scoreList) {
        totalScore += score.value
    }
    return totalScore
}

fun Player.getZeroNum(): Int {
    var zeroNum = 0
    for (score in scoreList) {
        if (score.type == 0) {
            zeroNum++
        }

        if(zeroNum == 4) {
            zeroNum = 1
        }
    }

    if (zeroNum == 3) {
        zeroNum = 0
    }

    return zeroNum
}
